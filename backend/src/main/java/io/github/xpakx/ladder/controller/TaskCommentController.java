package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.TaskComment;
import io.github.xpakx.ladder.entity.dto.AddCommentRequest;
import io.github.xpakx.ladder.entity.dto.TaskCommentDetails;
import io.github.xpakx.ladder.service.TaskCommentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class TaskCommentController {
    private final TaskCommentService commentService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/tasks/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId, @PathVariable Integer userId) {
        commentService.deleteComment(commentId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<TaskComment> addComment(@RequestBody AddCommentRequest request, @PathVariable Integer userId,
                                                  @PathVariable Integer taskId) {
        return  new ResponseEntity<>(commentService.addComment(request, taskId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<Page<TaskCommentDetails>> getAllCommentsForTask(@PathVariable Integer taskId,
                                                                          @PathVariable Integer userId,
                                                                          @RequestParam Optional<Integer> page) {
        return new ResponseEntity<>(
                commentService.getAllForTask(taskId, page.orElse(0)),
                HttpStatus.OK
        );
    }
}
