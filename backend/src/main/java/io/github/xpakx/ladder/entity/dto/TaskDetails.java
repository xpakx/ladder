package io.github.xpakx.ladder.entity.dto;

public interface TaskDetails {
    Integer getId();
    String getTitle();
    String getDescription();
    ProjectWithNameAndId getProject();
    ParentWithId getParent();
}
