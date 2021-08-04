package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRequest {
    private String name;
    private Integer parentId;
}
