package com.yasinsez.library.controller;

import com.yasinsez.library.dto.AuthResponse;
import com.yasinsez.library.dto.LoginRequest;
import com.yasinsez.library.dto.RegisterRequest;
import com.yasinsez.library.model.User;
import com.yasinsez.library.service.AuthenticationService;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.yasinsez.library.dto.UserMeDTO;
import com.yasinsez.library.service.UserService;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "User authentication operations")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Inject
    AuthenticationService authenticationService;

    @Inject
    UserService userService;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @GET
    @Path("/me")
    @RolesAllowed({ "ADMIN", "MEMBER", "LIBRARIAN" })
    public Response getMe() {
        if (jwt == null || jwt.getName() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String username = jwt.getName();
        var userMeDTO = userService.getUserMe(username);

        return Response.ok(userMeDTO).build();
    }

    @POST
    @Path("/register")
    public Response register(RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());

            authenticationService.register(user, request.getPassword());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            log.error("Error during registration", e);
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            log.info("Attempting to log in user: {}", request.getUsername());
            User user = authenticationService.authenticate(request.getUsername(), request.getPassword());
            log.info("User '{}' authenticated successfully", user.getUsername());

            Set<String> roles = new HashSet<>();
            if (user.getRoles() != null) {
                log.info("Retrieving roles for user '{}'", user.getUsername());
                roles = user.getRoles().stream()
                        .filter(role -> role != null && role.getName() != null)
                        .map(role -> {
                            log.debug("Found role: {}", role.getName());
                            return role.getName();
                        })
                        .collect(Collectors.toSet());
                log.info("Roles found: {}", roles);
            } else {
                log.warn("User '{}' has no roles assigned.", user.getUsername());
            }

            log.info("Generating JWT token for user '{}'", user.getUsername());
            String token = Jwt.issuer(issuer)
                    .subject(user.getUsername())
                    .claim("fullName", user.getFullName())
                    .groups(roles)
                    .expiresIn(Duration.ofHours(1))
                    .sign();
            log.info("Token generated successfully for user '{}'", user.getUsername());

            return Response.ok(new AuthResponse(token)).build();
        } catch (NotAuthorizedException e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            log.error("An unexpected error occurred during login for user: {}", request.getUsername(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An internal error occurred.").build();
        }
    }
}
