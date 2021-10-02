package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelRequest {
    private String name;
    private String color;
    private boolean favorite;
}
