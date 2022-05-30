package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.task.dto.TaskDetails;
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

    Optional<Task> getByIdAndOwnerId(Integer taskId, Integer ownerId);
    Optional<Task> findByIdAndOwnerId(Integer taskId, Integer ownerId);
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    List<Task> getByOwnerIdAndProjectIsNotNull(Integer userId);

    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerIdAndArchived(Integer userId, boolean archived, Class<T> type);
    List<Task> findByOwnerIdAndProjectId(Integer ownerId, Integer projectId);
    List<Task> findByOwnerIdAndProjectIdAndArchived(Integer ownerId, Integer projectId, boolean archived);
    @EntityGraph("task-with-labels")
    <T> List<T> getByOwnerIdAndProjectIdAndArchived(Integer ownerId, Integer projectId, boolean archived, Class<T> type);
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
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent.id = :parentId AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndParentIdAndOrderGreaterThan(Integer ownerId, Integer parentId, Integer projectOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThan(Integer ownerId, Integer projectId, Integer projectOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndOrderGreaterThanEqual(Integer ownerId, Integer projectOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent.id = :parentId AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndParentIdAndOrderGreaterThanEqual(Integer ownerId, Integer parentId, Integer projectOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId AND t.projectOrder >= :projectOrder")
    void incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThanEqual(Integer ownerId, Integer projectId, Integer projectOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL AND t.projectOrder > :projectOrder")
    void incrementOrderByOwnerIdAndOrderGreaterThan(Integer ownerId, Integer projectOrder, LocalDateTime modifiedAt);

    @Query("SELECT coalesce(max(t.dailyViewOrder), 0) FROM Task t WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0")
    Integer getMaxOrderByOwnerIdAndDate(Integer ownerId, LocalDateTime date);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0 AND t.dailyViewOrder > :dailyOrder")
    void incrementOrderByOwnerIdAndDateAndOrderGreaterThan(Integer ownerId, LocalDateTime date, Integer dailyOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0 AND t.dailyViewOrder >= :dailyOrder")
    void incrementOrderByOwnerIdAndDateAndOrderGreaterThanEqual(Integer ownerId, LocalDateTime date, Integer dailyOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.dailyViewOrder = t.dailyViewOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND date_part('day', t.due - :date) = 0")
    void incrementOrderByOwnerIdAndDate(Integer ownerId, LocalDateTime date, LocalDateTime modifiedAt);

    @EntityGraph("task-with-labels")
    List<TaskDetails> findByIdIn(List<Integer> ids);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent.id = :parentId")
    void incrementGeneralOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project.id = :projectId")
    void incrementGeneralOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Task t SET t.projectOrder = t.projectOrder + 1, t.modifiedAt = :modifiedAt WHERE t.owner.id = :ownerId AND t.parent IS NULL AND t.project IS NULL")
    void incrementGeneralOrderByOwnerId(Integer ownerId, LocalDateTime modifiedAt);

    @EntityGraph("task-with-labels")
    List<Task> findByProjectIdInAndArchived(List<Integer> ids, boolean archived);

    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerIdAndModifiedAtAfter(Integer ownerId, LocalDateTime modifiedAt, Class<T> type);

    List<Task> findByOwnerIdAndDueBeforeAndCompletedIsFalse(Integer ownerId, LocalDateTime due);

    @Query("SELECT t FROM Task t WHERE t.owner.id = :ownerId AND t.project.id = :projectId AND date_part('year', t.completedAt) = :year")
    List<Task> getByOwnerIdAndProjectIdAndYear(Integer ownerId, Integer projectId, Integer year);

    @Query("SELECT t FROM Task t WHERE t.owner.id = :ownerId AND t.project.id = :projectId AND date_part('month', t.completedAt) = :month")
    List<Task> getByOwnerIdAndProjectIdAndMonth(Integer ownerId, Integer projectId, Integer month);

    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerIdAndProjectId(Integer ownerId, Integer projectId, Class<T> type);
    @EntityGraph("task-with-labels")
    <T> List<T> findByOwnerIdAndProjectIdAndArchived(Integer ownerId, Integer projectId, boolean archived, Class<T> type);

    List<Integer> findIdByOwnerIdAndIdIn(Integer ownerId, List<Integer> ids);
    List<Task> findByOwnerIdAndIdIn(Integer ownerId, List<Integer> ids);

    @Query("SELECT case when count(u)> 0 then true else false end FROM Task t LEFT JOIN t.project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE u.id = :userId AND t.id = :taskId AND c.accepted = true")
    boolean existsCollaboratorById(Integer taskId, Integer userId);

    @Query("SELECT case when count(u)> 0 then true else false end FROM Task t LEFT JOIN t.project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE u.id = :userId AND t.id = :taskId AND c.accepted = true AND c.taskCompletionAllowed = true")
    boolean existsDoerCollaboratorById(Integer taskId, Integer userId);

    @Query("SELECT case when count(u)> 0 then true else false end FROM Task t LEFT JOIN t.project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE u.id = :userId AND t.id = :taskId AND c.accepted = true AND c.editionAllowed = true")
    boolean existsEditorCollaboratorById(Integer taskId, Integer userId);

    <T> List<T> findByProjectIdInAndArchived(List<Integer> projectIds, boolean archived, Class<T> type);
    <T> List<T> findByProjectIdInAndArchivedAndModifiedAtAfter(List<Integer> projectIds, boolean archived, Class<T> type, LocalDateTime modifiedAt);

    @Query("select t from Task t LEFT JOIN t.project.collaborators c LEFT JOIN c.owner u where t.project.id in :projectIds and t.archived = false and u.id = :collaboratorId AND c.accepted = true")
    <T> List<T> getTasksInProjectsForCollaborator(List<Integer> projectIds, Integer collaboratorId, Class<T> type);
    List<Task> findByAssignedIdAndProjectId(Integer assignedId, Integer projectId);
}
