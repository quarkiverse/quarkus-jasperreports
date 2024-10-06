package io.quarkiverse.jasperreports.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public abstract class AbstractJasperResource {

    @ConfigProperty(name = "quarkus.jasperreports.build.destination", defaultValue = "jasperreports")
    Optional<String> reportPath;

    protected JasperReport compileReport(String jasperFile) throws JRException {
        JasperReport result;
        // TODO - Do we need this check anymore?
        //        if (isRunningInContainer()) {
        //            result = (JasperReport) JRLoader.loadObject(JRLoader.getLocationInputStream(jasperFile + ".jasper"));
        //        } else {
        result = compile(jasperFile);
        //        }
        return result;
    }

    private static boolean isNativeImage() {
        return System.getProperty("org.graalvm.nativeimage.imagecode") != null;
    }

    /**
     * Determines if the application is running inside a container (such as Docker or Kubernetes).
     * This is done by inspecting the '/proc/1/cgroup' file and checking for the presence of
     * "docker" or "kubepods". Additionally, it checks specific environment variables to verify
     * the container environment.
     *
     * @return {@code true} if the application is running inside a container; {@code false} otherwise.
     */
    protected static boolean isRunningInContainer() {
        if (isNativeImage())
            return true;

        try {
            List<String> lines = Files.readAllLines(Paths.get("/proc/1/cgroup"));
            for (String line : lines) {
                if (line.contains("docker") || line.contains("kubepods")) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Ignore, likely not in a container if the file doesn't exist
        }

        // check environment variables
        return System.getenv("CONTAINER") != null || System.getenv("KUBERNETES_SERVICE_HOST") != null;
    }

    protected JasperReport compile(String reportName) throws JRException {
        long start = System.currentTimeMillis();

        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(
                JRLoader.getLocationInputStream(java.nio.file.Path.of(reportPath.get(), reportName + ".jasper").toString()));

        Log.infof("Loading time : %s", (System.currentTimeMillis() - start));

        return jasperReport;
    }
}
