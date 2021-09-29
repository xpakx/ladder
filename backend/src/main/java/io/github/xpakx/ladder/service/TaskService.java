package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserAccountRepository userRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserAccountRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void deleteTask(Integer taskId, Integer userId) {
        this.taskRepository.deleteByIdAndOwnerId(taskId, userId);
    }

    public TaskDetails getTaskById(Integer taskId, Integer userId) {
        return taskRepository.findProjectedByIdAndOwnerId(taskId, userId, TaskDetails.class)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    public Task updateTask(AddTaskRequest request, Integer taskId, Integer userId) {
        Project project = request.getProjectId() != null ? projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setProjectOrder(request.getProjectOrder());
        taskToUpdate.setDue(request.getDue());
        //taskToUpdate.setParent(parent);
        taskToUpdate.setProject(project);
        taskToUpdate.setCompletedAt(request.getCompletedAt());
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setOwner(userRepository.getById(userId));
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                        .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setDue(request.getDate());
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setPriority(request.getPriority());
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskProject(IdRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        Project project = request.getId() != null ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        taskToUpdate.setProject(project);
        taskToUpdate.setProjectOrder(getMaxProjectOrder(request, userId)+1);
        return taskRepository.save(taskToUpdate);
    }

    private Integer getMaxProjectOrder(IdRequest request, Integer userId) {
        return hasId(request) ? getMaxProjectOrderForParent(request, userId) : getMaxProjectOrderIfNoParent(userId, request.getId());
    }

    private Integer getMaxProjectOrderForParent(IdRequest request, Integer userId) {
        return taskRepository.findByOwnerIdAndParentId(userId, request.getId()).stream()
                .max(Comparator.comparing(Task::getProjectOrder))
                .map(Task::getProjectOrder)
                .orElse(0);
    }

    private Integer getMaxProjectOrderIfNoParent(Integer userId, Integer projectId) {
        return taskRepository.findByOwnerIdAndProjectIdAndParentIsNull(userId, projectId).stream()
                .max(Comparator.comparing(Task::getProjectOrder))
                .map(Task::getProjectOrder)
                .orElse(0);
    }

    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.getByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        if(request.isFlag()) {
            return taskRepository.saveAll(completeTask(userId, taskToUpdate)).stream()
                    .filter((a) -> a.getId().equals(taskId))
                    .findAny()
                    .orElse(null);
        } else {
            taskToUpdate.setCompleted(false);
            taskToUpdate.setCompletedAt(null);
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
        while(toComplete.size() > 0) {
            List<Task> newToComplete = new ArrayList<>();
            for (Task parent : toComplete) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setCompleted(true);
                parent.setCompletedAt(LocalDateTime.now());
                toReturn.add(parent);
                newToComplete.addAll(children);
            }
            toComplete = newToComplete;
        }
        return toReturn;
    }

    public Task duplicate(Integer taskId, Integer userId) {
        Task taskToDuplicate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));

        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                taskToDuplicate.getProject() != null ? taskToDuplicate.getProject().getId() : null);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        Task duplicatedTask = duplicate(taskToDuplicate, taskToDuplicate.getParent());

        List<Task> toDuplicate = List.of(duplicatedTask);
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
        }

        return taskRepository.save(duplicatedTask);
    }

    private Task duplicate(Task originalTask, Task parent) {
        return Task.builder()
                .id(originalTask.getId())
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .projectOrder(originalTask.getProjectOrder())
                .project(originalTask.getProject())
                .createdAt(LocalDateTime.now())
                .due(originalTask.getDue())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .parent(parent)
                .build();
    }

    public Task moveTaskAfter(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task afterTask = findIdFromIdRequest(request);
        List<Task> projectsAfter = getAllTasksAfterGivenTask(userId, afterTask);

        taskToMove.setParent(afterTask.getParent());

        taskToMove.setProjectOrder(afterTask.getProjectOrder()+1);
        projectsAfter.forEach(((p) -> p.setProjectOrder(p.getProjectOrder()+1)));
        taskRepository.saveAll(projectsAfter);
        return taskRepository.save(taskToMove);
    }

    private Task findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? taskRepository.findById(request.getId()).orElse(null) : null;
    }
    private List<Task> getAllTasksAfterGivenTask(Integer userId, Task task) {
        return hasParent(task) ? getAllSiblingsAfter(userId, task) : getAllProjectsWithNoParentAfter(userId, task);
    }
    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private boolean hasParent(Task task) {
        return task.getParent() != null;
    }

    private List<Task> getAllSiblingsAfter(Integer userId, Task task) {
        return taskRepository
                .findByOwnerIdAndParentIdAndProjectOrderGreaterThan(userId, task.getParent().getId(), task.getProjectOrder());
    }

    private List<Task> getAllProjectsWithNoParentAfter(Integer userId, Task task) {
        return hasProject(task) ? getAllFirstOrderTasksFromProjectAfter(userId, task) : getAllFirstOrderTasksFromInboxAfter(userId, task);
    }

    public Task moveTaskAsFirstChild(IdRequest request, Integer userId, Integer taskToMoveId) {
        Task taskToMove = taskRepository.findByIdAndOwnerId(taskToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent task!"));
        Task parentTask = findIdFromIdRequest(request);
        List<Task> children = request.getId() != null ? taskRepository.findByOwnerIdAndParentId(userId, request.getId()) :
                taskRepository.findByOwnerIdAndParentIsNull(userId, Task.class);

        taskToMove.setParent(parentTask);

        taskToMove.setProjectOrder(1);
        children.forEach(((p) -> p.setProjectOrder(p.getProjectOrder()+1)));
        taskRepository.saveAll(children);
        return taskRepository.save(taskToMove);
    }

    public Task updateTaskCollapsion(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        taskToUpdate.setCollapsed(request.isFlag());
        return taskRepository.save(taskToUpdate);
    }

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
                .priority(0)
                .completed(false)
                .collapsed(false)
                .owner(userRepository.getById(userId))
                .build();
    }

    public Task addTaskAfter(AddTaskRequest request, Integer userId, Integer afterId) {
        Task taskToAdd = buildTaskToAddFromRequest(request, userId);
        Task afterTask = taskRepository.findByIdAndOwnerId(afterId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent task!"));
        List<Task> tasksAfter = getAllTasksAfterGivenTask(userId, afterTask);

        taskToAdd.setParent(afterTask.getParent());
        taskToAdd.setProject(afterTask.getProject());

        taskToAdd.setProjectOrder(afterTask.getProjectOrder()+1);
        tasksAfter.forEach(((p) -> p.setProjectOrder(p.getProjectOrder()+1)));
        taskRepository.saveAll(tasksAfter);
        return taskRepository.save(taskToAdd);
    }

    public Task addTaskBefore(AddTaskRequest request, Integer userId, Integer beforeId) {
        Task taskToAdd = buildTaskToAddFromRequest(request, userId);
        Task beforeTask = taskRepository.findByIdAndOwnerId(beforeId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent task!"));
        List<Task> tasksAfter = getAllTasksAfterGivenTaskAndThisTask(userId, beforeTask);

        taskToAdd.setParent(beforeTask.getParent());

        taskToAdd.setProjectOrder(beforeTask.getProjectOrder());
        tasksAfter.forEach(((p) -> p.setProjectOrder(p.getProjectOrder()+1)));
        taskRepository.saveAll(tasksAfter);
        return taskRepository.save(taskToAdd);
    }

    private List<Task> getAllTasksAfterGivenTaskAndThisTask(Integer userId, Task task) {
        return hasParent(task) ? getAllSiblingsAfterIncl(userId, task) : getAllTasksWithNoParentAfterIncl(userId, task);
    }

    private List<Task> getAllTasksWithNoParentAfterIncl(Integer userId, Task task) {
        return hasProject(task) ? getAllFirstOrderTasksFromProjectAfterIncl(userId, task) : getAllFirstOrderTasksFromInboxAfterIncl(userId, task);
    }

    private List<Task> getAllFirstOrderTasksFromInboxAfterIncl(Integer userId, Task task) {
        return taskRepository
            .findByOwnerIdAndAndProjectIdAndParentIsNullAndProjectOrderGreaterThanEqual(userId, task.getProject().getId(), task.getProjectOrder());
    }

    private List<Task> getAllFirstOrderTasksFromInboxAfter(Integer userId, Task task) {
        return taskRepository
                .findByOwnerIdAndAndProjectIdAndParentIsNullAndProjectOrderGreaterThan(userId, task.getProject().getId(), task.getProjectOrder());
    }

    private List<Task> getAllFirstOrderTasksFromProjectAfterIncl(Integer userId, Task task) {
        return taskRepository
                .findByOwnerIdAndAndProjectIsNullAndParentIsNullAndProjectOrderGreaterThanEqual(userId, task.getProjectOrder());
    }
    private List<Task> getAllFirstOrderTasksFromProjectAfter(Integer userId, Task task) {
        return taskRepository
                .findByOwnerIdAndAndProjectIsNullAndParentIsNullAndProjectOrderGreaterThan(userId, task.getProjectOrder());
    }

    private boolean hasProject(Task task) {
        return task.getProject() != null;
    }

    private List<Task> getAllSiblingsAfterIncl(Integer userId, Task task) {
        return taskRepository
                .findByOwnerIdAndParentIdAndProjectOrderGreaterThanEqual(userId, task.getParent().getId(), task.getProjectOrder());
    }
}
