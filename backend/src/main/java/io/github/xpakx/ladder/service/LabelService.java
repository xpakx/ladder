package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.Project;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.entity.dto.ProjectRequest;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Label labelToAdd = Label.builder()
                .name(request.getName())
                .owner(userRepository.getById(userId))
                .build();
        return labelRepository.save(labelToAdd);
    }

    public Label updateLabel(LabelRequest request, Integer userId, Integer labelId) {
        Label labelToUpdate = labelRepository.findByIdAndOwnerId(labelId, userId)
                .orElseThrow(() -> new NotFoundException("No such label!"));
        labelToUpdate.setName(request.getName());
        return labelRepository.save(labelToUpdate);
    }

    @Transactional
    public void deleteLabel(Integer labelId, Integer userId) {
        labelRepository.deleteByIdAndOwnerId(labelId, userId);
    }
}
