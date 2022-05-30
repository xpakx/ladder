package io.github.xpakx.ladder.filter;

import io.github.xpakx.ladder.filter.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Integer> {
    <T> List<T> findByOwnerIdAndModifiedAtAfter(Integer ownerId, LocalDateTime modifiedAt, Class<T> type);
    <T> List<T> findByOwnerId(Integer userId, Class<T> type);
    Optional<Filter> findByIdAndOwnerId(Integer id, Integer ownerId);

    @Transactional
    void deleteByIdAndOwnerId(Integer labelId, Integer ownerId);

    @Query("SELECT coalesce(max(f.generalOrder), 0) FROM Filter f WHERE f.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer ownerId);

    @Modifying
    @Transactional
    @Query("Update Filter f SET f.generalOrder = f.generalOrder + 1, f.modifiedAt = :modifiedAt WHERE f.owner.id = :ownerId")
    void incrementGeneralOrderByOwnerId(Integer ownerId, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Filter f SET f.generalOrder = f.generalOrder + 1, f.modifiedAt = :modifiedAt WHERE f.owner.id = :ownerId AND f.generalOrder > :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

    @Modifying
    @Transactional
    @Query("Update Filter f SET f.generalOrder = f.generalOrder + 1, f.modifiedAt = :modifiedAt WHERE f.owner.id = :ownerId AND f.generalOrder >= :generalOrder")
    void incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(Integer ownerId, Integer generalOrder, LocalDateTime modifiedAt);

}
