package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsPdfEncryptResourceTest {

    @Test
    void testPdfEncryption() {
        given()
                .when().get("/jasper/pdf/encrypt")
                .then()
                .statusCode(200);
    }

}
