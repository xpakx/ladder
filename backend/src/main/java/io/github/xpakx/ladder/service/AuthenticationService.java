package io.github.xpakx.ladder.service;

import io.github.xpakx.ladder.repository.UserAccountRepository;
import io.github.xpakx.ladder.entity.UserAccount;
import io.github.xpakx.ladder.entity.UserRole;
import io.github.xpakx.ladder.entity.dto.AuthenticationRequest;
import io.github.xpakx.ladder.entity.dto.AuthenticationResponse;
import io.github.xpakx.ladder.entity.dto.RegistrationRequest;
import io.github.xpakx.ladder.error.JwtBadCredentialsException;
import io.github.xpakx.ladder.error.UserDisabledException;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.ValidationException;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                                 UserService userService, UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticationResponse generateAuthenticationToken(AuthenticationRequest authenticationRequest) {
        final UserDetails userDetails = userService.loadUserToLogin(authenticationRequest.getUsername());
        authenticate(userDetails.getUsername(), authenticationRequest.getPassword());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return new AuthenticationResponse(token, userDetails.getUsername());
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new UserDisabledException("User " +username+" disabled!");
        } catch (BadCredentialsException e) {
            throw new JwtBadCredentialsException("Invalid password!");
        }
    }

    public AuthenticationResponse register(RegistrationRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ValidationException("Username exists!");
        }
        if (!request.getPassword().equals(request.getPasswordRe())) {
            throw new ValidationException("Passwords don't match!");
        }
        Set<UserRole> roles = new HashSet<>();

        UserAccount userToAdd = new UserAccount();
        userToAdd.setPassword(passwordEncoder.encode(request.getPassword()));
        userToAdd.setUsername(request.getUsername());
        userToAdd.setRoles(roles);
        userToAdd.setProjectCollapsed(true);

        userRepository.save(userToAdd);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setUsername(request.getUsername());
        authenticationRequest.setPassword(request.getPassword());

        return generateAuthenticationToken(authenticationRequest);
    }
}
