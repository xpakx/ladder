package io.github.xpakx.ladder.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DateRequest {
    private LocalDateTime date;
    private boolean timeboxed;
}
