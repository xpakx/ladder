package io.github.xpakx.ladder.imports;

public interface ImportServiceInterface {
    void importProjectList(Integer userId, String csv);
    void importTasksToProjectById(Integer userId, Integer projectId, String csv);
    void importTasks(Integer userId, String csv);
}
