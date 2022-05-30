package io.github.xpakx.ladder.collaboration.dto;

import io.github.xpakx.ladder.user.dto.UserWithNameAndId;

public interface CollaborationWithOwner {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();
    UserWithNameAndId getOwner();
}
