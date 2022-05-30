package io.github.xpakx.ladder.comment;

import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.comment.dto.AddCommentRequest;
import io.github.xpakx.ladder.comment.dto.TaskCommentDetails;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.common.error.WrongOwnerException;
import io.github.xpakx.ladder.task.TaskRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskCommentService {
    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;

    public Page<TaskCommentDetails> getAllForTask(Integer taskId, Integer page) {
        return commentRepository.findByTaskId(
                taskId,
                PageRequest.of(page, 5, Sort.by("createdAt"))
        );
    }

    public TaskComment addComment(AddCommentRequest request, Integer taskId, Integer userId) {
        Optional<Task> task = taskRepository.findByIdAndOwnerId(taskId, userId);
        if(task.isEmpty() && !taskRepository.existsCollaboratorById(taskId, userId)) {
            throw new WrongOwnerException("You cannot add comments to this task!");
        }
        TaskComment commentToAdd = buildCommentToAddFromRequest(request, taskId, userId);
        return commentRepository.save(commentToAdd);
    }

    public TaskComment buildCommentToAddFromRequest(AddCommentRequest request, Integer taskId, Integer userId) {
        return TaskComment.builder()
                .owner(userRepository.getById(userId))
                .task(taskRepository.getById(taskId))
                .createdAt(LocalDateTime.now())
                .content(request.getContent())
                .build();
    }

    public void deleteComment(Integer commentId, Integer userId) {
        TaskComment comment = commentRepository.getByIdAndOwnerId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("No such comment!"));
        commentRepository.delete(comment);
    }

}
