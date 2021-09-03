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
import java.util.List;
import java.util.Map;
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
        ProjectMin project = projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectMin.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        List<TaskDetails> tasks = taskRepository.findByOwnerId(userId, TaskDetails.class);
        Map<Integer, List<ProjectDetails>> projectByParent = projects.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
        Map<Integer, List<TaskDetails>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
        Map<Integer, List<TaskDetails>> tasksByProject = tasks.stream()
                .filter((a) -> a.getParent() == null)
                .filter((a) -> a.getProject() != null)
                .collect(Collectors.groupingBy((a) -> a.getProject().getId()));


        FullProjectTree result = new FullProjectTree(project);
        List<FullProjectTree> toAdd = List.of(result);
        addProjectsToTree(projectByParent, tasksByParent, tasksByProject, toAdd);

        return result;
    }

    private void addProjectsToTree(Map<Integer, List<ProjectDetails>> projectByParent, Map<Integer, List<TaskDetails>> tasksByParent, Map<Integer, List<TaskDetails>> tasksByProject, List<FullProjectTree> toAdd) {
        while(toAdd.size() > 0) {
            List<FullProjectTree> newToAdd = new ArrayList<>();
            for (FullProjectTree parent : toAdd) {
                List<FullProjectTree> children = projectByParent
                        .getOrDefault(parent.getId(), new ArrayList<>()).stream()
                                .map(FullProjectTree::new)
                                        .collect(Collectors.toList());
                parent.setTasks(addTasksToTree(parent, tasksByParent, tasksByProject));
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
    }

    private List<TaskForTree> addTasksToTree(FullProjectTree project, Map<Integer, List<TaskDetails>> tasksByParent,
                                      Map<Integer, List<TaskDetails>> tasksByProject) {
        List<TaskForTree> toAdd = tasksByProject.getOrDefault(project.getId(), new ArrayList<>()).stream()
                .map(TaskForTree::new)
                .collect(Collectors.toList());
        List<TaskForTree> result = toAdd;
        while(toAdd.size() > 0) {
            List<TaskForTree> newToAdd = new ArrayList<>();
            for (TaskForTree parent : toAdd) {
                List<TaskForTree> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>()).stream()
                                .map(TaskForTree::new)
                                .collect(Collectors.toList());
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
        return result;
    }

    public List<FullProjectTree> getFullTree(Integer userId) {
        return projectRepository.findByOwnerIdAndParentIsNull(userId, FullProjectTree.class);
    }

    public Project duplicate(Integer projectId, Integer userId) {
        Project projectToDuplicate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No project with id " + projectId));

        List<Project> projects = projectRepository.getByOwnerId(userId);
        List<Task> tasks = taskRepository.getByOwnerIdAndProjectIsNotNull(userId);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
        Map<Integer, List<Task>> tasksByProject = tasks.stream()
                .filter((a) -> a.getParent() == null)
                .filter((a) -> a.getProject() != null)
                .collect(Collectors.groupingBy((a) -> a.getProject().getId()));
        Map<Integer, List<Project>> projectByParent = projects.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        Project duplicatedProject = duplicate(projectToDuplicate, projectToDuplicate.getParent());

        List<Project> toDuplicate = List.of(duplicatedProject);
        while(toDuplicate.size() > 0) {
            List<Project> newToDuplicate = new ArrayList<>();
            for (Project parent : toDuplicate) {
                List<Project> children = projectByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setTasks(duplicateTasks(parent, tasksByParent, tasksByProject));
                parent.setId(null);
                children = children.stream()
                        .map((a) -> duplicate(a, parent))
                        .collect(Collectors.toList());
                parent.setChildren(children);
                newToDuplicate.addAll(children);
            }
            toDuplicate = newToDuplicate;
        }

        return projectRepository.save(duplicatedProject);
    }

    private List<Task> duplicateTasks(Project project, Map<Integer, List<Task>> tasksByParent,
                                      Map<Integer, List<Task>> tasksByProject) {
        List<Task> toDuplicate = tasksByProject.getOrDefault(project.getId(), new ArrayList<>()).stream()
                .map((a) -> duplicate(a, null, project))
                .collect(Collectors.toList());
        List<Task> result = new ArrayList<>();
        while(toDuplicate.size() > 0) {
            List<Task> newToDuplicate = new ArrayList<>();
            for (Task parent : toDuplicate) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setId(null);

                children = children.stream()
                        .map((a) -> duplicate(a, parent, project))
                        .collect(Collectors.toList());
                parent.setChildren(children);
                result.add(parent);
                newToDuplicate.addAll(children);
            }
            toDuplicate = newToDuplicate;
        }

        return result;
    }

    private Project duplicate(Project originalProject, Project parent) {
        return Project.builder()
                .name(originalProject.getName())
                .favorite(false)
                .color(originalProject.getColor())
                .parent(originalProject.getParent())
                .owner(originalProject.getOwner())
                .id(originalProject.getId())
                .parent(parent)
                .tasks(originalProject.getTasks())
                .build();
    }

    private Task duplicate(Task originalTask, Task parent, Project project) {
        return Task.builder()
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .projectOrder(originalTask.getProjectOrder())
                .project(project)
                .parent(parent)
                .createdAt(LocalDateTime.now())
                .due(originalTask.getDue())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .id(originalTask.getId())
                .build();
    }

}
