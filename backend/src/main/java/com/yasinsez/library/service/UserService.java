package com.yasinsez.library.service;

import com.yasinsez.library.model.User;
import com.yasinsez.library.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.elytron.security.common.BcryptUtil;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import com.yasinsez.library.model.Role;
import com.yasinsez.library.repository.RoleRepository;
import com.yasinsez.library.dto.UserUpdateDTO;
import com.yasinsez.library.dto.UserMeDTO;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.repository.MemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    MemberRepository memberRepository;

    @Transactional
    public User createUser(User user, String password, Set<Long> roleIds) {
        log.debug("Creating user: {}", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BadRequestException("Email already exists");
        }
        user.setPasswordHash(BcryptUtil.bcryptHash(password));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> roles = roleIds.stream()
                    .map(roleId -> roleRepository.findByIdOptional(roleId)
                            .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userRepository.persist(user);

        Member member = Member.builder()
                .user(user)
                .id(user.getId())
                .membershipStartDate(LocalDate.now())
                .build();
        memberRepository.persist(member);

        return user;
    }

    @Transactional
    public UserMeDTO getUserMe(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserMeDTO(user.getUsername(), user.getFullName(), roles);
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateDTO updatedUser) {
        log.debug("Updating user: {}", userId);
        User existingUser = findById(userId);

        if (updatedUser.fullName() != null) {
            existingUser.setFullName(updatedUser.fullName());
        }
        if (updatedUser.phoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.phoneNumber());
        }
        if (updatedUser.status() != null) {
            existingUser.setStatus(updatedUser.status());
        }
        if (updatedUser.active() != null) {
            existingUser.setActive(updatedUser.active());
        }
        if (updatedUser.email() != null) {
            existingUser.setEmail(updatedUser.email());
        }

        if (updatedUser.roleIds() != null && !updatedUser.roleIds().isEmpty()) {
            Set<Role> roles = updatedUser.roleIds().stream()
                    .map(roleId -> roleRepository.findByIdOptional(roleId)
                            .orElseThrow(() -> new NotFoundException("Role not found with ID: " + roleId)))
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        return existingUser;
    }

    @Transactional
    public boolean deactivateUser(Long userId) {
        log.debug("Deactivating user: {}", userId);
        User user = findById(userId);
        user.setActive(false);
        return true;
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.debug("Deleting user: {}", userId);
        boolean deleted = userRepository.deleteById(userId);
        if (!deleted) {
            throw new NotFoundException("User not found with ID: " + userId);
        }
    }

    public List<User> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.listAll();
    }

    public User findById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findByIdOptional(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    @Transactional
    public User updateUserByUsername(String username, User updatedUser) {
        log.debug("Updating user: {}", username);
        User existingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        existingUser.setFullName(updatedUser.getFullName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());

        return existingUser;
    }

    public Optional<User> getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findByIdOptional(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username);
    }

    public List<User> searchUsers(String searchTerm) {
        log.debug("Searching users with term: {}", searchTerm);
        return userRepository.searchUsers(searchTerm);
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }
        if (!BcryptUtil.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }
        user.setPasswordHash(BcryptUtil.bcryptHash(newPassword));
        return true;
    }
}
