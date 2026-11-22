package io.mcs.taskmanager;

import org.junit.jupiter.api.Test;

/**
 * Starts the app's Spring side on a new, empty database. Drift between an entity and its migration fails here, not on
 * a user's computer.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
class FoundationTest extends DatabaseTestBase {

    /**
     * Spring starts, Flyway makes the tables and Hibernate finds them as the entities expect.
     */
    @Test
    void startsOnAFreshDatabase() {
    }
}
