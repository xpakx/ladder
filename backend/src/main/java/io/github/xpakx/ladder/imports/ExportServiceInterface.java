package io.github.xpakx.ladder.imports;

import org.springframework.core.io.InputStreamResource;

public interface ExportServiceInterface {
    InputStreamResource exportProjectList(Integer userId);
    InputStreamResource exportTasksFromProjectById(Integer userId, Integer projectId);
    InputStreamResource exportTasks(Integer userId);
}
