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
import java.util.stream.Collectors;

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

    /**
     * Duplicate given task, and its subtasks
     * @param taskId ID of the task to duplicate
     * @param userId ID of an owner of the task
     * @return All created tasks
     */
    public List<TaskDetails> duplicate(Integer taskId, Integer userId) {
        Task taskToDuplicate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));

        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                taskToDuplicate.getProject() != null ? taskToDuplicate.getProject().getId() : null);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        Task duplicatedTask = duplicate(taskToDuplicate, taskToDuplicate.getParent());

        List<Task> toDuplicate = List.of(duplicatedTask);
        List<Task> allDuplicated = new ArrayList<>();
        allDuplicated.add(duplicatedTask);
        while(toDuplicate.size() > 0) {
            List<Task> newToDuplicate = new ArrayList<>();
            for (Task parent : toDuplicate) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setId(null);

                children = children.stream()
                        .map((a) -> duplicate(a, parent))
                        .collect(Collectors.toList());
                parent.setChildren(children);
                newToDuplicate.addAll(children);
            }
            toDuplicate = newToDuplicate;
            allDuplicated.addAll(newToDuplicate);
        }

        incrementOrderOfTasksAfter(userId, duplicatedTask);
        duplicatedTask.setProjectOrder(duplicatedTask.getProjectOrder()+1);
        incrementDailyOrderOfTasksAfter(userId, duplicatedTask);
        duplicatedTask.setDailyViewOrder(duplicatedTask.getDailyViewOrder()+1);

        List<Integer> ids = taskRepository.saveAll(allDuplicated).stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        return taskRepository.findByIdIn(ids);
    }

    private Task duplicate(Task originalTask, Task parent) {
        return Task.builder()
                .id(originalTask.getId())
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .projectOrder(originalTask.getProjectOrder())
                .dailyViewOrder(originalTask.getDailyViewOrder())
                .project(originalTask.getProject())
                .createdAt(LocalDateTime.now())
                .due(originalTask.getDue())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .archived(false)
                .parent(parent)
                .build();
    }

    /**
     * Move task after given task
     * @param request Request with id of the task which should be before moved task
     * @param userId ID of an owner of tasks
     * @param taskToMoveId ID of the task to move
     * @return Moved task
     */
    @NotifyOnTaskChange
    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task afterTask = findIdFromIdRequest(request)
                .orElseThrow(() -> new NotFoundException("Cannot move anything after non-existent task!"));
        taskToMove.setParent(afterTask.getParent());
        taskToMove.setProjectOrder(afterTask.getProjectOrder()+1);
        incrementOrderOfTasksAfter(userId, afterTask);
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    private Optional<Task> findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? taskRepository.findById(request.getId()) : Optional.empty();
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private boolean hasParent(Task task) {
        return task.getParent() != null;
    }

    /**
     * Move task as first child of given task.
     * @param request Request with id of the new parent for moved task. If ID is null task is moved at first position.
     * @param userId ID of an owner of tasks
     * @param taskToMoveId ID of the task to move
     * @return Moved task
     */
    @NotifyOnTaskChange
    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task parentTask = findIdFromIdRequest(request)
                .orElse(null);
        taskToMove.setParent(parentTask);
        taskToMove.setProjectOrder(1);
        incrementTasksOrder(request, userId, taskToMove.getProject());
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    private void incrementTasksOrder(IdRequest request, Integer userId, Project project) {
        if(request.getId() != null) {
            taskRepository.incrementGeneralOrderByOwnerIdAndParentId(
                    userId,
                    request.getId(),
                    LocalDateTime.now()
            );
        } else if(project != null) {
            taskRepository.incrementGeneralOrderByOwnerIdAndProjectId(
                    userId,
                    project.getId(),
                    LocalDateTime.now()
            );
        } else {
            taskRepository.incrementGeneralOrderByOwnerId(
                    userId,
                    LocalDateTime.now()
            );
        }
    }

    /**
     * Move task at first position
     * @param userId ID of an owner of task
     * @param taskToMoveId ID of the task to move
     * @return Moved task
     */
    @NotifyOnTaskChange
    public Task moveTaskAsFirst(Integer userId, Integer taskToMoveId) {
        IdRequest request = new IdRequest();
        request.setId(null);
        return moveTaskAsFirstChild(request, userId, taskToMoveId);
    }

    private Task buildTaskToAddFromRequest(AddTaskRequest request, Integer userId) {
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .due(request.getDue())
                .priority(request.getPriority())
                .dailyViewOrder(utils.getMaxDailyOrder(request, userId)+1)
                .completed(false)
                .collapsed(false)
                .archived(false)
                .owner(userRepository.getById(userId))
                .labels(utils.transformLabelIdsToLabelReferences(request.getLabelIds(), userId))
                .build();
    }

    /**
     * Add new task with order after given task
     * @param request Request with data to build new task
     * @param userId ID of an owner of tasks
     * @param afterId ID of the task which should be before newly created task
     * @return Newly created task
     */
    @NotifyOnTaskChange
    public Task addTaskAfter(AddTaskRequest request, Integer userId, Integer afterId) {
        Task afterTask = taskRepository.findByIdAndOwnerId(afterId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent task!"));
        Task taskToAdd = buildTaskFromRequestWithSiblingAndOrder(request, afterTask.getProjectOrder()+1,
                afterTask, userId);
        incrementOrderOfTasksAfter(userId, afterTask);
        taskToAdd.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToAdd);
    }

    /**
     * Add new task with order before given task
     * @param request Request with data to build new task
     * @param userId ID of an owner of tasks
     * @param beforeId ID of the task which should be after newly created task
     * @return Newly created task
     */
    @NotifyOnTaskChange
    public Task addTaskBefore(AddTaskRequest request, Integer userId, Integer beforeId) {
        Task beforeTask = taskRepository.findByIdAndOwnerId(beforeId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent task!"));
        Task taskToAdd = buildTaskFromRequestWithSiblingAndOrder(request, beforeTask.getProjectOrder(),
                beforeTask, userId);
        incrementOrderOfTasksBefore(userId, beforeTask);
        taskToAdd.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToAdd);
    }

    @NotifyOnTaskChange
    public Task addTaskAsChild(AddTaskRequest request, Integer userId, Integer parentId) {
        Task parentTask = taskRepository.findByIdAndOwnerId(parentId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent task!"));
        Task taskToAdd = buildTaskFromRequestWithParentAndOrder(request, parentTask, userId);
        taskToAdd.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToAdd);
    }

    private Task buildTaskFromRequestWithParentAndOrder(AddTaskRequest request,
                                                         Task parent, Integer userId) {
        Task taskToAdd = buildTaskToAddFromRequest(request, userId);
        taskToAdd.setParent(parent);
        taskToAdd.setProject(parent.getProject());
        taskToAdd.setProjectOrder(
                taskRepository.getMaxOrderByOwnerIdAndParentId(userId, parent.getId())
        );
        return taskToAdd;
    }

    private Task buildTaskFromRequestWithSiblingAndOrder(AddTaskRequest request, Integer order,
                                                         Task sibling, Integer userId) {
        Task taskToAdd = buildTaskToAddFromRequest(request, userId);
        taskToAdd.setParent(sibling.getParent());
        taskToAdd.setProject(sibling.getProject());
        taskToAdd.setProjectOrder(order);
        return taskToAdd;
    }

    private boolean hasProject(Task task) {
        return task.getProject() != null;
    }

    private void incrementOrderOfTasksAfter(Integer userId, Task task) {
        if(hasParent(task)) {
            taskRepository.incrementOrderByOwnerIdAndParentIdAndOrderGreaterThan(
                    userId,
                    task.getParent().getId(),
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
        } else if(hasProject(task)) {
            taskRepository.incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThan(
                    userId,
                    task.getProject().getId(),
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
        } else {
            taskRepository.incrementOrderByOwnerIdAndOrderGreaterThan(
                    userId,
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
        }
    }

    private void incrementOrderOfTasksBefore(Integer userId, Task task) {
        if(hasParent(task)) {
            taskRepository.incrementOrderByOwnerIdAndParentIdAndOrderGreaterThanEqual(
                    userId,
                    task.getParent().getId(),
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
        } else if(hasProject(task)) {
            taskRepository.incrementOrderByOwnerIdAndProjectIdAndOrderGreaterThanEqual(
                    userId,
                    task.getProject().getId(),
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
        } else {
            taskRepository.incrementOrderByOwnerIdAndOrderGreaterThanEqual(
                    userId,
                    task.getProjectOrder(),
                    LocalDateTime.now()
            );
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
