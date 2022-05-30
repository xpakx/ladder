package io.github.xpakx.ladder.common.dto;

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
