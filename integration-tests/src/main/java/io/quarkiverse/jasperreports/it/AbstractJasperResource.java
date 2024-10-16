package io.quarkiverse.jasperreports.it;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.jboss.logmanager.Logger;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXmlExporter;
import net.sf.jasperreports.engine.export.oasis.JROdsExporter;
import net.sf.jasperreports.engine.export.oasis.JROdtExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOdsReportConfiguration;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.export.SimpleXmlExporterOutput;
import net.sf.jasperreports.pdf.JRPdfExporter;

public abstract class AbstractJasperResource {

    protected ByteArrayOutputStream exportCsv(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportXml(JasperPrint jasperPrint, boolean embeddedImages) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXmlExporter exporter = new JRXmlExporter(DefaultJasperReportsContext.getInstance());

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        SimpleXmlExporterOutput xmlOutput = new SimpleXmlExporterOutput(outputStream);
        xmlOutput.setEmbeddingImages(embeddedImages);
        exporter.setExporterOutput(xmlOutput);

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportHtml(JasperPrint jasperPrint, ReportContext reportContext) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlExporter exporter = new HtmlExporter(DefaultJasperReportsContext.getInstance());

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

        Logger log = Logger.getLogger("DEBUG");
        SimpleHtmlExporterOutput htmlExporter = new SimpleHtmlExporterOutput(outputStream);
        htmlExporter.setImageHandler(new HtmlResourceHandler() {
            @Override
            public String getResourcePath(String id) {
                log.log(Level.WARNING, "getResourcePath: {0}", id);
                return id;
            }

            @Override
            public void handleResource(String id, byte[] data) {
                log.log(Level.WARNING, "handleResource: {0}, size: {1}", new Object[] { id, data.length });
            }

        });
        exporter.setExporterOutput(htmlExporter);
        exporter.setReportContext(reportContext);

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportRtf(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRRtfExporter exporter = new JRRtfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportOdt(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JROdtExporter exporter = new JROdtExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportOds(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JROdsExporter exporter = new JROdsExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimpleOdsReportConfiguration configuration = new SimpleOdsReportConfiguration();
        configuration.setOnePagePerSheet(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();
        return outputStream;
    }

    protected ByteArrayOutputStream exportPdf(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRPdfExporter exporter = new JRPdfExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        exporter.exportReport();
        return outputStream;
    }
    
    protected ByteArrayOutputStream exportXlsx(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        JRXlsxExporter exporter = new JRXlsxExporter();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(true);
        exporter.setConfiguration(configuration);

        exporter.exportReport();
        return outputStream;
    }

}
