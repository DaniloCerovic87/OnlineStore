package com.order.orderservice;

import com.order.orderservice.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

    @Autowired
    private MySQLContainer<?> mySQLContainer;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        if (!mySQLContainer.isRunning()) {
            mySQLContainer.start();
        }
    }

    @Test
    void shouldCreateOrder() {
        String requestBody = """
                		{
                      "skuCode":"iphone_15",
                      "price":1000,
                      "quantity":1
                  }
                """;

        InventoryClientStub.stubInventoryCallReturnTrue("iphone_15", 1);

        var responseBodyString = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .body().asString();

        assertEquals(responseBodyString, "Order Placed Successfully");

    }

    @Test
    void shouldNoCreateOrderNotEnoughInStock() {
        String requestBody = """
                		{
                      "skuCode":"iphone_15",
                      "price":1000,
                      "quantity":101
                  }
                """;

        InventoryClientStub.stubInventoryCallReturnFalse("iphone_15", 101);

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(500);

    }


}
