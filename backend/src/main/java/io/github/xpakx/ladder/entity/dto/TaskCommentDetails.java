package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface TaskCommentDetails {
    Integer getId();
    String getContent();
    LocalDateTime getCreatedAt();
    UserWithNameAndId getOwner();
}
