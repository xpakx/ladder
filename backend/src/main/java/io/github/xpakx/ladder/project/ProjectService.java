package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.common.error.WrongOwnerException;
import io.github.xpakx.ladder.notification.NotifyOnProjectChange;
import io.github.xpakx.ladder.notification.NotifyOnProjectDeletion;
import io.github.xpakx.ladder.project.dto.ProjectDetails;
import io.github.xpakx.ladder.project.dto.ProjectRequest;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserAccountRepository userRepository;

    /**
     * Getting object with project's data from repository.
     * @param projectId ID of the project to get
     * @param userId ID of an owner of the project
     * @return Object with project's details
     */
    public ProjectDetails getProjectById(Integer projectId, Integer userId) {
        return projectRepository.findProjectedByIdAndOwnerId(projectId, userId, ProjectDetails.class)
                .orElseThrow(() -> new NotFoundException("No such project!"));
    }

    /**
     * Adding new project to repository.
     * @param request Data to build new projects
     * @param userId ID of an owner of the newly created project
     * @return Created project
     */
    @Transactional
    @NotifyOnProjectChange
    public Project addProject(ProjectRequest request, Integer userId) {
        Project projectToAdd = buildProjectToAddFromRequest(request, userId);
        projectToAdd.setGeneralOrder(getMaxGeneralOrder(request, userId)+1);
        return projectRepository.save(projectToAdd);
    }

    private Integer getMaxGeneralOrder(ProjectRequest request, Integer userId) {
        return hasParent(request) ? getMaxOrderFromDb(userId, request.getParentId()) : getMaxOrderFromDb(userId);
    }

    private Integer getMaxOrderFromDb(Integer userId, Integer parentId) {
        return projectRepository.getMaxOrderByOwnerIdAndParentId(userId, parentId);
    }

    private Integer getMaxOrderFromDb(Integer userId) {
        return projectRepository.getMaxOrderByOwnerId(userId);
    }

    private boolean hasParent(ProjectRequest request) {
        return nonNull(request.getParentId());
    }

    private Project buildProjectToAddFromRequest(ProjectRequest request, Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        return Project.builder()
                .name(request.getName())
                .parent(getParentFromProjectRequest(request, userId))
                .favorite(request.isFavorite())
                .color(request.getColor())
                .createdAt(now)
                .modifiedAt(now)
                .collapsed(true)
                .archived(false)
                .owner(userRepository.getById(userId))
                .build();
    }

    private Project getParentFromProjectRequest(ProjectRequest request, Integer userId) {
        if(!hasParent(request)) {
            return null;
        }
        testParentOwnership(request.getParentId(), userId);
        return projectRepository.getById(request.getParentId());
    }

    private void testParentOwnership(Integer parentId, Integer userId) {
        Integer ownerId = projectRepository.findOwnerIdById(parentId);
        if(isNull(ownerId) || !ownerId.equals(userId)) {
            throw new WrongOwnerException("Cannot add nonexistent project as parent!");
        }
    }

    /**
     * Updating project in repository.
     * @param request Data to update the project
     * @param projectId ID of the project to update
     * @param userId ID of an owner of the project
     * @return Project with updated data
     */
    @Transactional
    @NotifyOnProjectChange
    public Project updateProject(ProjectRequest request, Integer projectId, Integer userId) {
        Project projectToUpdate = projectRepository.findByIdAndOwnerId(projectId, userId)
                .map((p) -> transformWithRequestData(p, request))
                .orElseThrow(() -> new NotFoundException("No such project!"));
        return projectRepository.save(projectToUpdate);
    }

    private Project transformWithRequestData(Project project, ProjectRequest request) {
        project.setName(request.getName());
        project.setColor(request.getColor());
        project.setFavorite(request.isFavorite());
        project.setModifiedAt(LocalDateTime.now());
        return project;
    }

    /**
     * Delete project from repository.
     * @param projectId ID of the project to delete
     * @param userId ID of an owner of the project
     */
    @Transactional
    @NotifyOnProjectDeletion
    public void deleteProject(Integer projectId, Integer userId) {
        projectRepository.deleteByIdAndOwnerId(projectId, userId);
    }
}
