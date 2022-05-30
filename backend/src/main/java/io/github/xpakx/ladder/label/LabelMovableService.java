package io.github.xpakx.ladder.label;

import io.github.xpakx.ladder.notification.NotifyOnLabelChange;
import io.github.xpakx.ladder.common.dto.IdRequest;
import io.github.xpakx.ladder.label.dto.LabelRequest;
import io.github.xpakx.ladder.common.error.NotFoundException;
import io.github.xpakx.ladder.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LabelMovableService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

    /**
     * Move label after given label
     * @param request Request with id of the label which should be before moved label
     * @param userId ID of an owner of labels
     * @param labelToMoveId ID of the label to move
     * @return Moved label
     */
    @NotifyOnLabelChange
    public Label moveLabelAfter(IdRequest request, Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        Label afterLabel = findIdFromIdRequest(request)
                .orElseThrow(() -> new NotFoundException("Cannot move label after non-existent label!"));
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                afterLabel.getGeneralOrder(),
                LocalDateTime.now()
        );
        labelToMove.setGeneralOrder(afterLabel.getGeneralOrder() + 1);
        labelToMove.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToMove);
    }

    /**
     * Move label at first position
     * @param userId ID of an owner of labels
     * @param labelToMoveId ID of the label to move
     * @return Moved label
     */
    @NotifyOnLabelChange
    public Label moveLabelAsFirst(Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        labelRepository.incrementGeneralOrderByOwnerId(
                userId,
                LocalDateTime.now()
        );
        labelToMove.setGeneralOrder(1);
        labelToMove.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToMove);
    }

    /**
     * Add new label with order after given label
     * @param request Request with data to build new label
     * @param userId ID of an owner of labels
     * @param labelId ID of the label which should be before newly created label
     * @return Newly created label
     */
    @NotifyOnLabelChange
    public Label addLabelAfter(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent label!"));
        labelToAdd.setGeneralOrder(label.getGeneralOrder()+1);
        labelToAdd.setModifiedAt(LocalDateTime.now());
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                label.getGeneralOrder(),
                LocalDateTime.now()
        );
        return labelRepository.save(labelToAdd);
    }

    /**
     * Add new label with order before given label
     * @param request Request with data to build new label
     * @param userId ID of an owner of labels
     * @param labelId ID of the label which should be after newly created label
     * @return Newly created label
     */
    @NotifyOnLabelChange
    public Label addLabelBefore(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent label!"));
        labelToAdd.setGeneralOrder(label.getGeneralOrder());
        labelToAdd.setModifiedAt(LocalDateTime.now());
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(
                userId,
                label.getGeneralOrder(),
                LocalDateTime.now()
        );
        return labelRepository.save(labelToAdd);
    }

    private Optional<Label> findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? labelRepository.findById(request.getId()) : Optional.empty();
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
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
}
