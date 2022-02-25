package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CollabService {
    private final TaskService taskService;
    private final ProjectService projectService;

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        return projectService.addTask(request, projectId, userId);
    }

    public Task addTaskAfter(AddTaskRequest request, Integer userId, Integer afterId) {
        return taskService.addTaskAfter(request, userId, afterId);
    }

    public Task addTaskBefore(AddTaskRequest request, Integer userId, Integer beforeId) {
        return taskService.addTaskBefore(request, userId, beforeId);
    }

    public Task addTaskAsChild(AddTaskRequest request, Integer userId, Integer parentId) {
        return taskService.addTaskAsChild(request, userId, parentId);
    }

    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
        return projectService.duplicate(projectId, userId);
    }

    public void deleteTask(Integer taskId, Integer userId) {
        taskService.deleteTask(taskId, userId);
    }

    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        return taskService.updateTask(request, taskId, userId);
    }

    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        return taskService.updateTaskDueDate(request, taskId, userId);
    }

    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        return taskService.updateTaskPriority(request, taskId, userId);
    }

    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        return taskService.completeTask(request, taskId, userId);
    }

    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        return taskService.moveTaskAfter(request, userId, taskToMoveId);
    }

    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        return taskService.moveTaskAsFirstChild(request, userId, taskToMoveId);
    }

    public Task updateTaskCollapsion(BooleanRequest request, Integer taskId, Integer userId) {
        return taskService.updateTaskCollapsion(request, taskId, userId);
    }

    public Task moveTaskAsFirst(Integer userId, Integer taskToMoveId) {
        return  taskService.moveTaskAsFirst(userId, taskToMoveId);
    }
}
