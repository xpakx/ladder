package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    @EntityGraph("task-with-children")
    Optional<Task> getByIdAndOwnerId(Integer taskId, Integer ownerId);
    Optional<Task> findByIdAndOwnerId(Integer taskId, Integer ownerId);
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    List<Task> getByOwnerIdAndProjectIsNotNull(Integer userId);

    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
    List<Task> findByOwnerIdAndProjectId(Integer userId, Integer projectId);

    void deleteByIdAndOwnerId(Integer taskId, Integer ownerId);
    List<Task> findByOwnerIdAndParentIdAndProjectOrderGreaterThan(Integer ownerId, Integer parentId, Integer projectOrder);

    List<Task> findByOwnerIdAndParentId(Integer ownerId, Integer parentId);
    List<Task> findByOwnerIdAndProjectIdAndParentIsNull(Integer ownerId, Integer projectId);
    <T> List<T> findByOwnerIdAndParentIsNull(Integer ownerId, Class<T> type);

    List<Task> findByOwnerIdAndParentIdAndProjectOrderGreaterThanEqual(Integer ownerId, Integer parentId, Integer projectOrder);

    List<Task> findByOwnerIdAndProjectIdAndParentIsNullAndProjectOrderGreaterThanEqual(Integer ownerId, Integer projectId, Integer projectOrder);
    List<Task> findByOwnerIdAndProjectIdAndParentIsNullAndProjectOrderGreaterThan(Integer ownerId, Integer projectId, Integer projectOrder);

    List<Task> findByOwnerIdAndProjectIsNullAndParentIsNullAndProjectOrderGreaterThanEqual(Integer ownerId, Integer projectOrder);
    List<Task> findByOwnerIdAndProjectIsNullAndParentIsNullAndProjectOrderGreaterThan(Integer ownerId, Integer projectOrder);

    List<Task> findByOwnerIdAndProjectIsNullAndParentIsNull(Integer ownerId);



    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project IS NULL AND p.parent IS NULL")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.parent.id = :parentId AND p.project IS NULL")
    Integer getMaxOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project.id = :projectId AND p.parent IS NULL")
    Integer getMaxOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project.id = :projectId AND p.parent.id = :parentId")
    Integer getMaxOrderByOwnerIdAndProjectIdAndParentId(Integer ownerId, Integer projectId, Integer parentId);
}
