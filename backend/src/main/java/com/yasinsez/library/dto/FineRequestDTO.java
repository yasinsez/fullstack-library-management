package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.FineReason;
import com.yasinsez.library.model.enums.FineStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FineRequestDTO(
        @NotNull(message = "Member ID is required")
        Long memberId,

        Long loanId,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Reason is required")
        FineReason reason,

        LocalDate issueDate,

        LocalDate dueDate,

        FineStatus status
) {}
