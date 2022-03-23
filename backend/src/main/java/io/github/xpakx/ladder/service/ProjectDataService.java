package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.HabitRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectDataService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;

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
                List<FullProjectTree> children = getAllProjectChildrenAsTreeElements(projectByParent, parent);
                parent.setTasks(addTasksToTree(parent, tasksByParent, tasksByProject));
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
    }

    private List<FullProjectTree> getAllProjectChildrenAsTreeElements(Map<Integer, List<ProjectDetails>> projectByParent, FullProjectTree parent) {
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
                List<TaskForTree> children = getAllTaskChildrenAsTreeElements(tasksByParent, parent);
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            toAdd = newToAdd;
        }
        return result;
    }

    private List<TaskForTree> getAllTaskChildrenAsTreeElements(Map<Integer, List<TaskDetails>> tasksByParent, TaskForTree parent) {
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
}
