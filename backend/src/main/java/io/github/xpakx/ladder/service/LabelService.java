package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LabelService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

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
                .build();
    }

    public Label updateLabel(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label!"));
        labelToUpdate.setName(request.getName());
        labelToUpdate.setColor(request.getColor());
        labelToUpdate.setFavorite(request.isFavorite());
        return labelRepository.save(labelToUpdate);
    }

    public void deleteLabel(Integer labelId, Integer userId) {
        labelRepository.deleteByIdAndOwnerId(labelId, userId);
    }

    private Label findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? labelRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    public Label moveLabelAfter(IdRequest request, Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        Label afterLabel = findIdFromIdRequest(request);
        labelToMove.setGeneralOrder(afterLabel.getGeneralOrder() + 1);
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(userId, afterLabel.getGeneralOrder());
        return labelRepository.save(labelToMove);
    }

    public Label updateLabelFav(BooleanRequest request, Integer labelId, Integer userId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label"));
        labelToUpdate.setFavorite(request.isFlag());
        return labelRepository.save(labelToUpdate);
    }

    public Label moveLabelAsFirst(Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        labelToMove.setGeneralOrder(1);
        labelRepository.incrementGeneralOrderByOwnerId(userId);
        return labelRepository.save(labelToMove);
    }

    public Label addLabelAfter(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent label!"));
        labelToAdd.setGeneralOrder(label.getGeneralOrder()+1);
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThan(userId, label.getGeneralOrder());
        return labelRepository.save(labelToAdd);
    }

    public Label addLabelBefore(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent label!"));
        labelToAdd.setGeneralOrder(label.getGeneralOrder());
        labelRepository.incrementGeneralOrderByOwnerIdAndGeneralOrderGreaterThanEqual(userId, label.getGeneralOrder());
        return labelRepository.save(labelToAdd);
    }
}
