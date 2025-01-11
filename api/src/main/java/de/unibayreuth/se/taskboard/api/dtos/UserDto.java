package de.unibayreuth.se.taskboard.api.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for user metadata.
 */
@Data
public class UserDto {
        @Nullable
        private final UUID id; // user id is null when creating or update a new user
        @Nullable
        private final LocalDateTime createdAt; // is null when using DTO to create or update a new user
        @NotNull
        private final String name;
}
