package com.yasinsez.library.controller;

import com.yasinsez.library.dto.ProfileResponse;
import com.yasinsez.library.dto.ChangePasswordRequest;
import com.yasinsez.library.dto.ProfileUpdateRequest;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.UserService;
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

@Path("/api/profile")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({ "MEMBER", "ADMIN" })
@Tag(name = "Member Profile", description = "Operations for the member profile")
@SecurityRequirement(name = "jwt")
public class ProfileController {

    @Inject
    UserService userService;

    @GET
    @Operation(summary = "Get the current member's profile")
    public Response getMyProfile(@Context SecurityContext securityContext) {
        String userName = securityContext.getUserPrincipal().getName();
        return userService.getUserByUsername(userName)
                .map(user -> Response.ok(toProfileResponse(user)).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @PUT
    @Operation(summary = "Update the current member's profile")
    public Response updateMyProfile(ProfileUpdateRequest request, @Context SecurityContext securityContext) {
        String userName = securityContext.getUserPrincipal().getName();

        User updatedDetails = new User();
        updatedDetails.setFullName(request.getFullName());
        updatedDetails.setPhoneNumber(request.getPhoneNumber());

        User updatedUser = userService.updateUserByUsername(userName, updatedDetails);
        return Response.ok(toProfileResponse(updatedUser)).build();
    }

    @POST
    @Path("/change-password")
    @Operation(summary = "Change the current user's password")
    public Response changePassword(ChangePasswordRequest request, @Context SecurityContext securityContext) {
        String userName = securityContext.getUserPrincipal().getName();
        var userOpt = userService.getUserByUsername(userName);
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        boolean changed = userService.changePassword(userOpt.get().getId(), request.getCurrentPassword(),
                request.getNewPassword());
        if (!changed) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid current password").build();
        }
        return Response.noContent().build();
    }

    private ProfileResponse toProfileResponse(User user) {
        return ProfileResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(user.getRoles() == null ? null
                        : user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
                .build();
    }
}
