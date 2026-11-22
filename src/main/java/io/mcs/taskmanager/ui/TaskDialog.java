package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.services.TaskService;
import io.mcs.taskmanager.ui.kit.Icons;
import io.mcs.taskmanager.ui.kit.Ui;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * The form a task is added and edited in: its title and description, each with a count of its characters against
 * the limit, and, for a task already saved, its status. A new task starts Pending. Saving goes through the
 * service, which trims both texts and refuses a blank title or a text over its limit with a sentence the form shows.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public class TaskDialog extends Dialog {

    /** The service that saves the task. */
    private final TaskService tasks;

    /** The window the form opens over. */
    private final MainWindowController window;

    /** The task as it was when the form opened, or {@code null} for a new one. */
    private final @Nullable Task task;

    /** What is to be done. */
    private final TextField title = new TextField();

    /** More about it. */
    private final TextArea description = new TextArea();

    /** The title's count of characters against its limit. */
    private final Label titleCount = Ui.text("", "counter");

    /** The description's count of characters against its limit. */
    private final Label descriptionCount = Ui.text("", "counter");

    /** The three statuses side by side, one chosen, for a task already saved. */
    private final ToggleGroup statuses = new ToggleGroup();

    /** Everything above, laid out once and kept, so a refusal never loses what was typed. */
    private final VBox fields;

    /** The id of the task the last save added or changed, for the window to show it. */
    private @Nullable UUID savedId;

    /**
     * Makes the form for a new task, or for one already saved.
     *
     * @param tasks the service that saves it
     * @param window the window it opens over
     * @param task the task to edit as it is saved now, or {@code null} to add one
     */
    public TaskDialog(TaskService tasks, MainWindowController window, @Nullable Task task) {
        this.tasks = tasks;
        this.window = window;
        this.task = task;
        title.getStyleClass().add("field");
        title.setPromptText("Submit the OEL report");
        description.getStyleClass().add("field");
        description.setPromptText("Write it up in IEEE format, then upload the PDF and the source ZIP.");
        description.setWrapText(true);
        description.setPrefRowCount(4);
        Ui.uncached(description);
        title.textProperty().addListener((_, _, typed) -> count(titleCount, typed, Task.MAX_TITLE_CHARACTERS));
        description.textProperty().addListener(
                (_, _, typed) -> count(descriptionCount, typed, Task.MAX_DESCRIPTION_CHARACTERS));
        description.addEventFilter(KeyEvent.KEY_PRESSED, event -> leaveOnTab(event));
        fields = new VBox(16,
                Ui.labelled("Title", titleCount, title),
                Ui.labelled("Description", descriptionCount, description));
        if (task != null) {
            title.setText(task.getTitle());
            description.setText(task.getDescription());
            fields.getChildren().add(Ui.labelled("Status", null, statusChoice(task.getStatus())));
        }
        count(titleCount, title.getText(), Task.MAX_TITLE_CHARACTERS);
        count(descriptionCount, description.getText(), Task.MAX_DESCRIPTION_CHARACTERS);
    }

    /**
     * The form's heading.
     *
     * @return "Add task" or "Edit task"
     */
    @Override
    public String title() {
        return task == null ? "Add task" : "Edit task";
    }

    /**
     * For a new task, the status it starts in; for a saved one, when it was added and last changed.
     *
     * @return the note
     */
    @Override
    public String note() {
        Task editing = task;
        if (editing == null) {
            return "It starts as Pending; change its status from the list as the work goes.";
        }
        Instant now = Instant.now();
        ZoneId zone = ZoneId.systemDefault();
        return "Added " + TaskText.lower(TaskText.when(editing.getCreatedAt(), now, zone)) + ", last changed "
                + TaskText.lower(TaskText.when(editing.getUpdatedAt(), now, zone)) + ".";
    }

    /**
     * The title, the description and, for a saved task, its status.
     *
     * @return the fields
     */
    @Override
    public Node fields() {
        return fields;
    }

    /**
     * The title, where typing starts.
     *
     * @return the title field
     */
    @Override
    public Node firstFocus() {
        return title;
    }

    /**
     * What the main button says.
     *
     * @return "Add task" for a new task, "Save" for a saved one
     */
    @Override
    public String saveLabel() {
        return task == null ? "Add task" : "Save";
    }

    /**
     * A saved task can be deleted from its form.
     *
     * @return "Delete task", or {@code null} for a new task
     */
    @Override
    public @Nullable String deleteLabel() {
        return task == null ? null : "Delete task";
    }

    /**
     * Adds the task, or saves its title, description and status.
     *
     * @throws io.mcs.taskmanager.services.Refused if the title is blank, a text is over its limit, or the task has
     *         gone
     */
    @Override
    public void save() {
        Task editing = task;
        if (editing == null) {
            savedId = tasks.create(title.getText(), description.getText()).getId();
        } else {
            savedId = tasks.update(editing.getId(), title.getText(), description.getText(), chosenStatus()).getId();
        }
    }

    /**
     * Shows the task saved in the list, chosen.
     */
    @Override
    public void afterSave() {
        window.saved(savedId);
    }

    /**
     * Asks to delete the task the form edits, in a dialog of its own.
     */
    @Override
    public void delete() {
        Task editing = task;
        if (editing != null) {
            window.confirmDelete(editing);
        }
    }

    /**
     * Whether the task being edited has been deleted since the form opened, in another copy of the app.
     *
     * @return {@code true} if it no longer exists; never for a new task
     */
    @Override
    public boolean taskGone() {
        Task editing = task;
        return editing != null && !window.exists(editing.getId());
    }

    /**
     * The three statuses as a segmented choice, the task's own chosen, each with the icon the list shows it with.
     *
     * @param current the task's status now
     * @return the choice
     */
    private HBox statusChoice(TaskStatus current) {
        HBox track = new HBox();
        track.getStyleClass().add("segmented");
        track.setMaxWidth(Region.USE_PREF_SIZE);
        for (TaskStatus status : TaskStatus.values()) {
            ToggleButton segment = new ToggleButton(status.getLabel());
            segment.getStyleClass().setAll("segment");
            segment.setGraphic(Icons.line(StatusLook.smallIcon(status), 14, 1.6));
            segment.setContentDisplay(ContentDisplay.LEFT);
            segment.setUserData(status);
            segment.setToggleGroup(statuses);
            segment.setSelected(status == current);
            segment.setMinWidth(Region.USE_PREF_SIZE);
            track.getChildren().add(segment);
        }
        // a choice of one of three: clicking the chosen one again keeps it chosen
        statuses.selectedToggleProperty().addListener((_, before, now) -> {
            if (now == null && before != null) {
                before.setSelected(true);
            }
        });
        track.setAlignment(Pos.CENTER_LEFT);
        return track;
    }

    /**
     * The status chosen in the form.
     *
     * @return the status, or {@code null} if none is chosen, which the service refuses
     */
    private @Nullable TaskStatus chosenStatus() {
        Toggle chosen = statuses.getSelectedToggle();
        return chosen != null && chosen.getUserData() instanceof TaskStatus status ? status : null;
    }

    /**
     * Shows how many characters a field holds against its limit, in red once it is over.
     *
     * @param counter the count's label
     * @param typed the field's text
     * @param limit the most characters it may have
     */
    private static void count(Label counter, String typed, int limit) {
        counter.setText(TaskText.count(typed, limit));
        if (TaskText.over(typed, limit)) {
            if (!counter.getStyleClass().contains("over")) {
                counter.getStyleClass().add("over");
            }
        } else {
            counter.getStyleClass().remove("over");
        }
    }

    /**
     * Moves the focus on from the description with Tab, and back with Shift+Tab, rather than typing a tab into it:
     * a task's description has no use for one. A text area moves the focus on Ctrl+Tab, so Tab is handed to it as
     * that.
     *
     * @param event the key pressed in the description
     */
    private void leaveOnTab(KeyEvent event) {
        if (event.getCode() != KeyCode.TAB || event.isControlDown()) {
            return;
        }
        event.consume();
        description.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, event.isShiftDown(), true,
                false, false));
    }
}
