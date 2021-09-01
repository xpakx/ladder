package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ListOfProjectsTasksAndLabels {
    List<ProjectDetails> projects;
    List<TaskDetails> tasks;
    List<LabelDetails> labels;
}
