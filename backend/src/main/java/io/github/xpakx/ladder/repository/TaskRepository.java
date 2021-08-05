package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskRepository  extends JpaRepository<Task, Integer> {
    <T> Optional<T> findProjectedById(Integer id, Class<T> type);
}
