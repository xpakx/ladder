package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project getProjectById(Integer projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow();
    }

    public Project addProject(ProjectRequest request) {
        Project parent = null;
        if(request.getParentId() != null) {
            parent = projectRepository.getById(request.getParentId());
        }
        Project projectToAdd = Project.builder()
                .name(request.getName())
                .parent(parent)
                .build();
        return projectRepository.save(projectToAdd);
    }
}
