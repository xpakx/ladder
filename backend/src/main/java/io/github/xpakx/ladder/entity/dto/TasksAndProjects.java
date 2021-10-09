package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TasksAndProjects {
    private List<ProjectDetails> projects;
    private List<TaskDetails> tasks;
}
