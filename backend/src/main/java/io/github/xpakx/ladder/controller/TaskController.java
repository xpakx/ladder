package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/{userId}/tasks")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        taskService.deleteTask(taskId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetails> getTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(taskService.getTaskById(taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@RequestBody AddTaskRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/due")
    public ResponseEntity<Task> updateTaskDueDate(@RequestBody DateRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskDueDate(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/priority")
    public ResponseEntity<Task> updateTaskPriority(@RequestBody PriorityRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskPriority(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/project")
    public ResponseEntity<Task> updateTaskProject(@RequestBody IdRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskProject(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/completed")
    public ResponseEntity<Task> completeTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.completeTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{taskId}/duplicate")
    public ResponseEntity<List<TaskDetails>> duplicateTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.duplicate(taskId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/move/after")
    public ResponseEntity<Task> moveTaskAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                    @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAfter(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/move/asChild")
    public ResponseEntity<Task> moveTaskAsFirstChild(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                           @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAsFirstChild(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/collapse")
    public ResponseEntity<Task> updateTaskCollapsion(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskCollapsion(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/move/asFirst")
    public ResponseEntity<Task> moveTaskAsFirst(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.moveTaskAsFirst(userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{taskId}/after")
    public ResponseEntity<Task> addTaskAfter(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                                   @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.addTaskAfter(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/{taskId}/before")
    public ResponseEntity<Task> addTaskBefore(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                                    @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.addTaskBefore(request, userId, taskId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/daily/move/asFirst")
    public ResponseEntity<Task> moveTaskAsFirstDaily(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.moveTaskAsFirstInDailyView(userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/{taskId}/daily/move/after")
    public ResponseEntity<Task> moveTaskAfterDaily(@RequestBody IdRequest request, @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAfterInDailyView(request, userId, taskId), HttpStatus.OK);
    }
}
