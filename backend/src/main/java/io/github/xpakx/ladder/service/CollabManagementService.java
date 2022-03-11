package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnCollaborationChange;
import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.CollaborationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CollabManagementService {
    private final CollaborationRepository collabRepository;

    @NotifyOnCollaborationChange
    public Collaboration updateCollabEdit(BooleanRequest request, Integer collabId, Integer userId) {
        Collaboration collaborationToUpdate = collabRepository.findByIdAndProjectOwnerId(collabId, userId)
                .orElseThrow(() -> new NotFoundException("No such collaboration!"));
        collaborationToUpdate.setEditionAllowed(request.isFlag());
        collaborationToUpdate.setModifiedAt(LocalDateTime.now());
        return collabRepository.save(collaborationToUpdate);
    }

    @NotifyOnCollaborationChange
    public Collaboration updateCollabComplete(BooleanRequest request, Integer collabId, Integer userId) {
        Collaboration collaborationToUpdate = collabRepository.findByIdAndProjectOwnerId(collabId, userId)
                .orElseThrow(() -> new NotFoundException("No such collaboration!"));
        collaborationToUpdate.setTaskCompletionAllowed(request.isFlag());
        collaborationToUpdate.setModifiedAt(LocalDateTime.now());
        return collabRepository.save(collaborationToUpdate);
    }
}
