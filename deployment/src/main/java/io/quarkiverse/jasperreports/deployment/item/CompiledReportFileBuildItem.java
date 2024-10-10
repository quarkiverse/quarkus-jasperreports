package io.quarkiverse.jasperreports.deployment.item;

import java.nio.file.Path;

/**
 * This build item represents a single compiled .jasper file.
 */
public final class CompiledReportFileBuildItem extends AbstractReportFileBuildItem {

    public CompiledReportFileBuildItem(Path path) {
        super(path);
    }
}