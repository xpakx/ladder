package io.github.xpakx.ladder.entity.dto;

import java.util.List;

public interface FullProjectTree {
    Integer getId();
    String getName();
    List<TaskWithChildren> getTasks();
}
