package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
    <T> Optional<T> findProjectedByIdAndOwnerId(Integer id, Integer ownerId, Class<T> type);
    Optional<Project> findByIdAndOwnerId(Integer Id, Integer ownerId);

    <T> List<T> findByOwnerIdAndParentIsNull(Integer ownerId, Class<T> type);
    <T> List<T> findByOwnerId(Integer ownerId, Class<T> type);
    <T> List<T> findByOwnerIdAndArchived(Integer ownerId, boolean archived, Class<T> type);
    List<Project> getByOwnerId(Integer ownerId);
    void deleteByIdAndOwnerId(Integer Id, Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt WHERE p.owner.id = :ownerId AND p.parent IS NULL AND p.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt  WHERE p.owner.id = :ownerId AND p.parent IS NULL AND p.generalOrder >= :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt  WHERE p.owner.id = :ownerId AND p.parent.id = :parentId AND p.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThan(Integer ownerId, Integer parentId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt  WHERE p.owner.id = :ownerId AND p.parent.id = :parentId AND p.generalOrder >= :generalOrder")
    void incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer parentId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt  WHERE p.owner.id = :ownerId AND p.parent.id = :parentId")
    void incrementGeneralOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Project p SET p.generalOrder = p.generalOrder + 1, p.modifiedAt = :modifiedAt  WHERE p.owner.id = :ownerId AND p.parent IS NULL")
    void incrementGeneralOrderByOwnerId(Integer ownerId, LocalDateTime modifiedAt);

    List<Project> findByOwnerIdAndParentId(Integer ownerId, Integer parentId);

    @Query("SELECT coalesce(max(p.generalOrder), 0) FROM Project p WHERE p.owner.id = :ownerId AND p.parent IS NULL")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    @Query("SELECT coalesce(max(p.generalOrder), 0) FROM Project p WHERE p.owner.id = :ownerId AND p.parent.id = :parentId")
    Integer getMaxOrderByOwnerIdAndParentId(Integer ownerId, Integer parentId);

    @Query("SELECT p.owner.id FROM Project p WHERE p.id = :id")
    Integer findOwnerIdById(Integer id);

    List<ProjectDetails> findByIdIn(List<Integer> id);

    boolean existsByIdAndOwnerId(Integer id, Integer ownerId);

    <T> List<T> findByOwnerIdAndModifiedAtAfter(Integer ownerId, LocalDateTime modifiedAt, Class<T> type);

    List<Integer> findIdByOwnerIdAndIdIn(Integer userId, List<Integer> ids);
    List<Project> findByOwnerIdAndIdIn(Integer userId, List<Integer> ids);

    @Query("SELECT COUNT(1) FROM Project p LEFT JOIN p.collaborators u WHERE u.id = :id")
    boolean existsCollaboratorById(Integer id);
}
