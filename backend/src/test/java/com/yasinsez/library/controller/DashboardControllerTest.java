package com.yasinsez.library.controller;

import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.service.LoanService;
import com.yasinsez.library.service.ReservationService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@QuarkusTest
class DashboardControllerTest {

    @InjectMock
    LoanService loanService;

    @InjectMock
    ReservationService reservationService;

    @Test
    @TestSecurity(user = "testUser", roles = {"MEMBER"})
    void testGetMyLoans() {
        when(loanService.getLoansForMember(anyLong())).thenReturn(List.of(new Loan()));
        given()
                .when().get("/api/dashboard/loans")
                .then()
                .statusCode(200)
                .body("$.size()", is(1));
    }

    @Test
    @TestSecurity(user = "testUser", roles = {"MEMBER"})
    void testGetMyReservations() {
        when(reservationService.getReservationsByMember(anyLong())).thenReturn(List.of(new Reservation()));
        given()
                .when().get("/api/dashboard/reservations")
                .then()
                .statusCode(200)
                .body("$.size()", is(1));
    }

    @Test
    void testGetMyLoans_unauthorized() {
        given()
                .when().get("/api/dashboard/loans")
                .then()
                .statusCode(401);
    }
}
