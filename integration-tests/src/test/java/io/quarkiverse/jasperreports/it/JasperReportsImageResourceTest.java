package io.quarkiverse.jasperreports.it;

import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_PDF;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_XLSX;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JasperReportsImageResourceTest {

    @Test
    public void testExportHTML() {
        given()
                .when().get("/jasper/image/html")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(equalTo(TEXT_HTML))
                .header(CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue())
                .and()
                .body(not(emptyString()))
                .and()
                .body(not(containsString("<img src=\"\"")));
    }

    @Test
    public void testExportPDF() {
        given()
                .when().get("/jasper/image/pdf")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(equalTo(APPLICATION_PDF))
                .header(CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue());
    }

    @Test
    public void testExportXLSX() {
        given()
                .when().get("/jasper/image/xlsx")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(equalTo(APPLICATION_XLSX))
                .header(CONTENT_LENGTH, Integer::parseInt, greaterThan(0))
                .body(notNullValue());
    }

}
