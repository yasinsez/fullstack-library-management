package com.yasinsez.library.database;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse; 
import org.eclipse.microprofile.health.Readiness;
import io.agroal.api.AgroalDataSource;

/**
 * Health check for database connectivity.
 */
@ApplicationScoped
@Readiness
public class DatabaseHealthCheck implements HealthCheck {

    @Inject
    AgroalDataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try {
            // Simple connection test
            dataSource.getConnection().close();
            return HealthCheckResponse.up("Database connection successful");
        } catch (Exception e) {
            return HealthCheckResponse.down("Database connection failed: " + e.getMessage());
        }
    }
}
