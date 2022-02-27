package io.github.xpakx.ladder.entity.dto;

import java.time.LocalDateTime;

public interface CollabProjectDetails {
    Integer getId();
    String getName();
    String getColor();
    boolean getFavorite();
    LocalDateTime getModifiedAt();
}
