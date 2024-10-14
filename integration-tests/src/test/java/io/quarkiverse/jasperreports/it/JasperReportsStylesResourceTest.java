package io.quarkiverse.jasperreports.it;

import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_OPENDOCUMENT_TEXT;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_RTF;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.TEXT_CSV;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsStylesResourceTest {

    @Test
    void testStyleCsv() {
        given()
                .when().header("accept", TEXT_CSV).get("/jasper/style")
                .then()
                .statusCode(200)
                .body(containsString("Regular (default): font size = 12, centered, border"));
    }

    @Test
    void testExportXML() {
        given()
                .when().header("accept", APPLICATION_XML).get("/jasper/style")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportHTML() {
        given()
                .when().header("accept", TEXT_HTML).get("/jasper/style")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportRTF() {
        given()
                .when().header("accept", APPLICATION_RTF).get("/jasper/style")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODT() {
        given()
                .when().header("accept", APPLICATION_OPENDOCUMENT_TEXT).get("/jasper/style")
                .then()
                .statusCode(200);
    }
}