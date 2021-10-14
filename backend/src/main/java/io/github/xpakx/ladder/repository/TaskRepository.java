package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    List<Task> findByOwnerIdAndProjectIsNull(Integer userId);

    void deleteByIdAndOwnerId(Integer taskId, Integer ownerId);
    List<Task> findByOwnerIdAndParentId(Integer ownerId, Integer parentId);
    <T> List<T> findByOwnerIdAndParentIsNull(Integer ownerId, Class<T> type);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project IS NULL AND p.parent IS NULL")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.parent.id = :parentId AND p.project IS NULL")
    Integer getMaxOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project.id = :projectId AND p.parent IS NULL")
    Integer getMaxOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId);

    @Query("SELECT coalesce(max(p.projectOrder), 0) FROM Task p WHERE p.owner.id = :ownerId AND p.project.id = :projectId AND p.parent.id = :parentId")
    Integer getMaxOrderByOwnerIdAndProjectIdAndParentId(Integer ownerId, Integer projectId, Integer parentId);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent.id = :parentId AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndParentIdAndOrderGreaterThan(Integer ownerId, Integer parentId, Integer projectOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThan(Integer ownerId, Integer projectId, Integer projectOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndOrderGreaterThanEqual(Integer ownerId, Integer projectOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent.id = :parentId AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndParentIdAndOrderGreaterThanEqual(Integer ownerId, Integer parentId, Integer projectOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThanEqual(Integer ownerId, Integer projectId, Integer projectOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndOrderGreaterThan(Integer ownerId, Integer projectOrder);

    @Query("SELECT coalesce(max(t.dailyViewOrder), 0) FROM Task t WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0")
    Integer getMaxOrderByOwnerIdAndDate(Integer ownerId, LocalDateTime date);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1 WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0 AND t.dailyViewOrder > :dailyOrder")
    void incrementOrderByOwnerIdAndDateAndOrderGreaterThan(Integer ownerId, LocalDateTime date, Integer dailyOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1 WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0 AND t.dailyViewOrder >= :dailyOrder")
    void incrementOrderByOwnerIdAndDateAndOrderGreaterThanEqual(Integer ownerId, LocalDateTime date, Integer dailyOrder);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1 WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0")
    void incrementOrderByOwnerIdAndDate(Integer ownerId, LocalDateTime date);

    @EntityGraph("task-with-labels")
    List<TaskDetails> findByIdIn(List<Integer> ids);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent.id = :parentId")
    void incrementGeneralOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId")
    void incrementGeneralOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1 WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL")
    void incrementGeneralOrderByOwnerId(Integer ownerId);

    @EntityGraph("task-with-labels")
    List<Task> findByProjectIdIn(List<Integer> ids);
}
