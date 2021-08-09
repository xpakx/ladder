package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FullProjectTree {
    private Integer id;
    private String name;
    private ProjectWithNameAndId parent;
    private List<TaskWithChildren> tasks;

    public FullProjectTree(ProjectDetails project, List<TaskWithChildren> tasks) {
        this.id = project.getId();
        this.name = project.getName();
        this.parent = project.getParent();
        this.tasks = tasks;
    }
}
