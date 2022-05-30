package io.github.xpakx.ladder.comment;

import io.github.xpakx.ladder.comment.TaskComment;
import io.github.xpakx.ladder.comment.dto.TaskCommentDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Integer> {
    Page<TaskCommentDetails> findByTaskId(Integer taskId, Pageable page);

    Optional<TaskComment> getByIdAndOwnerId(Integer id, Integer ownerId);
}
