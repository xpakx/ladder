package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnLabelChange;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class LabelMovableService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

    @NotifyOnLabelChange
    public Label moveLabelAfter(IdRequest request, Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        Label afterLabel = findIdFromIdRequest(request);
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(
                userId,
                afterLabel.getGeneralOrder(),
                LocalDateTime.now()
        );
        labelToMove.setGeneralOrder(afterLabel.getGeneralOrder() + 1);
        labelToMove.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToMove);
    }

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

    private Label findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? labelRepository.findById(request.getId()).orElse(null) : null;
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
