package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.common.dto.DateRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.task.dto.TaskUpdateDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/{userId}")
@AllArgsConstructor
public class TaskDailyController {
    private final TaskDailyService taskService;

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/asFirst")
    public ResponseEntity<TaskUpdateDto> moveTaskAsFirstDaily(@PathVariable Integer userId, @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAsFirstInDailyView(userId, taskId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/asFirstWithDate")
    public ResponseEntity<TaskUpdateDto> moveTaskAsFirstForDate(@RequestBody DateRequest request, @PathVariable Integer userId, @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAsFirstForDate(userId, taskId, request)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/{taskId}/daily/move/after")
    public ResponseEntity<TaskUpdateDto> moveTaskAfterDaily(@RequestBody IdRequest request, @PathVariable Integer userId,
                                                            @PathVariable Integer taskId) {
        return new ResponseEntity<>(
                TaskUpdateDto.from(taskService.moveTaskAfterInDailyView(request, userId, taskId)),
                HttpStatus.OK
        );
    }

    @PreAuthorize("#userId.toString() == authentication.principal.username")
    @PutMapping("/tasks/overdue/due")
    public ResponseEntity<List<TaskUpdateDto>> updateOverdueTasksDueDate(@RequestBody DateRequest request, @PathVariable Integer userId) {
        return new ResponseEntity<>(
                taskService.updateDueDateForOverdue(request, userId).stream().map(TaskUpdateDto::from).collect(Collectors.toList()),
                HttpStatus.OK
        );
    }
}
