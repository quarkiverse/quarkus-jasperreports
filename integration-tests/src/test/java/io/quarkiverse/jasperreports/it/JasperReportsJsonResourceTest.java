package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsJsonResourceTest {

    @Test
    void testJsonDatasource() {
        given()
                .when().get("/jasper/json/ds")
                .then()
                .statusCode(200)
                .body(containsString("Customer Orders Report JSON"));
    }

    @Test
    void testJsonqlDatasource() {
        given()
                .when().get("/jasper/json/jsonql")
                .then()
                .statusCode(200)
                .body(containsString("Customer Orders Report JsonQL"));
    }
}