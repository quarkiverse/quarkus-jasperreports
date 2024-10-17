/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package io.quarkiverse.jasperreports.it;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import com.lowagie.text.pdf.PdfWriter;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.pdf.SimplePdfExporterConfiguration;

@Path("/jasper/pdf/")
@ApplicationScoped
public class JasperReportsPdfEncryptResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "PdfEncryptReport.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @GET
    @Path("encrypt")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response encrypt() {
        try {
            long start = System.currentTimeMillis();
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME,
                    new HashMap<>(), new JREmptyDataSource());
            Log.infof("Report : %s.jasper. Filling time : %d", TEST_REPORT_NAME, (System.currentTimeMillis() - start));
            JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
            SimplePdfExporterConfiguration configuration = new SimplePdfExporterConfiguration();
            configuration.setEncrypted(true);
            configuration.set128BitKey(true);
            configuration.setUserPassword("jasper");
            configuration.setOwnerPassword("reports");
            configuration.setPermissions(PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING);
            exporter.setConfiguration(configuration);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

            exporter.exportReport();

            Log.infof("PDF creation time : %s", (System.currentTimeMillis() - start));

            final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=encrypted.pdf");
            response.header(HttpHeaders.CONTENT_TYPE, "application/pdf");
            return response.build();
        } catch (final JRException ex) {
            Log.error("Unexpected DB Error", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
