package io.mcs.taskmanager.entities;

/**
 * How far a task has got. Every task starts {@link #PENDING}; the user moves it on, or back, as the work goes.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public enum TaskStatus {

    /** Not started yet. Every new task starts here. */
    PENDING("Pending"),

    /** Being worked on. */
    IN_PROGRESS("In Progress"),

    /** Done. */
    COMPLETED("Completed");

    /** The status as the screens show it, such as "In Progress". */
    private final String label;

    /**
     * Makes a status with the words the screens show for it.
     *
     * @param label the status as the screens show it
     */
    TaskStatus(String label) {
        this.label = label;
    }

    /**
     * The status as the screens show it.
     *
     * @return the label, such as "In Progress"
     */
    public String getLabel() {
        return label;
    }

    /**
     * The label, so a list or choice box shows "In Progress" rather than {@code IN_PROGRESS}. The database keeps the
     * constant's name, never this.
     *
     * @return the label
     */
    @Override
    public String toString() {
        return label;
    }
}
