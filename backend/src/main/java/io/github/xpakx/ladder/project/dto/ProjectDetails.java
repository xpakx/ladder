package io.github.xpakx.ladder.project.dto;

import java.time.LocalDateTime;

public interface ProjectDetails {
    Integer getId();
    String getName();
    String getColor();
    boolean getFavorite();
    boolean getCollapsed();
    boolean getCollaborative();
    boolean getArchived();
    ProjectWithNameAndId getParent();
    Integer getGeneralOrder();
    LocalDateTime getModifiedAt();
}
