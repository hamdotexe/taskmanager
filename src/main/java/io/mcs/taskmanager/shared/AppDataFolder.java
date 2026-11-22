package io.mcs.taskmanager.shared;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Where the Task Manager keeps its data on this computer: the database, for the signed-in Windows user, in
 * {@code %LOCALAPPDATA%\TaskManager}. Local rather than roaming, so the database is never copied around with the
 * user's profile; never beside the program, where the user may not be allowed to write.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public final class AppDataFolder {

    /** The folder's name under {@code %LOCALAPPDATA%}. */
    public static final String FOLDER_NAME = "TaskManager";

    /**
     * The Spring property the database's location is read from. The app sets it to {@link #create()} before Spring
     * starts; a test sets it to a temporary folder of its own.
     */
    public static final String PROPERTY = "taskmanager.data-folder";

    /**
     * Not made: the class only finds the folder.
     */
    private AppDataFolder() {
    }

    /**
     * Finds the folder, making it if this is the first start: SQLite makes the database file but not the folders
     * above it. Outside Windows, where there is no {@code LOCALAPPDATA}, it is a hidden folder in the user's home.
     *
     * @return the folder, which exists once this returns
     * @throws UncheckedIOException if the folder could not be made
     */
    public static Path create() {
        String localAppData = System.getenv("LOCALAPPDATA");
        Path folder = localAppData != null
                ? Path.of(localAppData, FOLDER_NAME)
                : Path.of(System.getProperty("user.home"), "." + FOLDER_NAME.toLowerCase());
        try {
            return Files.createDirectories(folder);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not make the data folder " + folder, e);
        }
    }
}
