package io.quarkiverse.jasperreports.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import io.quarkus.logging.Log;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

public abstract class AbstractJasperResource {

    protected JasperReport compileReport(String jasperFile) throws JRException {
        JasperReport result;
        if (isRunningInContainer()) {
            result = (JasperReport) JRLoader.loadObject(JRLoader.getLocationInputStream(jasperFile + ".jasper"));
        } else {
            result = compile(jasperFile);
        }
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
        JasperDesign jasperDesign = JRXmlLoader.load(JRLoader.getLocationInputStream(reportName + ".jrxml"));

        JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

        // Save the compiled Jasper file
        //JasperCompileManager.compileReportToFile(jasperDesign, reportName + ".jasper");

        Log.infof("Compilation time : %s", (System.currentTimeMillis() - start));

        return jasperReport;
    }
}