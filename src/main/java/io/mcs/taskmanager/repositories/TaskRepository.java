package io.mcs.taskmanager.repositories;

import io.mcs.taskmanager.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * The user's tasks, newest first.
 *
 * @author hamdotexe
 */
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /**
     * Every task, the one added last first.
     *
     * @return the tasks, newest first; empty when there are none
     */
    List<Task> findAllByOrderByCreatedAtDesc();
}
