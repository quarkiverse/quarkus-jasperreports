package io.quarkiverse.jasperreports.repository;

import static io.quarkiverse.jasperreports.Constants.EXT_COMPILED;
import static io.quarkiverse.jasperreports.Constants.EXT_DATA_ADAPTER;
import static io.quarkiverse.jasperreports.Constants.EXT_STYLE;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import io.quarkiverse.jasperreports.Constants;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.repo.FileRepositoryPersistenceServiceFactory;
import net.sf.jasperreports.repo.FileRepositoryService;
import net.sf.jasperreports.repo.PersistenceService;
import net.sf.jasperreports.repo.PersistenceServiceFactory;
import net.sf.jasperreports.repo.PersistenceUtil;
import net.sf.jasperreports.repo.RepositoryService;
import net.sf.jasperreports.repo.Resource;
import net.sf.jasperreports.repo.SimpleRepositoryContext;
import net.sf.jasperreports.repo.StreamRepositoryService;

@Dependent
public class ReadOnlyStreamingService implements StreamRepositoryService {

    private static final Logger LOG = Logger.getLogger(ReadOnlyStreamingService.class);

    private final JasperReportsContext context = new SimpleJasperReportsContext();

    // TODO - why is it not picking up the default value from ReportConfig???
    @ConfigProperty(name = "quarkus.jasperreports.build.destination", defaultValue = Constants.DEFAULT_DEST_PATH)
    Optional<Path> reportPathConfig;

    @PostConstruct
    public void onInit() {
        ((SimpleJasperReportsContext) context).setExtensions(RepositoryService.class, Collections.singletonList(this));
        ((SimpleJasperReportsContext) context).setExtensions(PersistenceServiceFactory.class,
                Collections.singletonList(FileRepositoryPersistenceServiceFactory.getInstance()));
    }

    public JasperReportsContext getContext() {
        return context;
    }

    @Override
    public InputStream getInputStream(String uri) {
        InputStream is = null;

        if (uri.endsWith(EXT_COMPILED) || uri.endsWith(EXT_STYLE)) {
            final Path reportPath = Path.of(reportPathConfig.get().toString(), uri);
            final String reportFile = reportPath.toString();

            try {
                LOG.debugf("Loading %s file %s", (uri.endsWith(EXT_COMPILED) ? "report" : "style"), reportFile);

                return JRLoader.getLocationInputStream(reportFile);
            } catch (JRException ex) {
                LOG.warnf("Failed to load %s - %s", (uri.endsWith(EXT_COMPILED) ? "report" : "style"), ex.getMessage());
                LOG.debug(ex);
            }
        } else if (uri.endsWith(EXT_DATA_ADAPTER)) {
            try {
                LOG.debugf("Loading data adapter file %s", uri);

                return JRLoader.getLocationInputStream(uri);
            } catch (JRException ex) {
                LOG.warnf("Failed to load data adapter - %s", ex.getMessage());
                LOG.debug(ex);
            }
        }

        return is;
    }

    @Override
    public OutputStream getOutputStream(String uri) {
        throw new IllegalStateException("This repository is read only");
    }

    @Override
    public Resource getResource(String uri) {
        throw new IllegalStateException("Can only return an InputStream");
    }

    @Override
    public void saveResource(String uri, Resource resource) {
        throw new IllegalStateException("This repository is read only");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> T getResource(String uri, Class<T> resourceType) {
        final PersistenceService persistenceService = PersistenceUtil.getInstance(context).getService(
                FileRepositoryService.class,
                resourceType);

        if (null != persistenceService) {
            return (T) persistenceService.load(SimpleRepositoryContext.of(context), uri, this);
        }

        return null;
    }

}
