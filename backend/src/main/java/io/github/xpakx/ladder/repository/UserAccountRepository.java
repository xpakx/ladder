package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Integer> {
    Optional<UserAccount> findByUsername(String username);

    @Query("SELECT u.id FROM Project p LEFT JOIN p.owner u WHERE p.id = :projectId")
    Optional<Integer> getOwnerIdByProjectId(Integer projectId);

    @Query("SELECT u.id FROM Task t LEFT JOIN t.owner u WHERE t.id = :taskId")
    Optional<Integer> getOwnerIdByTaskId(Integer taskId);
}
