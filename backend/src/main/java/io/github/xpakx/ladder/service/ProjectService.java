package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongOwnerException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final LabelRepository labelRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    /**
     * Getting object with project's data from repository.
     * @param projectId Id of the project to get
     * @param userId Id of an owner of the project
     * @return Object with project's details
     */
    public ProjectDetails getProjectById(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    /**
     * Adding new project to repository.
     * @param request Data to build new projects
     * @param userId Id of an owner of the newly created project
     * @return Created project
     */
    @Transactional
    public Project addProject(ProjectRequest request, Integer userId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        projectToAdd.setGeneralOrder(getMaxGeneralOrder(request, userId)+1);
        return projectRepository.save(projectToAdd);
    }

    private Integer getMaxGeneralOrder(ProjectRequest request, Integer userId) {
        if(hasParent(request)) {
            return projectRepository.getMaxOrderByOwnerIdAndParentId(userId, request.getParentId());
        } else {
            return projectRepository.getMaxOrderByOwnerId(userId);
        }
    }

    private boolean hasParent(ProjectRequest request) {
        return request.getParentId() != null;
    }

    private Project buildProjectToAddFromRequest(ProjectRequest request, Integer userId) {
        return Project.builder()
                .name(request.getName())
                .parent(getParentFromProjectRequest(request, userId))
                .favorite(request.isFavorite())
                .color(request.getColor())
                .collapsed(true)
                .owner(userRepository.getById(userId))
                .build();
    }

    private Project getParentFromProjectRequest(ProjectRequest request, Integer userId) {
        if(!hasParent(request)) {
            return null;
        }
        if(!projectRepository.existsByIdAndOwnerId(request.getParentId(), userId)) {
            throw new WrongOwnerException("Cannot add nonexistent project as task!");
        }
        return projectRepository.getById(request.getParentId());
    }

    /**
     * Updating project in repository.
     * @param request Data to update the project
     * @param projectId Id of the project to update
     * @param userId Id of an owner of the project
     * @return Project with updated data
     */
    @Transactional
    public Project updateProject(ProjectRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setName(request.getName());
        projectToUpdate.setColor(request.getColor());
        projectToUpdate.setParent(getParentFromProjectRequest(request, userId));
        projectToUpdate.setFavorite(request.isFavorite());
        return projectRepository.save(projectToUpdate);
    }

    /**
     * Delete project from repository.
     * @param projectId Id of the project to delete
     * @param userId Id of an owner of the project
     */
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
        projectToUpdate.setParent( getIdFromIdRequest(request));
        return projectRepository.save(projectToUpdate);
    }

    private Project getIdFromIdRequest(IdRequest request) {
        return hasId(request) ? projectRepository.getById(request.getId()) : null;
    }
    
    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    public Project updateProjectFav(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setFavorite(request.isFlag());
        return projectRepository.save(projectToUpdate);
    }
    
    public Project updateProjectCollapsion(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setCollapsed(request.isFlag());
        return projectRepository.save(projectToUpdate);
    }

    public Optional<Project> checkProjectOwnerAndGetReference(Integer projectId, Integer userId) {
        if(!userId.equals(projectRepository.findOwnerIdById(projectId))) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.getById(projectId));
    }

    public Task addTask(AddTaskRequest request, Integer projectId, Integer userId) {
        Project project = projectId != null ? checkProjectOwnerAndGetReference(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
        Task taskToAdd = buildTaskToAddFromRequest(request, userId, project);
        taskToAdd.setProjectOrder(getMaxProjectOrder(request, userId)+1);
        return taskRepository.save(taskToAdd);
    }

    private Integer getMaxProjectOrder(AddTaskRequest request, Integer userId) {
        if(hasParent(request)) {
            if (request.getProjectId() != null) {
                return taskRepository.getMaxOrderByOwnerIdAndProjectIdAndParentId(userId, request.getProjectId(), request.getParentId());
            } else {
                return taskRepository.getMaxOrderByOwnerIdAndParentId(userId, request.getParentId());
            }
        } else {
            if (request.getProjectId() != null) {
                return taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getProjectId());
            } else {
                return taskRepository.getMaxOrderByOwnerId(userId);
            }
        }
    }

    private Task buildTaskToAddFromRequest(AddTaskRequest request, Integer userId, Project project) {
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectOrder(request.getProjectOrder())
                .project(project)
                .createdAt(LocalDateTime.now())
                .due(request.getDue())
                .dailyViewOrder(getMaxDailyOrder(request, userId)+1)
                .parent(getParentFromAddTaskRequest(request))
                .priority(request.getPriority())
                .completed(false)
                .collapsed(false)
                .owner(userRepository.getById(userId))
                .labels(transformLabelIdsToLabelReferences(request, userId))
                .build();
    }

    private Integer getMaxDailyOrder(AddTaskRequest request, Integer userId) {
        return getMaxDailyOrder(request.getDue(), userId);
    }

    private Integer getMaxDailyOrder(LocalDateTime date, Integer userId) {
        if(date == null) {
            return 0;
        } else {
            return taskRepository.getMaxOrderByOwnerIdAndDate(userId, date);
        }
    }

    private Set<Label> transformLabelIdsToLabelReferences(AddTaskRequest request, Integer userId) {
        if(labelsWithDiffOwner(request.getLabelIds(), userId)) {
            throw new NotFoundException("Cannot add labels you don't own!");
        }
        return request.getLabelIds() != null ? request.getLabelIds().stream()
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

    private Task getParentFromAddTaskRequest(AddTaskRequest request) {
        return hasParent(request) ? taskRepository.getById(request.getParentId()) : null;
    }

    private boolean hasParent(AddTaskRequest request) {
        return request.getParentId() != null;
    }

    public FullProjectTree getFullProject(Integer projectId, Integer userId) {
        ProjectMin project = projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectMin.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        List<TaskDetails> tasks = taskRepository.findByOwnerId(userId, TaskDetails.class);
        Map<Integer, List<ProjectDetails>> projectByParent = constructMapWithProjectsGroupedByParentId(projects);
        Map<Integer, List<TaskDetails>> tasksByParent = constructMapWithTasksGroupedByParentId(tasks);
        Map<Integer, List<TaskDetails>> tasksByProject = constructMapWithTasksGroupedByProjectId(tasks);

        FullProjectTree result = new FullProjectTree(project);
        List<FullProjectTree> toAdd = List.of(result);
        addProjectsToTree(projectByParent, tasksByParent, tasksByProject, toAdd);

        return result;
    }

    private Map<Integer, List<TaskDetails>> constructMapWithTasksGroupedByProjectId(List<TaskDetails> tasks) {
        return tasks.stream()
                .filter((a) -> a.getParent() == null)
                .filter((a) -> a.getProject() != null)
                .collect(Collectors.groupingBy((a) -> a.getProject().getId()));
    }

    private Map<Integer, List<ProjectDetails>> constructMapWithProjectsGroupedByParentId(List<ProjectDetails> projects) {
        return projects.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
    }

    private void addProjectsToTree(Map<Integer, List<ProjectDetails>> projectByParent, Map<Integer,
            List<TaskDetails>> tasksByParent, Map<Integer, List<TaskDetails>> tasksByProject,
                                   List<FullProjectTree> toAdd) {
        while(toAdd.size() > 0) {
            List<FullProjectTree> newToAdd = new ArrayList<>();
            for (FullProjectTree parent : toAdd) {
                List<FullProjectTree> children = getAllProjectChildrenAsTreeElems(projectByParent, parent);
                parent.setTasks(addTasksToTree(parent, tasksByParent, tasksByProject));
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
    }

    private List<FullProjectTree> getAllProjectChildrenAsTreeElems(Map<Integer, List<ProjectDetails>> projectByParent, FullProjectTree parent) {
        return projectByParent
                .getOrDefault(parent.getId(), new ArrayList<>()).stream()
                        .map(FullProjectTree::new)
                        .collect(Collectors.toList());
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
                List<TaskForTree> children = getAllTaskChildrenAsTreeElems(tasksByParent, parent);
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
        return result;
    }

    private List<TaskForTree> getAllTaskChildrenAsTreeElems(Map<Integer, List<TaskDetails>> tasksByParent, TaskForTree parent) {
        return tasksByParent.getOrDefault(parent.getId(), new ArrayList<>()).stream()
                        .map(TaskForTree::new)
                        .collect(Collectors.toList());
    }

    public List<FullProjectTree> getFullTree(Integer userId) {
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        List<TaskDetails> tasks = taskRepository.findByOwnerId(userId, TaskDetails.class);
        Map<Integer, List<ProjectDetails>> projectByParent = constructMapWithProjectsGroupedByParentId(projects);
        Map<Integer, List<TaskDetails>> tasksByParent = constructMapWithTasksGroupedByParentId(tasks);
        Map<Integer, List<TaskDetails>> tasksByProject = constructMapWithTasksGroupedByProjectId(tasks);


        List<FullProjectTree> toAdd = projects.stream()
                .filter((a) -> a.getParent() == null)
                .map(FullProjectTree::new)
                .collect(Collectors.toList());
        addProjectsToTree(projectByParent, tasksByParent, tasksByProject, toAdd);

        return toAdd;
    }

    private Map<Integer, List<TaskDetails>> constructMapWithTasksGroupedByParentId(List<TaskDetails> tasks) {
        return tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
    }

    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
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
        List<Project> allDuplicatedProjects = new ArrayList<>();
        allDuplicatedProjects.add(duplicatedProject);
        List<Task> allDuplicatedTasks = new ArrayList<>();
        while(toDuplicate.size() > 0) {
            List<Project> newToDuplicate = new ArrayList<>();
            for (Project parent : toDuplicate) {
                List<Project> children = projectByParent.getOrDefault(parent.getId(), new ArrayList<>());
                List<Task> duplicatedTasks = duplicateTasks(parent, tasksByParent, tasksByProject);
                //parent.setTasks(duplicatedTasks);
                allDuplicatedTasks.addAll(duplicatedTasks);
                parent.setId(null);
                children = children.stream()
                        .map((a) -> duplicate(a, parent))
                        .collect(Collectors.toList());
                //parent.setChildren(children);
                parent.setTasks(null);
                newToDuplicate.addAll(children);
            }
            toDuplicate = newToDuplicate;
            allDuplicatedProjects.addAll(newToDuplicate);
        }
        List<Integer> projectIds = projectRepository.saveAll(allDuplicatedProjects).stream()
                .map(Project::getId)
                .collect(Collectors.toList());
        List<Integer> taskIds = taskRepository.saveAll(allDuplicatedTasks).stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        TasksAndProjects result = new TasksAndProjects();
        result.setTasks(taskRepository.findByIdIn(taskIds));
        result.setProjects(projectRepository.findByIdIn(projectIds));
        return result;
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
                .generalOrder(originalProject.getGeneralOrder())
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

    public Project addProjectAfter(ProjectRequest request, Integer userId, Integer projectId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        Project proj = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent project!"));
        projectToAdd.setParent(proj.getParent());
        projectToAdd.setGeneralOrder(proj.getGeneralOrder()+1);
        incrementGeneralOrderIfGreaterThan(userId, proj);
        return projectRepository.save(projectToAdd);
    }

    private void incrementGeneralOrderIfGreaterThan(Integer userId, Project proj) {
        if(hasParent(proj)) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThan(
                    userId,
                    proj.getParent().getId(),
                    proj.getGeneralOrder());
        } else {
            projectRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                    userId,
                    proj.getGeneralOrder());
        }
    }

    private void incrementGeneralOrderIfGreaterThanEquals(Integer userId, Project proj) {
        if(hasParent(proj)) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentIdAndGeneralOrderGreaterThanEqual(
                    userId,
                    proj.getParent().getId(),
                    proj.getGeneralOrder());
        } else {
            projectRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(
                    userId,
                    proj.getGeneralOrder());
        }
    }

    private boolean hasParent(Project project) {
        return project.getParent() != null;
    }

    public Project addProjectBefore(ProjectRequest request, Integer userId, Integer projectId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        Project proj = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent project!"));
        projectToAdd.setParent(proj.getParent());
        projectToAdd.setGeneralOrder(proj.getGeneralOrder());
        incrementGeneralOrderIfGreaterThanEquals(userId, proj);
        return projectRepository.save(projectToAdd);
    }

    public Project moveProjectAfter(IdRequest request, Integer userId, Integer projectToMoveId) {
        Project projectToMove = projectRepository.findByIdAndOwnerId(projectToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent project!"));
        Project afterProject = findIdFromIdRequest(request)
                .orElseThrow(() -> new NotFoundException("Cannot insert anything after non-existent project!"));
        projectToMove.setParent(afterProject.getParent());
        projectToMove.setGeneralOrder(afterProject.getGeneralOrder()+1);
        incrementGeneralOrderIfGreaterThan(userId, afterProject);
        return projectRepository.save(projectToMove);
    }
    
    private Optional<Project> findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? projectRepository.findById(request.getId()) : Optional.empty();
    }

    public Project moveProjectAsFirstChild(IdRequest request, Integer userId, Integer projectToMoveId) {
        Project projectToMove = projectRepository.findByIdAndOwnerId(projectToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent project!"));
        Optional<Project> parentProject = findIdFromIdRequest(request);
        projectToMove.setParent(parentProject.orElse(null));
        projectToMove.setGeneralOrder(1);
        incrementGeneralOrderOfAllChildren(request, userId);
        return projectRepository.save(projectToMove);
    }

    private void incrementGeneralOrderOfAllChildren(IdRequest request, Integer userId) {
        if(request.getId() == null) {
            projectRepository.incrementGeneralOrderByOwnerIdAndParentId(
                    userId,
                    request.getId());
        } else {
            projectRepository.incrementGeneralOrderByOwnerId(
                    userId);
        }
    }

    public Project moveProjectAsFirst(Integer userId, Integer projectToMoveId) {
        IdRequest request = new IdRequest();
        request.setId(null);
        return moveProjectAsFirstChild(request, userId, projectToMoveId);
    }
}
