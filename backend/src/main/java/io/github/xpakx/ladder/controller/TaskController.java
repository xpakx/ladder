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
@RequestMapping("/{userId}")
@AllArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        taskService.deleteTask(taskId, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDetails> getTask(@PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(taskService.getTaskById(taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<Task> updateTask(@RequestBody AddTaskRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTask(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/asFirst")
    public ResponseEntity<Task> moveTaskAsFirstDaily(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.moveTaskAsFirstInDailyView(userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/asFirstWithDate")
    public ResponseEntity<Task> moveTaskAsFirstForDate(@RequestBody DateRequest request, @PathVariable Integer userId, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.moveTaskAsFirstForDate(userId, taskId, request), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/after")
    public ResponseEntity<Task> moveTaskAfterDaily(@RequestBody IdRequest request, @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.moveTaskAfterInDailyView(request, userId, taskId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/overdue/due")
    public ResponseEntity<List<Task>> updateOverdueTasksDueDate(@RequestBody DateRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateDueDateForOverdue(request, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<Task> addTaskToProject(@RequestBody AddTaskRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.addTask(request, projectId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/projects/inbox/tasks")
    public ResponseEntity<Task> addTaskToInbox(@RequestBody AddTaskRequest request, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.addTask(request, null, userId), HttpStatus.CREATED);
    }
}
