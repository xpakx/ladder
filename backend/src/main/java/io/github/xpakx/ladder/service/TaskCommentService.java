package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.TaskComment;
import io.github.xpakx.ladder.entity.dto.TaskCommentDetails;
import io.github.xpakx.ladder.repository.TaskCommentRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskCommentService {
    private final TaskCommentRepository commentRepository;

    public Page<TaskCommentDetails> getAllForTask(Integer taskId, Integer page) {
        return commentRepository.findByTaskId(
                taskId,
                PageRequest.of(page, 5, Sort.by("createdAt"))
        );
    }


}
