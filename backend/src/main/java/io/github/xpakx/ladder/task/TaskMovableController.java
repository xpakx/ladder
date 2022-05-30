package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.task.dto.TaskUpdateDto;
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
        return new ResponseEntity<>(taskService.duplicate(taskId, userId), HttpStatus.CREATED);
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/after")
    public ResponseEntity<TaskUpdateDto> moveTaskAfter(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                       @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAfter(request, userId, taskId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asChild")
    public ResponseEntity<TaskUpdateDto> moveTaskAsFirstChild(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                     @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAsFirstChild(request, userId, taskId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/move/asFirst")
    public ResponseEntity<TaskUpdateDto> moveTaskAsFirst(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAsFirst(userId, taskId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/after")
    public ResponseEntity<TaskUpdateDto> addTaskAfter(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                             @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.addTaskAfter(request, userId, taskId)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/children")
    public ResponseEntity<TaskUpdateDto> addTaskAsChild(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                               @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.addTaskAsChild(request, userId, taskId)),
                HttpStatus.CREATED
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PostMapping("/tasks/{taskId}/before")
    public ResponseEntity<TaskUpdateDto> addTaskBefore(@RequestBody AddTaskRequest request, @PathVariable Integer userId,
                                              @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.addTaskBefore(request, userId, taskId)),
                HttpStatus.CREATED
        );
    }
}
