package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollaborationRequest {
    String collaboratorToken;
    boolean completionAllowed;
    boolean editionAllowed;
}
