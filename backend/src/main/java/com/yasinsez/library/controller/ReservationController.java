package com.yasinsez.library.controller;

import com.yasinsez.library.dto.ReservationRequestDTO;
import com.yasinsez.library.dto.ReservationResponseDTO;
import com.yasinsez.library.mapper.ReservationMapper;
import com.yasinsez.library.model.Reservation;
import com.yasinsez.library.model.enums.ReservationStatus;
import com.yasinsez.library.service.ReservationService;
import com.yasinsez.library.repository.MemberRepository;
import com.yasinsez.library.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.security.PermitAll;

@Path("/api/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Reservations", description = "Book reservation operations")
public class ReservationController {

    @Inject
    ReservationService reservationService;

    @Inject
    MemberRepository memberRepository;

    @Inject
    UserService userService;

    @POST
    @RolesAllowed("MEMBER")
    @Operation(summary = "Reserve a book")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public Response reserveBook(@Valid ReservationRequestDTO request, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        Long memberId = memberRepository.findByUsername(username)
                .map(m -> m.getId())
                .orElseGet(() -> userService.getUserByUsername(username)
                        .orElseThrow(() -> new NotFoundException("User not found with username: " + username))
                        .getId());
        Reservation reservation = reservationService.createReservation(memberId, request.bookId());
        return Response.status(Response.Status.CREATED).entity(ReservationMapper.toResponseDTO(reservation)).build();
    }

    @GET
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "List all reservations")
    @SecurityRequirement(name = "jwt")
    public List<ReservationResponseDTO> getAllReservations() {
        return reservationService.getAllReservations().stream()
                .map(ReservationMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Update reservation status")
    @SecurityRequirement(name = "jwt")
    public Response updateReservationStatus(@PathParam("id") Long id, com.fasterxml.jackson.databind.node.ObjectNode body) {
        if (body == null || body.get("status") == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing status").build();
        }
        ReservationStatus status;
        try {
            status = ReservationStatus.valueOf(body.get("status").asText());
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid status").build();
        }
        reservationService.updateReservationStatus(id, status);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Cancel (soft delete) or hard delete a reservation (admin/librarian)")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public Response deleteReservation(@PathParam("id") Long id, @QueryParam("hard") @DefaultValue("false") boolean hard) {
        if (hard) {
            reservationService.hardDeleteReservation(id);
        } else {
            reservationService.cancelReservation(id);
        }
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({ "MEMBER", "LIBRARIAN", "ADMIN" })
    @Operation(summary = "Get current user's reservations")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public List<ReservationResponseDTO> getMyReservations(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        Long memberId = memberRepository.findByUsername(username)
                .map(m -> m.getId())
                .orElseGet(() -> userService.getUserByUsername(username)
                        .orElseThrow(() -> new NotFoundException("User not found with username: " + username))
                        .getId());
        return reservationService.getReservationsByMember(memberId).stream()
                .map(ReservationMapper::toResponseDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @POST
    @Path("/{id}/cancel")
    @RolesAllowed({ "MEMBER", "LIBRARIAN", "ADMIN" })
    @Operation(summary = "Cancel own reservation (member) or by staff")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public Response cancelReservationByMember(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        // Members can cancel their own; staff can also call this for convenience
        try {
            reservationService.cancelReservationByMember(id, username);
        } catch (jakarta.ws.rs.BadRequestException ex) {
            // If staff is cancelling someone else's via this endpoint, fallback to admin cancel
            if (securityContext.isUserInRole("ADMIN") || securityContext.isUserInRole("LIBRARIAN")) {
                reservationService.cancelReservation(id);
            } else {
                throw ex;
            }
        }
        return Response.noContent().build();
    }

    @POST
    @Path("/{id}/borrow")
    @RolesAllowed({ "MEMBER" })
    @Operation(summary = "Borrow a READY_FOR_PICKUP reservation for the current member")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public Response borrowReadyReservation(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        var dto = reservationService.borrowReadyReservation(id, username);
        return Response.ok(dto).build();
    }

    @PUT
    @Path("/{id}/priority")
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Update reservation queue position (pending only)")
    @SecurityRequirement(name = "jwt")
    @Transactional
    public Response updatePriority(@PathParam("id") Long id, com.fasterxml.jackson.databind.node.ObjectNode body) {
        if (body == null || body.get("priority") == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing priority").build();
        }
        int newPriority = body.get("priority").asInt();
        reservationService.updateReservationPriority(id, newPriority);
        return Response.noContent().build();
    }
}
