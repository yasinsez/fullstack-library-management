package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.UserRequestDTO;
import com.yasinsez.library.dto.UserResponseDTO;
import com.yasinsez.library.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;

import java.util.stream.Collectors;

public class UserMapper {

    public static User toEntity(UserRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .username(dto.username())
                .passwordHash(dto.password() != null ? BcryptUtil.bcryptHash(dto.password()) : null)
                .fullName(dto.fullName())
                .email(dto.email())
                .phoneNumber(dto.phoneNumber())
                .active(dto.active())
                .status(dto.status())
                .build();
    }

    public static UserResponseDTO toResponseDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getActive(),
                user.getStatus(),
                user.getLastLogin(),
                user.getRoles().stream()
                        .map(RoleMapper::toResponseDTO)
                        .collect(Collectors.toSet()),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
