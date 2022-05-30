package io.github.xpakx.ladder.project.dto;

import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TasksAndProjects {
    private List<ProjectDetails> projects;
    private List<TaskDetails> tasks;
}
