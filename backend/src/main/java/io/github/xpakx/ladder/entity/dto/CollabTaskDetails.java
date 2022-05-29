package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;
import java.util.Set;

public interface CollabTaskDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    ParentWithId getParent();
    LocalDateTime getDue();
    boolean getTimeboxed();
    boolean getCompleted();
    boolean getCollapsed();
    boolean getArchived();
    Integer getProjectOrder();
    Integer getPriority();
    LocalDateTime getModifiedAt();
    UserWithNameAndId getAssigned();
}
