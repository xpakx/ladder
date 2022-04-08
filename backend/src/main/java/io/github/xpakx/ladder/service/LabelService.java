package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.aspect.NotifyOnLabelChange;
import io.github.xpakx.ladder.aspect.NotifyOnLabelDeletion;
import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
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
public class LabelService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

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

    @NotifyOnLabelDeletion
    public void deleteLabel(Integer labelId, Integer userId) {
        labelRepository.deleteByIdAndOwnerId(labelId, userId);
    }

    @NotifyOnLabelChange
    public Label updateLabelFav(BooleanRequest request, Integer labelId, Integer userId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label"));
        labelToUpdate.setFavorite(request.isFlag());
        labelToUpdate.setModifiedAt(LocalDateTime.now());
        return labelRepository.save(labelToUpdate);
    }
}
