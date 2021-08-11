package io.github.xpakx.ladder.entity.dto;

import java.util.List;

public class FullTree {
    private List<FullProjectTree> projects;

    public FullTree(List<FullProjectTree> projects) {
        this.projects = projects;
    }
}
