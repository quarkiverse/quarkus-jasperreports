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

import java.io.*;
import java.util.*;

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
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.json.data.JsonDataSource;

@Path("/jasper/203")
@ApplicationScoped
public class JasperReports203Resource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "Invoice_Table_Based.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @GET
    @APIResponse(responseCode = "200", description = "PDF generated", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response generateFromJson() {
        try {
            JsonDataSource jsonDataSource = new JsonDataSource(JRLoader.getLocationInputStream("data/sample.json"));

            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME,
                    new HashMap<>(), jsonDataSource);
            ByteArrayOutputStream outputStream = exportPdf(jasperPrint);

            return Response.ok(outputStream.toByteArray())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=report.pdf")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    .build();

        } catch (Exception e) {
            Log.error("Erreur lors de la génération du PDF", e);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
        }
    }

}
