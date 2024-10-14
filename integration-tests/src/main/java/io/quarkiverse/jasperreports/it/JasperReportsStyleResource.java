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

import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_OPENDOCUMENT_TEXT;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.APPLICATION_RTF;
import static io.quarkiverse.jasperreports.it.ExtendedMediaType.TEXT_CSV;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static jakarta.ws.rs.core.MediaType.WILDCARD;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.w3c.dom.Document;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.SimpleReportContext;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;

@Path("jasper/style")
@ApplicationScoped
@Produces({
        TEXT_CSV,
        APPLICATION_XML,
        TEXT_HTML,
        APPLICATION_RTF,
        APPLICATION_OPENDOCUMENT_TEXT
})
public class JasperReportsStyleResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "StylesReport.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @APIResponse(responseCode = "200", description = "Fetch the report output based on the requested content type", content = {
            @Content(mediaType = TEXT_CSV),
            @Content(mediaType = APPLICATION_XML),
            @Content(mediaType = TEXT_HTML),
            @Content(mediaType = APPLICATION_RTF, schema = @Schema(type = SchemaType.STRING, format = "binary")),
            @Content(mediaType = APPLICATION_OPENDOCUMENT_TEXT, schema = @Schema(type = SchemaType.STRING, format = "binary"))
    })
    @GET
    public Response get(@Context HttpHeaders headers,
            @DefaultValue("false") @QueryParam("embedded") boolean embedded) throws JRException {
        final ReportContext reportContext = new SimpleReportContext();

        final Map<String, Object> params = new HashMap<>();
        Document document = JRXmlUtils.parse(JRLoader.getLocationInputStream("data/northwind.xml"));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
        params.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, "yyyy-MM-dd");
        params.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, "#,##0.##");
        params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.ENGLISH);
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);

        final long start = System.currentTimeMillis();
        final JasperPrint jasperPrint = JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, params);

        final Response.ResponseBuilder response = Response.ok();

        switch (headers.getAcceptableMediaTypes().iterator().next().toString()) {
            case WILDCARD, TEXT_CSV -> {
                ByteArrayOutputStream outputStream = exportCsv(jasperPrint);
                Log.infof("CSV creation time : %s", (System.currentTimeMillis() - start));
                response.entity(outputStream.toByteArray());
                response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.csv");
                response.type(TEXT_CSV);
            }
            case APPLICATION_XML -> {
                ByteArrayOutputStream outputStream = exportXml(jasperPrint, embedded);
                Log.infof("XML creation time : %s", (System.currentTimeMillis() - start));
                response.entity(outputStream.toByteArray());
                response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.xml");
                response.type(MediaType.APPLICATION_XML);
            }
            case TEXT_HTML -> {
                ByteArrayOutputStream outputStream = exportHtml(jasperPrint);
                Log.infof("HTML creation time : %s", (System.currentTimeMillis() - start));
                response.entity(outputStream.toByteArray());
                response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.html");
                response.type(MediaType.TEXT_HTML);
            }
            case APPLICATION_RTF -> {
                ByteArrayOutputStream outputStream = exportRtf(jasperPrint);
                Log.infof("RTF creation time : %s", (System.currentTimeMillis() - start));
                response.entity(outputStream.toByteArray());
                response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.rtf");
                response.type(ExtendedMediaType.APPLICATION_RTF);
            }
            case APPLICATION_OPENDOCUMENT_TEXT -> {
                ByteArrayOutputStream outputStream = exportOdt(jasperPrint);
                Log.infof("ODT creation time : %s", (System.currentTimeMillis() - start));
                response.entity(outputStream.toByteArray());
                response.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=jasper.odt");
                response.type(ExtendedMediaType.APPLICATION_OPENDOCUMENT_TEXT);
            }
        }

        return response.build();
    }
}
