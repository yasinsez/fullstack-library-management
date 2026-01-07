package com.yasinsez.library.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MemberResponseDTO(
        Long id,
        UserResponseDTO user,
        LocalDate membershipStartDate,
        LocalDate membershipEndDate,
        String address,
        BigDecimal fineBalance
) {}
