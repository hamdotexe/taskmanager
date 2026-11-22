package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.TaskStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The words the task screen puts together from what it shows: a time as the list writes it, the line under the
 * title, what an empty list says, and a count of characters as the form keeps it. Kept apart from the controls so
 * they can be tested without a window.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public final class TaskText {

    /** A time of day, as 21:40. */
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

    /** A day this year, as 16 Nov. */
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH);

    /** A day in another year, as 16 Nov 2025. */
    private static final DateTimeFormatter DAY_AND_YEAR = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    /**
     * Not made: the class only puts words together.
     */
    private TaskText() {
    }

    /**
     * A moment as the list writes it: "Today, 21:40", "Yesterday, 09:05", "16 Nov, 14:00" earlier this year, and
     * "16 Nov 2025, 14:00" before it.
     *
     * @param moment the moment, such as when a task was last changed
     * @param now the moment it is now
     * @param zone the time zone the user reads times in
     * @return the words
     */
    public static String when(Instant moment, Instant now, ZoneId zone) {
        ZonedDateTime at = moment.atZone(zone);
        LocalDate day = at.toLocalDate();
        LocalDate today = now.atZone(zone).toLocalDate();
        String time = at.format(TIME);
        if (day.equals(today)) {
            return "Today, " + time;
        }
        if (day.equals(today.minusDays(1))) {
            return "Yesterday, " + time;
        }
        if (day.getYear() == today.getYear()) {
            return at.format(DAY) + ", " + time;
        }
        return at.format(DAY_AND_YEAR) + ", " + time;
    }

    /**
     * A time from {@link #when(Instant, Instant, ZoneId)} as it reads inside a sentence: "today, 21:40" rather than
     * "Today, 21:40". A date starts with its day's number and stays as it is.
     *
     * @param when the time as {@code when} wrote it
     * @return the same words, starting in lower case
     */
    public static String lower(String when) {
        if (when.isEmpty()) {
            return when;
        }
        return Character.toLowerCase(when.charAt(0)) + when.substring(1);
    }

    /**
     * The list's heading: "All tasks", or the status it is filtered by.
     *
     * @param filter the status shown, or {@code null} for every task
     * @return the heading
     */
    public static String heading(@Nullable TaskStatus filter) {
        return filter == null ? "All tasks" : filter.getLabel();
    }

    /**
     * A number of tasks, said with the status they are in: "1 task", "3 pending tasks", "2 tasks in progress".
     *
     * @param count how many
     * @param status their status, or {@code null} for tasks in any
     * @return the words
     */
    public static String tasks(long count, @Nullable TaskStatus status) {
        String tasks = count == 1 ? "task" : "tasks";
        if (status == null) {
            return count + " " + tasks;
        }
        return switch (status) {
            case PENDING -> count + " pending " + tasks;
            case IN_PROGRESS -> count + " " + tasks + " in progress";
            case COMPLETED -> count + " completed " + tasks;
        };
    }

    /**
     * The line under the heading, saying what the list shows: "3 tasks, newest first.", or with a search, "2 of 5
     * pending tasks match “report”."
     *
     * @param shown how many tasks the list shows
     * @param inStatus how many there are under the filter before the search
     * @param filter the status shown, or {@code null} for every task
     * @param search the search as typed; blank for none
     * @return the line
     */
    public static String summary(int shown, long inStatus, @Nullable TaskStatus filter, String search) {
        String words = search.strip();
        if (words.isEmpty()) {
            return inStatus == 0 ? "Nothing here yet." : tasks(inStatus, filter) + ", newest first.";
        }
        return shown + " of " + tasks(inStatus, filter) + (shown == 1 ? " matches " : " match ") + "“" + words
                + "”.";
    }

    /**
     * What an empty list says, and what to do about it.
     *
     * @param total how many tasks there are in all
     * @param filter the status shown, or {@code null} for every task
     * @param search the search as typed; blank for none
     * @return its heading and the hint under it
     */
    public static Empty empty(long total, @Nullable TaskStatus filter, String search) {
        String words = search.strip();
        if (total == 0) {
            return new Empty("No tasks yet",
                    "Add your first with Add task or Ctrl+N, such as “Submit the OEL report”.");
        }
        if (!words.isEmpty()) {
            return new Empty("Nothing matches “" + words + "”",
                    "Search looks in titles and descriptions. Press Esc in the search field to clear it.");
        }
        if (filter == null) {
            return new Empty("No tasks", "Add one with Add task or Ctrl+N.");
        }
        return switch (filter) {
            case PENDING -> new Empty("No pending tasks", "A task you add starts here, as Pending.");
            case IN_PROGRESS -> new Empty("No tasks in progress", "Set a task to In Progress once you start it.");
            case COMPLETED -> new Empty("No completed tasks", "Set a task to Completed once it is done.");
        };
    }

    /**
     * How many characters a field holds against its limit, counted as the service counts them, after trimming:
     * "12 / 100".
     *
     * @param typed the text as typed
     * @param limit the most characters it may have
     * @return the count
     */
    public static String count(String typed, int limit) {
        return typed.strip().length() + " / " + limit;
    }

    /**
     * Whether a field holds more than its limit, counted after trimming, so the form can warn before saving.
     *
     * @param typed the text as typed
     * @param limit the most characters it may have
     * @return {@code true} if the service would refuse it as too long
     */
    public static boolean over(String typed, int limit) {
        return typed.strip().length() > limit;
    }

    /**
     * What an empty list says.
     *
     * @param title its heading, such as "No tasks yet"
     * @param hint what to do about it, under the heading
     * @author Hamza Rasool
     * @author Abdullah Khan
     */
    public record Empty(String title, String hint) {
    }
}
