package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.aspect.NotifyOnTaskDeletion;
import io.github.xpakx.ladder.aspect.NotifyOnTasksChange;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserAccountRepository userRepository;
    private final TaskUpdateUtilsService utils;

    /**
     * Delete task from repository.
     * @param taskId ID of the task to delete
     * @param userId ID of an owner of the task
     */
    @Transactional
    @NotifyOnTaskDeletion
    public void deleteTask(Integer taskId, Integer userId) {
        this.taskRepository.deleteByIdAndOwnerId(taskId, userId);
    }


    /**
     * Get task details by ID.
     * @param taskId ID of the task
     * @param userId ID of an owner of the task
     * @return Task details
     */
    public TaskDetails getTaskById(Integer taskId, Integer userId) {
        return taskRepository.findProjectedByIdAndOwnerId(taskId, userId, TaskDetails.class)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    /**
     * Updating task in repository.
     * @param request Data to update the task
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Task with updated data
     */
    @NotifyOnTaskChange
    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        Project project = getProjectFromRequest(request, userId);
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        updateFields(request, userId, project, taskToUpdate);
        return taskRepository.save(taskToUpdate);
    }

    private Task getTaskFromDb(Integer taskId, Integer userId) {
        return taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    private Project getProjectFromRequest(AddTaskRequest request, Integer userId) {
        return request.getProjectId() != null ? projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
    }

    private void updateFields(AddTaskRequest request, Integer userId, Project project, Task taskToUpdate) {
        changeProject(request, userId, project, taskToUpdate);
        updateFieldsWithoutProjectChange(request, userId, taskToUpdate);
    }

    private void changeProject(AddTaskRequest request, Integer userId, Project project, Task taskToUpdate) {
        taskToUpdate.setProjectOrder(request.getProjectOrder());
        if(!utils.haveSameProject(taskToUpdate, project)) {
            List<Task> childrenWithUpdatedProject = utils.updateChildrenProject(project, taskToUpdate, userId);
            taskRepository.saveAll(childrenWithUpdatedProject);
            taskToUpdate.setParent(null);
        }
        taskToUpdate.setProject(project);
    }

    private void changeDate(AddTaskRequest request, Integer userId, Task taskToUpdate) {
        if(utils.haveDifferentDueDate(request.getDue(), taskToUpdate.getDue())) {
            taskToUpdate.setDailyViewOrder(utils.getMaxDailyOrder(request, userId)+1);
        }
        taskToUpdate.setDue(request.getDue());
    }

    /**
     * Updating task in repository, but ignore project changes.
     * @param request Data to update the task
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Task with updated data
     */
    @NotifyOnTaskChange
    public Task updateTaskWithoutProjectChange(AddTaskRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        updateFieldsWithoutProjectChange(request, userId, taskToUpdate);
        return taskRepository.save(taskToUpdate);
    }

    private void updateFieldsWithoutProjectChange(AddTaskRequest request, Integer userId, Task taskToUpdate) {
        taskToUpdate.setProjectOrder(request.getProjectOrder());
        changeDate(request, userId, taskToUpdate);
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setCompletedAt(request.getCompletedAt());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setOwner(userRepository.getById(userId));
        taskToUpdate.setLabels(utils.transformLabelIdsToLabelReferences(request.getLabelIds(), userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
    }

    /**
     * Add new task to given project
     * @param request Request with data to build new task
     * @param projectId ID of the project for task
     * @param userId If of an owner of the project and newly created task
     * @return Newly created task
     */
    @NotifyOnTaskChange
    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        Project project = projectId != null ? checkProjectOwnerAndGetReference(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Task taskToAdd = buildTaskToAddFromRequest(request, userId, project);
        taskToAdd.setProjectOrder(getMaxProjectOrder(request, userId)+1);
        return taskRepository.save(taskToAdd);
    }

    private Optional<Project> checkProjectOwnerAndGetReference(Integer projectId, Integer userId) {
        if(!userId.equals(projectRepository.findOwnerIdById(projectId))) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.getById(projectId));
    }

    private Task buildTaskToAddFromRequest(AddTaskRequest request, Integer userId, Project project) {
        LocalDateTime now = LocalDateTime.now();
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectOrder(request.getProjectOrder())
                .project(project)
                .createdAt(now)
                .modifiedAt(now)
                .due(request.getDue())
                .dailyViewOrder(utils.getMaxDailyOrder(request, userId)+1)
                .parent(getParentFromAddTaskRequest(request))
                .priority(request.getPriority())
                .completed(false)
                .collapsed(false)
                .archived(false)
                .owner(userRepository.getById(userId))
                .labels(utils.transformLabelIdsToLabelReferences(request.getLabelIds(), userId))
                .build();
    }

    private Task getParentFromAddTaskRequest(AddTaskRequest request) {
        return hasParent(request) ? taskRepository.getById(request.getParentId()) : null;
    }

    private boolean hasParent(AddTaskRequest request) {
        return nonNull(request.getParentId());
    }

    private Integer getMaxProjectOrder(AddTaskRequest request, Integer userId) {
        if(hasParent(request)) {
            if (request.getProjectId() != null) {
                return taskRepository.getMaxOrderByOwnerIdAndProjectIdAndParentId(userId, request.getProjectId(), request.getParentId());
            } else {
                return taskRepository.getMaxOrderByOwnerIdAndParentId(userId, request.getParentId());
            }
        } else {
            if (request.getProjectId() != null) {
                return taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getProjectId());
            } else {
                return taskRepository.getMaxOrderByOwnerId(userId);
            }
        }
    }

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
