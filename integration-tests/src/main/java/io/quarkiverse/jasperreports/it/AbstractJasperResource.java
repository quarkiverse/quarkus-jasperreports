package io.quarkiverse.jasperreports.it;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public abstract class AbstractJasperResource {

    private static final String DEFAULT_PATH = "jasperreports";
    @ConfigProperty(name = "quarkus.jasperreports.build.destination", defaultValue = DEFAULT_PATH)
    Optional<String> reportPathConfig;

    protected JasperReport compileReport(String jasperFile) throws JRException {
        final long start = System.currentTimeMillis();
        final Path reportPath = Path.of(reportPathConfig.orElse(DEFAULT_PATH), jasperFile + ".jasper");
        final String reportFile = reportPath.toString();
        final InputStream is = JRLoader.getLocationInputStream(reportFile);
        final JasperReport jasperReport = (JasperReport) JRLoader.loadObject(is);
        Log.infof("%S Loading time : %s", jasperFile, (System.currentTimeMillis() - start));
        return jasperReport;
    }

}