package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Integer> {
    <T> List<T> findByOwnerIdAndDateAfter(Integer ownerId, LocalDateTime date, Class<T> type);
}
