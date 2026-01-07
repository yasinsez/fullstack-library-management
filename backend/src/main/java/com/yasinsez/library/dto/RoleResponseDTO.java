package com.yasinsez.library.dto;

import java.time.LocalDateTime;

public record RoleResponseDTO(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
