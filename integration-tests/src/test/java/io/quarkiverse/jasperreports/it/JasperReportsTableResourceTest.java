package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsTableResourceTest {

    @Test
    void testTableSubDataSet() {
        given()
                .when().get("/jasper/table/csv")
                .then()
                .statusCode(200)
                .body(containsString("Hugo Meier"));
    }

    @Test
    void testComplexInvoice() {
        given()
                .when().get("/jasper/table/invoice")
                .then()
                .statusCode(200)
                .body(containsString("Dr Meier"));
    }
}
