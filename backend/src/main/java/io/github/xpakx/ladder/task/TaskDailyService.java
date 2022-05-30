package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.notification.NotifyOnTaskChange;
import io.github.xpakx.ladder.notification.NotifyOnTasksChange;
import io.github.xpakx.ladder.common.dto.DateRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.common.error.NotFoundException;
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

    /**
     * Move task at first position in today's list
     * @param userId ID of an owner of task
     * @param taskToMoveId ID of the task to move
     * @return Moved task
     */
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

    /**
     * Move task at first position in list for given day
     * @param userId ID of an owner of task
     * @param taskToMoveId ID of the task to move
     * @param request Request with new date
     * @return Moved task
     */
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

    /**
     * Move task after given task in daily view
     * @param request Request with id of the task which should be before moved task
     * @param userId ID of an owner of tasks
     * @param taskToMoveId ID of the task to move
     * @return Moved task
     */
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

    /**
     * Change due date for all overdue tasks and append them at the end of list for given day.
     * @param request Request with new date
     * @param userId ID of an owner of tasks
     * @return Moved task
     */
    @NotifyOnTasksChange
    public List<Task> updateDueDateForOverdue(DateRequest request, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.minusHours(now.getHour()).minusMinutes(now.getMinute()).minusSeconds(now.getSecond());
        List<Task> tasksToUpdate = taskRepository.findByOwnerIdAndDueBeforeAndCompletedIsFalse(userId, today);
        if(request.getDate() != null) {
            updateOverdueTasksWithNewDueDate(request, now, tasksToUpdate, utils.getMaxDailyOrder(request, userId) + 1);
        } else {
            updateOverdueTasksWithoutNewDueDate(now, tasksToUpdate);
        }
        return taskRepository.saveAll(tasksToUpdate);
    }

    private void updateOverdueTasksWithoutNewDueDate(LocalDateTime now, List<Task> tasksToUpdate) {
        for (Task task : tasksToUpdate) {
            task.setDue(null);
            task.setTimeboxed(false);
            task.setModifiedAt(now);
        }
    }

    private void updateOverdueTasksWithNewDueDate(DateRequest request, LocalDateTime now, List<Task> tasksToUpdate, int order) {
        for (Task task : tasksToUpdate) {
            task.setDue(request.getDate());
            task.setTimeboxed(false);
            task.setModifiedAt(now);
            task.setDailyViewOrder(order++);
        }
    }
}
