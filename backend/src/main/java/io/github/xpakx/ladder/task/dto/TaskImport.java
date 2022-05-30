package io.github.xpakx.ladder.task.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskImport {
    private Integer id;
    private String title;
    private String description;
    private Integer parentId;
    private LocalDateTime due;
    private boolean completed;
    private boolean collapsed;
    private boolean archived;
    private Integer projectOrder;
    private Integer dailyOrder;
    private Integer priority;
    private Set<String> labels;
    private Integer projectId;
    private String projectName;
}
