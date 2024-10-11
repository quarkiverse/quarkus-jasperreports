package io.quarkiverse.jasperreports.deployment.config;

import java.nio.file.Path;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.jasperreports")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ReportConfig {

    String DEFAULT_SOURCE_PATH = "src/main/jasperreports";
    String DEFAULT_DEST_PATH = "jasperreports";

    /**
     * Configuration options related to automatically building reports.
     */
    BuildConfig build();

    interface BuildConfig {

        /**
         * Enable building all report files.
         */
        @WithDefault("true")
        boolean enable();

        /**
         * The path where all source .jrxml files are located.
         */
        @WithDefault(DEFAULT_SOURCE_PATH)
        Path source();

        /**
         * The path where compiled reports are located next to compiled classes.
         */
        @WithDefault(DEFAULT_DEST_PATH)
        Path destination();
    }

}