package io.github.xpakx.ladder.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UserDisabledException extends RuntimeException {
    public UserDisabledException(String message) {
        super(message);
    }
}
