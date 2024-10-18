package io.quarkiverse.jasperreports.it;

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import lombok.extern.jbosslog.JBossLog;

@Path("images")
@RequestScoped
@JBossLog
public class ImageResource {

    @Inject
    Application app;

    @GET
    @Path("{fileName}")
    public Response get(@PathParam("fileName") final String fileName) throws IOException {
        final var path = java.nio.file.Path.of(app.getTempFolder().toString(), fileName);

        if (Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path)) {
            final String contentType = Files.probeContentType(path);

            log.debugf("Loading file %s of type %s", path.toString(), contentType);

            try (final InputStream is = Files.newInputStream(path)) {
                return Response.ok(is.readAllBytes(), contentType).build();
            }
        }

        return Response.status(NOT_FOUND).build();
    }
}