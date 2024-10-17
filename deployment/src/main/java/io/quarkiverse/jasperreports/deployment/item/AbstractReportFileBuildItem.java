package io.quarkiverse.jasperreports.deployment.item;

import static io.quarkiverse.jasperreports.config.Constants.EXT_COMPILED;
import static io.quarkiverse.jasperreports.config.Constants.EXT_DATA_ADAPTER;
import static io.quarkiverse.jasperreports.config.Constants.EXT_REPORT;
import static io.quarkiverse.jasperreports.config.Constants.EXT_STYLE;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import org.apache.commons.io.FilenameUtils;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * This build item represents a single watched Report file either .jrxml, .jasper, or .jrtx
 */
public abstract class AbstractReportFileBuildItem extends MultiBuildItem {

    public final static List<String> EXTENSIONS = List.of("." + EXT_REPORT, "." + EXT_COMPILED, "." + EXT_STYLE,
            "." + EXT_DATA_ADAPTER);

    private final Path path;

    public AbstractReportFileBuildItem(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    public String getFileName() {
        return path.getFileName().toString();
    }

    public String getParent() {
        return path.getParent().toString();
    }

    public String getType() {
        String extension = FilenameUtils.getExtension(path.getFileName().toString()).toLowerCase(Locale.ROOT);
        return switch (extension) {
            case EXT_COMPILED -> "COMPILED";
            case EXT_REPORT -> "REPORT";
            case EXT_STYLE -> "STYLES";
            case EXT_DATA_ADAPTER -> "ADAPTER";
            default -> "???";
        };
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AbstractReportFileBuildItem.class.getSimpleName() + "[", "]")
                .add("path=" + path)
                .toString();
    }
}