package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.notification.NotifyOnProjectChange;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.project.dto.ProjectRequest;
import io.github.xpakx.ladder.project.dto.TasksAndProjects;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.common.error.WrongOwnerException;
import io.github.xpakx.ladder.task.TaskRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class ProjectMovableService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;

    /**
     * Duplicate given project, its subprojects and tasks
     * @param projectId ID of the project to duplicate
     * @param userId ID of an owner of the project
     * @return All created projects and tasks
     */
    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
        Map<Integer, Project> projectsById = generateProjectMapForUser(userId);
        testIfProjectExists(projectId, projectsById);
        List<Project> projects = generateProjectDuplicatesToSave(projectId, projectsById);
        List<Task> tasks = generateTaskDuplicatesToSave(projectsById, projects);
        prepareProjectObjectsToSave(projectId, userId, projects);
        return constructResponseWithDuplicatedElements(
                saveProjectsAndReturnIds(projects),
                saveTasksAndReturnIds(tasks)
        );
    }

    private List<Integer> saveProjectsAndReturnIds(List<Project> projects) {
        return projectRepository.saveAll(projects).stream()
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    private List<Integer> saveTasksAndReturnIds(List<Task> tasks) {
        return taskRepository.saveAll(tasks).stream()
                .map(Task::getId)
                .collect(Collectors.toList());
    }

    private void prepareProjectObjectsToSave(Integer projectId, Integer userId, List<Project> projects) {
        projects.stream().filter((a) -> a.getId().equals(projectId))
                .findAny()
                .ifPresent((a) -> incrementOrderForDuplication(a, userId));
        projects.forEach((a) -> a.setId(null));
    }

    private void testIfProjectExists(Integer projectId, Map<Integer, Project> projectsById) {
        projectsById.values().stream().filter((a) -> a.getId().equals(projectId)).findAny()
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent project!"));
    }

    private void incrementOrderForDuplication(Project project, Integer userId) {
        incrementGeneralOrderIfGreaterThan(userId, project);
        project.setGeneralOrder(project.getGeneralOrder()+1);
    }

    private TasksAndProjects constructResponseWithDuplicatedElements(List<Integer> projectIds, List<Integer> taskIds) {
        TasksAndProjects result = new TasksAndProjects();
        result.setTasks(taskRepository.findByIdIn(taskIds));
        result.setProjects(projectRepository.findByIdIn(projectIds));
        return result;
    }

    private List<Project> generateProjectDuplicatesToSave(Integer projectId, Map<Integer, Project> projectsById) {
        return projectsById.values().stream()
                .filter((a) -> hasIdInParentTree(a, projectId))
                .collect(Collectors.toList());
    }

    private Map<Integer, Project> generateProjectMapForUser(Integer userId) {
        Map<Integer, Project> projectsById = projectRepository.getByOwnerId(userId).stream()
                .collect(Collectors.toMap(Project::getId, this::duplicate));
        projectsById.values()
                .stream().filter((a) -> a.getParent() != null)
                .forEach((a) -> a.setParent(projectsById.get(a.getParent().getId())));
        return projectsById;
    }

    private List<Task> generateTaskDuplicatesToSave(Map<Integer, Project> projectsById, List<Project> duplicatedProjects) {
        Map<Integer, Task> tasksById = taskRepository.findByProjectIdInAndArchived(
                        duplicatedProjects.stream().map(Project::getId).collect(Collectors.toList()),
                        false
                ).stream()
                .collect(Collectors.toMap(Task::getId, this::duplicate));
        tasksById.values()
                .stream().filter((a) -> a.getProject() != null)
                .forEach((a) -> a.setProject(projectsById.get(a.getProject().getId())));
        tasksById.values()
                .stream().filter((a) -> a.getParent() != null)
                .forEach((a) -> a.setParent(tasksById.get(a.getParent().getId())));
        List<Task> tasks = new ArrayList<>(tasksById.values());
        tasks.forEach((a) -> a.setId(null));
        return tasks;
    }

    private boolean hasIdInParentTree(Project a, Integer projectId) {
        while(a != null) {
            if(a.getId().equals(projectId)) {
                return true;
            }
            a = a.getParent();
        }
        return false;
    }

    private Project duplicate(Project originalProject) {
        LocalDateTime now = LocalDateTime.now();
        return Project.builder()
                .id(originalProject.getId())
                .name(originalProject.getName())
                .favorite(false)
                .color(originalProject.getColor())
                .parent(originalProject.getParent())
                .owner(originalProject.getOwner())
                .id(originalProject.getId())
                .tasks(null)
                .generalOrder(originalProject.getGeneralOrder())
                .createdAt(now)
                .modifiedAt(now)
                .archived(false)
                .build();
    }

    private Task duplicate(Task originalTask) {
        return Task.builder()
                .id(originalTask.getId())
                .title(originalTask.getTitle())
                .description(originalTask.getDescription())
                .projectOrder(originalTask.getProjectOrder())
                .createdAt(LocalDateTime.now())
                .priority(originalTask.getPriority())
                .owner(originalTask.getOwner())
                .completed(false)
                .archived(false)
                .timeboxed(false)
                .id(originalTask.getId())
                .parent(originalTask.getParent())
                .project(originalTask.getProject())
                .labels(new HashSet<>(originalTask.getLabels()))
                .build();
    }

    /**
     * Add new project with order after given project
     * @param request Request with data to build new project
     * @param userId ID of an owner of projects
     * @param projectId ID of the project which should be before newly created project
     * @return Newly created project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project addProjectAfter(ProjectRequest request, Integer userId, Integer projectId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent project!"));
        projectToAdd.setParent(project.getParent());
        projectToAdd.setGeneralOrder(project.getGeneralOrder()+1);
        projectToAdd.setModifiedAt(LocalDateTime.now());
        incrementGeneralOrderIfGreaterThan(userId, project);
        return projectRepository.save(projectToAdd);
    }

    private void incrementGeneralOrderIfGreaterThan(Integer userId, Project proj) {
        if(hasParent(proj)) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThan(
                    userId,
                    proj.getParent().getId(),
                    proj.getGeneralOrder(),
                    LocalDateTime.now()
            );
        } else {
            projectRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                    userId,
                    proj.getGeneralOrder(),
                    LocalDateTime.now()
            );
        }
    }

    private void incrementGeneralOrderIfGreaterThanEquals(Integer userId, Project proj) {
        if(hasParent(proj)) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThanEqual(
                    userId,
                    proj.getParent().getId(),
                    proj.getGeneralOrder(),
                    LocalDateTime.now()
            );
        } else {
            projectRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(
                    userId,
                    proj.getGeneralOrder(),
                    LocalDateTime.now()
            );
        }
    }

    private boolean hasParent(Project project) {
        return nonNull(project.getParent());
    }

    private boolean hasParent(ProjectRequest project) {
        return nonNull(project.getParentId());
    }

    /**
     * Add new project with order before given project
     * @param request Request with data to build new project
     * @param userId ID of an owner of projects
     * @param projectId ID of the project which should be after newly created project
     * @return Newly created project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project addProjectBefore(ProjectRequest request, Integer userId, Integer projectId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        Project proj = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent project!"));
        projectToAdd.setParent(proj.getParent());
        projectToAdd.setGeneralOrder(proj.getGeneralOrder());
        projectToAdd.setModifiedAt(LocalDateTime.now());
        incrementGeneralOrderIfGreaterThanEquals(userId, proj);
        return projectRepository.save(projectToAdd);
    }

    /**
     * Move project after given project
     * @param request Request with id of the project which should be before moved project
     * @param userId ID of an owner of projects
     * @param projectToMoveId ID of the project to move
     * @return Moved project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project moveProjectAfter(IdRequest request, Integer userId, Integer projectToMoveId) {
        Project projectToMove = projectRepository.findByIdAndOwnerId(projectToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent project!"));
        Project afterProject = findIdFromIdRequest(request)
                .orElseThrow(() -> new NotFoundException("Cannot insert anything after non-existent project!"));
        incrementGeneralOrderIfGreaterThan(userId, afterProject);
        projectToMove.setParent(afterProject.getParent());
        projectToMove.setGeneralOrder(afterProject.getGeneralOrder()+1);
        projectToMove.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToMove);
    }

    private Optional<Project> findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? projectRepository.findById(request.getId()) : Optional.empty();
    }

    private boolean hasId(IdRequest request) {
        return nonNull(request.getId());
    }

    /**
     * Move project as first child of given project.
     * @param request Request with id of the new parent for moved project. If ID is null project is moved at first position.
     * @param userId ID of an owner of projects
     * @param projectToMoveId ID of the project to move
     * @return Moved project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project moveProjectAsFirstChild(IdRequest request, Integer userId, Integer projectToMoveId) {
        Project projectToMove = projectRepository.findByIdAndOwnerId(projectToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent project!"));
        Optional<Project> parentProject = findIdFromIdRequest(request);
        incrementGeneralOrderOfAllChildren(request, userId);
        projectToMove.setParent(parentProject.orElse(null));
        projectToMove.setGeneralOrder(1);
        projectToMove.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToMove);
    }

    private void incrementGeneralOrderOfAllChildren(IdRequest request, Integer userId) {
        if(request.getId() != null) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentId(
                    userId,
                    request.getId(),
                    LocalDateTime.now()
            );
        } else {
            projectRepository.incrementGeneralOrderByOwnerId(
                    userId,
                    LocalDateTime.now()
            );
        }
    }

    /**
     * Move project at first position
     * @param userId ID of an owner of projects
     * @param projectToMoveId ID of the project to move
     * @return Moved project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project moveProjectAsFirst(Integer userId, Integer projectToMoveId) {
        IdRequest request = new IdRequest();
        request.setId(null);
        return moveProjectAsFirstChild(request, userId, projectToMoveId);
    }

    private Project buildProjectToAddFromRequest(ProjectRequest request, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return Project.builder()
                .name(request.getName())
                .parent(getParentFromProjectRequest(request, userId))
                .favorite(request.isFavorite())
                .color(request.getColor())
                .createdAt(now)
                .modifiedAt(now)
                .collapsed(true)
                .archived(false)
                .owner(userRepository.getById(userId))
                .build();
    }

    private Project getParentFromProjectRequest(ProjectRequest request, Integer userId) {
        if(!hasParent(request)) {
            return null;
        }
        testParentOwnership(request.getParentId(), userId);
        return projectRepository.getById(request.getParentId());
    }

    private void testParentOwnership(Integer parentId, Integer userId) {
        Integer ownerId = projectRepository.findOwnerIdById(parentId);
        if(isNull(ownerId) || !ownerId.equals(userId)) {
            throw new WrongOwnerException("Cannot add nonexistent project as parent!");
        }
    }
}
