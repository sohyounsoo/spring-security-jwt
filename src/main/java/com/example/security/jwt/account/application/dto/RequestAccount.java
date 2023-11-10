package com.example.security.jwt.account.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public record RequestAccount() {

    @Builder
    public record Login(
            @NotNull
            @Size(min = 3, max = 50)
            String username,
            @NotNull
            @Size(min = 5, max = 100)
            String password
    ) {
    }

    @Builder
    public record Refresh(
            @NotNull
            String toekn
    ) {
    }

    @Builder
    public record RegisterMember(
            @NotNull
            @Size(min = 3, max = 50)
            String username,

            @NotNull
            @Size(min = 5, max = 100)
            String password,

            @NotNull
            @Size(min = 5, max = 100)
            String nickname
    ) {
    }

    @Builder
    public record RegisterAdmin(
            @NotNull
            @Size(min = 3, max = 50)
            String username,

            @NotNull
            @Size(min = 3, max = 100)
            String password,

            @NotNull
            @Size(min = 5, max = 100)
            String nickname
    ) {
    }
}
