package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.ui.kit.Icons;
import io.mcs.taskmanager.ui.kit.Ui;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;

/**
 * One task in the list, as a row of columns: its title with the first line of its description under it; its status as
 * a tinted pill that opens a menu of the three, so a status is changed straight from the list; when it was last
 * changed, and added; and Edit and Delete, shown while the row is pointed at or chosen. A double click edits the task,
 * and a right click offers the same two actions.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
final class TaskCell extends ListCell<Task> {

    /** The window the list is in, which carries out what the row asks. */
    private final MainWindowController window;

    /** The task's title. */
    private final Label title = Ui.shrinking("", "task-title");

    /** The first line of its description, when it has one. */
    private final Label description = Ui.shrinking("", "task-text");

    /** Its status, as a pill that opens the status menu. */
    private final Button status = new Button();

    /** What the pill holds: the status's icon, its name and a chevron. */
    private final HBox statusInside = new HBox(6);

    /** When it was last changed. */
    private final Label updated = Ui.shrinking("", "when");

    /** When it was added. */
    private final Label created = Ui.shrinking("", "task-text");

    /** The row of columns the cell shows for a task. */
    private final HBox row;

    /**
     * Makes a row for the list; the list fills it with a task as it scrolls.
     *
     * @param window the window the list is in
     */
    TaskCell(MainWindowController window) {
        this.window = window;
        VBox words = new VBox(2, title, description);
        words.getStyleClass().add("td");
        words.setAlignment(Pos.CENTER_LEFT);
        words.setMinWidth(0);
        HBox.setHgrow(words, Priority.ALWAYS);

        statusInside.setAlignment(Pos.CENTER_LEFT);
        status.getStyleClass().setAll("status");
        status.setGraphic(statusInside);
        status.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        status.setMinWidth(Region.USE_PREF_SIZE);
        status.setOnAction(_ -> statusMenu().show(status, Side.BOTTOM, 0, 4));
        HBox statusColumn = Ui.fixWidth(new HBox(status), MainWindowController.STATUS_WIDTH);
        statusColumn.getStyleClass().add("td");

        VBox when = Ui.fixWidth(new VBox(2, updated, created), MainWindowController.WHEN_WIDTH);
        when.getStyleClass().add("td");

        HBox actions = Ui.fixWidth(new HBox(
                Ui.button("Edit", () -> edit(), "secondary", "size30"),
                Ui.button("Delete", () -> delete(), "quiet-danger", "size30")), MainWindowController.ACTIONS_WIDTH);
        actions.getStyleClass().add("row-actions");

        row = new HBox(words, statusColumn, when, actions);
        row.getStyleClass().add("task-row");
        row.setMinWidth(0);
        row.setPrefWidth(0);
        // the cell takes the list's width and never asks for more, so the list never scrolls sideways
        setPrefWidth(0);
        setText(null);
        setOnMouseClicked(event -> doubleClicked(event));
    }

    /**
     * Shows a task in the row, or nothing for an empty row below the last task.
     *
     * @param task the task, or {@code null} for an empty row
     * @param empty whether the row is empty
     */
    @Override
    protected void updateItem(@Nullable Task task, boolean empty) {
        super.updateItem(task, empty);
        if (empty || task == null) {
            setGraphic(null);
            setContextMenu(null);
            setAccessibleText(null);
            return;
        }
        TaskStatus now = task.getStatus();
        title.setText(task.getTitle());
        title.getStyleClass().remove("done");
        if (now == TaskStatus.COMPLETED) {
            title.getStyleClass().add("done");
        }
        String firstLine = task.getDescription().lines().findFirst().orElse("");
        description.setText(firstLine);
        description.setVisible(!firstLine.isEmpty());
        description.setManaged(!firstLine.isEmpty());

        statusInside.getChildren().setAll(
                Icons.line(StatusLook.smallIcon(now), 14, 1.6),
                Ui.text(now.getLabel(), "status-label"),
                Icons.line(Icons.CHEVRON_DOWN, 12, 1.6));
        status.getStyleClass().setAll("status", StatusLook.style(now));
        status.setAccessibleText("Status: " + now.getLabel() + ". Change status");

        Instant moment = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        updated.setText(TaskText.when(task.getUpdatedAt(), moment, zone));
        created.setText("Added " + TaskText.lower(TaskText.when(task.getCreatedAt(), moment, zone)));

        setContextMenu(rowMenu());
        setAccessibleText(task.getTitle() + ", " + now.getLabel() + ", last changed " + updated.getText());
        setGraphic(row);
    }

    /**
     * The menu the status pill opens: the three statuses, the task's own checked.
     *
     * @return the menu
     */
    private ContextMenu statusMenu() {
        Task task = getItem();
        ContextMenu menu = new ContextMenu();
        for (TaskStatus each : TaskStatus.values()) {
            boolean current = task != null && task.getStatus() == each;
            MenuItem item = new MenuItem(each.getLabel());
            item.setGraphic(current ? Icons.line(Icons.CHECK, 12, 1.8, "ink") : Ui.fixWidth(new Region(), 12));
            if (current) {
                item.getStyleClass().add("picked");
            }
            item.setOnAction(_ -> changeStatus(each));
            menu.getItems().add(item);
        }
        return menu;
    }

    /**
     * The menu a right click opens on the row: edit or delete the task.
     *
     * @return the menu
     */
    private ContextMenu rowMenu() {
        MenuItem edit = new MenuItem("Edit task");
        edit.setOnAction(_ -> edit());
        MenuItem delete = new MenuItem("Delete task");
        delete.getStyleClass().add("danger");
        delete.setOnAction(_ -> delete());
        return new ContextMenu(edit, new SeparatorMenuItem(), delete);
    }

    /**
     * Moves the row's task to a status, as chosen in the status menu.
     *
     * @param chosen the status chosen
     */
    private void changeStatus(TaskStatus chosen) {
        Task task = getItem();
        if (task != null) {
            window.changeStatus(task, chosen);
        }
    }

    /**
     * Edits the row's task.
     */
    private void edit() {
        Task task = getItem();
        if (task != null) {
            window.edit(task);
        }
    }

    /**
     * Asks to delete the row's task.
     */
    private void delete() {
        Task task = getItem();
        if (task != null) {
            window.confirmDelete(task);
        }
    }

    /**
     * Edits the task on a double click anywhere on the row but its buttons, which do their own thing.
     *
     * @param event the click
     */
    private void doubleClicked(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || event.getClickCount() != 2 || isEmpty()) {
            return;
        }
        Node at = event.getTarget() instanceof Node target ? target : null;
        while (at != null && at != this) {
            if (at instanceof Button) {
                return;
            }
            at = at.getParent();
        }
        edit();
    }
}
