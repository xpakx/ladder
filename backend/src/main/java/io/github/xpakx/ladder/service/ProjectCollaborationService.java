package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.Notify;
import io.github.xpakx.ladder.aspect.NotifyOnCollaborationDeletion;
import io.github.xpakx.ladder.aspect.NotifyOnProjectChange;
import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.dto.CollaborationRequest;
import io.github.xpakx.ladder.entity.dto.CollaborationWithOwner;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.CollaborationRepository;
import io.github.xpakx.ladder.repository.ProjectRepository;
import io.github.xpakx.ladder.repository.TaskRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
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
