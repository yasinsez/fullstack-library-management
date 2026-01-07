package com.yasinsez.library.controller;

import com.yasinsez.library.dto.UserCreateRequest;
import com.yasinsez.library.dto.UserDTO;
import com.yasinsez.library.model.Role;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.AdminService;
import com.yasinsez.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashSet;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@QuarkusTest
class AdminControllerTest {

        @InjectMock
        AdminService adminService;

        @InjectMock
        UserService userService;

        @BeforeEach
        void setUp() {
                Mockito.reset(adminService, userService);
        }

        @Test
        @TestSecurity(user = "admin", roles = { "ADMIN" })
        void testCreateUserEndpoint() {
                UserCreateRequest request = new UserCreateRequest("anotheruser", "password", "Another User",
                                "anotheruser@example.com", "MEMBER");

                User user = new User();
                user.setId(1L);
                user.setUsername(request.getUsername());
                user.setFullName(request.getFullName());
                user.setEmail(request.getEmail());
                Role memberRole = new Role();
                memberRole.setName("MEMBER");
                user.setRoles(Collections.singleton(memberRole));

                when(adminService.createUserWithRole(any(User.class), anyString(), anyString())).thenReturn(user);

                given()
                                .contentType("application/json")
                                .body(request)
                                .when()
                                .post("/api/admin/users")
                                .then()
                                .statusCode(201)
                                .body("username", is("anotheruser"))
                                .body("fullName", is("Another User"))
                                .body("email", is("anotheruser@example.com"))
                                .body("roles", hasItem("MEMBER"));
        }

        @Test
        @TestSecurity(user = "admin", roles = { "ADMIN" })
        void testGetAllUsersEndpoint() {
                when(userService.getAllUsers()).thenReturn(Collections.emptyList());
                given()
                                .when()
                                .get("/api/admin/users")
                                .then()
                                .statusCode(200)
                                .body("$.size()", is(0));
        }

        @Test
        @TestSecurity(user = "admin", roles = { "ADMIN" })
        void testDeleteUserEndpoint() {
                doNothing().when(adminService).deleteUser(any());
                given()
                                .when()
                                .delete("/api/admin/users/1")
                                .then()
                                .statusCode(204);
        }

        @Test
        @TestSecurity(user = "admin", roles = { "ADMIN" })
        void testAssignRoleToUserEndpoint() {
                User user = new User();
                user.setId(1L);
                user.setUsername("testuser");
                user.setFullName("Test User");
                user.setEmail("test@example.com");

                Role role = new Role();
                role.setName("LIBRARIAN");
                user.setRoles(new HashSet<>(Collections.singletonList(role)));

                when(adminService.assignRoleToUser(1L, "LIBRARIAN")).thenReturn(user);

                given()
                                .contentType("application/json")
                                .when()
                                .post("/api/admin/users/1/roles/LIBRARIAN")
                                .then()
                                .statusCode(200)
                                .body("id", is(1))
                                .body("username", is("testuser"))
                                .body("roles", hasItem("LIBRARIAN"));
        }
}
