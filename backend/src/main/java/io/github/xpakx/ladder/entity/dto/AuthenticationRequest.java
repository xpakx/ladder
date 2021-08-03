package io.github.xpakx.ladder.entity.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class AuthenticationRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
}