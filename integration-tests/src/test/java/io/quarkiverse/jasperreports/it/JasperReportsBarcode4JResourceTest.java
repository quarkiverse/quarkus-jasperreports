package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsBarcode4JResourceTest {

    @Test
    void testBarcode4JPdf() {
        given()
                .when().get("jasper/barcode4j/pdf")
                .then()
                .statusCode(200);
    }

}
