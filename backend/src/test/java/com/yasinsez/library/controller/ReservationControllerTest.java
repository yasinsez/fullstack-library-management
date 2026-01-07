package com.yasinsez.library.controller;

import com.yasinsez.library.dto.ReservationRequestDTO;
import com.yasinsez.library.model.enums.ReservationStatus;
import com.yasinsez.library.service.ReservationService;
import com.yasinsez.library.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import com.yasinsez.library.model.User;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

@QuarkusTest
class ReservationControllerTest {

    @InjectMock
    ReservationService reservationService;

    @InjectMock
    UserService userService;

    @Test
    @TestSecurity(user = "member", roles = { "MEMBER" })
    void testReserveBookEndpoint() {
        User user = new User();
        user.setId(1L);
        when(userService.getUserByUsername("member")).thenReturn(Optional.of(user));
        ReservationRequestDTO requestDTO = new ReservationRequestDTO(1L, 1L, null, ReservationStatus.PENDING, null);

        given()
                .contentType("application/json")
                .body(requestDTO)
                .when()
                .post("/api/reservations")
                .then()
                .statusCode(201);
    }

    @Test
    @TestSecurity(user = "librarian", roles = { "LIBRARIAN" })
    void testGetAllReservationsEndpoint() {
        given()
                .when()
                .get("/api/reservations")
                .then()
                .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = { "ADMIN" })
    void testUpdateReservationStatusEndpoint() {
        ReservationRequestDTO requestDTO = new ReservationRequestDTO(1L, 1L, null, ReservationStatus.CANCELLED, null);

        given()
                .contentType("application/json")
                .body(requestDTO)
                .when()
                .put("/api/reservations/1")
                .then()
                .statusCode(204);
    }
}
