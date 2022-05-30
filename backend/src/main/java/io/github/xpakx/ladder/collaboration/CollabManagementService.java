package io.github.xpakx.ladder.collaboration;

import io.github.xpakx.ladder.notification.NotifyOnCollaborationChange;
import io.github.xpakx.ladder.user.UserAccount;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.collaboration.dto.CollabTokenResponse;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CollabManagementService {
    private final CollaborationRepository collabRepository;
    private final UserAccountRepository userRepository;

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

    public UserAccount getNewToken(Integer userId) {
        System.out.println("USER_ID: " + userId);
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("No such user!"));
        LocalDateTime date = LocalDateTime.now();
        String token = "" + user.getId() + "-" + date.getYear() + date.getMonthValue() + date.getDayOfMonth();
        user.setCollaborationToken(token);
        return userRepository.save(user);
    }

    public CollabTokenResponse getToken(Integer userId) {
        return new CollabTokenResponse(
                userRepository.findById(userId).map(UserAccount::getCollaborationToken)
                .orElseThrow(() -> new NotFoundException("No token!"))
        );
    }
}
