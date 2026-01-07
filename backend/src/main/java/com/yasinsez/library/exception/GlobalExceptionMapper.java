package com.yasinsez.library.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global JAX-RS exception mapper that converts exceptions to a JSON payload.
 * Centralising error handling avoids repetitive try/catch in controllers and
 * keeps them focused on use-case orchestration.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        // Customised mappings – extend as new business exceptions are introduced
        if (exception instanceof ResourceNotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
        } else if (exception instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorMessage(exception.getMessage()))
                .build();
    }

    /**
     * Simple error representation sent to clients.
     */
    public static class ErrorMessage {
        public final String message;

        public ErrorMessage(String message) {
            this.message = message;
        }
    }
}