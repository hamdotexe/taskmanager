package io.mcs.taskmanager.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * Something the user has to do: a title, a description, how far it has got, and when it was added and last changed.
 * Kept until the user deletes it.
 *
 * <p>The rules a task is saved under (a title that is not blank, both texts trimmed and within their limits) are
 * {@code TaskService}'s, which refuses a task that breaks one with a message the screen shows. The constraints here
 * only stop a task that slipped past the service from reaching the database.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
@Entity
@Table(name = "tasks")
public class Task {

    /** The most characters a title may have, counted after trimming. */
    public static final int MAX_TITLE_CHARACTERS = 100;

    /** The most characters a description may have, counted after trimming. */
    public static final int MAX_DESCRIPTION_CHARACTERS = 1000;

    /** Given by Hibernate when the task is first saved, and never changed. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** What is to be done, in a few words, such as "Submit the OEL report". Never blank. */
    @NotBlank
    @Size(max = MAX_TITLE_CHARACTERS)
    @Column(name = "title", nullable = false, length = MAX_TITLE_CHARACTERS)
    private String title;

    /** More about it, in as many words as the user likes up to the limit; empty when there is nothing more to say. */
    @NotNull
    @Size(max = MAX_DESCRIPTION_CHARACTERS)
    @Column(name = "description", nullable = false, length = MAX_DESCRIPTION_CHARACTERS)
    private String description = "";

    /** How far it has got; {@link TaskStatus#PENDING} until the user says otherwise. */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private TaskStatus status = TaskStatus.PENDING;

    /** When it was added. */
    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** When it was last changed: its title, description or status. The same as {@link #createdAt} until then. */
    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Makes an empty task, still to be given its title and times. Hibernate uses it too, to load a saved one.
     */
    public Task() {
    }

    /**
     * The task's id.
     *
     * @return the id, or {@code null} while the task has not been saved
     */
    public UUID getId() {
        return id;
    }

    /**
     * What is to be done.
     *
     * @return the title, trimmed
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets what is to be done.
     *
     * @param title the title, already trimmed and checked by the service
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * More about the task.
     *
     * @return the description, trimmed; empty when there is none
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets more about the task.
     *
     * @param description the description, already trimmed and checked by the service; empty for none
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * How far the task has got.
     *
     * @return the status
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * Sets how far the task has got.
     *
     * @param status the status
     */
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    /**
     * When the task was added.
     *
     * @return the moment it was first saved
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets when the task was added. Saved once: later changes to it are never written.
     *
     * @param createdAt the moment it is first saved
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * When the task was last changed.
     *
     * @return the moment of its last change, or of its adding if it has not changed since
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets when the task was last changed.
     *
     * @param updatedAt the moment of this change
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
