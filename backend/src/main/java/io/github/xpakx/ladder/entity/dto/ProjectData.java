package io.github.xpakx.ladder.entity.dto;

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
