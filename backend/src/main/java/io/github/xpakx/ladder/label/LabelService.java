package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.notification.NotifyOnLabelChange;
import io.github.xpakx.ladder.notification.NotifyOnLabelDeletion;
import io.github.xpakx.ladder.common.dto.BooleanRequest;
import io.github.xpakx.ladder.label.dto.LabelRequest;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

    /**
     * Add new label.
     * @param request Request with data to build new label
     * @param userId ID of an owner of the newly created label
     * @return Newly created label
     */
    @NotifyOnLabelChange
    public Label addLabel(LabelRequest request, Integer userId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        labelToAdd.setGeneralOrder(labelRepository.getMaxOrderByOwnerId(userId)+1);
        return labelRepository.save(labelToAdd);
    }

    private Label buildLabelToAddFromRequest(LabelRequest request, Integer userId) {
        return Label.builder()
                .name(request.getName())
                .owner(userRepository.getById(userId))
                .color(request.getColor())
                .favorite(request.isFavorite())
                .modifiedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updating label in repository.
     * @param request Data to update the label
     * @param labelId ID of the label to update
     * @param userId ID of an owner of the label
     * @return Label with updated data
     */
    @NotifyOnLabelChange
    public Label updateLabel(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label!"));
        labelToUpdate.setName(request.getName());
        labelToUpdate.setColor(request.getColor());
        labelToUpdate.setFavorite(request.isFavorite());
        labelToUpdate.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToUpdate);
    }

    /**
     * Delete label from repository.
     * @param labelId ID of the label to delete
     * @param userId ID of an owner of the label
     */
    @NotifyOnLabelDeletion
    public void deleteLabel(Integer labelId, Integer userId) {
        labelRepository.deleteByIdAndOwnerId(labelId, userId);
    }

    /**
     * Change if label is favorite.
     * @param request Request with favorite flag
     * @param labelId ID of the label to update
     * @param userId ID of an owner of the label
     * @return Updated label
     */
    @NotifyOnLabelChange
    public Label updateLabelFav(BooleanRequest request, Integer labelId, Integer userId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label"));
        labelToUpdate.setFavorite(request.isFlag());
        labelToUpdate.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToUpdate);
    }
}
