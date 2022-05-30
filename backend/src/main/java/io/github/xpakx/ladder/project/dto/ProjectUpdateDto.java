package io.github.xpakx.ladder.project.dto;

import io.github.xpakx.ladder.project.Project;
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
    private boolean collaborative;

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
        newDto.setCollaborative(project.isCollaborative());
        return newDto;
    }
}
