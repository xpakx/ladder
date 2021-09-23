package io.github.xpakx.ladder.entity.dto;

public interface ProjectDetails {
    Integer getId();
    String getName();
    String getColor();
    boolean getFavorite();
    boolean getCollapsed();
    ProjectWithNameAndId getParent();
    Integer getGeneralOrder();
}
