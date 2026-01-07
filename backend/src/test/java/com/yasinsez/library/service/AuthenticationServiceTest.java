package com.yasinsez.library.service;

import com.yasinsez.library.model.User;
import com.yasinsez.library.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
public class AuthenticationServiceTest {

    @Inject
    AuthenticationService authenticationService;

    @InjectMock
    UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(BcryptUtil.bcryptHash("password"));
        user.setActive(true);
    }

    @Test
    void authenticate_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        User authenticatedUser = authenticationService.authenticate("testuser", "password");
        assertNotNull(authenticatedUser);
        assertEquals(user.getUsername(), authenticatedUser.getUsername());
    }

    @Test
    void authenticate_UserNotFound() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        assertThrows(NotAuthorizedException.class, () -> authenticationService.authenticate("wronguser", "password"));
    }

    @Test
    void authenticate_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertThrows(NotAuthorizedException.class, () -> authenticationService.authenticate("testuser", "wrongpassword"));
    }

    @Test
    void authenticate_InactiveUser() {
        user.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertThrows(NotAuthorizedException.class, () -> authenticationService.authenticate("testuser", "password"));
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");

        User registeredUser = authenticationService.register(newUser, "newpassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).persist(userCaptor.capture());

        User persistedUser = userCaptor.getValue();
        assertEquals("newuser", persistedUser.getUsername());
        assertTrue(BcryptUtil.matches("newpassword", persistedUser.getPasswordHash()));
    }

    @Test
    void register_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        User newUser = new User();
        newUser.setUsername("testuser");
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(newUser, "password"));
    }

    @Test
    void register_EmailAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("test@example.com");
        assertThrows(IllegalArgumentException.class, () -> authenticationService.register(newUser, "password"));
    }
}

