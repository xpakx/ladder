package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
import io.github.xpakx.ladder.entity.dto.TaskDetails;
import io.github.xpakx.ladder.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void deleteTask(Integer taskId) {
        this.taskRepository.deleteById(taskId);
    }

    public TaskDetails getTaskById(Integer taskId) {
        return taskRepository.findProjectedById(taskId, TaskDetails.class)
                .orElseThrow();
    }
}
