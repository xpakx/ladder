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
    @PutMapping("/tasks/{taskId}/project")
    public ResponseEntity<Task> updateTaskProject(@RequestBody IdRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskProject(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/completed")
    public ResponseEntity<Task> completeTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.completeTask(request, taskId, userId), HttpStatus.OK);
    }

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
    @PutMapping("/tasks/{taskId}/collapse")
    public ResponseEntity<Task> updateTaskCollapsion(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskCollapsion(request, taskId, userId), HttpStatus.OK);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/labels")
    public ResponseEntity<Task> updateTaskLabels(@RequestBody IdCollectionRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return  new ResponseEntity<>(taskService.updateTaskLabels(request, taskId, userId), HttpStatus.OK);
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
    @PutMapping("/tasks/{taskId}/assigned")
    public ResponseEntity<Task> updateAssigned(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                   @PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.updateAssigned(request, taskId, userId), HttpStatus.OK);
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
