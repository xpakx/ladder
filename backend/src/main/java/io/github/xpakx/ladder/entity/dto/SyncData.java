package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SyncData {
    private List<ProjectDetails> projects;
    private List<TaskDetails> tasks;
    private List<LabelDetails> labels;
    private List<HabitDetails> habits;
    private List<HabitCompletionDetails> habitCompletions;
    private List<FilterDetails> filters;
    private List<CollaborationWithProject> collabs;
    private List<CollabTaskDetails> collabTasks;
}
