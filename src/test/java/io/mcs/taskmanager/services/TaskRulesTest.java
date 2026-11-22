package io.mcs.taskmanager.services;

import io.mcs.taskmanager.DatabaseTestBase;
import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * The rules a task is saved under, beyond the brief's own cases: a title that is not blank, both texts trimmed and
 * within their limits, a status, and a task chosen. A refused change saves nothing of itself.
 *
 * @author hamdotexe
 */
class TaskRulesTest extends DatabaseTestBase {

    /** The service under test. */
    @Autowired
    TaskService taskService;

    /** Used only to empty the list before each test. */
    @Autowired
    TaskRepository taskRepository;

    /**
     * Starts each test from an empty list.
     */
    @BeforeEach
    void emptyList() {
        taskRepository.deleteAll();
    }

    /**
     * A title of spaces and tabs is as good as none, and so is no title at all.
     */
    @Test
    void aBlankOrMissingTitleIsRefused() {
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.create(" \t  ", "Only spaces"))
                .withMessage("A task needs a title, such as \"Submit the OEL report\".");
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.create(null, "No title at all"))
                .withMessage("A task needs a title, such as \"Submit the OEL report\".");
        assertThat(taskService.list()).isEmpty();
    }

    /**
     * Spaces and line breaks around the title and the description are not kept.
     */
    @Test
    void theTitleAndDescriptionAreTrimmed() {
        Task task = taskService.create("   Buy printer paper  ", "\n  A4, two reams \n");

        Task saved = taskService.get(task.getId());
        assertThat(saved.getTitle()).isEqualTo("Buy printer paper");
        assertThat(saved.getDescription()).isEqualTo("A4, two reams");
    }

    /**
     * A task needs no description: none is kept as empty, never as {@code null}.
     */
    @Test
    void noDescriptionIsKeptAsEmpty() {
        Task task = taskService.create("Call the lab engineer", null);

        assertThat(taskService.get(task.getId()).getDescription()).isEmpty();
    }

    /**
     * A title may have exactly 100 characters, counted after trimming, but not 101.
     */
    @Test
    void aTitleIsKeptWithinItsLimit() {
        String longest = "t".repeat(Task.MAX_TITLE_CHARACTERS);

        assertThat(taskService.create("  " + longest + "  ", "").getTitle()).isEqualTo(longest);
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.create(longest + "t", ""))
                .withMessage("A title can be at most 100 characters; this one has 101.");
        assertThat(taskService.list()).hasSize(1);
    }

    /**
     * A description may have exactly 1000 characters, but not 1001.
     */
    @Test
    void aDescriptionIsKeptWithinItsLimit() {
        String longest = "d".repeat(Task.MAX_DESCRIPTION_CHARACTERS);

        assertThat(taskService.create("Longest description", longest).getDescription()).isEqualTo(longest);
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.create("Too long a description", longest + "d"))
                .withMessage("A description can be at most 1000 characters; this one has 1001.");
        assertThat(taskService.list()).hasSize(1);
    }

    /**
     * An update that breaks a rule is refused and leaves the task as it was.
     */
    @Test
    void aRefusedUpdateChangesNothing() {
        UUID taskId = taskService.create("Revise for the viva", "Chapters 1 to 4").getId();

        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.update(taskId, "  ", "Chapters 1 to 6", TaskStatus.COMPLETED))
                .withMessage("A task needs a title, such as \"Submit the OEL report\".");
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.update(taskId, "Revise for the viva", "Chapters 1 to 6", null))
                .withMessage("A task needs a status: Pending, In Progress or Completed.");

        Task saved = taskService.get(taskId);
        assertThat(saved.getTitle()).isEqualTo("Revise for the viva");
        assertThat(saved.getDescription()).isEqualTo("Chapters 1 to 4");
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    /**
     * A change with no task chosen is refused with a message, not an error.
     */
    @Test
    void noTaskChosenIsRefused() {
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.update(null, "Anything", "", TaskStatus.PENDING))
                .withMessage("Choose a task in the list first.");
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.get(null))
                .withMessage("Choose a task in the list first.");
    }
}
