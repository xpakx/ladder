package io.github.xpakx.ladder.repository;

import io.github.xpakx.ladder.entity.TaskComment;
import io.github.xpakx.ladder.entity.dto.TaskCommentDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
    Page<TaskCommentDetails> findByTaskId(Integer taskId, Pageable page);
}
