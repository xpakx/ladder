package io.github.xpakx.ladder.controller;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId) {
        taskService.deleteTask(taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDetails> getTask(@PathVariable Integer taskId) {
        return new ResponseEntity<>(taskService.getTaskById(taskId), HttpStatus.OK);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@RequestBody UpdateTaskRequest request, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.updateTask(request, taskId), HttpStatus.OK);
    }

    @PutMapping("/{taskId}/due")
    public ResponseEntity<Task> updateTaskDueDate(@RequestBody DateRequest request, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.updateTaskDueDate(request, taskId), HttpStatus.OK);
    }

    @PutMapping("/{taskId}/priority")
    public ResponseEntity<Task> updateTaskPriority(@RequestBody PriorityRequest request, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.updateTaskPriority(request, taskId), HttpStatus.OK);
    }

    @PutMapping("/{taskId}/project")
    public ResponseEntity<Task> updateTaskProject(@RequestBody IdRequest request, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.updateTaskProject(request, taskId), HttpStatus.OK);
    }

    @PutMapping("/{taskId}/completed")
    public ResponseEntity<Task> completeTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId) {
        return  new ResponseEntity<>(taskService.completeTask(request, taskId), HttpStatus.OK);

    }
}
