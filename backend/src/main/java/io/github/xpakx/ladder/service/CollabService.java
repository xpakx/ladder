package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnCollaborationAcceptation;
import io.github.xpakx.ladder.aspect.NotifyOnCollaborationUnsubscription;
import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongOwnerException;
import io.github.xpakx.ladder.repository.CollaborationRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class CollabService {
    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final CollaborationRepository collabRepository;

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        Integer ownerId = testAccessToProject(projectId, userId, true).orElse(userId);
        return taskService.addTask(request, projectId, ownerId);
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

    @NotifyOnTaskChange
    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Integer ownerId = testAccessToTask(taskId, userId, false, true).orElse(userId);
        Task taskToUpdate = taskRepository.getByIdAndOwnerId(taskId, ownerId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        if(request.isFlag()) {
            taskToUpdate.setAssigned(userRepository.getById(userId));
            LocalDateTime now = LocalDateTime.now();
            taskToUpdate.setCompleted(true);
            taskToUpdate.setCompletedAt(now);
            taskToUpdate.setModifiedAt(now);
            return taskRepository.save(taskToUpdate);
        } else {
            if((taskToUpdate.getAssigned()==null || !taskToUpdate.getAssigned().getId().equals(userId)) && !testTaskCollaboration(taskId, userId, true, false)) {
                throw new WrongOwnerException("You cannot uncomplete tasks completed by someone else!");
            }
            taskToUpdate.setCompleted(false);
            taskToUpdate.setCompletedAt(null);
            taskToUpdate.setModifiedAt(LocalDateTime.now());
        }
        return taskRepository.save(taskToUpdate);
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

    public List<CollaborationDetails> getNotAcceptedCollaborations(Integer userId) {
        return collabRepository.findByOwnerIdAndAccepted(userId, false);
    }

    @NotifyOnCollaborationAcceptation
    public Collaboration updateAcceptation(BooleanRequest request, Integer userId, Integer collabId) {
        Collaboration collab = collabRepository.findByOwnerIdAndId(userId, collabId)
                .orElseThrow(() -> new NotFoundException("Not such collaboration!"));
        collab.setAccepted(request.isFlag());
        collab.setModifiedAt(LocalDateTime.now());
        return collabRepository.save(collab);
    }

    @NotifyOnCollaborationUnsubscription
    public List<Collaboration> unsubscribe(BooleanRequest request, Integer userId, Integer projectId) {
        List<Collaboration> collabs = collabRepository.findByOwnerIdAndProjectId(userId, projectId);
        collabs.forEach((a) -> a.setAccepted(request.isFlag()));
        return collabRepository.saveAll(collabs);
    }
}
