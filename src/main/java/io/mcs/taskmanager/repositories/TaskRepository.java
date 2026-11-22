package io.mcs.taskmanager.repositories;

import io.mcs.taskmanager.entities.Task;
import io.mcs.taskmanager.entities.TaskStatus;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * The user's tasks, newest first: all of them, those matching a search, and how many have each status.
 *
 * @author Hamza Rasool
 * @author Abdullah Khan
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Every task, the one added last first.
     *
     * @return the tasks, newest first; empty when there are none
     */
    List<Task> findAllByOrderByCreatedAtDesc();

    /**
     * The tasks whose title or description matches the pattern, whatever its case, in one status or in all.
     *
     * @param pattern a {@code like} pattern in lower case, such as {@code %report%}, with any {@code %}, {@code _}
     *        or {@code !} meant literally escaped by {@code !}
     * @param status the one status to list, or {@code null} for every status
     * @return the matching tasks, newest first; empty when none match
     */
    @Query("""
            select task from Task task
            where (:status is null or task.status = :status)
              and (lower(task.title) like :pattern escape '!' or lower(task.description) like :pattern escape '!')
            order by task.createdAt desc
            """)
    List<Task> search(@Param("pattern") String pattern, @Param("status") @Nullable TaskStatus status);

    /**
     * How many tasks have the status.
     *
     * @param status the status to count
     * @return the number of tasks in it; 0 when there are none
     */
    long countByStatus(TaskStatus status);
}
