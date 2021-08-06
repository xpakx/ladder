package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTaskRequest {
    private String title;
    private String description;
    private Integer order;
    private LocalDateTime due;
    private Integer parentId;
    private Integer projectId;
}
