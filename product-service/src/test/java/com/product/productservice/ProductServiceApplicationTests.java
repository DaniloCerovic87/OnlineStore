package com.product.productservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

    @Autowired
    private MongoDBContainer mongoDBContainer;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        if (!mongoDBContainer.isRunning()) {
            mongoDBContainer.start();
        }
    }

    @Test
    void shouldCreateProduct() {
        String requestBody = """
                					{
                					"name":"iPhone 16",
                					"description":"iPhone 16 is smartphone from Apple",
                					"price":1200
                					}
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/product")
                .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("iPhone 16"))
                .body("description", Matchers.equalTo("iPhone 16 is smartphone from Apple"))
                .body("price", Matchers.equalTo(1200));
    }

}
