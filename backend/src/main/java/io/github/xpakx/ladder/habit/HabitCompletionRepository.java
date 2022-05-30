package io.github.xpakx.ladder.habit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Integer> {
    <T> List<T> findByOwnerIdAndDateAfter(Integer ownerId, LocalDateTime date, Class<T> type);

    @Query("SELECT t FROM HabitCompletion t LEFT JOIN t.habit h WHERE h.owner.id = :ownerId AND h.project.id = :projectId AND date_part('year', t.date) = :year")
    List<HabitCompletion> getByOwnerIdAndProjectIdAndYear(Integer ownerId, Integer projectId, Integer year);

    @Query("SELECT t FROM HabitCompletion t LEFT JOIN t.habit h WHERE h.owner.id = :ownerId AND h.project.id = :projectId AND date_part('month', t.date) = :month")
    List<HabitCompletion> getByOwnerIdAndProjectIdAndMonth(Integer ownerId, Integer projectId, Integer month);

    @Query("SELECT t FROM HabitCompletion t LEFT JOIN t.habit h WHERE h.owner.id = :ownerId AND h.id = :habitId AND date_part('year', t.date) = :year")
    List<HabitCompletion> getByOwnerIdAndHabitIdAndYear(Integer ownerId, Integer habitId, Integer year);
}
