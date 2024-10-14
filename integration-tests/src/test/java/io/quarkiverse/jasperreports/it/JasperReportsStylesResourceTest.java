package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class JasperReportsStylesResourceTest {

    @Test
    void testStyleDatasource() {
        given()
                .when().header("accept", "text/csv").get("jasper/style")
                .then()
                .statusCode(200)
                .body(containsString("Regular (default): font size = 12, centered, border"));
    }

}
