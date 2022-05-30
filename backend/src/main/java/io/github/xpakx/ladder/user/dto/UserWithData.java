package io.github.xpakx.ladder.user.dto;

import io.github.xpakx.ladder.collaboration.dto.CollabTaskDetails;
import io.github.xpakx.ladder.collaboration.dto.CollaborationWithProject;
import io.github.xpakx.ladder.filter.dto.FilterDetails;
import io.github.xpakx.ladder.habit.dto.HabitCompletionDetails;
import io.github.xpakx.ladder.habit.dto.HabitDetails;
import io.github.xpakx.ladder.label.dto.LabelDetails;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.task.dto.TaskDetails;
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
    private List<FilterDetails> filters;
    private List<CollaborationWithProject> collabs;
    private List<CollabTaskDetails> collabTasks;
}
