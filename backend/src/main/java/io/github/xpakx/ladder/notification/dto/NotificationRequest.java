package io.github.xpakx.ladder.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationRequest {
    private Integer userId;
    private LocalDateTime time;
    private String type;
    private Integer id;
}
