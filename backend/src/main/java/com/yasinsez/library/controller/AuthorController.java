package com.yasinsez.library.controller;

import com.yasinsez.library.dto.AuthorDTO;
import com.yasinsez.library.mapper.AuthorMapper;
import com.yasinsez.library.service.AuthorService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/authors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authors", description = "Author management operations")
public class AuthorController {

    @Inject
    AuthorService authorService;

    @POST
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Create a new author")
    public Response createAuthor(AuthorDTO authorDTO) {
        var author = authorService.createAuthor(authorDTO);
        return Response.status(Response.Status.CREATED).entity(AuthorMapper.toDTO(author)).build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Update an existing author")
    public Response updateAuthor(@PathParam("id") Long id, AuthorDTO authorDTO) {
        var updatedAuthor = authorService.updateAuthor(id, authorDTO);
        return Response.ok(AuthorMapper.toDTO(updatedAuthor)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({ "ADMIN", "LIBRARIAN" })
    @Operation(summary = "Delete an author")
    public Response deleteAuthor(@PathParam("id") Long id) {
        authorService.deleteAuthor(id);
        return Response.noContent().build();
    }

    @GET
    @Operation(summary = "Get all authors")
    @PermitAll
    public List<AuthorDTO> getAllAuthors() {
        return authorService.getAllAuthors().stream()
                .map(AuthorMapper::toDTO)
                .collect(Collectors.toList());
    }
}
