package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public void deleteTask(Integer taskId) {
        this.taskRepository.deleteById(taskId);
    }

    public TaskDetails getTaskById(Integer taskId) {
        return taskRepository.findProjectedById(taskId, TaskDetails.class)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    public Task updateTask(UpdateTaskRequest request, Integer taskId) {
        Project project = projectRepository.getById(request.getProjectId());
        Task parent = taskRepository.getById(request.getParentId());
        Task taskToUpdate = taskRepository.getById(taskId);
        taskToUpdate.setTitle(request.getTitle());
        taskToUpdate.setDescription(request.getDescription());
        taskToUpdate.setOrder(request.getOrder());
        taskToUpdate.setDue(request.getDue());
        taskToUpdate.setParent(parent);
        taskToUpdate.setProject(project);
        taskToUpdate.setCompletedAt(request.getCompletedAt());
        taskToUpdate.setPriority(request.getPriority());
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskDueDate(DateRequest request, Integer taskId) {
        Task taskToUpdate = taskRepository.getById(taskId);
        taskToUpdate.setDue(request.getDate());
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskPriority(PriorityRequest request, Integer taskId) {
        Task taskToUpdate = taskRepository.getById(taskId);
        taskToUpdate.setPriority(request.getPriority());
        return taskRepository.save(taskToUpdate);
    }

    public Task updateTaskProject(IdRequest request, Integer taskId) {
        Task taskToUpdate = taskRepository.getById(taskId);
        Project parent = projectRepository.getById(request.getId());
        taskToUpdate.setProject(parent);
        return taskRepository.save(taskToUpdate);
    }

    public Task completeTask(BooleanRequest request, Integer taskId) {
        Task taskToUpdate = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        taskToUpdate.setCompleted(request.isFlag());
        taskToUpdate.setCompletedAt(request.isFlag() ? LocalDateTime.now() : null);
        return taskRepository.save(taskToUpdate);
    }
}
