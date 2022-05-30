package io.github.xpakx.ladder.project.dto;

import io.github.xpakx.ladder.user.dto.UserWithNameAndId;

public interface ProjectWithNameAndUser {
    Integer getId();
    String getName();
    UserWithNameAndId getOwner();
}
