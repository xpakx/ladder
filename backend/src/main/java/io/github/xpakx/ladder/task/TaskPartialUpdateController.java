package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.common.dto.*;
import io.github.xpakx.ladder.task.dto.TaskUpdateDto;
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
    public ResponseEntity<TaskUpdateDto> updateTaskDueDate(@RequestBody DateRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTaskDueDate(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/priority")
    public ResponseEntity<TaskUpdateDto> updateTaskPriority(@RequestBody PriorityRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTaskPriority(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/completed")
    public ResponseEntity<TaskUpdateDto> completeTask(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.completeTask(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/collapse")
    public ResponseEntity<TaskUpdateDto> updateTaskCollapsedState(@RequestBody BooleanRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTaskCollapsedState(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/labels")
    public ResponseEntity<TaskUpdateDto> updateTaskLabels(@RequestBody IdCollectionRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTaskLabels(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/assigned")
    public ResponseEntity<TaskUpdateDto> updateAssigned(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                        @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateAssigned(request, taskId, userId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/project")
    public ResponseEntity<TaskUpdateDto> updateTaskProject(@RequestBody IdRequest request, @PathVariable Integer taskId, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.updateTaskProject(request, taskId, userId)),
                HttpStatus.OK
        );
    }
}
