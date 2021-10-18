package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface TaskCommentDetails {
    Integer getIs();
    String getContent();
    LocalDateTime getCreatedAt();
    UserWithNameAndId getOwner();
}
