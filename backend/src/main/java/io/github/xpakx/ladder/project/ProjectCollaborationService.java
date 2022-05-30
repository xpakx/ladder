package io.github.xpakx.ladder.project;

import io.github.xpakx.ladder.notification.Notify;
import io.github.xpakx.ladder.notification.NotifyOnCollaborationDeletion;
import io.github.xpakx.ladder.notification.NotifyOnProjectChange;
import io.github.xpakx.ladder.collaboration.Collaboration;
import io.github.xpakx.ladder.task.Task;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.collaboration.dto.CollaborationRequest;
import io.github.xpakx.ladder.collaboration.dto.CollaborationWithOwner;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.collaboration.CollaborationRepository;
import io.github.xpakx.ladder.task.TaskRepository;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProjectCollaborationService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserAccountRepository userRepository;
    private final CollaborationRepository collaborationRepository;

    /**
     * Add user as a collaborator to project.
     * @param projectId ID of the project
     * @param ownerId ID of an owner of the project
     * @param request Request with collaboration token of new collaborator and permissions
     * @return Object with project's details
     */
    @NotifyOnProjectChange
    @Transactional
    @Notify(message = "You have an invitation to project")
    public CollaborationWithOwner addCollaborator(CollaborationRequest request, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        UserAccount user = userRepository.findByCollaborationToken(request.getCollaborationToken())
                .orElseThrow(() -> new NotFoundException("No user with such token!"));
        projectRepository.save(updateProject(request, projectId, toUpdate, user));
        return collaborationRepository.findProjectedByOwnerIdAndProjectId(user.getId(), projectId).orElse(null);
    }

    private Project updateProject(CollaborationRequest request, Integer projectId, Project toUpdate, UserAccount user) {
        LocalDateTime now = LocalDateTime.now();
        toUpdate.getCollaborators().add(createCollaborationForUser(request, projectId, now, user));
        toUpdate.setCollaborative(true);
        toUpdate.setModifiedAt(now);
        return toUpdate;
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

    /**
     * Delete user from collaborators in the project.
     * @param projectId ID of the project
     * @param ownerId ID of an owner of the project
     * @param collaboratorId ID of a user to delete from collaborator list
     */
    @NotifyOnCollaborationDeletion
    public void deleteCollaborator(Integer collaboratorId, Integer projectId, Integer ownerId) {
        Project toUpdate = projectRepository
                .getByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new NotFoundException("No such project!"));
        List<Collaboration> collaborations = toUpdate.getCollaborators();
        LocalDateTime now = LocalDateTime.now();
        updateProject(collaboratorId, toUpdate, collaborations, now);
        deassignTasks(collaboratorId, toUpdate, now);
        deleteCollaborations(collaboratorId, collaborations);
    }

    private void deleteCollaborations(Integer collaboratorId, List<Collaboration> collaborations) {
        collaborationRepository.deleteAll(
                collaborations.stream()
                        .filter((a) -> collaboratorId.equals(a.getOwner().getId()))
                        .collect(Collectors.toSet())
        );
    }

    private void deassignTasks(Integer collaboratorId, Project toUpdate, LocalDateTime now) {
        List<Task> tasks = taskRepository.findByAssignedIdAndProjectId(collaboratorId, toUpdate.getId());
        for(Task task : tasks) {
            task.setModifiedAt(now);
            task.setAssigned(null);
        }
        taskRepository.saveAll(tasks);
    }

    private void updateProject(Integer collaboratorId, Project toUpdate, List<Collaboration> collaborations, LocalDateTime now) {
        toUpdate.setCollaborators(collaborations.stream()
                .filter((a) -> !collaboratorId.equals(a.getOwner().getId()))
                .collect(Collectors.toList())
        );
        if(toUpdate.getCollaborators().size() == 0) {
            toUpdate.setCollaborative(false);
        }
        toUpdate.setModifiedAt(now);
        projectRepository.save(toUpdate);
    }

    /**
     * Get list of collaborators for given project.
     * @param projectId ID of the project
     * @param ownerId ID of an owner of the project
     * @return List of collaborators with permissions
     */
    public List<CollaborationWithOwner> getCollaborators(Integer projectId, Integer ownerId) {
        return collaborationRepository.findByProjectIdAndProjectOwnerId(projectId, ownerId);
    }
}
