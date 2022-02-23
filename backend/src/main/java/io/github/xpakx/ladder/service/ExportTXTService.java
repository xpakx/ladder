package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.LabelDetails;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExportTXTService implements ExportServiceInterface {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;

    @Override
    public InputStreamResource exportProjectList(Integer userId) {
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        StringBuilder result = new StringBuilder();
        for(ProjectDetails project : projects) {
            result.append(project.getName())
                    .append("(").append(project.getColor()).append(")")
                    .append(project.getFavorite() ? " fav:true" : "")
                    .append(project.getArchived() ? " arch:true" : "")
                    .append("\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    @Override
    public InputStreamResource exportTasksFromProjectById(Integer userId, Integer projectId) {
        List<TaskDetails> tasks = taskRepository.findByOwnerIdAndProjectId(userId, projectId, TaskDetails.class);
        StringBuilder result = new StringBuilder();
        Map<Integer, List<TaskDetails>> taskByParentId = tasks.stream()
                .filter((a) -> a.getParent() != null)
                .collect(Collectors.groupingBy((a) -> a.getParent().getId()));
        List<TaskDetails> firstOrderTasks = tasks.stream()
                .filter((a) -> a.getParent() == null)
                .sorted(Comparator.comparingInt(TaskDetails::getProjectOrder))
                .collect(Collectors.toList());
        for(TaskDetails task : firstOrderTasks) {
            addTaskToResult(result, task);
            addChildrenToResult(result, task, taskByParentId, 1);
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    private void addChildrenToResult(StringBuilder result, TaskDetails parent,
                                     Map<Integer, List<TaskDetails>> taskByParentId, Integer order) {
        List<TaskDetails> tasks = taskByParentId.get(parent.getId()).stream()
                .sorted(Comparator.comparingInt(TaskDetails::getProjectOrder))
                .collect(Collectors.toList());
        for(TaskDetails task : tasks) {
            result.append(" ".repeat(order));
            addTaskToResult(result, task);
            addChildrenToResult(result, task, taskByParentId, order+1);
        }
    }

    private void addTaskToResult(StringBuilder result, TaskDetails task) {
        result.append("[").append(task.getCompleted() ? "x" : " ").append("]")
                .append(" ")
                .append("(").append(task.getPriority()).append(")")
                .append(" ")
                .append(task.getTitle())
                .append(task.getDescription() != null && task.getDescription().length() > 0 ? ": "+ task.getDescription() : "")
                .append(" ")
                .append(getLabelList(task.getLabels()))
                .append(" ")
                .append(task.getProject() != null ? "@"+ task.getProject().getName() : "")
                .append(task.getDue() != null ? "due:"+ task.getDue() : "")
                .append("\n");
    }

    @Override
    public InputStreamResource exportTasks(Integer userId) {
        List<TaskDetails> tasks = taskRepository.findByOwnerId(userId, TaskDetails.class);
        StringBuilder result = new StringBuilder();
        for(TaskDetails task : tasks) {
            addTaskToResult(result, task);
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    private String getLabelList(Set<LabelDetails> labels) {
        return labels.stream()
                .map(LabelDetails::getName)
                .map((a) -> "+"+a)
                .collect(Collectors.joining(" "));
    }
}
