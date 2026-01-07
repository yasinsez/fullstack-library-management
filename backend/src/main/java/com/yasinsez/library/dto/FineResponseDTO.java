package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.FineReason;
import com.yasinsez.library.model.enums.FineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FineResponseDTO(
        Long id,
        MemberResponseDTO member,
        LoanResponseDTO loan,
        BigDecimal amount,
        FineReason reason,
        LocalDate issueDate,
        LocalDate dueDate,
        FineStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
