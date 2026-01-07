package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserUpdateDTO(
        @Size(max = 255, message = "Full name must not exceed 255 characters") String fullName,

        @Email(message = "Email should be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters") String phoneNumber,

        Boolean active,

        UserStatus status,

        Set<Long> roleIds) {
}
