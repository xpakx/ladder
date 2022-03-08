package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
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
    private final TaskRepository taskRepository;

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        Integer ownerId = testAccessToProject(projectId, userId, true).orElse(userId);
        return projectService.addTask(request, projectId, ownerId);
    }

    private Optional<Integer> testAccessToProject(Integer projectId, Integer userId, boolean edit) {
        boolean isCollaborator = testProjectCollaboration(projectId, userId, edit);
        Optional<Integer> owner = userRepository.getOwnerIdByProjectId(projectId);
        if(!isCollaborator || owner.isEmpty()) {
            throw new AccessDeniedException("You aren't collaborator in this project!");
        }
        return owner;
    }

    private boolean testProjectCollaboration(Integer projectId, Integer userId, boolean edit) {
        return edit ? projectRepository.existsEditorCollaboratorById(projectId, userId) : projectRepository.existsCollaboratorById(projectId, userId);
    }

    private Optional<Integer> testAccessToTask(Integer taskId, Integer userId, boolean edit, boolean complete) {
        boolean isCollaborator = testTaskCollaboration(taskId, userId, edit, complete);
        Optional<Integer> owner = userRepository.getOwnerIdByTaskId(taskId);
        if(!isCollaborator || owner.isEmpty()) {
            throw new AccessDeniedException("You aren't collaborator in this project!");
        }
        return owner;
    }

    private boolean testTaskCollaboration(Integer taskId, Integer userId, boolean edit, boolean complete) {
        if(complete) {
            return taskRepository.existsDoerCollaboratorById(taskId, userId);
        }
        return edit ? taskRepository.existsEditorCollaboratorById(taskId, userId) : taskRepository.existsCollaboratorById(taskId, userId);
    }

    public Task addTaskAfter(AddTaskRequest request, Integer userId, Integer afterId) {
        Integer ownerId = testAccessToTask(afterId, userId, true, false).orElse(userId);
        return taskService.addTaskAfter(request, ownerId, afterId);
    }

    public Task addTaskBefore(AddTaskRequest request, Integer userId, Integer beforeId) {
        Integer ownerId = testAccessToTask(beforeId, userId, true, false).orElse(userId);
        return taskService.addTaskBefore(request, ownerId, beforeId);
    }

    public Task addTaskAsChild(AddTaskRequest request, Integer userId, Integer parentId) {
        Integer ownerId = testAccessToTask(parentId, userId, true, false).orElse(userId);
        return taskService.addTaskAsChild(request, ownerId, parentId);
    }

    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
        Integer ownerId = testAccessToProject(projectId, userId, false).orElse(userId);
        return projectService.duplicate(projectId, ownerId);
    }

    public void deleteTask(Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, true, false).orElse(userId);
        taskService.deleteTask(taskId, ownerId);
    }

    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, true, false).orElse(userId);
        return taskService.updateTaskWithoutProjectChange(request, taskId, ownerId);
    }

    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, true, false).orElse(userId);
        return taskService.updateTaskDueDate(request, taskId, ownerId);
    }

    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, true, false).orElse(userId);
        return taskService.updateTaskPriority(request, taskId, ownerId);
    }

    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, false, true).orElse(userId);
        return taskService.completeTask(request, taskId, ownerId);
    }

    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        Integer ownerId = testAccessToTask(taskToMoveId, userId, true, false).orElse(userId);
        return taskService.moveTaskAfter(request, ownerId, taskToMoveId);
    }

    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        Integer ownerId = testAccessToTask(taskToMoveId, userId, true, false).orElse(userId);
        return taskService.moveTaskAsFirstChild(request, ownerId, taskToMoveId);
    }

    public Task updateTaskCollapsion(BooleanRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, true, false).orElse(userId);
        return taskService.updateTaskCollapsion(request, taskId, ownerId);
    }

    public Task moveTaskAsFirst(Integer userId, Integer taskToMoveId) {
        Integer ownerId = testAccessToTask(taskToMoveId, userId, true, false).orElse(userId);
        return  taskService.moveTaskAsFirst(ownerId, taskToMoveId);
    }
}
