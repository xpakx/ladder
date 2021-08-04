package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.ProjectDetails;
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

    public ProjectDetails getProjectById(Integer projectId) {
        return projectRepository.findProjectedById(projectId, ProjectDetails.class)
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

    public Project updateProject(ProjectRequest request, Integer projectId) {
        Project projectToUpdate = projectRepository.getById(projectId);
        projectToUpdate.setName(request.getName());
        projectToUpdate.setParent(
                request.getParentId() != null ? projectRepository.getById(request.getParentId()) : null
        );
        return projectRepository.save(projectToUpdate);
    }

    public void deleteProject(Integer projectId) {
        projectRepository.deleteById(projectId);
    }
}
