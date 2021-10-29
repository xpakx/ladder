package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.HabitCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Integer> {

}
