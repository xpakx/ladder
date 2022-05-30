package io.github.xpakx.ladder.filter.dto;

import java.time.LocalDateTime;

public interface FilterDetails {
    Integer getId();
    String getName();
    String getColor();
    String getSearchString();
    boolean getFavorite();
    Integer getGeneralOrder();
    LocalDateTime getModifiedAt();
}
