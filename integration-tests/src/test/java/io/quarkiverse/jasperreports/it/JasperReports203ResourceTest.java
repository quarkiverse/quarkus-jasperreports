package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReports203ResourceTest {

    @Test
    void testCsvDatasource() {
        given()
                .when().get("/jasper/203")
                .then()
                .statusCode(200);
    }

}
