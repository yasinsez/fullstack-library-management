package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.RoleResponseDTO;
import com.yasinsez.library.model.Role;

public class RoleMapper {

    public static RoleResponseDTO toResponseDTO(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleResponseDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getCreatedAt(),
                role.getUpdatedAt()
        );
    }
}
