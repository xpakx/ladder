package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HabitRequest {
    private String title;
    private String description;
    private Integer priority;
    private boolean positive;
}
