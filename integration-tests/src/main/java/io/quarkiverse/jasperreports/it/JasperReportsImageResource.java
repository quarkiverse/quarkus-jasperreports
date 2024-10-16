package io.quarkiverse.jasperreports.it;

import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_PDF;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_XLSX;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.SimpleReportContext;

@Path("jasper/image")
@ApplicationScoped
public class JasperReportsImageResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "ImagesReport.jasper";

    @Context
    UriInfo uriInfo;

    @Inject
    ReadOnlyStreamingService repo;

    @APIResponse(responseCode = "200", description = "Fetch an HTML report with images", content = @Content(mediaType = TEXT_HTML))
    @GET
    @Path("html")
    public Response getHtml() throws JRException, IOException {
        final SimpleReportContext reportContext = new SimpleReportContext();
        final Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);
        params.put("QUARKUS_URL", uriInfo.getBaseUri().toString());

        final long start = System.currentTimeMillis();
        final JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, params,
                new JREmptyDataSource());

        final Response.ResponseBuilder response = Response.ok();
        ByteArrayOutputStream outputStream = exportHtml(jasperPrint, reportContext);
        Log.infof("HTML creation time : %s", (System.currentTimeMillis() - start));
        response.entity(outputStream.toByteArray());
        response.type(TEXT_HTML);

        return response.build();
    }

    @APIResponse(responseCode = "200", description = "Fetch a PDF report with images", content = @Content(mediaType = APPLICATION_PDF))
    @GET
    @Path("pdf")
    public Response getPdf() throws JRException {
        final SimpleReportContext reportContext = new SimpleReportContext();
        final Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);
        params.put("QUARKUS_URL", uriInfo.getBaseUri().toString());

        final long start = System.currentTimeMillis();
        final JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, params,
                new JREmptyDataSource());

        final Response.ResponseBuilder response = Response.ok();
        ByteArrayOutputStream outputStream = exportPdf(jasperPrint);
        Log.infof("PDF creation time : %s", (System.currentTimeMillis() - start));
        response.entity(outputStream.toByteArray());
        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.pdf");
        response.type(APPLICATION_PDF);

        return response.build();
    }

    @APIResponse(responseCode = "200", description = "Fetch an XLSX report with images", content = @Content(mediaType = APPLICATION_XLSX))
    @GET
    @Path("xlsx")
    public Response getXlsx() throws JRException {
        final SimpleReportContext reportContext = new SimpleReportContext();
        final Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);
        params.put("QUARKUS_URL", uriInfo.getBaseUri().toString());

        final long start = System.currentTimeMillis();
        final JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, params,
                new JREmptyDataSource());

        final Response.ResponseBuilder response = Response.ok();
        ByteArrayOutputStream outputStream = exportXlsx(jasperPrint);
        Log.infof("XLSX creation time : %s", (System.currentTimeMillis() - start));
        response.entity(outputStream.toByteArray());
        response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.xlsx");
        response.type(APPLICATION_XLSX);

        return response.build();
    }

}
