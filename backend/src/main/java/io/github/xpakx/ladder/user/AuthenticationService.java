package io.github.xpakx.ladder.user;

import io.github.xpakx.ladder.user.dto.AuthenticationRequest;
import io.github.xpakx.ladder.user.dto.AuthenticationResponse;
import io.github.xpakx.ladder.user.dto.RegistrationRequest;
import io.github.xpakx.ladder.common.error.JwtBadCredentialsException;
import io.github.xpakx.ladder.common.error.UserDisabledException;
import io.github.xpakx.ladder.security.JwtTokenUtil;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
