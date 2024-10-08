package io.quarkiverse.jasperreports.deployment.item;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FilenameUtils;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * This build item represents a single watched Report file either .jrxml, .jasper, or .jrtx
 */
public abstract class AbstractReportFileBuildItem extends MultiBuildItem {

    public final static String EXT_REPORT = "jrxml";
    public final static String EXT_STYLE = "jrtx";
    public final static String EXT_COMPILED = "jasper";
    public final static List<String> EXTENSIONS = List.of("." + EXT_REPORT, "." + EXT_COMPILED, "." + EXT_STYLE);

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
            default -> "???";
        };
    }

}
