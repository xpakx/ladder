package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.FullProjectTree;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    <T> List<T> findByOwnerIdAndParentIsNull(Integer ownerId, Class<T> type);

    Optional<Project> findByIdAndOwnerId(Integer Id, Integer ownerId);
    void deleteByIdAndOwnerId(Integer Id, Integer ownerId);
}
