package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@AllArgsConstructor
public class CollabService {
    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        boolean isCollaborator = projectRepository.existsCollaboratorById(userId);
        Optional<Integer> owner = userRepository.getOwnerIdByProjectId(projectId);
        if(!isCollaborator && owner.isEmpty()) {
            throw new AccessDeniedException("You aren't collaborator in this project!");
        }
        Integer ownerId = owner.orElse(userId);
        return projectService.addTask(request, projectId, ownerId);
    }

    public Task addTaskAfter(AddTaskRequest request, Integer userId, Integer afterId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(afterId).orElse(userId);
        return taskService.addTaskAfter(request, ownerId, afterId);
    }

    public Task addTaskBefore(AddTaskRequest request, Integer userId, Integer beforeId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(beforeId).orElse(userId);
        return taskService.addTaskBefore(request, ownerId, beforeId);
    }

    public Task addTaskAsChild(AddTaskRequest request, Integer userId, Integer parentId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(parentId).orElse(userId);
        return taskService.addTaskAsChild(request, ownerId, parentId);
    }

    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByProjectId(projectId).orElse(userId);
        return projectService.duplicate(projectId, ownerId);
    }

    public void deleteTask(Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        taskService.deleteTask(taskId, ownerId);
    }

    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        return taskService.updateTask(request, taskId, ownerId);
    }

    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        return taskService.updateTaskDueDate(request, taskId, ownerId);
    }

    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        return taskService.updateTaskPriority(request, taskId, ownerId);
    }

    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        return taskService.completeTask(request, taskId, ownerId);
    }

    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskToMoveId).orElse(userId);
        return taskService.moveTaskAfter(request, ownerId, taskToMoveId);
    }

    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskToMoveId).orElse(userId);
        return taskService.moveTaskAsFirstChild(request, ownerId, taskToMoveId);
    }

    public Task updateTaskCollapsion(BooleanRequest request, Integer taskId, Integer userId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskId).orElse(userId);
        return taskService.updateTaskCollapsion(request, taskId, ownerId);
    }

    public Task moveTaskAsFirst(Integer userId, Integer taskToMoveId) {
        Integer ownerId = userRepository.getOwnerIdByTaskId(taskToMoveId).orElse(userId);
        return  taskService.moveTaskAsFirst(ownerId, taskToMoveId);
    }
}
