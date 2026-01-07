package com.yasinsez.library.controller;

import com.yasinsez.library.dto.UserCreateRequest;
import com.yasinsez.library.dto.UserDTO;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.AdminService;
import com.yasinsez.library.service.UserService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
@Tag(name = "Admin - User Management", description = "Admin operations for managing users")
@SecurityRequirement(name = "jwt")
public class AdminController {

    @Inject
    AdminService adminService;

    @Inject
    UserService userService;

    @POST
    @Operation(summary = "Create a new user")
    public Response createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword()); // Will be hashed in the service

        User createdUser = adminService.createUserWithRole(user, request.getPassword(), request.getRole());
        return Response.status(Response.Status.CREATED).entity(new UserDTO(createdUser)).build();
    }

    @GET
    @Operation(summary = "Get all users")
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(UserDTO::new).collect(Collectors.toList());
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete a user")
    public Response deleteUser(@PathParam("id") Long id) {
        adminService.deleteUser(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/{userId}/roles/{roleName}")
    @Operation(summary = "Assign a role to a user")
    public Response assignRoleToUser(@PathParam("userId") Long userId, @PathParam("roleName") String roleName) {
        User updatedUser = adminService.assignRoleToUser(userId, roleName);
        return Response.ok(new UserDTO(updatedUser)).build();
    }
}
