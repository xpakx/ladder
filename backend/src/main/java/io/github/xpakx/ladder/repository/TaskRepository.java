package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Project;
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
    List<Task> getByOwnerIdAndProjectIsNotNull(Integer userId);

    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
    List<Task> findByOwnerIdAndProjectId(Integer userId, Integer projectId);

    void deleteByIdAndOwnerId(Integer taskId, Integer ownerId);
    List<Task> findByOwnerIdAndParentIdAndProjectOrderGreaterThan(Integer ownerId, Integer parentId, Integer projectOrder);
    List<Task> findByOwnerIdAndParentIsNullAndProjectOrderGreaterThan(Integer ownerID, Integer projectOrder);

    List<Task> findByOwnerIdAndParentId(Integer ownerId, Integer parentId);
    List<Task> findByOwnerIdAndProjectIdAndParentIsNull(Integer ownerId, Integer projectId);
    <T> List<T> findByOwnerIdAndParentIsNull(Integer ownerId, Class<T> type);
}
