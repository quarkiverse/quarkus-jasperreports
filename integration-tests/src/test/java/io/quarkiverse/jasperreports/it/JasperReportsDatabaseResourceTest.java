package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsDatabaseResourceTest {

    @Test
    void testEntities() {
        given()
                .when().get("/jasper/db/")
                .then()
                .body(is("Panache EntityCount - 3"));
    }

    @Test
    void testExportText() {
        given()
                .when().get("/jasper/db/text")
                .then()
                .statusCode(200)
                .body(containsString("field-3"));
    }

}
