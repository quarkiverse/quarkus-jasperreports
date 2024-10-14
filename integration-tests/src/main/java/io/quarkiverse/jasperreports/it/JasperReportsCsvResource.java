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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
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

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Path("/jasper/csv/")
@ApplicationScoped
public class JasperReportsCsvResource extends AbstractJasperResource {

    private static final String TEST_DS_NAME = "CsvDataSourceReport";

    @GET
    @Path("ds")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response datasource() {
        try {
            long start = System.currentTimeMillis();
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", "Address Report");
            parameters.put("DataFile", "CsvDataSource.txt - CSV data source");
            Set<String> states = new HashSet<>();
            states.add("Active");
            states.add("Trial");
            parameters.put("IncludedStates", states);

            String[] columnNames = new String[] { "city", "id", "name", "address", "state" };
            JRCsvDataSource dataSource = new JRCsvDataSource(JRLoader.getLocationInputStream("data/CsvDataSource.csv"));
            dataSource.setRecordDelimiter("\n");
            //				dataSource.setUseFirstRowAsHeader(true);
            dataSource.setColumnNames(columnNames);

            JasperFillManager.fillReportToFile("target/classes/jasperreports/" + TEST_DS_NAME + ".jasper", parameters,
                    dataSource);
            Log.infof("Report : CsvDataSourceReport.jasper. Filling time : %d", (System.currentTimeMillis() - start));

            JasperPrint jasperPrint = (JasperPrint) JRLoader
                    .loadObject(JRLoader.getLocationInputStream("target/classes/jasperreports/" + TEST_DS_NAME + ".jrprint"));
            ByteArrayOutputStream outputStream = exportCsv(jasperPrint);

            Log.infof("CSV creation time : %s", (System.currentTimeMillis() - start));
            final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
            response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=csvdatasource.csv");
            response.header(HttpHeaders.CONTENT_TYPE, ExtendedMediaType.TEXT_CSV);
            return response.build();
        } catch (final JRException ex) {
            Log.error("Unexpected DB Error", ex);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, ex);
        }
    }
}
