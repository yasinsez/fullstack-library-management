package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.LoanAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoanHistoryResponseDTO(
        Long id,
        MemberResponseDTO member,
        LoanResponseDTO loan,
        BookResponseDTO book,
        LoanAction action,
        LocalDate actionDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
