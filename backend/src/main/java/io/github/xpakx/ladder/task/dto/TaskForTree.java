package io.github.xpakx.ladder.task.dto;

import io.github.xpakx.ladder.project.dto.ProjectWithNameAndId;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TaskForTree {
    Integer id;
    String title;
    String description;
    ProjectWithNameAndId project;
    List<TaskForTree> children;

    public TaskForTree(TaskDetails task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.project = task.getProject();
    }
}
