package io.github.xpakx.ladder.entity.dto;

public interface CollaborationWithProject {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();
    CollabProjectDetails getProject();
}
