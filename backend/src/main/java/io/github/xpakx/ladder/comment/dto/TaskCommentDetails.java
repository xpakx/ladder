package io.github.xpakx.ladder.comment.dto;

import io.github.xpakx.ladder.user.dto.UserWithNameAndId;

import java.time.LocalDateTime;

public interface TaskCommentDetails {
    Integer getId();
    String getContent();
    LocalDateTime getCreatedAt();
    UserWithNameAndId getOwner();
}
