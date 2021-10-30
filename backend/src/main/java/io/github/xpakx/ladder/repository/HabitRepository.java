package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Integer> {
    @Query("SELECT coalesce(max(h.generalOrder), 0) FROM Habit h WHERE h.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer userId);
}
