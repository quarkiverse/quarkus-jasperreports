package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsResourceTest {

    @Test
    void testExportCSV() {
        given()
                .when().get("/jasperreports/csv")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXML() {
        given()
                .when().get("/jasperreports/xml")
                .then()
                .statusCode(200);

        given()
                .when().get("/jasperreports/xml?embedded=true")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportHTML() {
        given()
                .when().get("/jasperreports/html")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportRTF() {
        given()
                .when().get("/jasperreports/rtf")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODT() {
        given()
                .when().get("/jasperreports/odt")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODS() {
        given()
                .when().get("/jasperreports/ods")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportDOCX() {
        given()
                .when().get("/jasperreports/docx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLSX() {
        given()
                .when().get("/jasperreports/xlsx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportPPTX() {
        given()
                .when().get("/jasperreports/pptx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLS() {
        given()
                .when().get("/jasperreports/xls")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportPDF() {
        given()
                .when().get("/jasperreports/pdf")
                .then()
                .statusCode(200);
    }

}