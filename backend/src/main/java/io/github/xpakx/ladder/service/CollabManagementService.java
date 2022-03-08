package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Collaboration;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.CollaborationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CollabManagementService {
    private final CollaborationRepository collabRepository;

    public Collaboration updateCollabEdit(BooleanRequest request, Integer collabId, Integer userId) {
        Collaboration collaborationToUpdate = collabRepository.findByIdAndProjectOwnerId(collabId, userId)
                .orElseThrow(() -> new NotFoundException("No such collaboration!"));
        collaborationToUpdate.setEditionAllowed(request.isFlag());
        return collabRepository.save(collaborationToUpdate);
    }

    public Collaboration updateCollabComplete(BooleanRequest request, Integer collabId, Integer userId) {
        Collaboration collaborationToUpdate = collabRepository.findByIdAndProjectOwnerId(collabId, userId)
                .orElseThrow(() -> new NotFoundException("No such collaboration!"));
        collaborationToUpdate.setTaskCompletionAllowed(request.isFlag());
        return collabRepository.save(collaborationToUpdate);
    }
}
