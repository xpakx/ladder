package io.github.xpakx.ladder.entity.dto;

import io.github.xpakx.ladder.entity.Project;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectUpdateDto {
    private Integer id;
    private String name;
    private boolean favorite;
    private String color;
    private Integer generalOrder;
    private boolean collapsed;
    private boolean archived;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static ProjectUpdateDto from(Project project) {
        ProjectUpdateDto newDto = new ProjectUpdateDto();
        newDto.setId(project.getId());
        newDto.setName(project.getName());
        newDto.setFavorite(project.isFavorite());
        newDto.setColor(project.getColor());
        newDto.setGeneralOrder(project.getGeneralOrder());
        newDto.setCollapsed(project.isCollapsed());
        newDto.setArchived(project.isArchived());
        newDto.setCreatedAt(project.getCreatedAt());
        newDto.setModifiedAt(project.getModifiedAt());
        return newDto;
    }
}
