package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.Label;
import io.github.xpakx.ladder.entity.dto.LabelRequest;
import io.github.xpakx.ladder.repository.LabelRepository;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
