package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.AddTaskRequest;
import io.github.xpakx.ladder.entity.dto.DateRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class TaskUpdateUtilsService {
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;

    private boolean bothNull(Object a, Object b) {
        return isNull(a) && isNull(b);
    }

    private boolean atLeastOneNull(Object a, Object b) {
        return isNull(a) || isNull(b);
    }

    public Integer getMaxDailyOrder(DateRequest request, Integer userId) {
        return getMaxDailyOrder(request.getDate(), userId);
    }

    public Integer getMaxDailyOrder(AddTaskRequest request, Integer userId) {
        return getMaxDailyOrder(request.getDue(), userId);
    }

    private Integer getMaxDailyOrder(LocalDateTime date, Integer userId) {
        if(date == null) {
            return 0;
        } else {
            return taskRepository.getMaxOrderByOwnerIdAndDate(userId, date);
        }
    }

    public boolean haveDifferentDueDate(LocalDateTime dueDate1, LocalDateTime dueDate2) {
        if(bothNull(dueDate1, dueDate2)) {
            return false;
        }
        if(atLeastOneNull(dueDate1, dueDate2)) {
            return true;
        }
        return dueDate1.getYear() != dueDate2.getYear() || dueDate1.getDayOfYear() != dueDate2.getDayOfYear();
    }

    public Set<Label> transformLabelIdsToLabelReferences(List<Integer> labelIds, Integer userId) {
        if(labelsWithDiffOwner(labelIds, userId)) {
            throw new NotFoundException("Cannot add labels you don't own!");
        }
        return labelIds != null ? labelIds.stream()
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

    public List<Task> updateChildrenProject(Project project, Task parent, Integer userId) {
        List<Task> tasksForProject = getTasksForProjectOrInbox(parent, userId)
                .stream().filter((a) -> a.getParent() != null)
                .collect(Collectors.toList());
        List<Task> children = getImminentChildren(List.of(parent), tasksForProject);
        List<Task> toUpdate = new ArrayList<>();
        while(children.size() > 0) {
            children.forEach((a) -> a.setProject(project));
            toUpdate.addAll(children);
            children = getImminentChildren(children, tasksForProject);
        }
        return toUpdate;
    }

    private List<Task> getTasksForProjectOrInbox(Task parent, Integer userId) {
        return parent.getProject() != null ? taskRepository.findByOwnerIdAndProjectId(userId, parent.getProject().getId()) :
                taskRepository.findByOwnerIdAndProjectIsNull(userId);
    }

    private List<Task> getImminentChildren(List<Task> parentList, List<Task> tasksForProject) {
        List<Integer> ids = parentList.stream().map(Task::getId).collect(Collectors.toList());
        return tasksForProject.stream()
                .filter((a) -> ids.contains(a.getParent().getId()))
                .collect(Collectors.toList());
    }

    public boolean haveSameProject(Task taskToUpdate, Project project) {
        return (
                (hasProject(taskToUpdate) && project != null && taskToUpdate.getProject().getId().equals(project.getId()))
                        ||
                        (!hasProject(taskToUpdate) && project == null)
        );
    }

    private boolean hasProject(Task task) {
        return task.getProject() != null;
    }
}
