package io.github.xpakx.ladder.controller;


import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.TaskPartialUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class TaskPartialUpdateController {
    private final TaskPartialUpdateService taskService;
    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/due")
    public ResponseEntity<Task> updateTaskDueDate(@RequestBody DateRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskDueDate(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/priority")
    public ResponseEntity<Task> updateTaskPriority(@RequestBody PriorityRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskPriority(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/completed")
    public ResponseEntity<Task> completeTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.completeTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/collapse")
    public ResponseEntity<Task> updateTaskCollapsion(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskCollapsedState(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/labels")
    public ResponseEntity<Task> updateTaskLabels(@RequestBody IdCollectionRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskLabels(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/assigned")
    public ResponseEntity<Task> updateAssigned(@RequestBody IdRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.updateAssigned(request, taskId, userId), HttpStatus.OK);
    }
}
