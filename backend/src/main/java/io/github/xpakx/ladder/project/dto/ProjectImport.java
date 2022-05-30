package io.github.xpakx.ladder.project.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectImport {
    private Integer id;
    private String name;
    private boolean favorite;
    private String color;
    private Integer generalOrder;
    private boolean collapsed;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer parentId;
}
