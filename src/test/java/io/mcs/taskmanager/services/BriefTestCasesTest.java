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
 * The seven test cases the lab's brief lists, each against the task service on a real SQLite database, in the brief's
 * order. Every test starts from an empty list.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
class BriefTestCasesTest extends DatabaseTestBase {

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
     * Test case 1, add a valid task: the task is added, Pending, with its id and times.
     */
    @Test
    void tc1AValidTaskIsAdded() {
        Task task = taskService.create("Submit the OEL report", "PDF and source ZIP on GCR");

        assertThat(task.getId()).isNotNull();
        assertThat(taskService.list()).singleElement().satisfies(saved -> {
            assertThat(saved.getId()).isEqualTo(task.getId());
            assertThat(saved.getTitle()).isEqualTo("Submit the OEL report");
            assertThat(saved.getDescription()).isEqualTo("PDF and source ZIP on GCR");
            assertThat(saved.getStatus()).isEqualTo(TaskStatus.PENDING);
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());
        });
    }

    /**
     * Test case 2, add a task with an empty title: refused with the message the screen shows, and nothing saved.
     */
    @Test
    void tc2ATaskWithAnEmptyTitleIsRefusedWithAMessage() {
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.create("", "No title given"))
                .withMessage("A task needs a title, such as \"Submit the OEL report\".");
        assertThat(taskService.list()).isEmpty();
    }

    /**
     * Test case 3, view all tasks: every task comes back, whatever its status, the newest first.
     */
    @Test
    void tc3AllTasksAreListed() {
        Task first = taskService.create("Write the entity", "");
        Task second = taskService.create("Write the service", "");
        Task third = taskService.create("Write the tests", "");
        taskService.update(second.getId(), "Write the service", "", TaskStatus.IN_PROGRESS);
        taskService.update(third.getId(), "Write the tests", "", TaskStatus.COMPLETED);

        assertThat(taskService.list())
                .extracting(task -> task.getTitle())
                .containsExactlyInAnyOrder("Write the entity", "Write the service", "Write the tests");
        assertThat(taskService.list())
                .extracting(task -> task.getStatus())
                .containsExactlyInAnyOrder(TaskStatus.PENDING, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED);
        assertThat(taskService.list().getLast().getId()).isEqualTo(first.getId());
    }

    /**
     * Test case 4, view an empty list: the list is empty, not an error, so the screen can say there are no tasks.
     */
    @Test
    void tc4AnEmptyListIsEmpty() {
        assertThat(taskService.list()).isEmpty();
    }

    /**
     * Test case 5, update an existing task: its title, description and status are replaced and its update time
     * moves on.
     */
    @Test
    void tc5AnExistingTaskIsUpdated() {
        UUID taskId = taskService.create("Draft the report", "Two pages").getId();
        Task before = taskService.get(taskId);

        taskService.update(taskId, "Draft the IEEE report", "Two columns, four pages", TaskStatus.IN_PROGRESS);

        Task saved = taskService.get(taskId);
        assertThat(saved.getTitle()).isEqualTo("Draft the IEEE report");
        assertThat(saved.getDescription()).isEqualTo("Two columns, four pages");
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(saved.getCreatedAt()).isEqualTo(before.getCreatedAt());
        assertThat(saved.getUpdatedAt()).isAfterOrEqualTo(before.getUpdatedAt());
        assertThat(taskService.list()).hasSize(1);
    }

    /**
     * Test case 6, update a non-existing task: refused with a plain message, and nothing is added in its place.
     */
    @Test
    void tc6UpdatingANonExistingTaskIsRefusedGracefully() {
        UUID noSuchTask = UUID.randomUUID();

        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.update(noSuchTask, "Ghost", "", TaskStatus.PENDING))
                .withMessage("This task no longer exists: it may have been deleted in another copy of the Task "
                        + "Manager.");
        assertThat(taskService.list()).isEmpty();
    }

    /**
     * Test case 7, delete an existing task: it is removed and the others stay.
     */
    @Test
    void tc7AnExistingTaskIsDeleted() {
        Task kept = taskService.create("Keep me", "");
        Task deleted = taskService.create("Delete me", "");

        taskService.delete(deleted.getId());

        assertThat(taskService.list()).extracting(task -> task.getId()).containsExactly(kept.getId());
    }
}
