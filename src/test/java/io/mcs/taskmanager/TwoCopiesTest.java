package io.mcs.taskmanager;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.repositories.TaskRepository;
import io.mcs.taskmanager.services.Refused;
import io.mcs.taskmanager.services.TaskService;
import io.mcs.taskmanager.shared.AppDataFolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Two copies of the app open on the same database file, as brief test case 6 is shown by hand: this test's context is
 * one copy, and a second Spring context started on the same folder is the other. Each has its own connection to the
 * file, as two running copies of the app do.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
class TwoCopiesTest extends DatabaseTestBase {

    /** The message every change to a task deleted elsewhere is refused with. */
    private static final String GONE =
            "This task no longer exists: it may have been deleted in another copy of the Task Manager.";

    /** This copy's service. */
    @Autowired
    TaskService taskService;

    /** This copy's repository, to read a task inside a transaction the test holds open. */
    @Autowired
    TaskRepository taskRepository;

    /** Runs a transaction in this copy, as a service method does. */
    @Autowired
    TransactionTemplate transaction;

    /**
     * Starts each test from an empty list.
     */
    @BeforeEach
    void emptyList() {
        taskRepository.deleteAll();
    }

    /**
     * Test case 6 as the user meets it: the other copy deletes a task this copy still lists, and this copy's update,
     * status change and delete of it are each refused with a plain message.
     */
    @Test
    void aTaskDeletedInTheOtherCopyIsRefusedHereWithAMessage() {
        UUID taskId = taskService.create("Shared task", "Open in both copies").getId();
        try (ConfigurableApplicationContext otherCopy = otherCopy()) {
            TaskService otherTaskService = otherCopy.getBean(TaskService.class);
            assertThat(otherTaskService.get(taskId).getTitle()).isEqualTo("Shared task");
            otherTaskService.delete(taskId);
        }

        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.update(taskId, "Changed here", "", TaskStatus.IN_PROGRESS))
                .withMessage(GONE);
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.changeStatus(taskId, TaskStatus.COMPLETED))
                .withMessage(GONE);
        assertThatExceptionOfType(Refused.class)
                .isThrownBy(() -> taskService.delete(taskId))
                .withMessage(GONE);
        assertThat(taskService.list()).isEmpty();
    }

    /**
     * What one copy adds or changes, the other reads at once.
     */
    @Test
    void eachCopySeesTheOthersChanges() {
        UUID taskId = taskService.create("Made here", "").getId();
        try (ConfigurableApplicationContext otherCopy = otherCopy()) {
            TaskService otherTaskService = otherCopy.getBean(TaskService.class);
            otherTaskService.update(taskId, "Changed there", "By the other copy", TaskStatus.COMPLETED);
            otherTaskService.create("Made there", "");

            Task changed = taskService.get(taskId);
            assertThat(changed.getTitle()).isEqualTo("Changed there");
            assertThat(changed.getStatus()).isEqualTo(TaskStatus.COMPLETED);
            assertThat(taskService.list()).extracting(task -> task.getTitle())
                    .containsExactlyInAnyOrder("Changed there", "Made there");
            assertThat(otherTaskService.count().total()).isEqualTo(2);
        }
    }

    /**
     * The other copy deletes a task while this copy, having read it, is still saving a change to it. The delete waits
     * for the save to finish and then goes through, so neither copy fails: this copy's save is never undone by a
     * database error halfway.
     *
     * @throws Exception if this copy's save fails, or either side does not finish in time
     */
    @Test
    void aDeleteInTheOtherCopyWaitsWhileThisCopyIsSaving() throws Exception {
        UUID taskId = taskService.create("Saved while deleted", "").getId();
        try (ConfigurableApplicationContext otherCopy = otherCopy()) {
            TaskService otherTaskService = otherCopy.getBean(TaskService.class);
            CountDownLatch read = new CountDownLatch(1);

            CompletableFuture<Void> saving = CompletableFuture.runAsync(() -> transaction.executeWithoutResult(
                    status -> {
                        Task task = taskRepository.findById(taskId).orElseThrow();
                        read.countDown();
                        pause();
                        task.setTitle("Saved first");
                    }));
            assertThat(read.await(10, TimeUnit.SECONDS)).isTrue();
            otherTaskService.delete(taskId);
            saving.get(10, TimeUnit.SECONDS);
        }

        assertThat(taskService.list()).isEmpty();
    }

    /**
     * Starts the other copy: the app's Spring side on this test's data folder.
     *
     * @return the other copy's context; closing it lets go of the database file
     */
    private static ConfigurableApplicationContext otherCopy() {
        return new SpringApplicationBuilder(TaskManagerApplication.class)
                .properties(AppDataFolder.PROPERTY + "=" + dataFolder)
                .run();
    }

    /**
     * Holds this copy's save open for half a second, long enough for the other copy to try its delete.
     */
    private static void pause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while holding the save open", e);
        }
    }
}
