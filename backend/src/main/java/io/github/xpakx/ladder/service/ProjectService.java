package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnProjectChange;
import io.github.xpakx.ladder.aspect.NotifyOnProjectDeletion;
import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongOwnerException;
import io.github.xpakx.ladder.repository.*;
import lombok.AllArgsConstructor;
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
    private final HabitRepository habitRepository;

    /**
     * Getting object with project's data from repository.
     * @param projectId ID of the project to get
     * @param userId ID of an owner of the project
     * @return Object with project's details
     */
    public ProjectDetails getProjectById(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    /**
     * Adding new project to repository.
     * @param request Data to build new projects
     * @param userId ID of an owner of the newly created project
     * @return Created project
     */
    @Transactional
    @NotifyOnProjectChange
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
        Integer ownerId = projectRepository.findOwnerIdById(request.getParentId());
        if(ownerId == null ||  !ownerId.equals(userId)) {
            throw new WrongOwnerException("Cannot add nonexistent project as task!");
        }
        return projectRepository.getById(request.getParentId());
    }

    /**
     * Updating project in repository.
     * @param request Data to update the project
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Project with updated data
     */
    @Transactional
    @NotifyOnProjectChange
    public Project updateProject(ProjectRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setName(request.getName());
        projectToUpdate.setColor(request.getColor());
        projectToUpdate.setFavorite(request.isFavorite());
        projectToUpdate.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToUpdate);
    }

    /**
     * Delete project from repository.
     * @param projectId ID of the project to delete
     * @param userId ID of an owner of the project
     */
    @Transactional
    @NotifyOnProjectDeletion
    public void deleteProject(Integer projectId, Integer userId) {
        projectRepository.deleteByIdAndOwnerId(projectId, userId);
    }

    /**
     * Change project's name without editing any other field.
     * @param request request with new name
     * @param projectId ID of the project do update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectName(NameRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setName(request.getName());
        projectToUpdate.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToUpdate);
    }

    /**
     * Change project's parent without editing any other field.
     * @param request Request with parent id
     * @param projectId ID of the project to update
     * @param userId ID an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectParent(IdRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setParent( getIdFromIdRequest(request));
        projectToUpdate.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToUpdate);
    }

    private Project getIdFromIdRequest(IdRequest request) {
        return hasId(request) ? projectRepository.getById(request.getId()) : null;
    }
    
    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    /**
     * Change if project is favorite without editing any other field.
     * @param request Request with favorite flag
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectFav(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setFavorite(request.isFlag());
        projectToUpdate.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToUpdate);
    }

    /**
     * Change if project is collapsed without editing any other field.
     * @param request Request with collapse flag
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectCollapsion(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        projectToUpdate.setCollapsed(request.isFlag());
        projectToUpdate.setModifiedAt(LocalDateTime.now());
        return projectRepository.save(projectToUpdate);
    }

    private Optional<Project> checkProjectOwnerAndGetReference(Integer projectId, Integer userId) {
        if(!userId.equals(projectRepository.findOwnerIdById(projectId))) {
            return Optional.empty();
        }
        return Optional.of(projectRepository.getById(projectId));
    }

    /**
     * Add new task to given project
     * @param request Request with data to build new project
     * @param projectId ID of the project for task
     * @param userId If of an owner of the project and newly created task
     * @return Newly created task
     */
    @NotifyOnTaskChange
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
        LocalDateTime now = LocalDateTime.now();
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .projectOrder(request.getProjectOrder())
                .project(project)
                .createdAt(now)
                .modifiedAt(now)
                .due(request.getDue())
                .dailyViewOrder(getMaxDailyOrder(request, userId)+1)
                .parent(getParentFromAddTaskRequest(request))
                .priority(request.getPriority())
                .completed(false)
                .collapsed(false)
                .archived(false)
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

    /**
     * Get tree with all subprojects and tasks of the given project
     * @param projectId Project id
     * @param userId ID of an owner of the project
     * @return Project tree
     */
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

    /**
     * Get whole tree of all projects for given user
     * @param userId If of a user
     * @return Projects tree
     */
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

    /**
     * Duplicate given project, its subprojects and tasks
     * @param projectId ID of the project to duplicate
     * @param userId ID of an owner of the project
     * @return All created projects and tasks
     */
    public TasksAndProjects duplicate(Integer projectId, Integer userId) {
        Map<Integer, Project> projectsById = generateProjectMapForUser(userId);
        projectsById.values().stream().filter((a) -> a.getId().equals(projectId)).findAny()
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent project!"));
        List<Project> projects = generateProjectDuplicatesToSave(projectId, projectsById);
        List<Task> tasks2 = generateTaskDuplicatesToSave(projectsById, projects);
        projects.stream().filter((a) -> a.getId().equals(projectId))
                .findAny()
                .ifPresent((a) -> incrementOrderForDuplication(a, userId));
        projects.forEach((a) -> a.setId(null));

        List<Integer> projectIds = projectRepository.saveAll(projects).stream()
                .map(Project::getId)
                .collect(Collectors.toList());
        List<Integer> taskIds = taskRepository.saveAll(tasks2).stream()
                .map(Task::getId)
                .collect(Collectors.toList());
        return constructResponseWithDuplicatedElements(projectIds, taskIds);
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
        Project proj = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent project!"));
        projectToAdd.setParent(proj.getParent());
        projectToAdd.setGeneralOrder(proj.getGeneralOrder()+1);
        projectToAdd.setModifiedAt(LocalDateTime.now());
        incrementGeneralOrderIfGreaterThan(userId, proj);
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
        return project.getParent() != null;
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

    @Transactional
    @NotifyOnProjectChange
    public Project archiveProject(BooleanRequest request, Integer projectId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        LocalDateTime now = LocalDateTime.now();
        project.setArchived(request.isFlag());
        project.setModifiedAt(now);
        if(request.isFlag()) {
            project.setGeneralOrder(0);
            project.setParent(null);
            detachProjectFromTree(request, projectId, userId, project, now);
        } else {
            project.setGeneralOrder(projectRepository.getMaxOrderByOwnerId(userId));
            archiveTasks(request, projectId, userId, now, false);
        }
        return projectRepository.save(project);
    }

    private void archiveTasks(BooleanRequest request, Integer projectId, Integer userId, LocalDateTime now, boolean onlyCompleted) {
        List<Task> tasks = request.isFlag() ? taskRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false) :
                taskRepository.findByOwnerIdAndProjectId(userId, projectId);
        if(onlyCompleted) {
            List<Task> tasksTemp = tasks.stream()
                    .filter(Task::isCompleted)
                    .collect(Collectors.toList());
            tasksTemp.addAll(archiveChildren(tasks, tasksTemp, now, request.isFlag()));
            tasks = tasksTemp;
        }
        tasks.forEach((a) -> {
            a.setArchived(request.isFlag());
            a.setModifiedAt(now);
        });
        taskRepository.saveAll(tasks);
    }

    private void detachProjectFromTree(BooleanRequest request, Integer projectId, Integer userId, Project project, LocalDateTime now) {
        if(request.isFlag()) {
            List<Project> children = projectRepository.findByOwnerIdAndParentId(userId, projectId);
            Integer order = getMaxOrderForParent(userId, project);

            for(Project a : children) {
                a.setParent(project.getParent());
                a.setModifiedAt(now);
                a.setGeneralOrder(order++);
            }
            projectRepository.saveAll(children);
        }
    }

    private Integer getMaxOrderForParent(Integer userId, Project project) {
        Integer order;
        if(project.getParent() == null) {
            order = projectRepository.getMaxOrderByOwnerId(userId);
        } else {
            order = projectRepository.getMaxOrderByOwnerIdAndParentId(userId, project.getParent().getId());
        }
        return order;
    }

    public List<ProjectDetails> getArchivedProjects(Integer userId) {
        return projectRepository.findByOwnerIdAndArchived(userId, true, ProjectDetails.class);
    }

    public List<TaskDetails> getArchivedTasks(Integer userId, Integer projectId) {
        return taskRepository.getByOwnerIdAndProjectIdAndArchived(userId, projectId, true, TaskDetails.class);
    }

    @Transactional
    @NotifyOnProjectChange
    public Project archiveCompletedTasks(BooleanRequest request, Integer projectId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        LocalDateTime now = LocalDateTime.now();
        archiveTasks(request, projectId, userId, now, true);
        return projectRepository.save(project);
    }

    private List<Task> archiveChildren(List<Task> projectTasks, List<Task> parentTasks, LocalDateTime now, boolean archived) {
        Map<Integer, List<Task>> tasksByParent = projectTasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

        List<Task> toArchive = parentTasks;
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

    public ProjectData getProjectData(Integer projectId, Integer userId) {
        ProjectData result = new ProjectData();
        result.setProject(
                projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                        .orElseThrow(() -> new NotFoundException("No such project!"))
        );
        result.setTasks(
                taskRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false, TaskDetails.class)
        );
        result.setHabits(
                habitRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false, HabitDetails.class)
        );
        return result;
    }

    public ProjectData getProjectDataWithArchived(Integer projectId, Integer userId) {
        ProjectData result = new ProjectData();
        result.setProject(
                projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                        .orElseThrow(() -> new NotFoundException("No such project!"))
        );
        result.setTasks(
                taskRepository.findByOwnerIdAndProjectId(userId, projectId, TaskDetails.class)
        );
        result.setHabits(
                habitRepository.findByOwnerIdAndProjectId(userId, projectId, HabitDetails.class)
        );
        return result;
    }

    public Project addCollaborator(IdRequest request, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        toUpdate.getCollaborators().add(userRepository.getById(request.getId()));
        toUpdate.setCollaborative(true);
        return projectRepository.save(toUpdate);
    }

    public Project deleteCollaborator(Integer collabId, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        toUpdate.setCollaborators(
                toUpdate.getCollaborators().stream()
                        .filter((a) -> !collabId.equals(a.getId()))
                        .collect(Collectors.toSet())
        );
        if(toUpdate.getCollaborators().size() == 0) {
            toUpdate.setCollaborative(false);
        }
        return projectRepository.save(toUpdate);
    }

    public List<UserWithNameAndId> getCollaborators( Integer projectId, Integer ownerId) {
        return userRepository.getCollaboratorsByProjectIdAndOwnerId(projectId, ownerId);
    }
}
