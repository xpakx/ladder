package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ImportCSVService implements ImportServiceInterface {
    private ProjectRepository projectRepository;
    private TaskRepository taskRepository;
    private static final String DELIMITER = ",";

    @Override
    public void importProjectList(Integer userId, String csv) {

    }

    @Override
    public void importTasksFromProjectById(Integer userId, Integer projectId, String csv) {

    }

    @Override
    public void importTasks(Integer userId, String csv) {

    }
}
