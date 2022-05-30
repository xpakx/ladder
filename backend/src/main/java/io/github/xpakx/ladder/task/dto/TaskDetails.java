package io.github.xpakx.ladder.task.dto;

import io.github.xpakx.ladder.label.dto.LabelDetails;
import io.github.xpakx.ladder.common.dto.ParentWithId;
import io.github.xpakx.ladder.user.dto.UserWithNameAndId;
import io.github.xpakx.ladder.project.dto.ProjectWithNameAndId;

import java.time.LocalDateTime;
import java.util.Set;

public interface TaskDetails {
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
    Integer getDailyViewOrder();
    Set<LabelDetails> getLabels();
    Integer getPriority();
    LocalDateTime getModifiedAt();
    UserWithNameAndId getAssigned();
}
