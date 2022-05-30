package io.github.xpakx.ladder.project.dto;

import io.github.xpakx.ladder.habit.dto.HabitDetails;
import io.github.xpakx.ladder.task.dto.TaskDetails;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectData {
    private ProjectDetails project;
    private List<TaskDetails> tasks;
    private List<HabitDetails> habits;
}
