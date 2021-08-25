package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AddTaskRequest {
    private String title;
    private String description;
    private Integer projectOrder;
    private LocalDateTime due;
    private LocalDateTime completedAt;
    private Integer parentId;
    private Integer projectId;
    private Integer priority;
}