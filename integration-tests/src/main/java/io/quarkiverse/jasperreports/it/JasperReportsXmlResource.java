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
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.w3c.dom.Document;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.SimpleReportContext;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.JRFiller;
import net.sf.jasperreports.engine.fill.SimpleJasperReportSource;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRXmlUtils;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.repo.SimpleRepositoryResourceContext;

@Path("/jasper/xml/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_OCTET_STREAM)
public class JasperReportsXmlResource extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "XmlDatasourceCustomersReport";
    private static final String TEST_SUB_REPORT_NAME = "XmlDatasourceOrdersReport";

    private JasperPrint fill() throws JRException {
        long start = System.currentTimeMillis();
        JasperReport mainReport = compileReport(TEST_REPORT_NAME);
        JasperReport subReport = compileReport(TEST_SUB_REPORT_NAME);

        ReportContext reportContext = new SimpleReportContext();

        Map<String, Object> params = new HashMap<>();
        Document document = JRXmlUtils.parse(JRLoader.getLocationInputStream("data/northwind.xml"));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
        params.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, "yyyy-MM-dd");
        params.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, "#,##0.##");
        params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.ENGLISH);
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);

        JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), reportContext).set(
                TEST_SUB_REPORT_NAME + ".jasper",
                subReport);

        JasperPrint jasperPrint = JRFiller.fill(DefaultJasperReportsContext.getInstance(),
                SimpleJasperReportSource.from(mainReport, null, new SimpleRepositoryResourceContext()),
                params);

        Log.infof("Filling time : %s", (System.currentTimeMillis() - start));

        return jasperPrint;
    }

    @GET
    @Path("csv")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response csv() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();
        ByteArrayOutputStream outputStream = exportCsv(jasperPrint);
        Log.infof("CSV creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.csv");
        response.header("Content-Type", "text/csv");
        return response.build();
    }

    @GET
    @Path("xml")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response xml(@QueryParam("embedded") boolean embedded) throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRXmlExporter exporter = new JRXmlExporter(DefaultJasperReportsContext.getInstance());

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SimpleXmlExporterOutput xmlOutput = new SimpleXmlExporterOutput(outputStream);
        xmlOutput.setEmbeddingImages(embedded);
        exporter.setExporterOutput(xmlOutput);
        exporter.exportReport();
        Log.infof("XML creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.xml");
        response.header("Content-Type", "application/xml");
        return response.build();
    }

    @GET
    @Path("html")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response html() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        HtmlExporter exporter = new HtmlExporter(DefaultJasperReportsContext.getInstance());
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));

        exporter.exportReport();
        Log.infof("HTML creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.html");
        response.header("Content-Type", "text/html");
        return response.build();
    }

    @GET
    @Path("rtf")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response rtf() throws JRException {
        long start = System.currentTimeMillis();

        JasperPrint jasperPrint = fill();

        JRRtfExporter exporter = new JRRtfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        exporter.exportReport();

        Log.infof("RTF creation time : %s", (System.currentTimeMillis() - start));

        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.rtf");
        response.header("Content-Type", "application/rtf");
        return response.build();
    }

    @GET
    @Path("odt")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response odt() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JROdtExporter exporter = new JROdtExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.infof("ODT creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.odt");
        response.header("Content-Type", "application/vnd.oasis.opendocument.text");
        return response.build();
    }

    @GET
    @Path("ods")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response ods() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JROdsExporter exporter = new JROdsExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleOdsReportConfiguration configuration = new SimpleOdsReportConfiguration();
        configuration.setOnePagePerSheet(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        Log.infof("ODS creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.ods");
        response.header("Content-Type", "application/vnd.oasis.opendocument.spreadsheet");
        return response.build();
    }

    @GET
    @Path("docx")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response docx() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRDocxExporter exporter = new JRDocxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.infof("DOCX creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.docx");
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        return response.build();
    }

    @GET
    @Path("xlsx")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response xlsx() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRXlsxExporter exporter = new JRXlsxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        Log.infof("XLSX creation time : %s", (System.currentTimeMillis() - start));
        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.xlsx");
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return response.build();
    }

    @GET
    @Path("pptx")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response pptx() throws JRException {
        long start = System.currentTimeMillis();

        JasperPrint jasperPrint = fill();

        JRPptxExporter exporter = new JRPptxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.infof("PPTX creation time : %s", (System.currentTimeMillis() - start));

        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.pptx");
        response.header("Content-Type", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        return response.build();
    }

    @GET
    @Path("print")
    @Produces(MediaType.TEXT_PLAIN)
    public String print() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrintManager.printReport("CustomersReport.jrprint", true);
        Log.info("Printing time : " + (System.currentTimeMillis() - start));
        return "Printing time : " + (System.currentTimeMillis() - start);
    }

    @GET
    @Path("xls")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response xls() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRXlsExporter exporter = new JRXlsExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
        configuration.setOnePagePerSheet(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();

        Log.infof("XLS creation time : %s", (System.currentTimeMillis() - start));

        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.xls");
        response.header("Content-Type", "application/vnd.ms-excel");
        return response.build();
    }

    @GET
    @Path("pdf")
    @APIResponse(responseCode = "200", description = "Document downloaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM, schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response pdf() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.infof("PDF creation time : %s", (System.currentTimeMillis() - start));

        final Response.ResponseBuilder response = Response.ok(outputStream.toByteArray());
        response.header("Content-Disposition", "attachment;filename=jasper.pdf");
        response.header("Content-Type", "application/pdf");
        return response.build();
    }

}