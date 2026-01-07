package com.yasinsez.library.service;

import com.yasinsez.library.repository.UserRepository;
import com.yasinsez.library.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
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
        String hashedPassword = hashPassword(password);

        if (user.getPasswordHash().equals(hashedPassword) && user.getActive()) {
            return user;
        }

        return null;
    }

    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId);
        if (user == null) {
            return false;
        }

        String currentHashed = hashPassword(currentPassword);
        if (!user.getPasswordHash().equals(currentHashed)) {
            return false;
        }

        String newHashed = hashPassword(newPassword);
        user.setPasswordHash(newHashed);
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
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public boolean verifyPassword(String password, String storedHash) {
        return hashPassword(password).equals(storedHash);
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
