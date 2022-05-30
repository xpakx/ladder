package io.github.xpakx.ladder.habit.dto;

import io.github.xpakx.ladder.label.dto.LabelDetails;
import io.github.xpakx.ladder.project.dto.ProjectWithNameAndId;

import java.time.LocalDateTime;
import java.util.Set;

public interface HabitDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    Integer getGeneralOrder();
    ProjectWithNameAndId getProject();
    boolean getAllowPositive();
    boolean getAllowNegative();
    LocalDateTime getModifiedAt();
    Integer getPriority();
    Set<LabelDetails> getLabels();
}
