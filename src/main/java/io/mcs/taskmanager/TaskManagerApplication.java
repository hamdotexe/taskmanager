package io.mcs.taskmanager;

import io.mcs.taskmanager.shared.AppDataFolder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Starts the Task Manager: the class Spring Boot is configured from, and the one the IDE and the Maven wrapper run.
 *
 * @author hamdotexe
 */
@SpringBootApplication
public class TaskManagerApplication {

    /**
     * Makes the application class; Spring does this itself when it starts.
     */
    public TaskManagerApplication() {
    }

    /**
     * Starts Spring with this class as its configuration, its database in the user's data folder.
     *
     * @param args the command-line arguments, handed on to Spring as they are
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(TaskManagerApplication.class)
                .properties(AppDataFolder.PROPERTY + "=" + AppDataFolder.create())
                .run(args);
    }
}
