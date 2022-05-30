package io.github.xpakx.ladder.filter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterRequest {
    private String name;
    private String color;
    private boolean favorite;
    private String searchString;
}
