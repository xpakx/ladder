package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface HabitDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    Integer getGeneralOrder();
    ProjectWithNameAndId getProject();    
    boolean getAllowPositive();
    boolean getAllowNegative();
    LocalDateTime getModifiedAt();
}
