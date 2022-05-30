package io.github.xpakx.ladder.label.dto;

import java.time.LocalDateTime;

public interface LabelDetails {
    Integer getId();
    String getName();
    String getColor();
    boolean getFavorite();
    Integer getGeneralOrder();
    LocalDateTime getModifiedAt();
}
