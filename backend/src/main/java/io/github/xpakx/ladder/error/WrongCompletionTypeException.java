package io.github.xpakx.ladder.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongCompletionTypeException extends RuntimeException {
    public WrongCompletionTypeException(String message) {
        super(message);
    }
}