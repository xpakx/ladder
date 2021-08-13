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

import java.time.LocalDateTime;
import java.util.List;
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

    public void deleteTask(Integer taskId, Integer userId) {
        this.taskRepository.deleteByIdAndOwnerId(taskId, userId);
    }

    public TaskDetails getTaskById(Integer taskId, Integer userId) {
        return taskRepository.findProjectedByIdAndOwnerId(taskId, userId, TaskDetails.class)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    public Task updateTask(UpdateTaskRequest request, Integer taskId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(request.getProjectId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        Task parent = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        Task taskToUpdate = taskRepository.getById(taskId);
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setOrder(request.getOrder());
        taskToUpdate.setDue(request.getDue());
        taskToUpdate.setParent(parent);
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
        Project parent = projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        taskToUpdate.setProject(parent);
        return taskRepository.save(taskToUpdate);
    }

    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        taskToUpdate.setCompleted(request.isFlag());
        taskToUpdate.setCompletedAt(request.isFlag() ? LocalDateTime.now() : null);
        return taskRepository.save(taskToUpdate);
    }

    public Task duplicate(Integer taskId, Integer userId) {
        Task taskToDuplicate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));

        Task duplicatedTask = duplicate(taskToDuplicate);
        return taskRepository.save(duplicatedTask);
    }

    private Task duplicate(Task originalTask) {
        Task task = Task.builder()
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .order(originalTask.getOrder())
                .project(originalTask.getProject())
                .createdAt(LocalDateTime.now())
                .due(originalTask.getDue())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .build();
        List<Task> children = originalTask.getChildren().stream()
                .map(this::duplicate)
                .collect(Collectors.toList());
        task.setChildren(children);
        for(Task child : children) {
            child.setParent(task);
        }
        return task;
    }
}
