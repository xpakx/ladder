package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.notification.NotifyOnTaskChange;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.task.dto.AddTaskRequest;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskMovableService {
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final TaskUpdateUtilsService utils;

    /**
     * Duplicate given task, and its subtasks
     * @param taskId ID of the task to duplicate
     * @param userId ID of an owner of the task
     * @return All created tasks
     */
    public List<TaskDetails> duplicate(Integer taskId, Integer userId) {
        Task taskToDuplicate = getTaskFromDb(taskId, userId);
        List<Task> tasks = getAllTasksFromProject(userId, taskToDuplicate.getProject());
        Task duplicatedTask = duplicate(taskToDuplicate, taskToDuplicate.getParent());
        List<Task> allDuplicated = duplicateChildren(generateTaskMapForUser(tasks), duplicatedTask);
        updateOrders(userId, duplicatedTask);
        List<Integer> ids = taskRepository.saveAll(allDuplicated).stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        return taskRepository.findByIdIn(ids);
    }

    private List<Task> getAllTasksFromProject(Integer userId, Project project) {
        return taskRepository.findByOwnerIdAndProjectId(userId,
                project != null ? project.getId() : null);
    }

    private Task getTaskFromDb(Integer taskId, Integer userId) {
        return taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
    }

    private void updateOrders(Integer userId, Task duplicatedTask) {
        incrementOrderOfTasksAfter(userId, duplicatedTask);
        duplicatedTask.setProjectOrder(duplicatedTask.getProjectOrder()+1);
        incrementDailyOrderOfTasksAfter(userId, duplicatedTask);
        duplicatedTask.setDailyViewOrder(duplicatedTask.getDailyViewOrder()+1);
    }

    private List<Task> duplicateChildren(Map<Integer, List<Task>> tasksByParent, Task duplicatedTask) {
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
        return allDuplicated;
    }

    private Map<Integer, List<Task>> generateTaskMapForUser(List<Task> tasks) {
        return tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
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
                .timeboxed(originalTask.isTimeboxed())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .archived(false)
                .parent(parent)
                .build();
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

    /**
     * Add task as child of given task.
     * @param request Request with data to build new task
     * @param userId ID of an owner of tasks
     * @param parentId ID of the task which should be a parent of new task
     * @return Newly created task
     */
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
                taskRepository.getMaxOrderByOwnerIdAndParentId(userId, parent.getId())+1
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
}
