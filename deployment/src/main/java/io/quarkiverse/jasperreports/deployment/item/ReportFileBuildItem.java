package io.quarkiverse.jasperreports.deployment.item;

import java.nio.file.Path;

/**
 * This build item represents a single watched Report file either .jrxml, .jasper, or .jrtx
 */
public final class ReportFileBuildItem extends AbstractReportFileBuildItem {

    public ReportFileBuildItem(Path path) {
        super(path);
    }
}
