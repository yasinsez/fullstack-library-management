package com.yasinsez.library.dto;

import java.time.LocalDate;
import java.util.Set;

public record BookRequestDTO(
                String title,
                String isbn,
                LocalDate publicationDate,
                String genre,
                String language,
                String coverImageUrl,
                Long publisherId,
                Long categoryId,
                Set<Long> authorIds,
                Integer stock) {
}
