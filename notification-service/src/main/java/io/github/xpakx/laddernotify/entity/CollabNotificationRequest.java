package io.github.xpakx.laddernotify.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CollabNotificationRequest {
    private List<Integer> collabId;
    private LocalDateTime time;
    private String type;
    private Integer id;
}
