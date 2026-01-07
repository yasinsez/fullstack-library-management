package com.yasinsez.library.controller;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check endpoint for the application.
 */
@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Health", description = "Health check operations")
public class HealthController {

    @GET
    @Operation(summary = "Health check", description = "Check if the application is running")
    public Response health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Modern Library System");
        response.put("version", "1.0.0");
        return Response.ok(response).build();
    }
}