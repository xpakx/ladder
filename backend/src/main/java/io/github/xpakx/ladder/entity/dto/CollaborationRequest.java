package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollaborationRequest {
    Integer collaboratorId;
    boolean completionAllowed;
    boolean editionAllowed;
}
