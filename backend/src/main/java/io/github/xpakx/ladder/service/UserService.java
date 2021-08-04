package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.entity.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findById(Integer.valueOf(username))
                .orElseThrow(() -> new UsernameNotFoundException("No user with id " + username));
        return new User(user.getId().toString(), user.getPassword(), user.getRoles());
    }

    public UserDetails loadUserToLogin(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user with name " + username));
        return new User(user.getId().toString(), user.getPassword(), user.getRoles());
    }
}