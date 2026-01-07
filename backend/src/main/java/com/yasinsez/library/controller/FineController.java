package com.yasinsez.library.controller;

import com.yasinsez.library.dto.FineResponseDTO;
import com.yasinsez.library.mapper.FineMapper;
import com.yasinsez.library.model.Member;
import com.yasinsez.library.repository.FineRepository;
import com.yasinsez.library.repository.MemberRepository;
import com.yasinsez.library.service.FineService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
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

@Path("/api/fines")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Fines", description = "Fine management operations")
@SecurityRequirement(name = "jwt")
public class FineController {

    @Inject
    FineRepository fineRepository;

    @Inject
    FineService fineService;

    @Inject
    MemberRepository memberRepository;

    @GET
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "List all fines")
    public List<FineResponseDTO> getAllFines() {
        return fineRepository.listAllOrderByIssueDateDesc().stream()
                .map(FineMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GET
    @Path("/my")
    @RolesAllowed({ "MEMBER", "ADMIN" })
    @Operation(summary = "Get current user's fines")
    public Response getMyFines(@Context SecurityContext securityContext) {
        String username = securityContext.getUserPrincipal().getName();
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        List<FineResponseDTO> fines = fineRepository.findByMemberOrderByIssueDateDesc(member).stream()
                .map(FineMapper::toResponseDTO)
                .collect(Collectors.toList());
        return Response.ok(fines).build();
    }

    @PUT
    @Path("/{id}/pay")
    @RolesAllowed({ "MEMBER", "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Mark a fine as paid")
    public Response payFine(@PathParam("id") Long id) {
        fineService.payFine(id);
        return Response.noContent().build();
    }
}
