package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitRepository extends JpaRepository<Habit, Integer> {
}
