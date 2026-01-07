package com.yasinsez.library.controller;

import com.yasinsez.library.dto.ProfileUpdateRequest;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProfileControllerTest {

    @InjectMock
    UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Test User");
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"MEMBER"})
    void testGetMyProfile() {
        when(userService.getUserByUsername("testUser")).thenReturn(Optional.of(user));
        given()
                .when().get("/api/profile")
                .then()
                .statusCode(200)
                .body("fullName", is("Test User"));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"MEMBER"})
    void testUpdateMyProfile() {
        when(userService.getUserByUsername("testUser")).thenReturn(Optional.of(user));

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setFullName("Updated Name");

        when(userService.updateUserByUsername(eq("testUser"), any(User.class))).thenReturn(updatedUser);

        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("Updated Name");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when().put("/api/profile")
                .then()
                .statusCode(200)
                .body("fullName", is("Updated Name"));
    }

    @Test
    void testGetMyProfile_unauthorized() {
        given()
                .when().get("/api/profile")
                .then()
                .statusCode(401);
    }
}
