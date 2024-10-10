package io.quarkiverse.jasperreports.deployment;

import java.nio.file.Path;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.jasperreports")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ReportConfig {

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
        @WithDefault("src/main/jasperreports")
        Optional<Path> source();

        /**
         * The path where compiled reports are located.
         */
        @WithDefault("jasperreports")
        Optional<Path> destination();

    }

}
