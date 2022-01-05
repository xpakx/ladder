package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface ProjectDetails {
    Integer getId();
    String getName();
    String getColor();
    boolean getFavorite();
    boolean getCollapsed();
    boolean getArchived();
    ProjectWithNameAndId getParent();
    Integer getGeneralOrder();
    LocalDateTime getModifiedAt();
}
