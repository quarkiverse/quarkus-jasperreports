package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JasperReportsImageResourceTest {

    @Test
    void testExportHTML() {
        given()
                .when().header("accept", TEXT_HTML).get("/jasper/image")
                .then()
                .assertThat()
                .statusCode(200)
                .body(notNullValue())
                .and()
                .body(not(emptyString()))
                .and()
                .body(not(containsString("<img src=\"\"")));
    }

}
