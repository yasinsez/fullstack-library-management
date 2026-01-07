package com.yasinsez.library.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AuthorResponseDTO(
        Long id,
        String name,
        String biography,
        LocalDate birthDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
