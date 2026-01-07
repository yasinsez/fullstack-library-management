package com.yasinsez.library.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceiptResponseDTO(
        Long id,
        TransactionResponseDTO transaction,
        LocalDate issueDate,
        String items,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
