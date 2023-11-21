package com.example.security.jwt.admin.facade.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record RequestAdminFacade() {
    @Builder
    public record Register(
            @NotNull
            @Size(min = 3, max = 50)
            String username,

            @NotNull
            @Size(min = 3, max = 50)
            String password,

            @NotNull
            @Size(min = 3, max = 50)
            String nickname
    ) {
    }
}
