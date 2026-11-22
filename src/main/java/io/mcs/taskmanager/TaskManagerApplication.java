package io.mcs.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
     * Starts Spring with this class as its configuration.
     *
     * @param args the command-line arguments, handed on to Spring as they are
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskManagerApplication.class, args);
    }
}
