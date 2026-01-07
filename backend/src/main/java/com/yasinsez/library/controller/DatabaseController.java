package com.yasinsez.library.controller;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for database operations and health checks.
 */
@Path("/api/database")
@ApplicationScoped
public class DatabaseController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    @Inject
    AgroalDataSource dataSource;

    /**
     * Checks database connectivity.
     * 
     * @return database status response
     */
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout

            status.put("connected", isValid);
            status.put("message", isValid ? "Database connection successful" : "Database connection failed");

            if (isValid) {
                var metaData = connection.getMetaData();
                status.put("databaseProductName", metaData.getDatabaseProductName());
                status.put("databaseProductVersion", metaData.getDatabaseProductVersion());
                status.put("driverName", metaData.getDriverName());
                status.put("driverVersion", metaData.getDriverVersion());
            }

            return Response.ok(status).build();

        } catch (SQLException e) {
            logger.error("Database connection error", e);
            status.put("connected", false);
            status.put("message", "Database connection error: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(status).build();
        }
    }

    /**
     * Gets database information.
     * 
     * @return database information response
     */
    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            var metaData = connection.getMetaData();

            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            info.put("maxConnections", metaData.getMaxConnections());
            info.put("catalog", connection.getCatalog());
            info.put("schema", connection.getSchema());

            return Response.ok(info).build();

        } catch (SQLException e) {
            logger.error("Error getting database info", e);
            info.put("error", "Failed to get database info: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(info).build();
        }
    }
}
