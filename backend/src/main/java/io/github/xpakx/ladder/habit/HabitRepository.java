package io.github.xpakx.ladder.habit;

import io.github.xpakx.ladder.habit.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Integer> {
    @Query("SELECT coalesce(max(h.generalOrder), 0) FROM Habit h WHERE h.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    @Query("SELECT coalesce(max(h.generalOrder), 0) FROM Habit h WHERE h.owner.id = :ownerId AND h.project.id = :projectId" )
    Integer getMaxOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId);

    void deleteByIdAndOwnerId(Integer id, Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId")
    void incrementGeneralOrderByOwnerId(Integer ownerId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.project.id = :projectId")
    void incrementGeneralOrderByOwnerIdAndProjectId(Integer ownerId, Integer projectId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.project IS NULL")
    void incrementGeneralOrderByOwnerIdAndProjectIsNull(Integer ownerId, LocalDateTime modifiedAt);

    Optional<Habit> findByIdAndOwnerId(Integer id, Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder > :generalOrder AND h.project.id = :projectId")
    void incrementGeneralOrderByOwnerIdAndProjectIdAndGeneralOrderGreaterThan(Integer ownerId, Integer projectId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder > :generalOrder AND h.project IS NULL")
    void incrementGeneralOrderByOwnerIdAndProjectIsNullAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder >= :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder >= :generalOrder AND h.project.id = :projectId")
    void incrementGeneralOrderByOwnerIdAndProjectIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer projectId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder >= :generalOrder AND h.project IS NULL")
    void incrementGeneralOrderByOwnerIdAndProjectIsNullAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    
    @EntityGraph("habit-with-labels")
    <T> List<T> findByOwnerId(Integer ownerId, Class<T> type);
    @EntityGraph("habit-with-labels")
    <T> List<T> findByOwnerIdAndArchived(Integer ownerId, boolean archived, Class<T> type);
    @EntityGraph("habit-with-labels")
    <T> List<T> findByOwnerIdAndModifiedAtAfter(Integer ownerId, LocalDateTime modifiedAt, Class<T> type);
    @EntityGraph("habit-with-labels")
    <T> List<T> findByOwnerIdAndProjectId(Integer ownerId, Integer projectId, Class<T> type);
    @EntityGraph("habit-with-labels")
    <T> List<T> findByOwnerIdAndProjectIdAndArchived(Integer ownerId, Integer projectId, boolean archived, Class<T> type);
}
