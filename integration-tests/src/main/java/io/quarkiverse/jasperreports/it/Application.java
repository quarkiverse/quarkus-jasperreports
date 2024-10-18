package io.quarkiverse.jasperreports.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import net.sf.jasperreports.engine.export.FileHtmlResourceHandler;

@ApplicationScoped
@Getter
@JBossLog
public class Application {

    private FileHtmlResourceHandler imageHandler;

    private Path tempFolder;

    void onStart(@Observes StartupEvent evt) throws IOException {
        tempFolder = Files.createTempDirectory("jasperreports");
        imageHandler = new FileHtmlResourceHandler(tempFolder.toFile(), "/images/{0}");
    }

    void onStop(@Observes ShutdownEvent evt) {
        if (tempFolder != null && Files.exists(tempFolder)) {
            try (Stream<Path> paths = Files.walk(tempFolder)) {
                paths.sorted(Comparator.reverseOrder()) // Delete files before directories
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException ex) {
                                log.warn("Failed to delete: " + p, ex);
                            }
                        });
            } catch (IOException e) {
                log.error("Error occurred while deleting temp folder: " + tempFolder, e);
            }
        }
    }

}
