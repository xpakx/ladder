package io.github.xpakx.ladder.user;

import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.user.dto.UserWithNameAndId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u.id FROM Project p LEFT JOIN p.owner u WHERE p.id = :projectId")
    Optional<Integer> getOwnerIdByProjectId(Integer projectId);

    @Query("SELECT u.id FROM Task t LEFT JOIN t.owner u WHERE t.id = :taskId")
    Optional<Integer> getOwnerIdByTaskId(Integer taskId);

    @Query("SELECT u.id AS id, u.username AS username FROM Project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE p.id = :projectId AND p.owner.id = :ownerId")
    List<UserWithNameAndId> getCollaboratorsByProjectIdAndOwnerId(Integer projectId, Integer ownerId);

    @Query("SELECT u.id FROM Project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE p.id = :projectId")
    List<Integer> getCollaboratorsIdByProjectId(Integer projectId);

    @Query("SELECT u.id FROM Task t LEFT JOIN t.project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE t.id = :taskId AND p.collaborative = true")
    List<Integer> getCollaboratorsIdByTaskId(Integer taskId);

    @Query("SELECT u FROM Task t LEFT JOIN t.project p LEFT JOIN p.collaborators c LEFT JOIN c.owner u WHERE t.id = :taskId AND p.collaborative = true AND u.id = :userId")
    Optional<UserAccount> getCollaboratorByTaskIdAndId(Integer taskId, Integer userId);

    Optional<UserAccount> findByCollaborationToken(String collaborationToken);
}
