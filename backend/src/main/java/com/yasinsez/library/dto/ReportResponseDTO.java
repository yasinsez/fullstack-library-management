package com.yasinsez.library.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReportResponseDTO(
        Long id,
        String title,
        LocalDate startDate,
        LocalDate endDate,
        String content,
        String generatedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
