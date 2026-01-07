package com.yasinsez.library.mapper;

import com.yasinsez.library.dto.AdminResponseDTO;
import com.yasinsez.library.model.Admin;

public class AdminMapper {

    public static AdminResponseDTO toResponseDTO(Admin admin) {
        if (admin == null) {
            return null;
        }
        return new AdminResponseDTO(
                admin.getId(),
                UserMapper.toResponseDTO(admin.getUser()),
                admin.getAdminLevel(),
                admin.getDepartment(),
                admin.getPermissions(),
                admin.getLastActivity());
    }
}
