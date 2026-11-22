package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.services.TaskService;

/**
 * Asks before a task is deleted, naming it, since a delete cannot be undone. The red button deletes it; Cancel and
 * Escape leave it.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public class DeleteTaskDialog extends Dialog {

    /** The service that deletes the task. */
    private final TaskService tasks;

    /** The window the question opens over. */
    private final MainWindowController window;

    /** The task to delete, as the list showed it. */
    private final Task task;

    /**
     * Makes the question for a task.
     *
     * @param tasks the service that deletes it
     * @param window the window it opens over
     * @param task the task to delete
     */
    public DeleteTaskDialog(TaskService tasks, MainWindowController window, Task task) {
        this.tasks = tasks;
        this.window = window;
        this.task = task;
    }

    /**
     * The question.
     *
     * @return "Delete this task?"
     */
    @Override
    public String title() {
        return "Delete this task?";
    }

    /**
     * The task named, and that a delete is for good.
     *
     * @return the note
     */
    @Override
    public String note() {
        return "“" + task.getTitle() + "” will be deleted for good. This can't be undone.";
    }

    /**
     * What the red button says.
     *
     * @return "Delete task"
     */
    @Override
    public String saveLabel() {
        return "Delete task";
    }

    /**
     * The main button deletes, so it is red.
     *
     * @return {@code true}
     */
    @Override
    public boolean destructive() {
        return true;
    }

    /**
     * Deletes the task.
     *
     * @throws io.mcs.taskmanager.services.Refused if the task has gone already
     */
    @Override
    public void save() {
        tasks.delete(task.getId());
    }

    /**
     * Shows the list without the task.
     */
    @Override
    public void afterSave() {
        window.deleted();
    }

    /**
     * Whether the task has been deleted since the list showed it, in another copy of the app.
     *
     * @return {@code true} if it no longer exists
     */
    @Override
    public boolean taskGone() {
        return !window.exists(task.getId());
    }
}
