package io.github.xpakx.ladder.task.dto;

import io.github.xpakx.ladder.project.dto.ProjectWithNameAndId;

import java.util.List;

public interface TaskWithChildren {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    List<TaskWithChildren> getChildren();
}
