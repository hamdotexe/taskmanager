package io.mcs.taskmanager.services;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.repositories.TaskRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Tasks: adding, listing, changing and deleting them. A task needs a title that is not blank, and a status. A new
 * task starts {@link TaskStatus#PENDING}. A task that no longer exists, deleted in another copy of the app that has
 * the same database open, is refused by every change with a message saying so, never an error.
 *
 * <p>Each method is one transaction: a change the app will not make throws {@link Refused} and saves nothing of it.
 * The tasks returned are no longer attached to the database; change one through this service, never by its setters.
 *
 * @author hamdotexe
 */
@Service
public class TaskService {

    /** Where the tasks are kept. */
    private final TaskRepository tasks;

    /**
     * Makes the service over the tasks' repository; Spring does this at start.
     *
     * @param tasks where the tasks are kept
     */
    public TaskService(TaskRepository tasks) {
        this.tasks = tasks;
    }

    /**
     * Every task.
     *
     * @return the tasks, newest first; empty when there are none
     */
    @Transactional(readOnly = true)
    public List<Task> list() {
        return tasks.findAllByOrderByCreatedAtDesc();
    }

    /**
     * One task as it is saved now, such as before the edit form shows it.
     *
     * @param taskId the task's id
     * @return the task
     * @throws Refused if no task is chosen or the task no longer exists
     */
    @Transactional(readOnly = true)
    public Task get(@Nullable UUID taskId) {
        return find(taskId);
    }

    /**
     * Adds a task, {@link TaskStatus#PENDING} to begin with.
     *
     * @param title what is to be done; not blank
     * @param description more about it; {@code null} or blank for none
     * @return the task as saved, with its id and times
     * @throws Refused if the title is blank
     */
    @Transactional
    public Task create(@Nullable String title, @Nullable String description) {
        Task task = new Task();
        set(task, title, description);
        task.setStatus(TaskStatus.PENDING);
        Instant now = Instant.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return tasks.save(task);
    }

    /**
     * Replaces a task's title, description and status at once, as the edit form saves them.
     *
     * @param taskId the task's id
     * @param title what is to be done; not blank
     * @param description more about it; {@code null} or blank for none
     * @param status how far it has got
     * @return the task as saved
     * @throws Refused if no task is chosen, the task no longer exists, the title is blank or there is no status
     */
    @Transactional
    public Task update(@Nullable UUID taskId, @Nullable String title, @Nullable String description,
            @Nullable TaskStatus status) {
        Task task = find(taskId);
        set(task, title, description);
        task.setStatus(required(status));
        task.setUpdatedAt(Instant.now());
        return task;
    }

    /**
     * Deletes a task.
     *
     * @param taskId the task's id
     * @throws Refused if no task is chosen or the task no longer exists
     */
    @Transactional
    public void delete(@Nullable UUID taskId) {
        tasks.delete(find(taskId));
    }

    /**
     * The task with the id, refusing a missing id and a task that has gone.
     *
     * @param taskId the task's id
     * @return the task, attached to the current transaction
     * @throws Refused if no task is chosen or the task no longer exists
     */
    private Task find(@Nullable UUID taskId) {
        if (taskId == null) {
            throw new Refused("Choose a task in the list first.");
        }
        return tasks.findById(taskId).orElseThrow(() -> new Refused(
                "This task no longer exists: it may have been deleted in another copy of the Task Manager."));
    }

    /**
     * Sets the task's title and description, refusing a blank title.
     *
     * @param task the task to set them on
     * @param title what is to be done
     * @param description more about it; {@code null} for none
     * @throws Refused if the title is blank
     */
    private static void set(Task task, @Nullable String title, @Nullable String description) {
        if (title == null || title.isBlank()) {
            throw new Refused("A task needs a title, such as \"Submit the OEL report\".");
        }
        task.setTitle(title);
        task.setDescription(description == null ? "" : description);
    }

    /**
     * The status, refusing none.
     *
     * @param status the status chosen
     * @return the same status
     * @throws Refused if there is no status
     */
    private static TaskStatus required(@Nullable TaskStatus status) {
        if (status == null) {
            throw new Refused("A task needs a status: Pending, In Progress or Completed.");
        }
        return status;
    }
}
