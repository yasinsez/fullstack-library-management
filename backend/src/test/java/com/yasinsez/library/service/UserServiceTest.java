package com.yasinsez.library.service;

import com.yasinsez.library.dto.UserUpdateDTO;
import com.yasinsez.library.model.User;
import com.yasinsez.library.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.quarkus.narayana.jta.QuarkusTransaction;

@QuarkusTest
class UserServiceTest {

    @InjectMock
    UserRepository userRepository;

    @Inject
    UserService userService;

    @Test
    void testCreateUser() {
        QuarkusTransaction.begin();
        User user = new User();
        user.setUsername("testuser_create");
        user.setEmail("test_create@example.com");
        user.setFullName("Test User Create");

        when(userRepository.existsByUsername("testuser_create")).thenReturn(false);
        when(userRepository.existsByEmail("test_create@example.com")).thenReturn(false);

        User created = userService.createUser(user, "password", new HashSet<>());

        verify(userRepository).persist(user);
        assertEquals(user, created);
        QuarkusTransaction.rollback();
    }

    @Test
    void testCreateUserDuplicateUsername() {
        User user = new User();
        user.setUsername("duplicate");

        when(userRepository.existsByUsername("duplicate")).thenReturn(true);

        assertThrows(jakarta.ws.rs.BadRequestException.class,
                () -> userService.createUser(user, "password", new HashSet<>()));
    }

    @Test
    void testUpdateUser() {
        User existing = new User();
        existing.setId(1L);
        existing.setFullName("Old Name");

        UserUpdateDTO updated = new UserUpdateDTO("New Name", "newemail@example.com", null, null, null, null);

        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(existing));

        User result = userService.updateUser(1L, updated);

        assertEquals("New Name", result.getFullName());
    }

    @Test
    void testUpdateUserNotFound() {
        when(userRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(99L, new UserUpdateDTO(null, null, null, null, null, null)));
    }

    @Test
    void testDeactivateUser() {
        User user = new User();
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(user));

        boolean deactivated = userService.deactivateUser(1L);

        assertFalse(user.getActive());
        assertTrue(deactivated);
    }

    @Test
    void testGetAllUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User());

        when(userRepository.listAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
    }

    @Test
    void testFindById() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findByIdOptional(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertEquals(user, result);
    }

    @Test
    void testFindByIdNotFound() {
        when(userRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    void testCountUsers() {
        when(userRepository.count()).thenReturn(5L);

        long count = userService.countUsers();

        assertEquals(5, count);
    }
}
