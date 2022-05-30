package io.github.xpakx.ladder.task.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequest {
    private String title;
    private String description;
    private Integer order;
    private LocalDateTime due;
    private boolean timeboxed;
    private Integer parentId;
}
