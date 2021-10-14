package io.github.xpakx.laddernotify.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    private Integer userId;
    private LocalDateTime time;
}
