package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HabitRequest {
    private String title;
    private String description;
    private Integer priority;
    private boolean allowPositive;
    private boolean allowNegative;
    private Integer projectId;
    private Integer generalOrder;
    private List<Integer> labelIds;
}
