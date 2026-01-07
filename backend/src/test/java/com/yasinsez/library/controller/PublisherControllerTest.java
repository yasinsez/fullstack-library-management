package com.yasinsez.library.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class PublisherControllerTest {

    @Test
    void testGetAllPublishersEndpoint() {
        given()
                .when().get("/api/publishers")
                .then()
                .statusCode(200);
    }
}
