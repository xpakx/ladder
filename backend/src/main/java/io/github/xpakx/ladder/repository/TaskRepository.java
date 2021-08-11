package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.TaskWithChildren;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    List<TaskWithChildren> findByProjectIdAndParentIsNull(Integer projectId);

    void deleteByIdAndOwnerId(Integer taskId, Integer ownerId);

    Optional<Task> findByIdAndOwnerId(Integer taskId, Integer ownerId);
}
