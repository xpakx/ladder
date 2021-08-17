package io.github.xpakx.ladder.entity.dto;

import java.util.List;

public interface TaskWithChildren {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    List<TaskWithChildren> getChildren();
}
