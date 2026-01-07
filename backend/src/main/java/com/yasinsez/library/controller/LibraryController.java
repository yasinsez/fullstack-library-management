package com.yasinsez.library.controller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Path("/api/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Library", description = "Library information")
public class LibraryController {

    @GET
    @Operation(summary = "Get library information")
    public Response getLibraryInfo() {
        Map<String, String> libraryInfo = Map.of(
                "name", "Modern Library",
                "address", "123 Library St, Booksville, BK 12345",
                "openingHours", "Mon-Fri: 9am - 9pm, Sat-Sun: 10am - 6pm"
        );
        return Response.ok(libraryInfo).build();
    }
}
