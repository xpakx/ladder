package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.LabelDetails;
import io.github.xpakx.ladder.entity.dto.ListOfProjectsTasksAndLabels;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final LabelRepository labelRepository;

    @Autowired
    public MainService(ProjectRepository projectRepository, TaskRepository taskRepository, LabelRepository labelRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.labelRepository = labelRepository;
    }

    public ListOfProjectsTasksAndLabels getAll(Integer userId) {
        ListOfProjectsTasksAndLabels result = new ListOfProjectsTasksAndLabels();
        result.setProjects(projectRepository.findByOwnerId(userId, ProjectDetails.class));
        result.setTasks(taskRepository.findByOwnerId(userId, TaskDetails.class));
        result.setLabels(labelRepository.findByOwnerId(userId, LabelDetails.class));
        return null;
    }
}
