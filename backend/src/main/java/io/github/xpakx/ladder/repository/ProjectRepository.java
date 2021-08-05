package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    <T> Optional<T> findProjectedById(Integer id, Class<T> type);
}
