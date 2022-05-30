package io.github.xpakx.ladder.project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    private String name;
    private String color;
    private boolean favorite;
    private Integer parentId;
}
