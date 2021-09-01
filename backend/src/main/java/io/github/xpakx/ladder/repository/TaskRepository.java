package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @EntityGraph("task-with-children")
    Optional<Task> getByIdAndOwnerId(Integer taskId, Integer ownerId);
    Optional<Task> findByIdAndOwnerId(Integer taskId, Integer ownerId);
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);

    <T> List<T> findByOwnerId(Integer userId, Class<T> type);

    void deleteByIdAndOwnerId(Integer taskId, Integer ownerId);
}
