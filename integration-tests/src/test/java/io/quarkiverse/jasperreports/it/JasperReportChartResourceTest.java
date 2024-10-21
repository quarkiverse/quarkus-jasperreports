package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JasperReportChartResourceTest {

    @Test
    public void testPdfExport() {
        given()
                .when().get("/jasper/chart")
                .then()
                .statusCode(200)
                .header(HttpHeaders.CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue());
    }

}
