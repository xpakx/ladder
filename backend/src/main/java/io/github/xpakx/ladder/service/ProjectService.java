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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository, UserAccountRepository userRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public ProjectDetails getProjectById(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    public Project addProject(ProjectRequest request, Integer userId) {
        Project projectToAdd = Project.builder()
                .name(request.getName())
                .parent(getParentFromProjectRequest(request))
                .favorite(false)
                .color(request.getColor())
                .owner(userRepository.getById(userId))
                .build();
        return projectRepository.save(projectToAdd);
    }

    private Project getParentFromProjectRequest(ProjectRequest request) {
        return request.getParentId() != null ? projectRepository.getById(request.getParentId()) : null;
    }

    public Project updateProject(ProjectRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setName(request.getName());
        projectToUpdate.setColor(request.getColor());
        projectToUpdate.setParent(getParentFromProjectRequest(request));
        return projectRepository.save(projectToUpdate);
    }

    @Transactional
    public void deleteProject(Integer projectId, Integer userId) {
        projectRepository.deleteByIdAndOwnerId(projectId, userId);
    }

    public Project updateProjectName(NameRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setName(request.getName());
        return projectRepository.save(projectToUpdate);
    }

    public Project updateProjectParent(IdRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setParent(
                request.getId() != null ? projectRepository.getById(request.getId()) : null
        );
        return projectRepository.save(projectToUpdate);
    }

    public Project updateProjectFav(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setFavorite(request.isFlag());
        return projectRepository.save(projectToUpdate);
    }

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        Task parent = request.getParentId() == null ? null : taskRepository.getById(request.getParentId());
        Task taskToAdd = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectOrder(request.getProjectOrder())
                .project(project)
                .createdAt(LocalDateTime.now())
                .due(request.getDue())
                .parent(parent)
                .priority(0)
                .completed(false)
                .owner(userRepository.getById(userId))
                .build();
        return taskRepository.save(taskToAdd);
    }

    public FullProjectTree getFullProject(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, FullProjectTree.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    public List<FullProjectTree> getFullTree(Integer userId) {
        return projectRepository.findByOwnerIdAndParentIsNull(userId, FullProjectTree.class);
    }

    public Project duplicate(Integer projectId, Integer userId) {
        Project projectToDuplicate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No project with id " + projectId));
        Project duplicatedProject = duplicate(projectToDuplicate);
        return projectRepository.save(duplicatedProject);
    }

    private Project duplicate(Project originalProject) {
        Project project = Project.builder()
                .name(originalProject.getName())
                .favorite(false)
                .color(originalProject.getColor())
                .parent(originalProject.getParent())
                .owner(originalProject.getOwner())
                .build();
        List<Project> children = originalProject.getChildren().stream()
                .map(this::duplicate)
                .collect(Collectors.toList());
        project.setChildren(children);
        for(Project child : children) {
            child.setParent(project);
        }

        List<Task> tasks = originalProject.getTasks().stream()
                .map(this::duplicate)
                .collect(Collectors.toList());
        project.setTasks(tasks);
        for(Task child : tasks) {
            child.setProject(project);
        }
        return project;
    }

    private Task duplicate(Task originalTask) {
        Task task = Task.builder()
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .projectOrder(originalTask.getProjectOrder())
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
