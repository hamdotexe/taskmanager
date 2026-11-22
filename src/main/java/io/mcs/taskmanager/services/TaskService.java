package io.mcs.taskmanager.services;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.repositories.TaskRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Tasks: adding, listing, changing and deleting them. A task needs a title that is not blank, and a status. The title
 * and description are trimmed, and kept within {@link Task#MAX_TITLE_CHARACTERS} and
 * {@link Task#MAX_DESCRIPTION_CHARACTERS}. A new task starts {@link TaskStatus#PENDING}. A task that no longer
 * exists, deleted in another copy of the app that has the same database open, is refused by every change with a
 * message saying so, never an error.
 *
 * <p>Each method is one transaction: a change the app will not make throws {@link Refused} and saves nothing of it.
 * The tasks returned are no longer attached to the database; change one through this service, never by its setters.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
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
     * The tasks whose title or description holds the text, whatever its case, in one status or in all. Every
     * character typed is matched as it is: {@code %} and {@code _} are not wildcards.
     *
     * @param text what to look for, trimmed; {@code null} or blank to match every task
     * @param status the one status to list, or {@code null} for every status
     * @return the matching tasks, newest first; empty when none match
     */
    @Transactional(readOnly = true)
    public List<Task> search(@Nullable String text, @Nullable TaskStatus status) {
        return tasks.search(containing(trimmed(text)), status);
    }

    /**
     * How many tasks there are in each status, counted over every task.
     *
     * @return the counts; all 0 when there are no tasks
     */
    @Transactional(readOnly = true)
    public TaskCounts count() {
        return new TaskCounts(
                tasks.countByStatus(TaskStatus.PENDING),
                tasks.countByStatus(TaskStatus.IN_PROGRESS),
                tasks.countByStatus(TaskStatus.COMPLETED));
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
     * @param title what is to be done; not blank, and trimmed
     * @param description more about it, trimmed; {@code null} or blank for none
     * @return the task as saved, with its id and times
     * @throws Refused if the title is blank, or the title or the description is too long
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
     * @param title what is to be done; not blank, and trimmed
     * @param description more about it, trimmed; {@code null} or blank for none
     * @param status how far it has got
     * @return the task as saved
     * @throws Refused if no task is chosen, the task no longer exists, the title is blank, the title or the
     *         description is too long, or there is no status
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
     * Moves a task to another status, as the list does without opening the edit form. Its title and description stay
     * as they are.
     *
     * @param taskId the task's id
     * @param status how far it has got now
     * @return the task as saved
     * @throws Refused if no task is chosen, the task no longer exists, or there is no status
     */
    @Transactional
    public Task changeStatus(@Nullable UUID taskId, @Nullable TaskStatus status) {
        Task task = find(taskId);
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
     * Sets the task's title and description, trimmed, refusing a blank title and a text over its limit. The limits
     * are counted after trimming, so spaces typed around a text never push it over.
     *
     * @param task the task to set them on
     * @param title what is to be done
     * @param description more about it; {@code null} for none
     * @throws Refused if the title is blank, or the title or the description is too long
     */
    private static void set(Task task, @Nullable String title, @Nullable String description) {
        String trimmedTitle = trimmed(title);
        if (trimmedTitle.isEmpty()) {
            throw new Refused("A task needs a title, such as \"Submit the OEL report\".");
        }
        if (trimmedTitle.length() > Task.MAX_TITLE_CHARACTERS) {
            throw new Refused("A title can be at most " + Task.MAX_TITLE_CHARACTERS + " characters; this one has "
                    + trimmedTitle.length() + ".");
        }
        String trimmedDescription = trimmed(description);
        if (trimmedDescription.length() > Task.MAX_DESCRIPTION_CHARACTERS) {
            throw new Refused("A description can be at most " + Task.MAX_DESCRIPTION_CHARACTERS
                    + " characters; this one has " + trimmedDescription.length() + ".");
        }
        task.setTitle(trimmedTitle);
        task.setDescription(trimmedDescription);
    }

    /**
     * The text without the spaces, tabs and line breaks around it.
     *
     * @param text the text as typed; {@code null} for none
     * @return the text trimmed, empty for none
     */
    private static String trimmed(@Nullable String text) {
        return text == null ? "" : text.strip();
    }

    /**
     * A {@code like} pattern matching any text that holds this one, in lower case so the search ignores case. The
     * pattern's own wildcards, and {@code !} which escapes them, are escaped so each is matched as typed.
     *
     * @param text what to look for; empty to match everything
     * @return the pattern, such as {@code %report%}
     */
    private static String containing(String text) {
        String escaped = text.toLowerCase(Locale.ROOT)
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
        return "%" + escaped + "%";
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
