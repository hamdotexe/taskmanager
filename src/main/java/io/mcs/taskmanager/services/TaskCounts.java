package io.mcs.taskmanager.services;

import io.mcs.taskmanager.entities.TaskStatus;

/**
 * How many tasks there are in each status, as the counts kept in view show them. Counted over every task, whatever
 * the list is searched or filtered by.
 *
 * @param pending how many tasks are not started yet
 * @param inProgress how many are being worked on
 * @param completed how many are done
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public record TaskCounts(long pending, long inProgress, long completed) {

    /**
     * How many tasks there are in all.
     *
     * @return the number of tasks, whatever their status
     */
    public long total() {
        return pending + inProgress + completed;
    }

    /**
     * How many tasks have the status, for a screen that goes through {@link TaskStatus#values()}.
     *
     * @param status the status counted
     * @return the number of tasks in it
     */
    public long of(TaskStatus status) {
        return switch (status) {
            case PENDING -> pending;
            case IN_PROGRESS -> inProgress;
            case COMPLETED -> completed;
        };
    }
}
