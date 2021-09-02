package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.entity.dto.*;
import io.github.xpakx.ladder.error.NotFoundException;
import io.github.xpakx.ladder.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MainService {
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public MainService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public UserWithData getAll(Integer userId) {
        return userAccountRepository.findProjectedById(userId)
                .orElseThrow(() -> new NotFoundException("No user with id " + userId));
    }
}
