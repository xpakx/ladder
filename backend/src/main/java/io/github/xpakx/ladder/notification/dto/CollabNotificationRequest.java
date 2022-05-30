package io.github.xpakx.ladder.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CollabNotificationRequest {
    private List<Integer> collabId;
    private LocalDateTime time;
    private String type;
    private Integer id;
}
