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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExportCSVService implements ExportServiceInterface {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private static final String DELIMITER = ",";

    public InputStreamResource exportProjectList(Integer userId) {
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id").append(DELIMITER).append("name").append(DELIMITER)
                .append("color").append(DELIMITER).append("favorite").append(DELIMITER)
                .append("archived").append(DELIMITER).append("parentId").append(DELIMITER).append("order")
                .append(DELIMITER).append("\n");
        for(ProjectDetails project : projects) {
            result.append(project.getId())
                    .append(DELIMITER)
                    .append(prepareString(project.getName()))
                    .append(DELIMITER)
                    .append(project.getColor())
                    .append(DELIMITER)
                    .append(project.getFavorite())
                    .append(DELIMITER)
                    .append(project.getArchived())
                    .append(DELIMITER)
                    .append(project.getParent() != null ? project.getParent().getId() : "")
                    .append(DELIMITER)
                    .append(project.getGeneralOrder())
                    .append(DELIMITER)
                    .append("\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    public InputStreamResource exportTasksFromProjectById(Integer userId, Integer projectId) {
        List<TaskDetails> tasks = taskRepository.findByOwnerIdAndProjectId(userId, projectId, TaskDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id").append(DELIMITER).append("title").append(DELIMITER)
                .append("description").append(DELIMITER).append("parent_id").append(DELIMITER)
                .append("due").append(DELIMITER).append("completed").append(DELIMITER)
                .append("collapsed").append(DELIMITER).append("archived").append(DELIMITER)
                .append("project_order").append(DELIMITER)
                .append("daily_order").append(DELIMITER).append("priority").append(DELIMITER)
                .append("labels").append(DELIMITER).append("\n");
        for(TaskDetails task : tasks) {
            result.append(task.getId())
                    .append(DELIMITER)
                    .append(prepareString(task.getTitle()))
                    .append(DELIMITER)
                    .append(prepareString(task.getDescription()))
                    .append(DELIMITER)
                    .append(task.getParent() != null ? task.getParent().getId() : "")
                    .append(DELIMITER)
                    .append(task.getDue())
                    .append(DELIMITER)
                    .append(task.getCompleted())
                    .append(DELIMITER)
                    .append(task.getCollapsed())
                    .append(DELIMITER)
                    .append(task.getArchived())
                    .append(DELIMITER)
                    .append(task.getProjectOrder())
                    .append(DELIMITER)
                    .append(task.getDailyViewOrder())
                    .append(DELIMITER)
                    .append(task.getPriority())
                    .append(DELIMITER)
                    .append(prepareString(getLabelList(task.getLabels())))
                    .append(DELIMITER)
                    .append("\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    private String getLabelList(Set<LabelDetails> labels) {
        return labels.stream()
                .map(LabelDetails::getName)
                .collect(Collectors.joining(","));
    }

    public InputStreamResource exportTasks(Integer userId) {
        List<TaskDetails> tasks = taskRepository.findByOwnerId(userId, TaskDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id").append(DELIMITER).append("title").append(DELIMITER)
                .append("description").append(DELIMITER).append("parent_id").append(DELIMITER)
                .append("due").append(DELIMITER).append("completed").append(DELIMITER)
                .append("collapsed").append(DELIMITER).append("archived").append(DELIMITER)
                .append("project_order").append(DELIMITER)
                .append("daily_order").append(DELIMITER).append("priority").append(DELIMITER)
                .append("labels").append(DELIMITER).append("project_id").append(DELIMITER)
                .append("project_name").append(DELIMITER).append("\n");
        for(TaskDetails task : tasks) {
            result.append(task.getId())
                    .append(DELIMITER)
                    .append(prepareString(task.getTitle()))
                    .append(DELIMITER)
                    .append(prepareString(task.getDescription()))
                    .append(DELIMITER)
                    .append(task.getParent() != null ? task.getParent().getId() : "")
                    .append(DELIMITER)
                    .append(task.getDue())
                    .append(DELIMITER)
                    .append(task.getCompleted())
                    .append(DELIMITER)
                    .append(task.getCollapsed())
                    .append(DELIMITER)
                    .append(task.getArchived())
                    .append(DELIMITER)
                    .append(task.getProjectOrder())
                    .append(DELIMITER)
                    .append(task.getDailyViewOrder())
                    .append(DELIMITER)
                    .append(task.getPriority())
                    .append(DELIMITER)
                    .append(prepareString(getLabelList(task.getLabels())))
                    .append(DELIMITER)
                    .append(task.getProject() != null ? task.getProject().getId() : "")
                    .append(DELIMITER)
                    .append(task.getProject() != null ? prepareString(task.getProject().getName()) : "")
                    .append(DELIMITER)
                    .append("\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    private String prepareString(String s) {
        return "\"" +  s.replaceAll("\"", "\"\"") + "\"";
    }
}
