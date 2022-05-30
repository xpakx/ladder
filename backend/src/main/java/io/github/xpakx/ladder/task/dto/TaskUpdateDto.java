package io.github.xpakx.ladder.task.dto;

import io.github.xpakx.ladder.task.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskUpdateDto {
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime due;
    private boolean timeboxed;
    private LocalDateTime completedAt;
    private boolean completed;
    private boolean collapsed;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer projectOrder;
    private Integer dailyViewOrder;
    private Integer priority;

    public static TaskUpdateDto from(Task task) {
        TaskUpdateDto newDto = new TaskUpdateDto();
        newDto.setId(task.getId());
        newDto.setTitle(task.getTitle());
        newDto.setDescription(task.getDescription());
        newDto.setDue(task.getDue());
        newDto.setTimeboxed(task.isTimeboxed());
        newDto.setCompletedAt(task.getCompletedAt());
        newDto.setCompleted(task.isCompleted());
        newDto.setCollapsed(task.isCollapsed());
        newDto.setArchived(task.isArchived());;
        newDto.setCreatedAt(task.getCreatedAt());
        newDto.setModifiedAt(task.getModifiedAt());
        newDto.setProjectOrder(task.getProjectOrder());
        newDto.setDailyViewOrder(task.getDailyViewOrder());
        newDto.setPriority(task.getPriority());
        return newDto;
    }
}
