package com.yasinsez.library.dto;

import java.time.LocalDateTime;

public record PublisherResponseDTO(
        Long id,
        String name,
        String address,
        String contact,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
