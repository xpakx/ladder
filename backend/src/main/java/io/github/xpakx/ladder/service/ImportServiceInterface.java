package io.github.xpakx.ladder.service;

public interface ImportServiceInterface {
    void importProjectList(Integer userId, String csv);
    void importTasksFromProjectById(Integer userId, Integer projectId, String csv);
    void importTasks(Integer userId, String csv);
}
