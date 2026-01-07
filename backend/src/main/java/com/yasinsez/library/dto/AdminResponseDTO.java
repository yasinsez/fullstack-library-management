package com.yasinsez.library.dto;

import java.time.LocalDate;

public record AdminResponseDTO(
        Long id,
        UserResponseDTO user,
        Integer adminLevel,
        String department,
        String permissions,
        LocalDate lastActivity) {
}
