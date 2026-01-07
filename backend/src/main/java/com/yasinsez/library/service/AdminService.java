package com.yasinsez.library.service;

import com.yasinsez.library.model.Role;
import com.yasinsez.library.model.User;
import com.yasinsez.library.repository.RoleRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@ApplicationScoped
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    @Inject
    private UserService userService;

    @Inject
    private RoleRepository roleRepository;

    @Transactional
    public User createUserWithRole(User user, String password, String roleName) {
        log.info("Admin creating user {} with role {}", user.getUsername(), roleName);
        Role role = roleRepository.find("name", roleName).firstResultOptional()
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    roleRepository.persist(newRole);
                    return newRole;
                });
        Set<Long> roleIds = Set.of(role.getId());
        return userService.createUser(user, password, roleIds);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Admin deleting user {}", userId);
        userService.deleteUser(userId);
    }

    @Transactional
    public User assignRoleToUser(Long userId, String roleName) {
        log.info("Admin assigning role {} to user {}", roleName, userId);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Role role = roleRepository.find("name", roleName).firstResultOptional()
                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));

        user.getRoles().add(role);
        return user;
    }
}
