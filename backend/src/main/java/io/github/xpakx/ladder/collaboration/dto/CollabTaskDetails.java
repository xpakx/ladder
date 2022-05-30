package io.github.xpakx.ladder.collaboration.dto;

import io.github.xpakx.ladder.common.dto.ParentWithId;
import io.github.xpakx.ladder.user.dto.UserWithNameAndId;
import io.github.xpakx.ladder.project.dto.ProjectWithNameAndId;

import java.time.LocalDateTime;

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
