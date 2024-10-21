package io.quarkiverse.jasperreports.it;

import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_DOCX;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_PDF;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JasperReportsFontResourceTest {

    @Test
    public void testExportDOCX() {
        given()
                .when().get("/jasper/font/docx")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(equalTo(APPLICATION_DOCX))
                .header(CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue())
                .and()
                .body(not(emptyString()));
    }

    @Test
    public void testExportPDF() {
        given()
                .when().get("/jasper/font/pdf")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(equalTo(APPLICATION_PDF))
                .header(CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue());
    }
}
