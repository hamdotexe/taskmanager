package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.services.Refused;
import io.mcs.taskmanager.services.TaskCounts;
import io.mcs.taskmanager.services.TaskService;
import io.mcs.taskmanager.ui.kit.Icons;
import io.mcs.taskmanager.ui.kit.Ui;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The window, and the one screen in it: the header, which stands in for the system's title bar; the sidebar, with
 * the status filters and their counts, always in view, and the keyboard shortcuts; and the task list, with its
 * heading, the search, and the notice a task that has gone is reported in. Dialogs open over it all, on a layer of
 * their own.
 *
 * <p>Everything goes through {@link TaskService}. After each change the screen reads the list and the counts again,
 * so what it shows is what is saved, a second copy of the app's changes included once it is refreshed.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
@Component
@Scope("prototype")
public class MainWindowController {

    /** The width of the list's status column. */
    static final double STATUS_WIDTH = 150;

    /** The width of the list's column of times. */
    static final double WHEN_WIDTH = 160;

    /** The width kept at each row's end for Edit and Delete. */
    static final double ACTIONS_WIDTH = 150;

    /** The height of each row of the list. */
    static final double ROW_HEIGHT = 60;

    /** What the window says when something fails that nobody expected, such as the database not answering. */
    static final String FAILED = "Something went wrong, and nothing was changed. Try again, or restart the Task"
            + " Manager if it keeps happening.";

