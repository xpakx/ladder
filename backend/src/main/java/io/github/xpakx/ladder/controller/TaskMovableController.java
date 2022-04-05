package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.AddTaskRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.service.TaskMovableService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class TaskMovableController {
    private final TaskMovableService taskService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/duplicate")
    public ResponseEntity<List<TaskDetails>> duplicateTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.duplicate(taskId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/after")
    public ResponseEntity<Task> moveTaskAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAfter(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asChild")
    public ResponseEntity<Task> moveTaskAsFirstChild(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                     @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAsFirstChild(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asFirst")
    public ResponseEntity<Task> moveTaskAsFirst(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.moveTaskAsFirst(userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/after")
    public ResponseEntity<Task> addTaskAfter(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                             @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.addTaskAfter(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/children")
    public ResponseEntity<Task> addTaskAsChild(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.addTaskAsChild(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/before")
    public ResponseEntity<Task> addTaskBefore(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.addTaskBefore(request, userId, taskId), HttpStatus.CREATED);
    }
}
