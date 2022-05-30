package io.github.xpakx.ladder.common.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongOwnerException extends RuntimeException {
    public WrongOwnerException(String message) {
        super(message);
    }
}