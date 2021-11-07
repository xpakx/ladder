package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Habit;
import io.github.xpakx.ladder.entity.dto.HabitDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Integer> {
    @Query("SELECT coalesce(max(h.generalOrder), 0) FROM Habit h WHERE h.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    void deleteByIdAndOwnerId(Integer id, Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId")
    void incrementGeneralOrderByOwnerId(Integer ownerId, LocalDateTime modifiedAt);

    Optional<Habit> findByIdAndOwnerId(Integer id, Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Habit h SET h.generalOrder = h.generalOrder + 1, h.modifiedAt = :modifiedAt WHERE h.owner.id = :ownerId AND h.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    <T> List<T> findByOwnerId(Integer ownerId, Class<T> type);
}
