package com.yasinsez.library.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class AuthorControllerTest {

    @Test
    void testGetAllAuthorsEndpoint() {
        given()
                .when().get("/api/authors")
                .then()
                .statusCode(200);
    }
}
