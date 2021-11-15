package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserWithData {
    private Integer id;
    private String username;
    private boolean projectCollapsed;
    private List<ProjectDetails> projects;
    private List<TaskDetails> tasks;
    private List<LabelDetails> labels;
    private List<HabitDetails> habits;
    private List<HabitCompletionDetails> todayHabitCompletions;
}
