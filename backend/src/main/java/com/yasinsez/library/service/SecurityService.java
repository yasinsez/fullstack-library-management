package com.yasinsez.library.service;

import com.yasinsez.library.repository.UserRepository;
import com.yasinsez.library.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

@ApplicationScoped
public class SecurityService {

    private static final Logger logger = Logger.getLogger(SecurityService.class.getName());

    @Inject
    private UserRepository userRepository;

    private final Map<String, Long> verificationTokens = new HashMap<>();
    private final Map<String, Long> passwordResetTokens = new HashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new HashMap<>();

    public User validateCredentials(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        if (user.getPasswordHash() != null && BcryptUtil.matches(password, user.getPasswordHash()) && Boolean.TRUE.equals(user.getActive())) {
            return user;
        }

        return null;
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null || user.getPasswordHash() == null) {
            return false;
        }

        if (!BcryptUtil.matches(currentPassword, user.getPasswordHash())) {
            return false;
        }

        user.setPasswordHash(BcryptUtil.bcryptHash(newPassword));
        userRepository.persist(user);

        return true;
    }

    public boolean hasRole(User user, String requiredRole) {
        if (user == null || requiredRole == null) {
            return false;
        }

        return user.getRoles().stream().anyMatch(role -> requiredRole.equalsIgnoreCase(role.getName()));
    }

    public String hashPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BcryptUtil.bcryptHash(password);
    }

    public boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        return BcryptUtil.matches(password, storedHash);
    }

    public String generateVerificationToken(Long userId) {
        String token = UUID.randomUUID().toString();
        verificationTokens.put(token, userId);
        return token;
    }

    public Long validateVerificationToken(String token) {
        return verificationTokens.remove(token);
    }

    public String generatePasswordResetToken(Long userId) {
        String token = UUID.randomUUID().toString();
        passwordResetTokens.put(token, userId);
        return token;
    }

    public Long validatePasswordResetToken(String token) {
        return passwordResetTokens.remove(token);
    }

    public void lockAccount(String username) {
        lockedAccounts.put(username, LocalDateTime.now().plusHours(1));
    }

    public boolean isAccountLocked(String username) {
        LocalDateTime lockExpiration = lockedAccounts.get(username);
        if (lockExpiration == null) {
            return false;
        }

        if (lockExpiration.isBefore(LocalDateTime.now())) {
            lockedAccounts.remove(username);
            return false;
        }

        return true;
    }

    public void unlockAccount(String username) {
        lockedAccounts.remove(username);
    }

    public void logSecurityEvent(String eventType, String username) {
        System.out.println("SECURITY EVENT - " + eventType + " for user: " + username + " at " + LocalDateTime.now());
    }

    public void sendSecurityAlert(String alertMessage) {
        System.out.println("SECURITY ALERT: " + alertMessage);
    }

    public boolean processSecurityAlert(String alertType, String details) {
        logSecurityEvent("ALERT: " + alertType, details);
        return true;
    }
}
