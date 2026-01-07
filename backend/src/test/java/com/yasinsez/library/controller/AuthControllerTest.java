package com.yasinsez.library.controller;

import com.yasinsez.library.dto.LoginRequest;
import com.yasinsez.library.dto.RegisterRequest;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.AuthenticationService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AuthControllerTest {

    @InjectMock
    AuthenticationService authenticationService;

    @Test
    public void testRegisterEndpoint() {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        when(authenticationService.register(any(User.class), anyString())).thenReturn(mockUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password");
        request.setFullName("Test User");
        request.setEmail("test@example.com");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when().post("/api/auth/register")
                .then()
                .statusCode(201);
    }

    @Test
    public void testLoginEndpoint() {
        User user = new User();
        user.setUsername("testuser");
        user.setFullName("Test User");
        when(authenticationService.authenticate(anyString(), anyString())).thenReturn(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue());
    }
}
