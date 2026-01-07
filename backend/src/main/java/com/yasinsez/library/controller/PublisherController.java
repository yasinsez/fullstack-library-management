package com.yasinsez.library.controller;

import com.yasinsez.library.dto.PublisherDTO;
import com.yasinsez.library.mapper.PublisherMapper;
import com.yasinsez.library.service.PublisherService;
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

@Path("/api/publishers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Publishers", description = "Publisher management operations")
public class PublisherController {

    @Inject
    PublisherService publisherService;

    @GET
    @Operation(summary = "Get all publishers")
    @PermitAll
    public List<PublisherDTO> getAllPublishers() {
        return publisherService.getAllPublishers().stream()
                .map(PublisherMapper::toDTO)
                .collect(Collectors.toList());
    }
}
