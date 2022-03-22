package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnProjectChange;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.NameRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.ProjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ProjectPartialUpdateService {
    private final ProjectRepository projectRepository;

    /**
     * Change project's name without editing any other field.
     * @param request request with new name
     * @param projectId ID of the project do update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectName(NameRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .map((p) -> transformWithRequestData(p, request))
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(projectToUpdate);
    }

    private Project transformWithRequestData(Project project, NameRequest request) {
        project.setName(request.getName());
        project.setModifiedAt(LocalDateTime.now());
        return project;
    }

    /**
     * Change project's parent without editing any other field.
     * @param request Request with parent id
     * @param projectId ID of the project to update
     * @param userId ID an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectParent(IdRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .map((p) -> transformWithRequestData(p, request))
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(projectToUpdate);
    }

    private Project transformWithRequestData(Project project, IdRequest request) {
        project.setParent(getIdFromIdRequest(request));
        project.setModifiedAt(LocalDateTime.now());
        return project;
    }

    private Project getIdFromIdRequest(IdRequest request) {
        return hasId(request) ? projectRepository.getById(request.getId()) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    /**
     * Change if project is favorite without editing any other field.
     * @param request Request with favorite flag
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectFav(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .map((p) -> transformWithFavData(p, request))
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(projectToUpdate);
    }

    private Project transformWithFavData(Project project, BooleanRequest request) {
        project.setFavorite(request.isFlag());
        project.setModifiedAt(LocalDateTime.now());
        return project;
    }

    /**
     * Change if project is collapsed without editing any other field.
     * @param request Request with collapse flag
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Updated project
     */
    @NotifyOnProjectChange
    public Project updateProjectCollapsedState(BooleanRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .map((p) -> transformWithCollapseData(p, request))
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(projectToUpdate);
    }

    private Project transformWithCollapseData(Project project, BooleanRequest request) {
        project.setCollapsed(request.isFlag());
        project.setModifiedAt(LocalDateTime.now());
        return project;
    }
}
