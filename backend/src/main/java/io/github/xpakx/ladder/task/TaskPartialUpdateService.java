package io.github.xpakx.ladder.task;

import io.github.xpakx.ladder.notification.NotifyOnTaskChange;
import io.github.xpakx.ladder.common.dto.*;
import io.github.xpakx.ladder.project.Project;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.common.error.WrongOwnerException;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class TaskPartialUpdateService {
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskUpdateUtilsService utils;

    private Task getTaskFromDb(Integer taskId, Integer userId) {
        return taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such task!"));
    }

    /**
     * Change task due date and add at the end of daily list.
     * @param request Request with new due date
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task updateTaskDueDate(DateRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        if(utils.haveDifferentDueDate(request.getDate(), taskToUpdate.getDue())) {
            taskToUpdate.setDailyViewOrder(utils.getMaxDailyOrder(request, userId)+1);
        }
        taskToUpdate.setDue(request.getDate());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        taskToUpdate.setTimeboxed(request.isTimeboxed());
        return taskRepository.save(taskToUpdate);
    }

    /**
     * Change task priority
     * @param request Request with new priority
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task updateTaskPriority(PriorityRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        taskToUpdate.setPriority(request.getPriority());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    /**
     * Change task completion state (if task is completed, all subtasks will become completed too).
     * @param request Request with completion state
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task completeTask(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.getByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No task with id " + taskId));
        return request.isFlag() ? makeTaskCompleted(taskId, userId, taskToUpdate) : makeTaskUncompleted(taskToUpdate);
    }

    private Task makeTaskUncompleted(Task taskToUpdate) {
        taskToUpdate.setCompleted(false);
        taskToUpdate.setCompletedAt(null);
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    private Task makeTaskCompleted(Integer taskId, Integer userId, Task taskToUpdate) {
        taskToUpdate.setAssigned(userRepository.getById(userId));
        if(isTaskInCollaborativeProject(taskToUpdate)) {
            return saveCompletedTask(taskToUpdate);
        } else {
            return saveCompletedTaskAndSubtasks(taskId, userId, taskToUpdate);
        }
    }

    private Task saveCompletedTask(Task taskToUpdate) {
        LocalDateTime now = LocalDateTime.now();
        taskToUpdate.setCompleted(true);
        taskToUpdate.setCompletedAt(now);
        taskToUpdate.setModifiedAt(now);
        return taskRepository.save(taskToUpdate);
    }

    private Task saveCompletedTaskAndSubtasks(Integer taskId, Integer userId, Task taskToUpdate) {
        return taskRepository.saveAll(completeTask(userId, taskToUpdate)).stream()
                .filter((a) -> a.getId().equals(taskId))
                .findAny()
                .orElse(null);
    }

    private boolean isTaskInCollaborativeProject(Task task) {
        return hasProject(task) && task.getProject().isCollaborative();
    }

    private boolean hasProject(Task task) {
        return nonNull(task.getProject());
    }

    private List<Task> completeTask(Integer userId, Task task) {
        Map<Integer, List<Task>> tasksByParent = generateMapOfTasksByParentId(
                taskRepository.findByOwnerIdAndProjectId(userId, getProjectId(task))
        );
        return transformAllSubtasksToCompleted(task, tasksByParent);
    }

    private List<Task> transformAllSubtasksToCompleted(Task task, Map<Integer, List<Task>> tasksByParent) {
        List<Task> toComplete = List.of(task);
        List<Task> toReturn = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        while(toComplete.size() > 0) {
            List<Task> newToComplete = new ArrayList<>();
            for (Task parent : toComplete) {
                List<Task> children = tasksByParent.getOrDefault(parent.getId(), new ArrayList<>());
                parent.setCompleted(true);
                parent.setCompletedAt(now);
                parent.setModifiedAt(now);
                toReturn.add(parent);
                newToComplete.addAll(children);
            }
            toComplete = newToComplete;
        }
        return toReturn;
    }

    private Integer getProjectId(Task task) {
        return hasProject(task) ? task.getProject().getId() : null;
    }

    private Map<Integer, List<Task>> generateMapOfTasksByParentId(List<Task> tasks) {
        return tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
    }

    /**
     * Change task collapsed state.
     * @param request Request with collapsed state
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task updateTaskCollapsedState(BooleanRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        taskToUpdate.setCollapsed(request.isFlag());
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    /**
     * Change task's labels.
     * @param request Request with ids of labels
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task updateTaskLabels(IdCollectionRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        taskToUpdate.setLabels(utils.transformLabelIdsToLabelReferences(request.getIds(), userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    /**
     * Change task's assigned user.
     * @param request Request with id of newly assigned user
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @Transactional
    @NotifyOnTaskChange
    public Task updateAssigned(IdRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        taskToUpdate.setAssigned(getAssignedUserFromRequest(request, taskId, userId));
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    private UserAccount getAssignedUserFromRequest(IdRequest request, Integer taskId, Integer userId) {
        return isAssignedUserDifferentThanOwner(request, userId) ?
                getCollaboratorFromDb(request, taskId) : userRepository.getById(userId);
    }

    private UserAccount getCollaboratorFromDb(IdRequest request, Integer taskId) {
        return userRepository.getCollaboratorByTaskIdAndId(taskId, request.getId())
                .orElseThrow(() -> new WrongOwnerException("Given user isn't collaborator on this project!"));
    }

    private boolean isAssignedUserDifferentThanOwner(IdRequest request, Integer userId) {
        return !userId.equals(request.getId());
    }

    /**
     * Change task's project and add at the end of project's task list.
     * @param request Request with new project ID
     * @param taskId ID of the task to update
     * @param userId ID of an owner of the task
     * @return Updated task
     */
    @NotifyOnTaskChange
    public Task updateTaskProject(IdRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = getTaskFromDb(taskId, userId);
        Project project = getProjectFromDb(request, userId);
        if(!utils.haveSameProject(taskToUpdate, project)) {
            List<Task> childrenWithUpdatedProject = utils.updateChildrenProject(project, taskToUpdate, userId);
            taskRepository.saveAll(childrenWithUpdatedProject);
        }
        taskToUpdate.setParent(null);
        taskToUpdate.setProject(project);
        taskToUpdate.setProjectOrder(getMaxProjectOrder(request, userId)+1);
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
    }

    private Project getProjectFromDb(IdRequest request, Integer userId) {
        return nonNull(request.getId()) ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
    }

    private Integer getMaxProjectOrder(IdRequest request, Integer userId) {
        if(hasId(request)) {
            return taskRepository.getMaxOrderByOwnerIdAndProjectId(userId, request.getId());
        } else {
            return taskRepository.getMaxOrderByOwnerId(userId);
        }
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }
}
