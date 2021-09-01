package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {
    Optional<Label> findByIdAndOwnerId(Integer id, Integer ownerId);

    void deleteByIdAndOwnerId(Integer labelId, Integer ownerId);

    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
}
