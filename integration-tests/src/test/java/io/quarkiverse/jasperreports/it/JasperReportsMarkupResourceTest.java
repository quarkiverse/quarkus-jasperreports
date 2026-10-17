package io.quarkiverse.jasperreports.it;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Verifies markup rendering by checking the text of the generated PDF, see issue #161.
 */
@QuarkusTest
class JasperReportsMarkupResourceTest {

    @Test
    void testMarkup() throws IOException {
        byte[] pdf = given()
                .when().get("/jasper/markup/pdf")
                .then()
                .statusCode(200)
                .extract().asByteArray();

        String text = extractText(pdf);

        // content inside HTML tags must be rendered, not stripped
        assertTrue(text.contains("text field"), "Missing HTML inline content: " + text);
        assertTrue(text.contains("apple"), "Missing HTML bulleted list item: " + text);
        assertTrue(text.contains("banana"), "Missing HTML bulleted list item: " + text);
        assertTrue(text.contains("football"), "Missing HTML numbered list item: " + text);
        assertTrue(text.contains("circle"), "Missing HTML lettered list item: " + text);
        assertTrue(text.contains("zebra"), "Missing last HTML list item: " + text);

        // markup tags must be interpreted, not printed literally
        assertFalse(text.contains("<LI>"), "HTML tags were printed literally: " + text);
        assertFalse(text.contains("<B>"), "HTML tags were printed literally: " + text);
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

}
