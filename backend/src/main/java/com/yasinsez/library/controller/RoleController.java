package com.yasinsez.library.controller;

import com.yasinsez.library.dto.RoleResponseDTO;
import com.yasinsez.library.mapper.RoleMapper;
import com.yasinsez.library.service.RoleService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/roles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Roles", description = "Role management operations")
public class RoleController {

    @Inject
    RoleService roleService;

    @GET
    @Operation(summary = "Get all roles")
    @PermitAll
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.getAllRoles().stream()
                .map(RoleMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
