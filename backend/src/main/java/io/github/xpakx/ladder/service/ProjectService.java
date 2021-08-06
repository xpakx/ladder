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
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public ProjectDetails getProjectById(Integer projectId) {
        return projectRepository.findProjectedById(projectId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    public Project addProject(ProjectRequest request) {
        Project parent = null;
        if(request.getParentId() != null) {
            parent = projectRepository.getById(request.getParentId());
        }
        Project projectToAdd = Project.builder()
                .name(request.getName())
                .parent(parent)
                .favorite(false)
                .color(request.getColor())
                .build();
        return projectRepository.save(projectToAdd);
    }

    public Project updateProject(ProjectRequest request, Integer projectId) {
        Project projectToUpdate = projectRepository.getById(projectId);
        projectToUpdate.setName(request.getName());
        projectToUpdate.setColor(request.getColor());
        projectToUpdate.setParent(
                request.getParentId() != null ? projectRepository.getById(request.getParentId()) : null
        );
        return projectRepository.save(projectToUpdate);
    }

    public void deleteProject(Integer projectId) {
        projectRepository.deleteById(projectId);
    }

    public Project updateProjectName(NameRequest request, Integer projectId) {
        Project projectToUpdate = projectRepository.getById(projectId);
        projectToUpdate.setName(request.getName());
        return projectRepository.save(projectToUpdate);
    }

    public Project updateProjectParent(IdRequest request, Integer projectId) {
        Project projectToUpdate = projectRepository.getById(projectId);
        projectToUpdate.setParent(
                request.getId() != null ? projectRepository.getById(request.getId()) : null
        );
        return projectRepository.save(projectToUpdate);
    }

    public Project updateProjectFav(BooleanRequest request, Integer projectId) {
        Project projectToUpdate = projectRepository.getById(projectId);
        projectToUpdate.setFavorite(request.isFlag());
        return projectRepository.save(projectToUpdate);
    }

    public Task addTask(UpdateTaskRequest request, Integer projectId) {
        Project project = projectRepository.getById(projectId);
        Task parent = taskRepository.getById(request.getParentId());
        Task taskToAdd = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .order(request.getOrder())
                .project(project)
                .createdAt(LocalDateTime.now())
                .due(request.getDue())
                .parent(parent)
                .build();
        return taskRepository.save(taskToAdd);
    }
}
