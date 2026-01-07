package com.yasinsez.library.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class LibraryControllerTest {

    @Test
    void testGetLibraryInfo() {
        given()
                .when().get("/api/library")
                .then()
                .statusCode(200)
                .body("name", is("Modern Library"))
                .body("address", is("123 Library St, Booksville, BK 12345"));
    }
}
