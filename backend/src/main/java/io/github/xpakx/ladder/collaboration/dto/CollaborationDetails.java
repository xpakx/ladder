package io.github.xpakx.ladder.collaboration.dto;

import io.github.xpakx.ladder.project.dto.ProjectWithNameAndUser;

public interface CollaborationDetails {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();

    ProjectWithNameAndUser getProject();
}
