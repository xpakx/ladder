package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Integer> {
    <T> List<T> findByOwnerIdAndModifiedAtAfter(Integer ownerId, LocalDateTime modifiedAt, Class<T> type);
    <T> List<T> findByOwnerId(Integer userId, Class<T> type);

    @Transactional
    void deleteByIdAndOwnerId(Integer labelId, Integer ownerId);

    @Query("SELECT coalesce(max(f.generalOrder), 0) FROM Filter f WHERE f.owner.id = :ownerId")
    Integer getMaxOrderByOwnerId(Integer ownerId);
}
