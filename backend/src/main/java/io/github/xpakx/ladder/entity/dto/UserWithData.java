package io.github.xpakx.ladder.entity.dto;

import java.util.List;

public interface UserWithData {
    Integer getId();
    String getUsername();
    List<ProjectDetails> getProjects();
    List<TaskDetails> getTasks();
    List<LabelDetails> getLabels();
}
