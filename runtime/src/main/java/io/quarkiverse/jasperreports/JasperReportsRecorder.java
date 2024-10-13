package io.quarkiverse.jasperreports;

import io.quarkiverse.jasperreports.config.ReportBuildTimeConfig;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

/**
 * Recorder for JasperReports initialization.
 * This class is responsible for initializing the JasperReportsBeanProducer at runtime.
 */
@Recorder
public class JasperReportsRecorder {

    /**
     * Initializes the JasperReportsBeanProducer with the configured destination path.
     *
     * @param container The BeanContainer used to retrieve the JasperReportsBeanProducer instance.
     * @param config The ReportBuildTimeConfig containing the build configuration.
     */
    public void initProducer(BeanContainer container, ReportBuildTimeConfig config) {
        JasperReportsBeanProducer producer = container.beanInstance(JasperReportsBeanProducer.class);
        producer.initialize(config.build().destination());
    }
}
