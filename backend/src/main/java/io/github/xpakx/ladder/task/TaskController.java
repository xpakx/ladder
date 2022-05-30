package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.task.TaskService;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.task.dto.TaskUpdateDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<TaskUpdateDto> updateTask(@RequestBody AddTaskRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTask(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskUpdateDto> addTaskToProject(@RequestBody AddTaskRequest request, @PathVariable Integer projectId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.addTask(request, projectId, userId)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/projects/inbox/tasks")
    public ResponseEntity<TaskUpdateDto> addTaskToInbox(@RequestBody AddTaskRequest request, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.addTask(request, null, userId)),
                HttpStatus.CREATED
        );
    }
}
