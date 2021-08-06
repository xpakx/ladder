package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.entity.dto.UpdateTaskRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return taskRepository.save(taskToUpdate);
    }
}
