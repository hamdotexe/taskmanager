package io.mcs.taskmanager.services;

import java.io.Serial;

/**
 * A change the app will not make, and why, in words the screen shows as they are. Thrown inside a service's
 * transaction, so nothing of the change is saved.
 *
 * @author hamdotexe
 */
public class Refused extends RuntimeException {

    /** The version of this class's serialized form. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Refuses a change for the reason given.
     *
     * @param reason why, as the user is to read it, such as "A task needs a title."
     */
    public Refused(String reason) {
        super(reason);
    }
}
