package com.yasinsez.library.controller;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
class CategoryControllerTest {

    @Test
    void testGetAllCategoriesEndpoint() {
        given()
                .when().get("/api/categories")
                .then()
                .statusCode(200);
    }
}
