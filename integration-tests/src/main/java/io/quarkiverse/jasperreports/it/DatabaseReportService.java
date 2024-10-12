package io.quarkiverse.jasperreports.it;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.sql.DataSource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.ReportContext;
import net.sf.jasperreports.engine.SimpleReportContext;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

@ApplicationScoped
@Transactional(Transactional.TxType.NEVER)
public class DatabaseReportService extends AbstractJasperResource {

    private static final String TEST_REPORT_NAME = "DbDatasourceMain.jasper";

    @Inject
    ReadOnlyStreamingService repo;

    @Inject
    DataSource datasource;

    @Transactional(Transactional.TxType.NEVER)
    public byte[] text() throws JRException, SQLException {
        final JasperPrint jasperPrint = fill();
        final JRTextExporter exporter = new JRTextExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
        exporter.exportReport();
        return outputStream.toByteArray();
    }

    private JasperPrint fill() throws JRException, SQLException {
        final ReportContext reportContext = new SimpleReportContext();

        final Map<String, Object> params = new HashMap<>();
        params.put(JRParameter.REPORT_LOCALE, Locale.US);
        params.put(JRParameter.REPORT_CONTEXT, reportContext);

        try (final Connection connection = datasource.getConnection()) {
            return JasperFillManager.getInstance(repo.getContext()).fillFromRepo(TEST_REPORT_NAME, params, connection);
        }
    }

}
