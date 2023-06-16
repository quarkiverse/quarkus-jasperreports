package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperreportsResourceTest {

    @Test
    void testExportPDF() {
        given()
                .when().post("/jasperreports/pdf")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportRTF() {
        given()
                .when().post("/jasperreports/rtf")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXML() {
        given()
                .when().post("/jasperreports/xml")
                .then()
                .statusCode(200);

        given()
                .when().post("/jasperreports/xml?embedded=true")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportHTML() {
        given()
                .when().post("/jasperreports/html")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLS() {
        given()
                .when().post("/jasperreports/xls")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportCSV() {
        given()
                .when().post("/jasperreports/csv")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODT() {
        given()
                .when().post("/jasperreports/odt")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportODS() {
        given()
                .when().post("/jasperreports/ods")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportDOCX() {
        given()
                .when().post("/jasperreports/docx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportXLSX() {
        given()
                .when().post("/jasperreports/xlsx")
                .then()
                .statusCode(200);
    }

    @Test
    void testExportPPTX() {
        given()
                .when().post("/jasperreports/pptx")
                .then()
                .statusCode(200);
    }
}
