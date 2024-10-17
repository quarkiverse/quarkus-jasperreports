package io.quarkiverse.jasperreports.it;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Path("jasper/barcode4j")
@ApplicationScoped
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class JasperReportsBarcode4JResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "Barcode4JReport.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @APIResponse(responseCode = "200", description = "Fetch a report with barcodes", content = @Content(mediaType = ExtendedMediaType.APPLICATION_PDF))
    @GET
    @Path("/pdf")
    @Produces({ ExtendedMediaType.APPLICATION_PDF })
    public Response pdf() throws JRException {
        final long start = System.currentTimeMillis();
        JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME,
                new HashMap<>(), new JREmptyDataSource());

        ByteArrayOutputStream outputStream = exportPdf(jasperPrint);
        Log.infof("PDF creation time : %s", (System.currentTimeMillis() - start));

        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=barcode4j.pdf");
        response.header(HttpHeaders.CONTENT_TYPE, ExtendedMediaType.APPLICATION_PDF);
        return response.build();
    }
}
