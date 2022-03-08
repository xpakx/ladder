package io.github.xpakx.ladder.entity.dto;

public interface CollaborationWithOwner {
    Integer getId();
    boolean getTaskCompletionAllowed();
    boolean getEditionAllowed();
    UserWithNameAndId getOwner();
}
