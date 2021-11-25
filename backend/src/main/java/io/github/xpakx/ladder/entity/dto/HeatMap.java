package io.github.xpakx.ladder.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class HeatMap {
    List<HeatMapElem> map;
}
