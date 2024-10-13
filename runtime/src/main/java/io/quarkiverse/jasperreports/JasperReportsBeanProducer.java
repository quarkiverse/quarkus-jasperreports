package io.quarkiverse.jasperreports;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;

import io.quarkiverse.jasperreports.repository.ReadOnlyStreamingService;

/**
 * A bean producer for JasperReports-related services.
 * This class is responsible for initializing and producing the ReadOnlyStreamingService.
 */
@ApplicationScoped
public class JasperReportsBeanProducer {

    private volatile Path destinationPath;

    /**
     * Initializes the bean producer with the destination path for JasperReports files.
     *
     * @param destinationPath The path where compiled JasperReports files are located.
     */
    void initialize(Path destinationPath) {
        this.destinationPath = destinationPath;
    }

    /**
     * Produces a ReadOnlyStreamingService instance.
     *
     * @return A new ReadOnlyStreamingService initialized with the destination path.
     */
    @Dependent
    @Produces
    public ReadOnlyStreamingService readOnlyStreamingService() {
        return new ReadOnlyStreamingService(this.destinationPath);
    }
}
