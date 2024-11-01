package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsHyperLinkResourceTest {

    @Test
    void testHyperlinks() {
        given()
                .when().get("/jasper/hyperlink/pdf")
                .then()
                .statusCode(200);
    }

    @Test
    void testMarkup() {
        given()
                .when().get("/jasper/markup/pdf")
                .then()
                .statusCode(200);
    }

}