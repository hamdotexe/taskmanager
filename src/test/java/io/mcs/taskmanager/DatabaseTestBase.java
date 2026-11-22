package io.mcs.taskmanager;

import io.mcs.taskmanager.shared.AppDataFolder;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

/**
 * A test of the app's Spring side on a new, empty database of its own, in a temporary folder: Flyway runs every
 * migration and Hibernate checks every entity against the tables they made. No window opens.
 *
 * <p>The context is closed after each class, so its connection lets go of the database file before JUnit deletes the
 * folder: Windows refuses to delete a file that is still open.
 *
 * @author hamdotexe
 */
@SpringBootTest
@DirtiesContext
public abstract class DatabaseTestBase {

    /** The temporary folder the test's database is made in, deleted once the class has run. */
    @TempDir
    protected static Path dataFolder;

    /**
     * Points the database at the temporary folder rather than the user's data folder.
     *
     * @param registry where Spring takes properties from before the context starts
     */
    @DynamicPropertySource
    static void freshDatabase(DynamicPropertyRegistry registry) {
        registry.add(AppDataFolder.PROPERTY, () -> dataFolder.toString());
    }
}
