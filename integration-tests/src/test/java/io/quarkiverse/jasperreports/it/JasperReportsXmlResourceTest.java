package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsXmlResourceTest {

    @Test
    void testExportCSV() {
        given()
                .when().get("/jasper/xml/csv")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXML() {
        given()
                .when().get("/jasper/xml/xml")
                .then()
                .statusCode(200);

        given()
                .when().get("/jasper/xml/xml?embedded=true")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportHTML() {
        given()
                .when().get("/jasper/xml/html")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportRTF() {
        given()
                .when().get("/jasper/xml/rtf")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODT() {
        given()
                .when().get("/jasper/xml/odt")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODS() {
        given()
                .when().get("/jasper/xml/ods")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportDOCX() {
        given()
                .when().get("/jasper/xml/docx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLSX() {
        given()
                .when().get("/jasper/xml/xlsx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportPPTX() {
        given()
                .when().get("/jasper/xml/pptx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLS() {
        given()
                .when().get("/jasper/xml/xls")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportPDF() {
        given()
                .when().get("/jasper/xml/pdf")
                .then()
                .statusCode(200);
    }

}