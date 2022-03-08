package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.dto.CollaborationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Integer> {
    List<CollaborationDetails> findByOwnerIdAndAccepted(Integer ownerId, boolean accepted);
}