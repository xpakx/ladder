package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnTaskChange;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongOwnerException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        if(request.isFlag()) {
            taskToUpdate.setAssigned(userRepository.getById(userId));
            if(taskToUpdate.getProject()!=null && taskToUpdate.getProject().isCollaborative()) {
                LocalDateTime now = LocalDateTime.now();
                taskToUpdate.setCompleted(true);
                taskToUpdate.setCompletedAt(now);
                taskToUpdate.setModifiedAt(now);
                return taskRepository.save(taskToUpdate);
            } else {
                return taskRepository.saveAll(completeTask(userId, taskToUpdate)).stream()
                        .filter((a) -> a.getId().equals(taskId))
                        .findAny()
                        .orElse(null);
            }
        } else {
            taskToUpdate.setCompleted(false);
            taskToUpdate.setCompletedAt(null);
            taskToUpdate.setModifiedAt(LocalDateTime.now());
        }
        return taskRepository.save(taskToUpdate);
    }

    private List<Task> completeTask(Integer userId, Task task) {
        List<Task> tasks = taskRepository.findByOwnerIdAndProjectId(userId,
                task.getProject() != null ? task.getProject().getId() : null);
        Map<Integer, List<Task>> tasksByParent = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));

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

    @Transactional
    @NotifyOnTaskChange
    public Task updateAssigned(IdRequest request, Integer taskId, Integer userId) {
        Task taskToUpdate = taskRepository.findByIdAndOwnerId(taskId, userId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        UserAccount assigned = !userId.equals(request.getId()) ?
                userRepository.getCollaboratorByTaskIdAndId(taskId, request.getId())
                        .orElseThrow(() -> new WrongOwnerException("Given user isn't collaborator on this project!"))
                :
                userRepository.getById(userId);
        taskToUpdate.setAssigned(assigned);
        taskToUpdate.setModifiedAt(LocalDateTime.now());
        return taskRepository.save(taskToUpdate);
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
        Project project = request.getId() != null ? projectRepository.findByIdAndOwnerId(request.getId(), userId)
                .orElseThrow(() -> new NotFoundException("No such project!")) : null;
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
