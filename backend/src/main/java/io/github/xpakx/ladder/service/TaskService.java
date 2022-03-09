package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.aspect.NotifyOnTaskDeletion;
import io.github.xpakx.ladder.aspect.NotifyOnTasksChange;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserAccountRepository userRepository;
    private final LabelRepository labelRepository;

    @Transactional
    @NotifyOnTaskDeletion
    public void deleteTask(Integer taskId, Integer userId) {
        this.taskRepository.deleteByIdAndOwnerId(taskId, userId);
    }

    public TaskDetails getTaskById(Integer taskId, Integer userId) {
        return taskRepository.findProjectedByIdAndOwnerId(taskId, userId, TaskDetails.class)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    @NotifyOnTaskChange
    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        Project project = request.getProjectId() != null ? projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setProjectOrder(request.getProjectOrder());
        if(haveDifferentDueDate(request.getDue(), taskToUpdate.getDue())) {
            taskToUpdate.setDailyViewOrder(getMaxDailyOrder(request, userId)+1);
        }
        taskToUpdate.setDue(request.getDue());
        if(!haveSameProject(taskToUpdate, project)) {
            List<Task> childrenWithUpdatedProject = updateChildrenProject(project, taskToUpdate, userId);
            taskRepository.saveAll(childrenWithUpdatedProject);
            taskToUpdate.setParent(null);
        }
        taskToUpdate.setProject(project);
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setCompletedAt(request.getCompletedAt());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setOwner(userRepository.getById(userId));
        taskToUpdate.setLabels(transformLabelIdsToLabelReferences(request.getLabelIds(), userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    @NotifyOnTaskChange
    public Task updateTaskWithoutProjectChange(AddTaskRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setProjectOrder(request.getProjectOrder());
        if(haveDifferentDueDate(request.getDue(), taskToUpdate.getDue())) {
            taskToUpdate.setDailyViewOrder(getMaxDailyOrder(request, userId)+1);
        }
        taskToUpdate.setDue(request.getDue());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setCompletedAt(request.getCompletedAt());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setOwner(userRepository.getById(userId));
        taskToUpdate.setLabels(transformLabelIdsToLabelReferences(request.getLabelIds(), userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    private boolean haveDifferentDueDate(LocalDateTime dueDate1, LocalDateTime dueDate2) {
        if(dueDate1 == null && dueDate2 == null) {
            return false;
        }
        if(dueDate1 == null || dueDate2 == null) {
            return true;
        }
        return dueDate1.getYear() != dueDate2.getYear() || dueDate1.getDayOfYear() != dueDate2.getDayOfYear();
    }

    @NotifyOnTaskChange
    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                        .orElseThrow(() -> new NotFoundException("No such task!"));
        if(haveDifferentDueDate(request.getDate(), taskToUpdate.getDue())) {
            taskToUpdate.setDailyViewOrder(getMaxDailyOrder(request, userId)+1);
        }
        taskToUpdate.setDue(request.getDate());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    @NotifyOnTaskChange
    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    @NotifyOnTaskChange
    public Task updateTaskProject(IdRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        Project project = request.getId() != null ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        if(!haveSameProject(taskToUpdate, project)) {
            List<Task> childrenWithUpdatedProject = updateChildrenProject(project, taskToUpdate, userId);
            taskRepository.saveAll(childrenWithUpdatedProject);
        }
        taskToUpdate.setParent(null);
        taskToUpdate.setProject(project);
        taskToUpdate.setProjectOrder(getMaxProjectOrder(request, userId)+1);
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    private boolean haveSameProject(Task taskToUpdate, Project project) {
        return (
                (hasProject(taskToUpdate) && project != null && taskToUpdate.getProject().getId().equals(project.getId()))
                ||
                (!hasProject(taskToUpdate) && project == null)
        );
    }

    private List<Task> updateChildrenProject(Project project, Task parent, Integer userId) {
        List<Task> tasksForProject = getTasksForProjectOrInbox(parent, userId)
                .stream().filter((a) -> a.getParent() != null)
                .collect(Collectors.toList());
        List<Task> children = getImminentChildren(List.of(parent), tasksForProject);
        List<Task> toUpdate = new ArrayList<>();
        while(children.size() > 0) {
            children.forEach((a) -> a.setProject(project));
            toUpdate.addAll(children);
            children = getImminentChildren(children, tasksForProject);
        }
        return toUpdate;
    }

    private List<Task> getTasksForProjectOrInbox(Task parent, Integer userId) {
        return parent.getProject() != null ? taskRepository.findByOwnerIdAndProjectId(userId, parent.getProject().getId()) :
                taskRepository.findByOwnerIdAndProjectIsNull(userId);
    }

    private List<Task> getImminentChildren(List<Task> parentList, List<Task> tasksForProject) {
        List<Integer> ids = parentList.stream().map(Task::getId).collect(Collectors.toList());
        return tasksForProject.stream()
                .filter((a) -> ids.contains(a.getParent().getId()))
                .collect(Collectors.toList());
    }

    private Integer getMaxProjectOrder(IdRequest request, Integer userId) {
        if(hasId(request)) {
            return taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getId());
        } else {
            return taskRepository.getMaxOrderByOwnerId(userId);
        }
   }

    @NotifyOnTaskChange
    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.getByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        if(request.isFlag()) {
            taskToUpdate.setAssigned(userRepository.getById(userId));
            if(taskToUpdate.getProject().isCollaborative()) {
                LocalDateTime now = LocalDateTime.now();
                taskToUpdate.setCompleted(true);
                taskToUpdate.setCompletedAt(now);
                taskToUpdate.setModifiedAt(now);
                return taskRepository.save(taskToUpdate);
            } else {
                return taskRepository.saveAll(completeTask(userId, taskToUpdate)).stream()
                        .filter((a) -> a.getId().equals(taskId))
                        .findAny()
                        .orElse(null);
            }
        } else {
            taskToUpdate.setCompleted(false);
            taskToUpdate.setCompletedAt(null);
            taskToUpdate.setModifiedAt(LocalDateTime.now());
        }
        return taskRepository.save(taskToUpdate);
    }

    private List<Task> completeTask(Integer userId, Task task) {
        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                task.getProject() != null ? task.getProject().getId() : null);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        List<Task> toComplete = List.of(task);
        List<Task> toReturn = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        while(toComplete.size() > 0) {
            List<Task> newToComplete = new ArrayList<>();
            for (Task parent : toComplete) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setCompleted(true);
                parent.setCompletedAt(now);
                parent.setModifiedAt(now);
                toReturn.add(parent);
                newToComplete.addAll(children);
            }
            toComplete = newToComplete;
        }
        return toReturn;
    }

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

    @NotifyOnTaskChange
    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task afterTask = findIdFromIdRequest(request);
        taskToMove.setParent(afterTask.getParent());
        taskToMove.setProjectOrder(afterTask.getProjectOrder()+1);
        incrementOrderOfTasksAfter(userId, afterTask);
        taskToMove.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToMove);
    }

    private Task findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? taskRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private boolean hasParent(Task task) {
        return task.getParent() != null;
    }

    @NotifyOnTaskChange
    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task parentTask = findIdFromIdRequest(request);
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

    @NotifyOnTaskChange
    public Task updateTaskCollapsion(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setCollapsed(request.isFlag());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    @NotifyOnTaskChange
    public Task updateTaskLabels(IdCollectionRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setLabels(transformLabelIdsToLabelReferences(request.getIds(), userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

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
                .dailyViewOrder(getMaxDailyOrder(request, userId)+1)
                .completed(false)
                .collapsed(false)
                .archived(false)
                .owner(userRepository.getById(userId))
                .labels(transformLabelIdsToLabelReferences(request.getLabelIds(), userId))
                .build();
    }

    private Integer getMaxDailyOrder(AddTaskRequest request, Integer userId) {
        return getMaxDailyOrder(request.getDue(), userId);
    }

    private Integer getMaxDailyOrder(DateRequest request, Integer userId) {
        return getMaxDailyOrder(request.getDate(), userId);
    }

    private Integer getMaxDailyOrder(LocalDateTime date, Integer userId) {
        if(date == null) {
            return 0;
        } else {
            return taskRepository.getMaxOrderByOwnerIdAndDate(userId, date);
        }
    }

    private Set<Label> transformLabelIdsToLabelReferences(List<Integer> labelIds, Integer userId) {
        if(labelsWithDiffOwner(labelIds, userId)) {
            throw new NotFoundException("Cannot add labels you don't own!");
        }
        return labelIds != null ? labelIds.stream()
                .map(labelRepository::getById)
                .collect(Collectors.toSet()) : new HashSet<>();

    }

    private boolean labelsWithDiffOwner(List<Integer> labelIds, Integer userId) {
        if(labelIds == null || labelIds.size() == 0) {
            return false;
        }
        Long labelsWithDifferentOwner = labelRepository.findOwnerIdById(labelIds).stream()
                        .filter((a) -> !a.equals(userId))
                        .count();
        return !labelsWithDifferentOwner.equals(0L);
    }

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
        Task afterTask = findIdFromIdRequest(request);
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

    private void incrementDailyOrderOfTasksBefore(Integer userId, Task task) {
        if(task.getDue() != null) {
            taskRepository.incrementOrderByOwnerIdAndDateAndOrderGreaterThanEqual(
                    userId,
                    task.getDue(),
                    task.getDailyViewOrder(),
                    LocalDateTime.now()
            );
        }
    }

    @Transactional
    @NotifyOnTaskChange
    public Task archiveTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        LocalDateTime now = LocalDateTime.now();
        task.setArchived(request.isFlag());
        if(!request.isFlag()) {
            task.setProjectOrder(
                    task.getProject() != null ? taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, task.getProject().getId())+1 : taskRepository.getMaxOrderByOwnerId(userId)+1
            );
        }
        task.setModifiedAt(now);
        taskRepository.saveAll(
                archiveChildren(userId, task, now, request.isFlag())
        );
        return taskRepository.save(task);
    }

    private List<Task> archiveChildren(Integer userId, Task task, LocalDateTime now, boolean archived) {
        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                task.getProject() != null ? task.getProject().getId() : null);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        List<Task> toArchive = List.of(task);
        List<Task> toReturn = new ArrayList<>();
        while(toArchive.size() > 0) {
            List<Task> newToArchive = new ArrayList<>();
            for (Task parent : toArchive) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setArchived(archived);
                parent.setModifiedAt(now);
                toReturn.add(parent);
                newToArchive.addAll(children);
            }
            toArchive = newToArchive;
        }
        return toReturn;
    }

    @NotifyOnTasksChange
    public List<Task> updateDueDateForOverdue(DateRequest request, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today = now.minusHours(now.getHour()).minusMinutes(now.getMinute()).minusSeconds(now.getSecond());
        List<Task> tasksToUpdate = taskRepository.findByOwnerIdAndDueBeforeAndCompletedIsFalse(userId, today);
        if(request.getDate() != null) {
            int order = getMaxDailyOrder(request, userId) + 1;
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
