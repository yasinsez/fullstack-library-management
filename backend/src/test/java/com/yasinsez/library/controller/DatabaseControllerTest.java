package com.yasinsez.library.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DatabaseControllerTest {

    @Test
    public void testDatabaseStatusEndpoint() {
        given()
                .when().get("/api/database/status")
                .then()
                .statusCode(200)
                .body("connected", is(true))
                .body("databaseProductName", notNullValue());
    }

    @Test
    public void testDatabaseInfoEndpoint() {
        given()
                .when().get("/api/database/info")
                .then()
                .statusCode(200)
                .body("databaseProductName", notNullValue());
    }
}
