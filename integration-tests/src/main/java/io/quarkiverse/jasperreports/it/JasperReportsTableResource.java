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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.json.data.JsonDataSource;
import net.sf.jasperreports.json.query.JsonQueryExecuterFactory;

@Path("/jasper/table/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class JasperReportsTableResource extends AbstractJasperResource {

    private static final String REPORT_NAME = "TableReport.jasper";
    private static final String JSON_STRING = "{'name':'Hugo Meier','positions': [{'deliveryTimestamp' : '2024-10-24T16:40:56Z','product':'product1'},{'deliveryTimestamp' : '2024-10-24T16:40:56Z','product':'product2'}]}";

    @Inject
    ReadOnlyStreamingService repo;

    @GET
    @Path("csv")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response tableCsv() {
        Map<String, Object> params = new HashMap<>();
        params.put(JsonQueryExecuterFactory.JSON_DATE_PATTERN, "yyyy-MM-dd");
        params.put(JsonQueryExecuterFactory.JSON_NUMBER_PATTERN, "#,##0.##");
        params.put(JsonQueryExecuterFactory.JSON_LOCALE, Locale.ENGLISH);
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        return fillToCsv(REPORT_NAME, params);
    }

    protected Response fillToCsv(String reportFile, Map<String, Object> params) {
        try {
            InputStream fillParameterStream = IOUtils.toInputStream(JSON_STRING, StandardCharsets.UTF_8); // convert the json string to input stream
            long start = System.currentTimeMillis();
            JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(reportFile,
                    params, new JsonDataSource(fillParameterStream));
            ByteArrayOutputStream outputStream = exportCsv(jasperPrint);

            Log.infof("CSV creation time : %s", (System.currentTimeMillis() - start));
            final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + reportFile + ".csv");
            response.header(HttpHeaders.CONTENT_TYPE, ExtendedMediaType.TEXT_CSV);
            return response.build();
        } catch (final JRException ex) {
            Log.error("Unexpected DB Error", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }

}
