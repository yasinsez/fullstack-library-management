package com.yasinsez.library.service;

import com.yasinsez.library.model.User;
import com.yasinsez.library.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    @Inject
    UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User authenticate(String username, String password) {
        log.info("Authenticating user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotAuthorizedException("Invalid credentials"));

        if (!BcryptUtil.matches(password, user.getPasswordHash())) {
            log.error("Invalid credentials for user: {}", username);
            throw new NotAuthorizedException("Invalid credentials");
        }

        if (user.getActive() == null || !user.getActive()) {
            log.error("User account is not active: {}", username);
            throw new NotAuthorizedException("User account is not active");
        }

        return user;
    }

    @Transactional
    public User register(User user, String password) {
        log.info("Registering new user: {}", user.getUsername());
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setPasswordHash(BcryptUtil.bcryptHash(password));
        user.setActive(true);
        userRepository.persist(user);
        return user;
    }
}
