package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.*;
import io.github.xpakx.ladder.entity.*;
import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.error.WrongOwnerException;
import io.github.xpakx.ladder.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final HabitRepository habitRepository;
    private final CollaborationRepository collaborationRepository;

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

    @NotifyOnProjectChange
    @Transactional
    @Notify(message = "You have an invitation to project")
    public CollaborationWithOwner addCollaborator(CollaborationRequest request, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        UserAccount user = userRepository.findByCollaborationToken(request.getCollaborationToken())
                .orElseThrow(() -> new NotFoundException("No user with such token!"));
        LocalDateTime now = LocalDateTime.now();
        toUpdate.getCollaborators().add(createCollaborationForUser(request, projectId, now, user));
        toUpdate.setCollaborative(true);
        toUpdate.setModifiedAt(now);
        projectRepository.save(toUpdate);
        return collaborationRepository.findProjectedByOwnerIdAndProjectId(user.getId(), projectId).get();
    }

    private Collaboration createCollaborationForUser(CollaborationRequest request, Integer projectId, LocalDateTime now, UserAccount user) {
        return Collaboration.builder()
                .owner(user)
                .project(projectRepository.getById(projectId))
                .accepted(false)
                .editionAllowed(request.isEditionAllowed())
                .taskCompletionAllowed(request.isCompletionAllowed())
                .modifiedAt(now)
                .build();
    }

    @NotifyOnCollaborationDeletion
    public void deleteCollaborator(Integer collaborationId, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        List<Collaboration> collaborations = toUpdate.getCollaborators();
        toUpdate.setCollaborators(collaborations.stream()
                .filter((a) -> !collaborationId.equals(a.getOwner().getId()))
                .collect(Collectors.toList())
        );
        if(toUpdate.getCollaborators().size() == 0) {
            toUpdate.setCollaborative(false);
        }
        LocalDateTime now = LocalDateTime.now();
		toUpdate.setModifiedAt(now);
        projectRepository.save(toUpdate);
        List<Task> tasks = taskRepository.findByAssignedIdAndProjectId(collaborationId, toUpdate.getId());
        for(Task task : tasks) {
            task.setModifiedAt(now);
            task.setAssigned(null);
        }
        taskRepository.saveAll(tasks);
        collaborationRepository.deleteAll(
                collaborations.stream()
                        .filter((a) -> collaborationId.equals(a.getOwner().getId()))
                        .collect(Collectors.toSet())
        );
    }

    public List<CollaborationWithOwner> getCollaborators(Integer projectId, Integer ownerId) {
        return collaborationRepository.findByProjectIdAndProjectOwnerId(projectId, ownerId);
    }
}
