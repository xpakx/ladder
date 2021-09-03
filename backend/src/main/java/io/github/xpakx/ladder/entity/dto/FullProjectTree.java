package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FullProjectTree {
    Integer id;
    String name;
    List<TaskForTree> tasks;
    List<FullProjectTree> children;

    public FullProjectTree(ProjectDetails project) {
        this.id = project.getId();
        this.name = project.getName();
    }

    public FullProjectTree(ProjectMin project) {
        this.id = project.getId();
        this.name = project.getName();
    }
}
