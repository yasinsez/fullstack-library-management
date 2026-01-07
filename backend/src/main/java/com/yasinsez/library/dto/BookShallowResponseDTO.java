package com.yasinsez.library.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BookShallowResponseDTO(
        Long id,
        String title,
        String isbn,
        String description,
        Integer publicationYear,
        Integer pages,
        String coverImageUrl,
        PublisherDTO publisher,
        CategoryDTO category,
        List<AuthorDTO> authors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
