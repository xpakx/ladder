package io.github.xpakx.laddernotify.utils;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public class CustomEmitter extends SseEmitter {
    private final Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public CustomEmitter(Integer id, Long timeout) {
        super(timeout);
        this.userId = id;
    }
}
