package io.github.xpakx.ladder.entity.dto;

public interface CollaborationDetails {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();

    ProjectWithNameAndUser getProject();
}
