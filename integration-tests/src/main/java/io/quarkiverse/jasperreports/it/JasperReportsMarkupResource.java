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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

@Path("/jasper/markup/")
@ApplicationScoped
public class JasperReportsMarkupResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "MarkupReport.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @GET
    @Path("pdf")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response markup() {
        try {
            long start = System.currentTimeMillis();
            String rtfText = readFile("data/rtf.txt");
            String htmlText = readFile("data/html.txt");

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("RtfText", rtfText);
            parameters.put("HtmlText", htmlText);
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME,
                    parameters, new JREmptyDataSource());
            Log.infof("Report : %s.jasper. Filling time : %d", TEST_REPORT_NAME, (System.currentTimeMillis() - start));

            ByteArrayOutputStream outputStream = exportPdf(jasperPrint);
            Log.infof("PDF creation time : %s", (System.currentTimeMillis() - start));

            final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=markup.pdf");
            response.header(HttpHeaders.CONTENT_TYPE, "application/pdf");
            return response.build();
        } catch (final JRException | IOException ex) {
            Log.error("Unexpected Markup Error", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

    public static String readFile(String filename) throws IOException {
        ClassLoader classLoader = JasperReportsMarkupResource.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(filename);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(Objects.requireNonNull(inputStream), StandardCharsets.UTF_8))) {

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            return content.toString();
        }
    }
}