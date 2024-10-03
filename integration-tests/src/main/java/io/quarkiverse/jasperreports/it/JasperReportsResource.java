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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

import org.w3c.dom.Document;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.SimpleReportContext;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
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
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import net.sf.jasperreports.poi.export.JRXlsExporter;
import net.sf.jasperreports.repo.JasperDesignCache;
import net.sf.jasperreports.repo.SimpleRepositoryResourceContext;

@Path("/jasperreports")
@ApplicationScoped
public class JasperReportsResource {

    private static final String TEST_REPORT_NAME = "CustomersReport";
    private static final String TEST_SUB_REPORT_NAME = "OrdersReport";

    private JasperReport compile(String reportName) throws JRException {
        long start = System.currentTimeMillis();
        JasperDesign jasperDesign = JRXmlLoader
                .load(Thread.currentThread().getContextClassLoader().getResourceAsStream(reportName + ".jrxml"));

        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        Log.info("Compilation time : " + (System.currentTimeMillis() - start));

        return jasperReport;
    }

    private JasperPrint fill() throws JRException {
        long start = System.currentTimeMillis();

        JasperReport mainReport = compile(TEST_REPORT_NAME);
        JasperReport subReport = compile(TEST_SUB_REPORT_NAME);

        ReportContext reportContext = new SimpleReportContext();

        Map<String, Object> params = new HashMap<>();
        Document document = JRXmlUtils.parse(JRLoader.getLocationInputStream("northwind.xml"));
        params.put(JRXPathQueryExecuterFactory.PARAMETER_XML_DATA_DOCUMENT, document);
        params.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, "yyyy-MM-dd");
        params.put(JRXPathQueryExecuterFactory.XML_NUMBER_PATTERN, "#,##0.##");
        params.put(JRXPathQueryExecuterFactory.XML_LOCALE, Locale.ENGLISH);
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);

        JasperDesignCache.getInstance(DefaultJasperReportsContext.getInstance(), reportContext).set("OrdersReport.jasper",
                subReport);

        JasperPrint jasperPrint = JRFiller.fill(DefaultJasperReportsContext.getInstance(),
                SimpleJasperReportSource.from(mainReport, null, new SimpleRepositoryResourceContext()),
                params);

        Log.info("Filling time : " + (System.currentTimeMillis() - start));

        return jasperPrint;
    }

    @POST
    @Path("csv")
    public byte[] csv() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRCsvExporter exporter = new JRCsvExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        exporter.exportReport();

        Log.info("CSV creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("xml")
    public byte[] xml(@QueryParam("embedded") boolean embedded) throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRXmlExporter exporter = new JRXmlExporter(DefaultJasperReportsContext.getInstance());

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        SimpleXmlExporterOutput xmlOutput = new SimpleXmlExporterOutput(outputStream);
        xmlOutput.setEmbeddingImages(embedded);
        exporter.setExporterOutput(xmlOutput);
        exporter.exportReport();
        Log.info("XML creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("html")
    public byte[] html() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        HtmlExporter exporter = new HtmlExporter(DefaultJasperReportsContext.getInstance());
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));

        exporter.exportReport();
        Log.info("HTML creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("rtf")
    public byte[] rtf() throws JRException {
        long start = System.currentTimeMillis();

        JasperPrint jasperPrint = fill();

        JRRtfExporter exporter = new JRRtfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        exporter.exportReport();

        Log.info("RTF creation time : " + (System.currentTimeMillis() - start));

        return outputStream.toByteArray();
    }

    //

    @POST
    @Path("odt")
    public byte[] odt() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JROdtExporter exporter = new JROdtExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.info("ODT creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("ods")
    public byte[] ods() throws JRException {
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

        Log.info("ODS creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("docx")
    public byte[] docx() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrint jasperPrint = fill();

        JRDocxExporter exporter = new JRDocxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.info("DOCX creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("xlsx")
    public byte[] xlsx() throws JRException {
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

        Log.info("XLSX creation time : " + (System.currentTimeMillis() - start));
        return outputStream.toByteArray();
    }

    @POST
    @Path("pptx")
    public byte[] pptx() throws JRException {
        long start = System.currentTimeMillis();

        JasperPrint jasperPrint = fill();

        JRPptxExporter exporter = new JRPptxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();

        Log.info("PPTX creation time : " + (System.currentTimeMillis() - start));

        return outputStream.toByteArray();
    }

    @POST
    @Path("print")
    public void print() throws JRException {
        long start = System.currentTimeMillis();
        JasperPrintManager.printReport("CustomersReport.jrprint", true);
        Log.info("Printing time : " + (System.currentTimeMillis() - start));
    }

    @POST
    @Path("xls")
    public byte[] xls() throws JRException {
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

        Log.info("XLS creation time : " + (System.currentTimeMillis() - start));

        return outputStream.toByteArray();
    }

    //        @POST
    //        @Path("pdf")
    //        public byte[] pdf() throws JRException {
    //            long start = System.currentTimeMillis();
    //            JasperPrint jasperPrint = fill();
    //
    //            JRPdfExporter exporter = new JRPdfExporter(DefaultJasperReportsContext.getInstance());
    //
    //            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
    //
    //            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    //            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
    //
    //            exporter.exportReport();
    //
    //            Log.info("PDF creation time : " + (System.currentTimeMillis() - start));
    //
    //            return outputStream.toByteArray();
    //        }
    //

}
