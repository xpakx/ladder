package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;
import java.util.Set;

public interface TaskDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    ParentWithId getParent();
    LocalDateTime getDue();
    boolean getCompleted();
    boolean getCollapsed();
    boolean getArchived();
    Integer getProjectOrder();
    Integer getDailyViewOrder();
    Set<LabelDetails> getLabels();
    Integer getPriority();
    LocalDateTime getModifiedAt();
}
