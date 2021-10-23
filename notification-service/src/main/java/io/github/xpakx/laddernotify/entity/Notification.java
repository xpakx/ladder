package io.github.xpakx.laddernotify.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Notification {
    private LocalDateTime time;
    private String type;
    private Integer id;
}
