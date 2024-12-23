package io.quarkiverse.jasperreports.deployment.item;

import java.nio.file.Path;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * This build item represents a source of report files.
 * <p>
 * By default, the templates are found in the {@code src/main/resources} directory. However, an extension can produce this build
 * item to register an additional root path.
 * <p>
 * The path is relative to the artifact/project root and OS-agnostic, i.e. {@code /} is used as a path separator.
 */
public final class ReportRootBuildItem extends MultiBuildItem {

    private final Path originalPath;
    private final String path;

    public ReportRootBuildItem(Path path) {
        this.originalPath = path;
        this.path = normalize(path.toString());
    }

    public String getPath() {
        return path;
    }

    public Path getOriginalPath() {
        return originalPath;
    }

    static String normalize(String path) {
        path = path.strip();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}