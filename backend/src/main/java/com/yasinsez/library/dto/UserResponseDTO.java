package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record UserResponseDTO(
        Long id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        Boolean active,
        UserStatus status,
        LocalDate lastLogin,
        Set<RoleResponseDTO> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