    /** Ctrl+N: add a task. */
    private static final KeyCombination ADD_KEYS = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);

    /** Ctrl+F: search. */
    private static final KeyCombination SEARCH_KEYS = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);

    /** F5: read the list again. */
    private static final KeyCombination REFRESH_KEYS = new KeyCodeCombination(KeyCode.F5);

    /** Where an unexpected failure is written, for whoever looks into it. */
    private static final Logger LOG = LoggerFactory.getLogger(MainWindowController.class);

    /** Where the tasks are added, listed, changed and deleted. */
    private final TaskService tasks;

    /** The window's layers: the screen, and over it the dialog when one is open. */
    @FXML
    private StackPane layers;

    /** The mark and the app's name, at the header's start. */
    @FXML
    private HBox brand;

    /** The status filters, each with its count. */
    @FXML
    private VBox filters;

    /** The keyboard shortcuts, at the sidebar's foot. */
    @FXML
    private VBox shortcuts;

    /** The list's heading: "All tasks" or the status filtered by. */
    @FXML
    private Label heading;

    /** The line under the heading, saying what the list shows. */
    @FXML
    private Label summary;

    /** Reads the list again from the database. */
    @FXML
    private Button refreshButton;

    /** Opens the form for a new task: the screen's one main action. */
    @FXML
    private Button addButton;

    /** The search field with its icon and shortcut. */
    @FXML
    private HBox searchBox;

    /** What the list is searched for, in titles and descriptions. */
    @FXML
    private TextField search;

    /** Where the notice of a task that has gone shows, over the list. */
    @FXML
    private VBox noticeSlot;

    /** The list's column headings. */
    @FXML
    private HBox listHead;

    /** The tasks shown, newest first. */
    @FXML
    private ListView<Task> list;

    /** What the card says in place of the list when it has nothing to show. */
    @FXML
    private VBox empty;

    /** The status the list is filtered by, or {@code null} for every task. */
    private @Nullable TaskStatus filter;

    /** The dialog open over the window, or {@code null} for none. */
    private @Nullable DialogFrame dialog;

    /** The notice shown over the list; empty for none. */
    private String notice = "";

    /**
     * Makes the window's controller; Spring does this when the window's FXML is loaded.
     *
     * @param tasks where the tasks are kept
     */
    public MainWindowController(TaskService tasks) {
        this.tasks = tasks;
    }

    /**
     * Builds what the FXML does not say and shows the tasks: the header's mark, the buttons' icons and tooltips, the
     * search's icon and shortcut, the list's columns, the shortcuts, and the keys the window answers to.
     */
    @FXML
    private void initialize() {
        brand.getChildren().setAll(Icons.mark(), Ui.text("Task Manager", "brand"));
        HeaderBar.setDragType(brand, HeaderDragType.DRAGGABLE_SUBTREE);

        addButton.setGraphic(Icons.line(Icons.ADD, 12, 1.8));
        Ui.tip(addButton, "Add a task (Ctrl+N)");
        refreshButton.setGraphic(Icons.line(Icons.REFRESH_14, 14, 1.5));
        Ui.tip(refreshButton, "Read the list again from the database (F5)");

        searchBox.getChildren().addFirst(Icons.line(Icons.SEARCH, 16, 1.5));
        searchBox.getChildren().add(Ui.keycap("Ctrl+F"));
        HBox.setHgrow(search, Priority.ALWAYS);
        search.textProperty().addListener((_, _, _) -> render());
        search.addEventFilter(KeyEvent.KEY_PRESSED, event -> searchKey(event));

        listHead.getChildren().setAll(
                head("Task", -1), head("Status", STATUS_WIDTH), head("Updated", WHEN_WIDTH), head("", ACTIONS_WIDTH));
        list.setCellFactory(_ -> new TaskCell(this));
        list.setFixedCellSize(ROW_HEIGHT);
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.addEventFilter(KeyEvent.KEY_PRESSED, event -> listKey(event));
        list.skinProperty().addListener((_, _, skin) -> {
            if (skin != null) {
                followScrollBar();
            }
        });
        // the rows end in the card's rounded corners rather than drawing over them
        Rectangle corners = new Rectangle();
        corners.setArcWidth(22);
        corners.setArcHeight(22);
        corners.setY(-12);
        corners.widthProperty().bind(list.widthProperty());
        corners.heightProperty().bind(list.heightProperty().add(12));
        list.setClip(corners);

        shortcuts.getChildren().setAll(Ui.text("Shortcuts", "group-title"),
                shortcut("Add task", "Ctrl+N"), shortcut("Edit task", "Enter"), shortcut("Delete task", "Delete"),
                shortcut("Search", "Ctrl+F"), shortcut("Refresh", "F5"));

        layers.sceneProperty().addListener((_, _, scene) -> {
            if (scene != null) {
                scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> windowKey(event));
                keepFocusInDialog(scene);
                Platform.runLater(() -> list.requestFocus());
            }
        });
        render();
    }

    /**
     * Shows the tasks and the counts as they are saved now, keeping the chosen task chosen while it is still listed.
     */
    void render() {
        Task chosen = list.getSelectionModel().getSelectedItem();
        render(chosen == null ? null : chosen.getId());
    }

    /**
     * Shows the tasks and the counts as they are saved now, with a task chosen and scrolled to if it is listed.
     *
     * @param chosenId the id of the task to choose, or {@code null} for none
     */
    private void render(@Nullable UUID chosenId) {
        TaskCounts counts;
        List<Task> shown;
        try {
            counts = tasks.count();
            shown = tasks.search(search.getText(), filter);
        } catch (RuntimeException failure) {
            LOG.error("Could not read the tasks", failure);
            notice = FAILED;
            renderNotice();
            return;
        }
        list.getItems().setAll(shown);
        list.getSelectionModel().clearSelection();
        for (int index = 0; index < shown.size(); index++) {
            if (shown.get(index).getId().equals(chosenId)) {
                list.getSelectionModel().select(index);
                list.scrollTo(index);
                break;
            }
        }
        TaskStatus shownStatus = filter;
        long inStatus = shownStatus == null ? counts.total() : counts.of(shownStatus);
        heading.setText(TaskText.heading(shownStatus));
        summary.setText(TaskText.summary(shown.size(), inStatus, shownStatus, search.getText()));
        renderFilters(counts);
        renderEmpty(shown.isEmpty(), counts.total());
        renderNotice();
    }

    /**
     * The sidebar's filters: every task, then each status, each with its count, the one shown raised in white.
     *
     * @param counts how many tasks there are in each status
     */
    private void renderFilters(TaskCounts counts) {
        List<Node> items = new ArrayList<>();
        items.add(Ui.text("Tasks", "group-title"));
        items.add(filterItem(null, Icons.ALL, counts.total()));
        for (TaskStatus status : TaskStatus.values()) {
            items.add(filterItem(status, StatusLook.icon(status), counts.of(status)));
        }
        filters.getChildren().setAll(items);
    }

    /**
     * One filter in the sidebar.
     *
     * @param status the status it shows, or {@code null} for every task
     * @param icon its icon's SVG path
     * @param count how many tasks it holds
     * @return the filter, a button that shows its tasks
     */
    private Button filterItem(@Nullable TaskStatus status, String icon, long count) {
        boolean current = status == filter;
        Label label = Ui.shrinking(TaskText.heading(status), "nav-label");
        HBox.setHgrow(label, Priority.ALWAYS);
        HBox inside = new HBox(10, Icons.line(icon, 16, 1.5), label, Ui.text(String.valueOf(count), "nav-count"));
        inside.setAlignment(Pos.CENTER_LEFT);
        Button button = new Button();
        button.getStyleClass().setAll("nav");
        if (current) {
            button.getStyleClass().add("current");
        }
        button.setGraphic(inside);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setMaxWidth(Double.MAX_VALUE);
        inside.prefWidthProperty().bind(button.widthProperty().subtract(22));
        button.setAccessibleText(TaskText.heading(status) + ", " + count + (current ? ", shown" : ""));
        button.setOnAction(_ -> {
            filter = status;
            render();
        });
        return button;
    }

    /**
     * Shows the list, or in its place what the card says when there is nothing to show.
     *
     * @param nothing whether the list is empty
     * @param total how many tasks there are in all
     */
    private void renderEmpty(boolean nothing, long total) {
        list.setVisible(!nothing);
        list.setManaged(!nothing);
        listHead.setVisible(!nothing);
        listHead.setManaged(!nothing);
        empty.setVisible(nothing);
        empty.setManaged(nothing);
        if (!nothing) {
            return;
        }
        TaskStatus shownStatus = filter;
        TaskText.Empty says = TaskText.empty(total, shownStatus, search.getText());
        Label title = Ui.text(says.title(), "heading");
        VBox.setMargin(title, new Insets(6, 0, 0, 0));
        empty.getChildren().setAll(
                Ui.tile(Icons.line(shownStatus == null ? Icons.ALL : StatusLook.icon(shownStatus), 16, 1.5), 40,
                        "empty-tile"),
                title,
                Ui.wrapped(says.hint(), "caption", "centred"));
    }

    /**
     * Shows the notice over the list, or nothing when there is none.
     */
    private void renderNotice() {
        if (notice.isEmpty()) {
            noticeSlot.getChildren().clear();
        } else {
            noticeSlot.getChildren().setAll(Ui.notice(notice, () -> {
                notice = "";
                renderNotice();
            }));
        }
    }

    /**
     * Opens the form for a new task.
     */
    @FXML
    private void add() {
        show(new TaskDialog(tasks, this, null));
    }

    /**
     * Reads the list and the counts again from the database, as after another copy of the app has changed them.
     */
    @FXML
    private void refresh() {
        render();
    }

    /**
     * Opens the edit form on a task, as it is saved now; a task that has gone is reported instead.
     *
     * @param task the task as the list shows it
     */
    void edit(Task task) {
        Task saved;
        try {
            saved = tasks.get(task.getId());
        } catch (Refused refused) {
            gone(String.valueOf(refused.getMessage()));
            return;
        }
        show(new TaskDialog(tasks, this, saved));
    }

    /**
     * Asks before deleting a task; a task that has gone is reported instead.
     *
     * @param task the task as the list shows it
     */
    void confirmDelete(Task task) {
        Task saved;
        try {
            saved = tasks.get(task.getId());
        } catch (Refused refused) {
            gone(String.valueOf(refused.getMessage()));
            return;
        }
        show(new DeleteTaskDialog(tasks, this, saved));
    }

    /**
     * Moves a task to a status, straight from the list. A task that has gone is reported, and the list read again.
     *
     * @param task the task as the list shows it
     * @param status the status chosen
     */
    void changeStatus(Task task, TaskStatus status) {
        if (task.getStatus() == status) {
            return;
        }
        try {
            tasks.changeStatus(task.getId(), status);
        } catch (Refused refused) {
            gone(String.valueOf(refused.getMessage()));
            return;
        } catch (RuntimeException failure) {
            LOG.error("Could not change a task's status", failure);
            notice = FAILED;
            render();
            return;
        }
        notice = "";
        render(task.getId());
    }

    /**
     * Shows a task just added or saved, chosen in the list. A new task the filter or search would hide clears them,
     * so what was added is always seen.
     *
     * @param taskId the task's id, or {@code null} if nothing was saved
     */
    void saved(@Nullable UUID taskId) {
        notice = "";
        render(taskId);
        UUID savedId = taskId;
        if (savedId != null && list.getItems().stream().noneMatch(task -> savedId.equals(task.getId()))) {
            filter = null;
            search.clear();
            render(savedId);
        }
        list.requestFocus();
    }

    /**
     * Shows the list without the task just deleted.
     */
    void deleted() {
        notice = "";
        render();
        list.requestFocus();
    }

    /**
     * Reports a task that no longer exists, deleted in another copy of the app, and reads the list again, which no
     * longer has it.
     *
     * @param message the service's sentence saying so
     */
    void gone(String message) {
        notice = message;
        render();
    }

    /**
     * Shows a failure nobody expected as a plain sentence over the list; the details go to the log.
     *
     * @param failure what went wrong
     */
    void failed(Throwable failure) {
        LOG.error("Something went wrong in the window", failure);
        closeDialog();
        notice = FAILED;
        renderNotice();
    }

    /**
     * Whether a task is still saved, so a refusal can be told apart from a task deleted in another copy of the app.
     *
     * @param taskId the task's id
     * @return {@code true} if it is still among the tasks
     */
    boolean exists(UUID taskId) {
        return tasks.list().stream().anyMatch(task -> taskId.equals(task.getId()));
    }

    /**
     * Opens a dialog over the window, in place of any already open, and puts the keyboard in it.
     *
     * @param opened the dialog
     */
    void show(Dialog opened) {
        closeDialog();
        DialogFrame frame = new DialogFrame(opened, this);
        dialog = frame;
        layers.getChildren().add(frame.overlay());
        Platform.runLater(() -> frame.focus());
    }

    /**
     * Closes the dialog open over the window, if there is one, and gives the keyboard back to the list, where Enter
     * and Delete act on the chosen task.
     */
    void closeDialog() {
        DialogFrame open = dialog;
        if (open != null) {
            layers.getChildren().remove(open.overlay());
            dialog = null;
            list.requestFocus();
        }
    }

    /**
     * Keeps the keyboard inside an open dialog: the focus moving anywhere else, as Tab past the last button does, is
     * put back in the dialog.
     *
     * @param scene the window's scene
     */
    private void keepFocusInDialog(Scene scene) {
        scene.focusOwnerProperty().addListener((_, _, focused) -> {
            DialogFrame open = dialog;
            if (open != null && focused != null && !open.holds(focused)) {
                Platform.runLater(() -> open.focus());
            }
        });
    }

    /**
     * The keys the window answers to while no dialog is open: Ctrl+N adds a task, Ctrl+F goes to the search, and F5
     * reads the list again.
     *
     * @param event the key pressed
     */
    private void windowKey(KeyEvent event) {
        if (dialog != null) {
            return;
        }
        if (ADD_KEYS.match(event)) {
            event.consume();
            add();
        } else if (SEARCH_KEYS.match(event)) {
            event.consume();
            search.requestFocus();
            search.selectAll();
        } else if (REFRESH_KEYS.match(event)) {
            event.consume();
            refresh();
        }
    }

    /**
     * The keys the list answers to: Enter edits the chosen task and Delete asks to delete it.
     *
     * @param event the key pressed in the list
     */
    private void listKey(KeyEvent event) {
        Task chosen = list.getSelectionModel().getSelectedItem();
        if (chosen == null || event.isShortcutDown() || event.isAltDown()) {
            return;
        }
        if (event.getCode() == KeyCode.ENTER) {
            event.consume();
            edit(chosen);
        } else if (event.getCode() == KeyCode.DELETE) {
            event.consume();
            confirmDelete(chosen);
        }
    }

    /**
     * The keys the search answers to besides typing: Escape clears it, and the down arrow goes to the list.
     *
     * @param event the key pressed in the search
     */
    private void searchKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && !search.getText().isEmpty()) {
            event.consume();
            search.clear();
        } else if (event.getCode() == KeyCode.DOWN && !list.getItems().isEmpty()) {
            event.consume();
            list.requestFocus();
            if (list.getSelectionModel().isEmpty()) {
                list.getSelectionModel().selectFirst();
            }
        }
    }

    /**
     * A column heading of the list.
     *
     * @param text the heading
     * @param width the column's width, or a negative number for the column that takes what the others leave
     * @return the heading
     */
    private static Label head(String text, double width) {
        Label head = Ui.shrinking(text, "th");
        if (width < 0) {
            HBox.setHgrow(head, Priority.ALWAYS);
        } else {
            Ui.fixWidth(head, width);
        }
        return head;
    }

    /**
     * One shortcut at the sidebar's foot: what it does, and its key.
     *
     * @param action what the key does, such as "Add task"
     * @param keys the key, such as "Ctrl+N"
     * @return the row
     */
    private static HBox shortcut(String action, String keys) {
        HBox row = new HBox(Ui.text(action), Ui.grow(), Ui.keycap(keys));
        row.getStyleClass().add("shortcut");
        return row;
    }

    /**
     * Keeps the column headings over their columns while the list shows its scroll bar, which narrows every row by
     * its width.
     */
    private void followScrollBar() {
        for (Node node : list.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar bar && bar.getOrientation() == Orientation.VERTICAL) {
                Runnable pad = () -> listHead.setPadding(
                        new Insets(0, bar.isVisible() ? bar.getWidth() + 1 : 1, 0, 1));
                bar.visibleProperty().addListener((_, _, _) -> pad.run());
                bar.widthProperty().addListener((_, _, _) -> pad.run());
                pad.run();
            }
        }
    }
}
