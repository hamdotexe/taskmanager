package io.mcs.taskmanager.services;

import io.mcs.taskmanager.DatabaseTestBase;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Searching the list by title or description, filtering it by status, and counting the tasks in each status, over
 * four tasks: two Pending, one In Progress, one Completed.
 *
 * @author hamdotexe
 */
class SearchAndCountsTest extends DatabaseTestBase {

    /** The service under test. */
    @Autowired
    TaskService taskService;

    /** Used only to empty the list before each test. */
    @Autowired
    TaskRepository taskRepository;

    /**
     * Starts each test from the same four tasks.
     */
    @BeforeEach
    void fourTasks() {
        taskRepository.deleteAll();
        taskService.create("Write the OEL report", "IEEE format, two columns");
        taskService.create("Buy printer paper", "For the report's hard copy");
        taskService.changeStatus(taskService.create("Fix the 100% CPU bug", "In the solver_loop").getId(),
                TaskStatus.IN_PROGRESS);
        taskService.changeStatus(taskService.create("Push to GitHub", "").getId(), TaskStatus.COMPLETED);
    }

    /**
     * The text is found in a title or a description, whatever its case.
     */
    @Test
    void searchFindsTheTextInATitleOrDescriptionInAnyCase() {
        assertThat(titles("REPORT", null)).containsExactlyInAnyOrder("Write the OEL report", "Buy printer paper");
        assertThat(titles("github", null)).containsExactly("Push to GitHub");
    }

    /**
     * No text, or only spaces, matches every task.
     */
    @Test
    void noTextMatchesEveryTask() {
        assertThat(taskService.search(null, null)).hasSize(4);
        assertThat(taskService.search("   ", null)).hasSize(4);
    }

    /**
     * Text that is in no task finds nothing, rather than failing.
     */
    @Test
    void textInNoTaskFindsNothing() {
        assertThat(taskService.search("viva", null)).isEmpty();
    }

    /**
     * {@code %} and {@code _} are matched as typed, not as wildcards.
     */
    @Test
    void wildcardsAreMatchedAsTyped() {
        assertThat(titles("100%", null)).containsExactly("Fix the 100% CPU bug");
        assertThat(titles("_", null)).containsExactly("Fix the 100% CPU bug");
        assertThat(taskService.search("%%", null)).isEmpty();
    }

    /**
     * A status alone lists the tasks in it.
     */
    @Test
    void aStatusFilterListsOnlyThatStatus() {
        assertThat(titles("", TaskStatus.PENDING))
                .containsExactlyInAnyOrder("Write the OEL report", "Buy printer paper");
        assertThat(titles("", TaskStatus.IN_PROGRESS)).containsExactly("Fix the 100% CPU bug");
        assertThat(titles("", TaskStatus.COMPLETED)).containsExactly("Push to GitHub");
    }

    /**
     * Text and a status together narrow the list to tasks that match both.
     */
    @Test
    void textAndAStatusNarrowTheListTogether() {
        assertThat(titles("report", TaskStatus.PENDING))
                .containsExactlyInAnyOrder("Write the OEL report", "Buy printer paper");
        assertThat(titles("report", TaskStatus.COMPLETED)).isEmpty();
    }

    /**
     * The counts are per status over every task, and move as a task's status changes or a task is deleted.
     */
    @Test
    void theCountsFollowEveryChange() {
        TaskCounts counts = taskService.count();
        assertThat(counts.pending()).isEqualTo(2);
        assertThat(counts.inProgress()).isEqualTo(1);
        assertThat(counts.completed()).isEqualTo(1);
        assertThat(counts.total()).isEqualTo(4);
        assertThat(counts.of(TaskStatus.PENDING)).isEqualTo(2);

        taskService.changeStatus(taskService.search("paper", null).getFirst().getId(), TaskStatus.COMPLETED);
        taskService.delete(taskService.search("GitHub", null).getFirst().getId());

        TaskCounts after = taskService.count();
        assertThat(after.of(TaskStatus.PENDING)).isEqualTo(1);
        assertThat(after.of(TaskStatus.IN_PROGRESS)).isEqualTo(1);
        assertThat(after.of(TaskStatus.COMPLETED)).isEqualTo(1);
        assertThat(after.total()).isEqualTo(3);
    }

    /**
     * With no tasks every count is 0.
     */
    @Test
    void anEmptyListCountsNothing() {
        taskRepository.deleteAll();

        assertThat(taskService.count()).isEqualTo(new TaskCounts(0, 0, 0));
    }

    /**
     * The titles of the tasks a search finds.
     *
     * @param text what to look for
     * @param status the one status to list, or {@code null} for all
     * @return the titles found
     */
    private List<String> titles(String text, TaskStatus status) {
        return taskService.search(text, status).stream().map(task -> task.getTitle()).toList();
    }
}
