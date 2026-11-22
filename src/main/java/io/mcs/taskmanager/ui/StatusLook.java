package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.TaskStatus;
import io.mcs.taskmanager.ui.kit.Icons;

/**
 * How a status looks wherever the window shows it, in the design system's states: Pending is neutral, with a dash;
 * In Progress is info, under way, with a clock; Completed is success, with a check. The icon always goes with the
 * tint, so a status never rests on colour alone.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
final class StatusLook {

    /**
     * Not made: the class only looks statuses up.
     */
    private StatusLook() {
    }

    /**
     * The style class that tints a status pill.
     *
     * @param status the status
     * @return "pending", "in-progress" or "completed"
     */
    static String style(TaskStatus status) {
        return switch (status) {
            case PENDING -> "pending";
            case IN_PROGRESS -> "in-progress";
            case COMPLETED -> "completed";
        };
    }

    /**
     * The status's icon at 16 pixels, as the sidebar shows it.
     *
     * @param status the status
     * @return the icon's SVG path
     */
    static String icon(TaskStatus status) {
        return switch (status) {
            case PENDING -> Icons.PENDING;
            case IN_PROGRESS -> Icons.IN_PROGRESS;
            case COMPLETED -> Icons.COMPLETED;
        };
    }

    /**
     * The status's icon at 14 pixels, as a pill or the edit form's choice shows it.
     *
     * @param status the status
     * @return the icon's SVG path
     */
    static String smallIcon(TaskStatus status) {
        return switch (status) {
            case PENDING -> Icons.PENDING_14;
            case IN_PROGRESS -> Icons.IN_PROGRESS_14;
            case COMPLETED -> Icons.COMPLETED_14;
        };
    }
}
