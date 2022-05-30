package io.github.xpakx.ladder.user.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Setter
@Getter
public class RegistrationRequest {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String passwordRe;
}