package com.yasinsez.library.controller;

import com.yasinsez.library.dto.UserRequestDTO;
import com.yasinsez.library.dto.UserResponseDTO;
import com.yasinsez.library.dto.UserUpdateDTO;
import com.yasinsez.library.mapper.UserMapper;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/api/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Users", description = "User management operations")
public class UserController {

    @Inject
    UserService userService;

    @GET
    @RolesAllowed("ADMIN")
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    public Response getUsers(@QueryParam("filter") String filter) {
        List<User> users;
        if (filter == null || filter.isEmpty()) {
            users = userService.getAllUsers();
        } else {
            users = userService.searchUsers(filter);
        }
        List<UserResponseDTO> userResponseDTOS = users.stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
        return Response.ok(userResponseDTOS).build();
    }

    @POST
    @RolesAllowed("ADMIN")
    @Operation(summary = "Create new user", description = "Add a new user to the system")
    public Response createUser(@Valid UserRequestDTO userData) {
        User newUser = UserMapper.toEntity(userData);
        User createdUser = userService.createUser(newUser, userData.password(), userData.roleIds());
        return Response.status(Response.Status.CREATED).entity(UserMapper.toResponseDTO(createdUser)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Update user", description = "Update an existing user")
    public Response updateUser(@PathParam("id") Long id, @Valid UserUpdateDTO userData) {
        User updatedUser = userService.updateUser(id, userData);
        return Response.ok(UserMapper.toResponseDTO(updatedUser)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Deactivate user", description = "Deactivate a user")
    public Response deleteUser(@PathParam("id") Long id) {
        userService.deleteUser(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their ID")
    public Response getUserById(@PathParam("id") Long id) {
        User user = userService.findById(id);
        return Response.ok(UserMapper.toResponseDTO(user)).build();
    }

    @GET
    @Path("/search")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Search users", description = "Search for users by various criteria")
    public Response searchUsers(@QueryParam("q") String query) {
        List<User> users = userService.searchUsers(query != null ? query : "");
        List<UserResponseDTO> userResponseDTOS = users.stream()
                .map(UserMapper::toResponseDTO)
                .collect(Collectors.toList());
        return Response.ok(userResponseDTOS).build();
    }

    @GET
    @Path("/count")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Count users", description = "Get the total number of users")
    public Response countUsers() {
        long count = userService.countUsers();
        return Response.ok(Map.of("count", count)).build();
    }
}
