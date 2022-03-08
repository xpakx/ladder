package io.github.xpakx.ladder.entity.dto;

public interface ProjectWithNameAndUser {
    Integer getId();
    String getName();
    UserWithNameAndId getOwner();
}
