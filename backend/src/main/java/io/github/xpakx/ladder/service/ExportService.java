package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExportService {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;

    public String exportProjectList(Integer userId) {
        List<ProjectDetails> projects = projectRepository.findByOwnerId(userId, ProjectDetails.class);
        StringBuilder result = new StringBuilder();
        result.append("id;name;color;favorite;archived;parentId;order/n");
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
                    .append(";/n");
        }
        return result.toString();
    }

}
