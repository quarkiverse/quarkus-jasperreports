package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JasperreportsResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/jasperreports")
                .then()
                .statusCode(200)
                .body(is("Hello jasperreports"));
    }
}
