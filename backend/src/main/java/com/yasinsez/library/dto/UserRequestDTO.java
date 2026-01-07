package com.yasinsez.library.dto;

import com.yasinsez.library.model.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserRequestDTO(
                @NotBlank(message = "Username is required") @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters") String username,

                @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") String password,

                @NotBlank(message = "Full name is required") @Size(max = 255, message = "Full name must not exceed 255 characters") String fullName,

                @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

                @Size(max = 20, message = "Phone number must not exceed 20 characters") String phoneNumber,

                Boolean active,

                UserStatus status,

                Set<Long> roleIds) {
}
