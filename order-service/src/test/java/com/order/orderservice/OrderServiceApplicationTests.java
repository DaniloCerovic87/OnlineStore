package com.order.orderservice;

import com.order.orderservice.event.OrderPlacedEvent;
import com.order.orderservice.stubs.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceApplicationTests {

    @Autowired
    private MySQLContainer<?> mySQLContainer;

    @LocalServerPort
    private int port;

    @MockitoBean
    private KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

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
                      "quantity":101,
                      "userDetails":{
                      "email":"mika@gmail.com",
                      "firstName":"Danilo",
                      "lastName":"Cerovic"
                      }
                  }
                """;

        InventoryClientStub.stubInventoryCallReturnTrue("iphone_15", 101);

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .header("Location", Matchers.containsString("/api/order/"))
                .body("id", Matchers.notNullValue())
                .body("skuCode", Matchers.equalTo("iphone_15"))
                .body("price", Matchers.equalTo(1000))
                .body("quantity", Matchers.equalTo(101));

        Mockito.verify(kafkaTemplate).send(eq("order-placed"), any(OrderPlacedEvent.class));
    }

    @Test
    void shouldNoCreateOrderNotEnoughInStock() {
        String requestBody = """
                		{
                      "skuCode":"iphone_15",
                      "price":1000,
                      "quantity":101,
                      "userDetails":{
                      "email":"mika@gmail.com",
                      "firstName":"Danilo",
                      "lastName":"Cerovic"
                      }
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
