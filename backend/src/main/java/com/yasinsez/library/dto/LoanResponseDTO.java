package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.LoanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanResponseDTO(
                Long id,
                MemberResponseDTO member,
                BookResponseDTO book,
                LocalDate checkoutDate,
                LocalDate dueDate,
                LocalDate returnDate,
                LoanStatus status,
                Integer renewalCount,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
