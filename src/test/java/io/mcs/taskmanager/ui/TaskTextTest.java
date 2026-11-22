package io.mcs.taskmanager.ui;

import io.mcs.taskmanager.entities.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The words the task screen puts together: times as the list writes them, the line under the heading, what an empty
 * list says, and the form's count of characters. No window and no database: the words alone.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
class TaskTextTest {

    /** Karachi's time zone, so the days below fall where they would for the user. */
    private static final ZoneId KARACHI = ZoneId.of("Asia/Karachi");

    /** "Now" in every test: 22 Nov 2026, 22:40 in Karachi. */
    private static final Instant NOW = Instant.parse("2026-11-22T17:40:00Z");

    /**
     * A time today or yesterday is named so; earlier this year it gives the day and month, and before this year the
     * year too, always in the user's own time zone.
     */
    @Test
    void aTimeIsWrittenTheWayTheListWritesIt() {
        assertThat(TaskText.when(Instant.parse("2026-11-22T16:05:00Z"), NOW, KARACHI)).isEqualTo("Today, 21:05");
        assertThat(TaskText.when(Instant.parse("2026-11-21T04:30:00Z"), NOW, KARACHI)).isEqualTo("Yesterday, 09:30");
        assertThat(TaskText.when(Instant.parse("2026-03-16T09:00:00Z"), NOW, KARACHI)).isEqualTo("16 Mar, 14:00");
        assertThat(TaskText.when(Instant.parse("2025-11-16T09:00:00Z"), NOW, KARACHI))
                .isEqualTo("16 Nov 2025, 14:00");
    }

    /**
     * Just after midnight in Karachi is still the day before in UTC; the list goes by the user's day.
     */
    @Test
    void theDayIsTheUsersNotUtcs() {
        Instant justAfterMidnight = Instant.parse("2026-11-21T19:10:00Z");
        assertThat(TaskText.when(justAfterMidnight, NOW, KARACHI)).isEqualTo("Today, 00:10");
    }

    /**
     * Inside a sentence, "Today" and "Yesterday" start in lower case; a date stays as it is.
     */
    @Test
    void aTimeInsideASentenceStartsInLowerCase() {
        assertThat(TaskText.lower("Today, 21:05")).isEqualTo("today, 21:05");
        assertThat(TaskText.lower("Yesterday, 09:30")).isEqualTo("yesterday, 09:30");
        assertThat(TaskText.lower("16 Mar, 14:00")).isEqualTo("16 Mar, 14:00");
        assertThat(TaskText.lower("")).isEmpty();
    }

    /**
     * The heading is "All tasks" or the status filtered by.
     */
    @Test
    void theHeadingNamesWhatIsShown() {
        assertThat(TaskText.heading(null)).isEqualTo("All tasks");
        assertThat(TaskText.heading(TaskStatus.IN_PROGRESS)).isEqualTo("In Progress");
    }

    /**
     * A number of tasks is said with its status, and in the singular for one.
     */
    @Test
    void aNumberOfTasksIsSaidWithItsStatus() {
        assertThat(TaskText.tasks(1, null)).isEqualTo("1 task");
        assertThat(TaskText.tasks(3, null)).isEqualTo("3 tasks");
        assertThat(TaskText.tasks(1, TaskStatus.PENDING)).isEqualTo("1 pending task");
        assertThat(TaskText.tasks(2, TaskStatus.IN_PROGRESS)).isEqualTo("2 tasks in progress");
        assertThat(TaskText.tasks(4, TaskStatus.COMPLETED)).isEqualTo("4 completed tasks");
    }

    /**
     * The line under the heading counts what the filter holds, and with a search, how many of those match.
     */
    @Test
    void theSummarySaysWhatTheListShows() {
        assertThat(TaskText.summary(0, 0, null, "")).isEqualTo("Nothing here yet.");
        assertThat(TaskText.summary(3, 3, null, "  ")).isEqualTo("3 tasks, newest first.");
        assertThat(TaskText.summary(2, 2, TaskStatus.PENDING, "")).isEqualTo("2 pending tasks, newest first.");
        assertThat(TaskText.summary(1, 5, null, " report ")).isEqualTo("1 of 5 tasks matches “report”.");
        assertThat(TaskText.summary(2, 3, TaskStatus.COMPLETED, "git"))
                .isEqualTo("2 of 3 completed tasks match “git”.");
    }

    /**
     * An empty list says why: no tasks at all (brief test case 4), nothing matching the search, or nothing in the
     * status filtered by.
     */
    @Test
    void anEmptyListSaysWhy() {
        assertThat(TaskText.empty(0, null, "")).isEqualTo(new TaskText.Empty("No tasks yet",
                "Add your first with Add task or Ctrl+N, such as “Submit the OEL report”."));
        assertThat(TaskText.empty(0, TaskStatus.COMPLETED, "report").title()).isEqualTo("No tasks yet");
        assertThat(TaskText.empty(4, null, " viva ").title()).isEqualTo("Nothing matches “viva”");
        assertThat(TaskText.empty(4, TaskStatus.PENDING, "").title()).isEqualTo("No pending tasks");
        assertThat(TaskText.empty(4, TaskStatus.IN_PROGRESS, "").title()).isEqualTo("No tasks in progress");
        assertThat(TaskText.empty(4, TaskStatus.COMPLETED, "").title()).isEqualTo("No completed tasks");
    }

    /**
     * The form counts characters as the service does, after trimming, so spaces around a title never make it look
     * too long, and it warns only past the limit.
     */
    @Test
    void charactersAreCountedAfterTrimming() {
        assertThat(TaskText.count("  Submit the report  ", 100)).isEqualTo("17 / 100");
        assertThat(TaskText.count("", 1000)).isEqualTo("0 / 1000");
        assertThat(TaskText.over("  " + "x".repeat(100) + "  ", 100)).isFalse();
        assertThat(TaskText.over("x".repeat(101), 100)).isTrue();
    }
}
