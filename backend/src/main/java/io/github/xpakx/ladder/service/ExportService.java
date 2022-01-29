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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExportService {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;

    public InputStreamResource exportProjectList(Integer userId) {
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id;name;color;favorite;archived;parentId;order\n");
        for(ProjectDetails project : projects) {
            result.append(project.getId())
                    .append(";")
                    .append(project.getName())
                    .append(";")
                    .append(project.getColor())
                    .append(";")
                    .append(project.getFavorite())
                    .append(";")
                    .append(project.getArchived())
                    .append(";")
                    .append(project.getParent().getId())
                    .append(";")
                    .append(project.getGeneralOrder())
                    .append(";\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }

    public InputStreamResource exportTasksFromProjectById(Integer userId, Integer projectId) {
        List<TaskDetails> tasks = taskRepository.findByOwnerIdAndProjectId(userId, projectId, TaskDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id;title;description;parent_id;due;completed;collapsed;project_order;daily_order;priority;labels\n");
        for(TaskDetails task : tasks) {
            result.append(task.getId())
                    .append(";")
                    .append(task.getTitle())
                    .append(";")
                    .append(task.getParent().getId())
                    .append(";")
                    .append(task.getDue())
                    .append(";")
                    .append(task.getCompleted())
                    .append(";")
                    .append(task.getCollapsed())
                    .append(";")
                    .append(task.getProjectOrder())
                    .append(";")
                    .append(task.getDailyViewOrder())
                    .append(";")
                    .append(task.getPriority())
                    .append(";")
                    .append(getLabelList(task.getLabels()))
                    .append(";\n");
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
        result.append("id;title;description;parent_id;due;completed;collapsed;project_order;daily_order;priority;labels;project_id;project_name\n");
        for(TaskDetails task : tasks) {
            result.append(task.getId())
                    .append(";")
                    .append(task.getTitle())
                    .append(";")
                    .append(task.getParent().getId())
                    .append(";")
                    .append(task.getDue())
                    .append(";")
                    .append(task.getCompleted())
                    .append(";")
                    .append(task.getCollapsed())
                    .append(";")
                    .append(task.getProjectOrder())
                    .append(";")
                    .append(task.getDailyViewOrder())
                    .append(";")
                    .append(task.getPriority())
                    .append(";")
                    .append(getLabelList(task.getLabels()))
                    .append(";")
                    .append(task.getProject().getId())
                    .append(";")
                    .append(task.getProject().getName())
                    .append(";\n");
        }
        InputStream stream = new ByteArrayInputStream(result.toString().getBytes());
        return new InputStreamResource(stream);
    }
}
