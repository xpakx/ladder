package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.Task;
import io.github.xpakx.ladder.entity.dto.BooleanRequest;
import io.github.xpakx.ladder.entity.dto.IdRequest;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class LabelService {
    private final LabelRepository labelRepository;
    private final UserAccountRepository userRepository;

    @Autowired
    public LabelService(LabelRepository labelRepository, UserAccountRepository userRepository) {
        this.labelRepository = labelRepository;
        this.userRepository = userRepository;
    }

    public Label addLabel(LabelRequest request, Integer userId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        return labelRepository.save(labelToAdd);
    }

    private Label buildLabelToAddFromRequest(LabelRequest request, Integer userId) {
        return Label.builder()
                .name(request.getName())
                .owner(userRepository.getById(userId))
                .color(request.getColor())
                .favorite(request.isFavorite())
                .generalOrder(getMaxOrder(userId))
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

    @Transactional
    public void deleteLabel(Integer labelId, Integer userId) {
        labelRepository.deleteByIdAndOwnerId(labelId, userId);
    }

    private Integer getMaxOrder(Integer userId) {
        return labelRepository.findByOwnerId(userId, Label.class).stream()
                .max(Comparator.comparing(Label::getGeneralOrder))
                .map(Label::getGeneralOrder)
                .orElse(0);
    }

    private Label findIdFromIdRequest(IdRequest request) {
        return hasId(request) ? labelRepository.findById(request.getId()).orElse(null) : null;
    }

    private boolean hasId(IdRequest request) {
        return request.getId() != null;
    }

    private List<Label> getAllLabelsAfterGivenLabel(Integer userId, Label label) {
        return labelRepository
                .findByOwnerIdAndGeneralOrderGreaterThan(userId, label.getGeneralOrder());
    }

    private List<Label> getAllLabelsAfterGivenLabelAndThisLabel(Integer userId, Label label) {
        return labelRepository
                .findByOwnerIdAndGeneralOrderGreaterThanEqual(userId, label.getGeneralOrder());
    }

    public Label moveLabelAfter(IdRequest request, Integer userId, Integer labelToMoveId) {
        Label labelToMove = labelRepository.findByIdAndOwnerId(labelToMoveId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot move non-existent label!"));
        Label afterLabel = findIdFromIdRequest(request);
        List<Label> labelsAfter = getAllLabelsAfterGivenLabel(userId, afterLabel);

        labelToMove.setGeneralOrder(afterLabel.getGeneralOrder() + 1);
        labelsAfter.forEach(((p) -> p.setGeneralOrder(p.getGeneralOrder() + 1)));
        labelRepository.saveAll(labelsAfter);
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
        List<Label> children = labelRepository.findByOwnerId(userId, Label.class);

        labelToMove.setGeneralOrder(1);
        children.forEach(((p) -> p.setGeneralOrder(p.getGeneralOrder()+1)));
        labelRepository.saveAll(children);
        return labelRepository.save(labelToMove);
    }

    public Label addLabelAfter(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing after non-existent label!"));
        List<Label> labelsAfter = getAllLabelsAfterGivenLabel(userId, label);


        labelToAdd.setGeneralOrder(label.getGeneralOrder()+1);
        labelsAfter.forEach(((p) -> p.setGeneralOrder(p.getGeneralOrder()+1)));
        labelRepository.saveAll(labelsAfter);
        return labelRepository.save(labelToAdd);
    }

    public Label addLabelBefore(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToAdd = buildLabelToAddFromRequest(request, userId);
        Label label = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("Cannot add nothing before non-existent label!"));
        List<Label> labelsAfter = getAllLabelsAfterGivenLabelAndThisLabel(userId, label);


        labelToAdd.setGeneralOrder(label.getGeneralOrder());
        labelsAfter.forEach(((p) -> p.setGeneralOrder(p.getGeneralOrder()+1)));
        labelRepository.saveAll(labelsAfter);
        return labelRepository.save(labelToAdd);
    }
}
