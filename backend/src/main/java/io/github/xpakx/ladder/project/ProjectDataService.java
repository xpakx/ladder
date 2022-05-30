package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.habit.dto.HabitDetails;
import io.github.xpakx.ladder.project.dto.FullProjectTree;
import io.github.xpakx.ladder.project.dto.ProjectData;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.project.dto.ProjectMin;
import io.github.xpakx.ladder.habit.HabitRepository;
import io.github.xpakx.ladder.task.TaskRepository;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.task.dto.TaskForTree;
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
        return addProjectsToTree(
                constructMapWithProjectsGroupedByParentId(projects),
                constructMapWithTasksGroupedByParentId(tasks),
                constructMapWithTasksGroupedByProjectId(tasks),
                project);
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

    private FullProjectTree addProjectsToTree(Map<Integer, List<ProjectDetails>> projectByParent, Map<Integer,
            List<TaskDetails>> tasksByParent, Map<Integer, List<TaskDetails>> tasksByProject, ProjectMin project) {
        FullProjectTree tree = new FullProjectTree(project);
        addProjectsToTree(projectByParent, tasksByParent, tasksByProject, List.of(tree));
        return tree;
    }

    private List<FullProjectTree> addProjectsToTree(Map<Integer, List<ProjectDetails>> projectByParent, Map<Integer,
            List<TaskDetails>> tasksByParent, Map<Integer, List<TaskDetails>> tasksByProject,
                                   List<FullProjectTree> toAdd) {
        List<FullProjectTree> temp = toAdd;
        while(temp.size() > 0) {
            List<FullProjectTree> newToAdd = new ArrayList<>();
            for (FullProjectTree parent : temp) {
                List<FullProjectTree> children = getAllProjectChildrenAsTreeElements(projectByParent, parent);
                parent.setTasks(addTasksToTree(parent, tasksByParent, tasksByProject));
                parent.setChildren(children);
                newToAdd.addAll(children);
            }
            temp = newToAdd;
        }
        return toAdd;
    }

    private List<FullProjectTree> getAllProjectChildrenAsTreeElements(Map<Integer, List<ProjectDetails>> projectByParent, FullProjectTree parent) {
        return projectByParent
                .getOrDefault(parent.getId(), new ArrayList<>()).stream()
                .map(FullProjectTree::new)
                .collect(Collectors.toList());
    }

    private List<TaskForTree> addTasksToTree(FullProjectTree project, Map<Integer, List<TaskDetails>> tasksByParent,
                                             Map<Integer, List<TaskDetails>> tasksByProject) {
        List<TaskForTree> toAdd = getAllProjectTasksAsTreeElements(tasksByProject, project.getId());
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

    private List<TaskForTree> getAllProjectTasksAsTreeElements(Map<Integer, List<TaskDetails>> tasksByProject, Integer projectId) {
        return tasksByProject.getOrDefault(projectId, new ArrayList<>()).stream()
                .map(TaskForTree::new)
                .collect(Collectors.toList());
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
        return addProjectsToTree(
                constructMapWithProjectsGroupedByParentId(projects),
                constructMapWithTasksGroupedByParentId(tasks),
                constructMapWithTasksGroupedByProjectId(tasks),
                transformProjectsToTreeElements(projects)
        );
    }

    private List<FullProjectTree> transformProjectsToTreeElements(List<ProjectDetails> projects) {
        return projects.stream()
                .filter((a) -> a.getParent() == null)
                .map(FullProjectTree::new)
                .collect(Collectors.toList());
    }

    private Map<Integer, List<TaskDetails>> constructMapWithTasksGroupedByParentId(List<TaskDetails> tasks) {
        return tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
    }

    /**
     * Get project with all unarchived tasks and habits
     * @param projectId Project id
     * @param userId ID of an owner of the project
     * @return Object with project and all task and habits
     */
    public ProjectData getProjectData(Integer projectId, Integer userId) {
        ProjectData result = new ProjectData();
        result.setProject(getProjectFromDb(projectId, userId));
        result.setTasks(
                taskRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false, TaskDetails.class)
        );
        result.setHabits(
                habitRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false, HabitDetails.class)
        );
        return result;
    }

    private ProjectDetails getProjectFromDb(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    /**
     * Get project with all tasks and habits, including archived objects
     * @param projectId Project id
     * @param userId ID of an owner of the project
     * @return Object with project and all task and habits
     */
    public ProjectData getProjectDataWithArchived(Integer projectId, Integer userId) {
        ProjectData result = new ProjectData();
        result.setProject(getProjectFromDb(projectId, userId));
        result.setTasks(
                taskRepository.findByOwnerIdAndProjectId(userId, projectId, TaskDetails.class)
        );
        result.setHabits(
                habitRepository.findByOwnerIdAndProjectId(userId, projectId, HabitDetails.class)
        );
        return result;
    }
}
