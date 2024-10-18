package io.quarkiverse.jasperreports.it;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.jboss.logging.Logger;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import net.sf.jasperreports.engine.export.FileHtmlResourceHandler;

@ApplicationScoped
public class Application {

    private static final Logger LOG = Logger.getLogger(Application.class);

    private FileHtmlResourceHandler imageHandler;

    private Path tempFolder;

    void onStart(@Observes StartupEvent evt) throws IOException {
        tempFolder = Files.createTempDirectory("jasperreports");
        imageHandler = new FileHtmlResourceHandler(tempFolder.toFile(), "/images/{0}");
    }

    void onStop(@Observes ShutdownEvent evt) throws IOException {
        if (null != tempFolder) {
            Files.walk(tempFolder).forEach((p) -> {
                try {
                    Files.delete(p);
                } catch (IOException ex) {
                    LOG.warn(ex.getMessage());
                }
            });

            if (Files.exists(tempFolder)) {
                Files.delete(tempFolder);
            }
        }
    }

    public FileHtmlResourceHandler getImageHandler() {
        return imageHandler;
    }

    public Path getTempFolder() {
        return tempFolder;
    }

}
