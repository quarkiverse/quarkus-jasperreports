package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsCsvResourceTest {

    @Test
    void testCsvDatasource() {
        given()
                .when().get("/jasper/csv/ds")
                .then()
                .statusCode(200)
                .body(containsString("CsvDataSource.txt - CSV data source"));
    }

}
