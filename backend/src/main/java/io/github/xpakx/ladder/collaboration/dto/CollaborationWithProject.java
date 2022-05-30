package io.github.xpakx.ladder.collaboration.dto;

import java.time.LocalDateTime;

public interface CollaborationWithProject {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();
    CollabProjectDetails getProject();
    LocalDateTime getModifiedAt();
}
