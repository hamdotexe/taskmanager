package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.TaskManagerApplication;
import io.mcs.taskmanager.shared.AppDataFolder;
import io.mcs.taskmanager.ui.kit.Icons;
import io.mcs.taskmanager.ui.kit.Ui;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HeaderBar;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * The JavaFX application. Spring starts before the window opens, so the database is migrated by then, and closes
 * with it. The window is FXML whose controller is a Spring bean, handed the task service like any other bean. It
 * opens in Nunito at the size the screen is designed for, or the screen's if that is less, and has no system title
 * bar: the window's own header takes its place, the system's minimize, maximize and close buttons drawn on it, and the
 * Task Manager's mark is its icon in the taskbar and Alt+Tab.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public class DesktopApp extends Application {

    /** The app's name, in the taskbar and Alt+Tab. */
    public static final String NAME = "Task Manager";

    /** Nunito's weights the stylesheet uses, each a face of its own. */
    private static final List<String> FACES = List.of("Medium", "SemiBold", "Bold", "ExtraBold");

    /** The header's height in the stylesheet, which the system's buttons are drawn as tall as. */
    private static final double HEADER_HEIGHT = 48;

    /** The window's width when it opens, unless the screen is narrower. */
    private static final double WIDTH = 1200;

    /** The window's height when it opens, unless the screen is shorter. */
    private static final double HEIGHT = 760;

    /** The narrowest the window goes, at which the list still shows every column. */
    private static final double MIN_WIDTH = 960;

    /** The shortest the window goes, at which the sidebar still shows every filter and shortcut. */
    private static final double MIN_HEIGHT = 600;

    /** Spring's context: the services, the repositories, the database and the window's controller. */
    private ConfigurableApplicationContext context;

    /**
     * Makes the application; JavaFX does this when {@link TaskManagerApplication#main(String[])} launches it.
     */
    public DesktopApp() {
    }

    /**
     * Starts Spring, before the window opens: the database is found or made in the user's data folder, and brought
     * up to date by Flyway.
     */
    @Override
    public void init() {
        context = new SpringApplicationBuilder(TaskManagerApplication.class)
                .headless(false)
                .properties(AppDataFolder.PROPERTY + "=" + AppDataFolder.create())
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    /**
     * Opens the window: loads Nunito, the FXML with its controller from Spring, and the stylesheet, and puts the
     * header in place of the system's title bar. A failure nobody expected on the window's thread is shown in the
     * window as a plain sentence, never as an error's trace.
     *
     * @param stage the window JavaFX gives the application
     * @throws IOException if the window's FXML cannot be read
     */
    @Override
    public void start(Stage stage) throws IOException {
        loadFonts();
        FXMLLoader loader = new FXMLLoader(DesktopApp.class.getResource("main-window.fxml"));
        loader.setControllerFactory(type -> context.getBean(type));
        Parent root = loader.load();
        MainWindowController window = loader.getController();
        Thread.currentThread().setUncaughtExceptionHandler((_, failure) -> window.failed(failure));

        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, Math.min(WIDTH, screen.getWidth()), Math.min(HEIGHT, screen.getHeight()));
        scene.getStylesheets().add(Ui.stylesheet());
        stage.initStyle(StageStyle.EXTENDED);
        HeaderBar.setSystemColorScheme(stage, ColorScheme.LIGHT);
        HeaderBar.setSystemButtonHeight(stage, HEADER_HEIGHT);
        stage.getIcons().setAll(Icons.markImages());
        stage.setTitle(NAME);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Closes Spring once the window has closed, and with it the database.
     */
    @Override
    public void stop() {
        context.close();
    }

    /**
     * Loads Nunito, each weight its own face, as the stylesheet names them.
     *
     * @throws IOException if a font file cannot be read
     */
    private static void loadFonts() throws IOException {
        for (String face : FACES) {
            try (InputStream font = DesktopApp.class.getResourceAsStream("fonts/Nunito-" + face + ".ttf")) {
                if (font != null) {
                    Font.loadFont(font, 14);
                }
            }
        }
    }
}
