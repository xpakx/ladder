package io.github.xpakx.ladder.task.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AddTaskRequest {
    private String title;
    private String description;
    private Integer projectOrder;
    private LocalDateTime due;
    private LocalDateTime completedAt;
    private boolean timeboxed;
    private Integer parentId;
    private Integer projectId;
    private Integer priority;
    private List<Integer> labelIds;
}
