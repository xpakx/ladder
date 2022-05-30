package io.github.xpakx.ladder.archive;

import io.github.xpakx.ladder.notification.NotifyOnProjectChange;
import io.github.xpakx.ladder.notification.NotifyOnTaskChange;
import io.github.xpakx.ladder.habit.Habit;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.habit.HabitRepository;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.task.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class ArchiveService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final HabitRepository habitRepository;

    /**
     * Change project archived state; if request contains false flag,
     * all project's tasks are restored from archive too.
     * If request contains true flag, all tasks are archived and children projects
     * are attached as children to archived project's parent
     * @param userId ID of an owner of the project
     * @param projectId ID of the project to archive
     * @param request request with archived state
     * @return Updated project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project archiveProject(BooleanRequest request, Integer projectId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(changeArchivedState(request.isFlag(), userId, project));
    }

    private Project changeArchivedState(boolean archived, Integer userId, Project project) {
        LocalDateTime now = LocalDateTime.now();
        project.setArchived(archived);
        project.setModifiedAt(now);
        rearrangeProjectTreeAfterArchiveStateChanged(archived, userId, project, now);
        archiveTasks(archived, project.getId(), userId, now, false);
        archiveHabits(archived, project.getId(), userId, now);
        return project;
    }

    private void rearrangeProjectTreeAfterArchiveStateChanged(boolean archived, Integer userId, Project project, LocalDateTime now) {
        if(archived) {
            detachProjectFromTree(userId, project, now);
        } else {
            restoreProjectFromArchiveAtTheEndOfTree(userId, project);
        }
    }

    private void restoreProjectFromArchiveAtTheEndOfTree(Integer userId, Project project) {
        project.setGeneralOrder(projectRepository.getMaxOrderByOwnerId(userId));
    }

    private void detachProjectFromTree(Integer userId, Project project, LocalDateTime now) {
        project.setGeneralOrder(0);
        project.setParent(null);
        reassignChildrenProjects(userId, project, now);
    }

    private void archiveHabits(boolean request, Integer projectId, Integer userId, LocalDateTime now) {
        List<Habit> habits = habitRepository.findByOwnerIdAndProjectId(userId, projectId, Habit.class);
        for(Habit habit : habits) {
            habit.setArchived(request);
            habit.setModifiedAt(now);
        }
        habitRepository.saveAll(habits);
    }

    private void archiveTasks(boolean request, Integer projectId, Integer userId, LocalDateTime now, boolean onlyCompleted) {
        List<Task> tasks = getTasksForArchivedStateChange(request, projectId, userId);
        if(onlyCompleted) {
            tasks = prepareCompletedTasks(request, now, tasks);
        }
        tasks.forEach((a) -> {
            a.setArchived(request);
            a.setModifiedAt(now);
        });
        taskRepository.saveAll(tasks);
    }

    private List<Task> prepareCompletedTasks(boolean request, LocalDateTime now, List<Task> tasks) {
        List<Task> tasksTemp = tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.toList());
        tasksTemp.addAll(archiveChildren(tasks, tasksTemp, now, request));
        return tasksTemp;
    }

    private List<Task> getTasksForArchivedStateChange(boolean request, Integer projectId, Integer userId) {
        return request ? taskRepository.findByOwnerIdAndProjectIdAndArchived(userId, projectId, false) :
                taskRepository.findByOwnerIdAndProjectId(userId, projectId);
    }

    private void reassignChildrenProjects(Integer userId, Project project, LocalDateTime now) {
        List<Project> children = projectRepository.findByOwnerIdAndParentId(userId, project.getId());
        if(children.size() > 0) {
            reassignParent(project, now, children, getMaxOrderForParent(userId, project));
            projectRepository.saveAll(children);
        }
    }

    private void reassignParent(Project project, LocalDateTime now, List<Project> children, Integer order) {
        for(Project a : children) {
            a.setParent(project.getParent());
            a.setModifiedAt(now);
            a.setGeneralOrder(order++);
        }
    }

    private Integer getMaxOrderForParent(Integer userId, Project project) {
        return isNull(project.getParent()) ? projectRepository.getMaxOrderByOwnerId(userId) : projectRepository.getMaxOrderByOwnerIdAndParentId(userId, project.getParent().getId());
    }

    /**
     * Move completed tasks in given project to archive
     * @param userId ID of an owner of the project
     * @param projectId ID of the project
     * @param request request with archived state
     * @return Updated project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project archiveCompletedTasks(BooleanRequest request, Integer projectId, Integer userId) {
        Project project = projectRepository.findByIdAndOwnerId(projectId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        LocalDateTime now = LocalDateTime.now();
        archiveTasks(request.isFlag(), projectId, userId, now, true);
        return projectRepository.save(project);
    }

    private List<Task> archiveChildren(List<Task> projectTasks, List<Task> parentTasks, LocalDateTime now, boolean archived) {
        Map<Integer, List<Task>> tasksByParent = generateMapOfTasksByParentId(projectTasks);
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

    private Map<Integer, List<Task>> generateMapOfTasksByParentId(List<Task> projectTasks) {
        return projectTasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
    }

    /**
     * Return all archived projects
     * @param userId ID of an owner of projects
     * @return List of archived projects
     */
    public List<ProjectDetails> getArchivedProjects(Integer userId) {
        return projectRepository.findByOwnerIdAndArchived(userId, true, ProjectDetails.class);
    }

    /**
     * Return all archived tasks in given project
     * @param userId ID of an owner of the project
     * @param projectId ID of the project
     * @return List of archived projects
     */
    public List<TaskDetails> getArchivedTasks(Integer userId, Integer projectId) {
        return taskRepository.getByOwnerIdAndProjectIdAndArchived(userId, projectId, true, TaskDetails.class);
    }

    /**
     * Change task archived state; change archived state for all task's children too
     * @param userId ID of an owner of the task
     * @param taskId ID of the task to archive
     * @param request request with archived state
     * @return Updated task
     */
    @Transactional
    @NotifyOnTaskChange
    public Task archiveTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task task = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
        LocalDateTime now = LocalDateTime.now();
        task.setArchived(request.isFlag());
        if(!request.isFlag()) {
            updateTaskOrder(userId, task);
        }
        task.setModifiedAt(now);
        taskRepository.saveAll(
                archiveChildren(userId, task, now, request.isFlag())
        );
        return taskRepository.save(task);
    }

    private void updateTaskOrder(Integer userId, Task task) {
        task.setProjectOrder(
                task.getProject() != null ? taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, task.getProject().getId())+1 : taskRepository.getMaxOrderByOwnerId(userId)+1
        );
    }

    private List<Task> archiveChildren(Integer userId, Task task, LocalDateTime now, boolean archived) {
        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                task.getProject() != null ? task.getProject().getId() : null);
        return archiveChildren(tasks, List.of(task), now, archived);
    }
}
