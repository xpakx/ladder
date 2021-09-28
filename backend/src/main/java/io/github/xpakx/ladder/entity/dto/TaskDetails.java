package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface TaskDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    ParentWithId getParent();
    LocalDateTime getDue();
    boolean getCompleted();
    boolean getCollapsed();
    Integer getProjectOrder();
}
