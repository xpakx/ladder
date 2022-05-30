package io.github.xpakx.ladder.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IdCollectionRequest {
    private List<Integer> ids;
}
