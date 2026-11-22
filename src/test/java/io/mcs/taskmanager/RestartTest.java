package io.mcs.taskmanager;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.services.TaskService;
import io.mcs.taskmanager.shared.AppDataFolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tasks are kept in the SQLite file, not in memory: the app is started, given a task and closed, then started again on
 * the same folder, and finds the task as it was left.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
class RestartTest {

    /** The data folder both starts share, deleted once the test has run. */
    @TempDir
    Path dataFolder;

    /**
     * A task added and moved on before a restart is there after it, as it was left.
     */
    @Test
    void tasksSurviveARestart() {
        UUID taskId;
        try (ConfigurableApplicationContext app = start()) {
            TaskService taskService = app.getBean(TaskService.class);
            taskId = taskService.create("Survive a restart", "Kept in the SQLite file").getId();
            taskService.changeStatus(taskId, TaskStatus.IN_PROGRESS);
        }

        assertThat(dataFolder.resolve("taskmanager.db")).exists();
        try (ConfigurableApplicationContext app = start()) {
            TaskService taskService = app.getBean(TaskService.class);
            Task task = taskService.get(taskId);
            assertThat(task.getTitle()).isEqualTo("Survive a restart");
            assertThat(task.getDescription()).isEqualTo("Kept in the SQLite file");
            assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
            assertThat(taskService.list()).hasSize(1);
        }
    }

    /**
     * Starts the app's Spring side on the test's data folder, as the app starts on the user's.
     *
     * @return the running context; closing it lets go of the database file
     */
    private ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(TaskManagerApplication.class)
                .properties(AppDataFolder.PROPERTY + "=" + dataFolder)
                .run();
    }
}
