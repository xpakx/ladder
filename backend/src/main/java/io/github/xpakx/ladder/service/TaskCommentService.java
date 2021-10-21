package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.TaskComment;
import io.github.xpakx.ladder.entity.dto.AddCommentRequest;
import io.github.xpakx.ladder.entity.dto.TaskCommentDetails;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.TaskCommentRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
