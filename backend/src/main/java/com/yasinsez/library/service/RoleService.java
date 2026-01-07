package com.yasinsez.library.service;

import com.yasinsez.library.model.Role;
import com.yasinsez.library.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class RoleService {

    @Inject
    RoleRepository roleRepository;

    public List<Role> getAllRoles() {
        return roleRepository.listAll();
    }
}
