package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {
    Optional<Label> findByIdAndOwnerId(Integer labelId, Integer ownerId);
}
