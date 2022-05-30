package io.github.xpakx.ladder.imports;

import io.github.xpakx.ladder.label.dto.LabelDetails;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import io.github.xpakx.ladder.project.ProjectRepository;
import io.github.xpakx.ladder.task.TaskRepository;
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
        transformProjectTasksToTXT(tasks, result);
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    private void transformProjectTasksToTXT(List<TaskDetails> tasks, StringBuilder result) {
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
    }

    private void addChildrenToResult(StringBuilder result, TaskDetails parent,
                                     Map<Integer, List<TaskDetails>> taskByParentId, Integer order) {
        if(taskByParentId.get(parent.getId()) == null) {
            return;
        }
        List<TaskDetails> tasks = taskByParentId.get(parent.getId()).stream()
                .sorted(Comparator.comparingInt(TaskDetails::getProjectOrder))
                .collect(Collectors.toList());
        for(TaskDetails task : tasks) {
            result.append("\t".repeat(order));
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
        Map<Integer, List<TaskDetails>> taskByProjectId = tasks.stream()
                .filter((a) -> a.getProject() != null)
                .collect(Collectors.groupingBy((a) -> a.getProject().getId()));
        List<TaskDetails> inboxTasks = tasks.stream()
                .filter((a) -> a.getProject() == null)
                .sorted(Comparator.comparingInt(TaskDetails::getProjectOrder))
                .collect(Collectors.toList());
        StringBuilder result = new StringBuilder();
        transformProjectTasksToTXT(inboxTasks, result);
        for(List<TaskDetails> taskList : taskByProjectId.values()) {
            transformProjectTasksToTXT(taskList, result);
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
