package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.aspect.NotifyOnTasksChange;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.DateRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskDailyService {
    private final TaskRepository taskRepository;
    private final TaskUpdateUtilsService utils;

    @NotifyOnTaskChange
    public Task moveTaskAsFirstInDailyView(Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        taskToMove.setDailyViewOrder(1);
        taskRepository.incrementOrderByOwnerIdAndDate(
                userId,
                taskToMove.getDue(),
                LocalDateTime.now()
        );
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    @NotifyOnTaskChange
    public Task moveTaskAsFirstForDate(Integer userId, Integer taskToMoveId, DateRequest request) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        taskToMove.setDailyViewOrder(1);
        taskToMove.setDue(request.getDate());
        taskRepository.incrementOrderByOwnerIdAndDate(
                userId,
                taskToMove.getDue(),
                LocalDateTime.now()
        );
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    @NotifyOnTaskChange
    public Task moveTaskAfterInDailyView(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task afterTask = findIdFromIdRequest(request)
                .orElseThrow(() -> new NotFoundException("Cannot move anything after non-existent task!"));
        taskToMove.setDue(afterTask.getDue());
        taskToMove.setDailyViewOrder(afterTask.getDailyViewOrder()+1);
        incrementDailyOrderOfTasksAfter(userId, afterTask);
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    private Optional<Task> findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? taskRepository.findById(request.getId()) : Optional.empty();
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private void incrementDailyOrderOfTasksAfter(Integer userId, Task task) {
        if(task.getDue() != null) {
            taskRepository.incrementOrderByOwnerIdAndDateAndOrderGreaterThan(
                    userId,
                    task.getDue(),
                    task.getDailyViewOrder(),
                    LocalDateTime.now()
            );
        }
    }

    @NotifyOnTasksChange
    public List<Task> updateDueDateForOverdue(DateRequest request, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.minusHours(now.getHour()).minusMinutes(now.getMinute()).minusSeconds(now.getSecond());
        List<Task> tasksToUpdate = taskRepository.findByOwnerIdAndDueBeforeAndCompletedIsFalse(userId, today);
        if(request.getDate() != null) {
            int order = utils.getMaxDailyOrder(request, userId) + 1;
            for (Task task : tasksToUpdate) {
                task.setDue(request.getDate());
                task.setModifiedAt(now);
                task.setDailyViewOrder(order++);
            }
        } else {
            for (Task task : tasksToUpdate) {
                task.setDue(request.getDate());
                task.setModifiedAt(now);
            }
        }
        return taskRepository.saveAll(tasksToUpdate);
    }
}
