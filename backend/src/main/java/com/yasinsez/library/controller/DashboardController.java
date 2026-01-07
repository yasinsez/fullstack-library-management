package com.yasinsez.library.controller;

import com.yasinsez.library.model.Loan;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.service.LoanService;
import com.yasinsez.library.service.ReservationService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/api/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Member Dashboard", description = "Operations for the member dashboard")
@SecurityRequirement(name = "jwt")
public class DashboardController {

    @Inject
    LoanService loanService;

    @Inject
    ReservationService reservationService;

    @GET
    @Path("/loans")
    @RolesAllowed({ "MEMBER", "LIBRARIAN", "ADMIN" })
    @Operation(summary = "Get the current member's borrowed books")
    public List<Loan> getMyLoans(@Context SecurityContext securityContext) {
        // In a real application, you would get the member ID from the security context
        Long memberId = 1L; // Hardcoded for now
        return loanService.getLoansForMember(memberId);
    }

    @GET
    @Path("/reservations")
    @RolesAllowed({ "MEMBER", "LIBRARIAN", "ADMIN" })
    @Operation(summary = "Get the current member's reservations")
    public List<Reservation> getMyReservations(@Context SecurityContext securityContext) {
        // In a real application, you would get the member ID from the security context
        Long memberId = 1L; // Hardcoded for now
        return reservationService.getReservationsByMember(memberId);
    }
}
