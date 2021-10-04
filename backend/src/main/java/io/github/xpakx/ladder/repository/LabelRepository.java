package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabelRepository extends JpaRepository<Label, Integer> {
    Optional<Label> findByIdAndOwnerId(Integer id, Integer ownerId);

    @Transactional
    void deleteByIdAndOwnerId(Integer labelId, Integer ownerId);

    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
    @Modifying
    @Transactional
    @Query("Update Label l SET l.generalOrder = l.generalOrder + 1 WHERE l.owner.id = :ownerId AND l.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder);

    @Modifying
    @Transactional
    @Query("Update Label l SET l.generalOrder = l.generalOrder + 1 WHERE l.owner.id = :ownerId AND l.generalOrder >= :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer generalOrder);

    @Modifying
    @Transactional
    @Query("Update Label l SET l.generalOrder = l.generalOrder + 1 WHERE l.owner.id = :ownerId")
    void incrementGeneralOrderByOwnerId(Integer ownerId);

    @Query("SELECT coalesce(max(l.generalOrder)) FROM Label l WHERE l.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer ownerId);
}
