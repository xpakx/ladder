package io.github.xpakx.ladder.collaboration;

import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.collaboration.dto.CollaborationDetails;
import io.github.xpakx.ladder.collaboration.dto.CollaborationWithOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {
    List<CollaborationDetails> findByOwnerIdAndAccepted(Integer ownerId, boolean accepted);
    Optional<Collaboration> findByOwnerIdAndId(Integer ownerId, Integer id);
    Optional<Collaboration> findByIdAndProjectOwnerId(Integer id, Integer ownerId);

    @Query("SELECT c FROM Project p LEFT JOIN p.collaborators c WHERE c.owner.id = :ownerId AND p.id = :projectId")
    List<Collaboration> findByOwnerIdAndProjectId(Integer ownerId, Integer projectId);
    Optional<CollaborationWithOwner> findProjectedByOwnerIdAndProjectId(Integer ownerId, Integer projectId);

    List<CollaborationWithOwner> findByProjectIdAndProjectOwnerId(Integer projectId, Integer ownerId);

    @Query("SELECT c FROM Collaboration c LEFT JOIN c.project p LEFT JOIN c.owner u WHERE u.id = :id AND p.archived = false AND c.accepted = true")
    <T> List<T> findCollabsByUserIdAndNotArchived(Integer id, Class<T> type);

    @Query("SELECT c FROM Collaboration c LEFT JOIN c.project p LEFT JOIN c.owner u WHERE u.id = :id AND p.archived = false AND c.accepted = true AND (p.modifiedAt > :modifiedAt OR  c.modifiedAt > :modifiedAt )")
    <T> List<T> findCollabsByUserIdAndNotArchivedAndModifiedAtAfter(Integer id, Class<T> type, LocalDateTime modifiedAt);

}