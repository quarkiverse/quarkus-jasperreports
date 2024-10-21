package io.quarkiverse.jasperreports.it;

import java.sql.SQLException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;

@Path("jasper/chart")
@ApplicationScoped
public class JasperReportChartResource {

    @Inject
    DatabaseReportService databaseReportService;

    @GET
    @Path("")
    @Produces(ExtendedMediaType.APPLICATION_PDF)
    public byte[] pdf() {
        try {
            return databaseReportService.pdf("CustomChart.jasper");
        } catch (final JRException | SQLException ex) {
            Log.error("Unexpected DB Error", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

}
