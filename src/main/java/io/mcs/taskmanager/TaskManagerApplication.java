package io.mcs.taskmanager;

import io.mcs.taskmanager.ui.DesktopApp;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.Logger;

/**
 * Starts the Task Manager: the class Spring Boot is configured from, and the one the IDE and the Maven wrapper run.
 * Not itself the JavaFX application: a main class extending {@code Application} refuses to start from a plain
 * classpath, so this one hands over to {@link DesktopApp}, which brings up Spring and then the window.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
@SpringBootApplication
public class TaskManagerApplication {

    /**
     * JavaFX's own logger, held here so the filter set on it lasts: Java keeps loggers nobody holds only weakly, and a
     * logger made again would come without it.
     */
    private static final Logger JAVAFX_LOG = Logger.getLogger("javafx");

    /** How JavaFX's notice that it was loaded from the classpath begins. */
    private static final String CLASSPATH_NOTICE = "Unsupported JavaFX configuration";

    /**
     * Makes the application class; Spring does this itself when it starts.
     */
    public TaskManagerApplication() {
    }

    /**
     * Opens the Task Manager: sets how text is smoothed, then launches the window, which starts Spring.
     *
     * @param args the command-line arguments, handed on to JavaFX and from it to Spring as they are
     */
    public static void main(String[] args) {
        smoothTextVertically();
        acceptJavaFxOnTheClasspath();
        Application.launch(DesktopApp.class, args);
    }

    /**
     * Keeps JavaFX from warning, at every start, that its classes were loaded from the classpath rather than as
     * modules. That is how Spring Boot, the Maven wrapper and the IDE all run it, and it works in full; the warning
     * only says JavaFX's makers test the other way. The filter drops that one notice and nothing else: every other
     * message of JavaFX's, its stylesheet warnings among them, still reaches the console.
     */
    private static void acceptJavaFxOnTheClasspath() {
        JAVAFX_LOG.setFilter(entry -> !String.valueOf(entry.getMessage()).startsWith(CLASSPATH_NOTICE));
    }

    /**
     * Has JavaFX smooth text's tops and bottoms as well as its sides. By default it draws ClearType text smoothed
     * only sideways, so at display scales such as 125% the rounded tops of large letters come out flat, as if cut
     * off. It must be set before JavaFX starts, and a value given on the command line wins.
     */
    private static void smoothTextVertically() {
        if (System.getProperty("prism.subpixeltext") == null) {
            System.setProperty("prism.subpixeltext", "vertical");
        }
    }
}
