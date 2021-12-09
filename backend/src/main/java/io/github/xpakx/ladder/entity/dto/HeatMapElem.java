package io.github.xpakx.ladder.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
public class HeatMapElem {
    LocalDateTime date;
    Integer number;
}
