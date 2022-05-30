package io.github.xpakx.ladder.sync.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDateRequest {
    private LocalDateTime date;
}
