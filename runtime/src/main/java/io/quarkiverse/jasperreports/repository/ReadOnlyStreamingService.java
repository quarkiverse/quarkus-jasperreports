package io.quarkiverse.jasperreports.repository;

import static io.quarkiverse.jasperreports.config.Constants.EXT_COMPILED;
import static io.quarkiverse.jasperreports.config.Constants.EXT_DATA_ADAPTER;
import static io.quarkiverse.jasperreports.config.Constants.EXT_STYLE;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collections;

import org.jboss.logging.Logger;

import io.quarkus.runtime.util.ClassPathUtils;
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

/**
 * A read-only implementation of StreamRepositoryService for JasperReports.
 * This service provides access to compiled reports, styles, and data adapters
 * from a specified destination path.
 */
public class ReadOnlyStreamingService implements StreamRepositoryService {

    private static final Logger LOG = Logger.getLogger(ReadOnlyStreamingService.class);

    private final JasperReportsContext context = new SimpleJasperReportsContext();
    private final Path destinationPath;

    /**
     * Constructs a new ReadOnlyStreamingService with the specified destination path.
     *
     * @param destinationPath The path where compiled reports and styles are located.
     */
    public ReadOnlyStreamingService(Path destinationPath) {
        this.destinationPath = destinationPath;
        SimpleJasperReportsContext simpleContext = ((SimpleJasperReportsContext) context);
        simpleContext.setExtensions(RepositoryService.class, Collections.singletonList(this));
        simpleContext.setExtensions(PersistenceServiceFactory.class,
                Collections.singletonList(FileRepositoryPersistenceServiceFactory.getInstance()));
    }

    /**
     * Returns the JasperReportsContext associated with this service.
     *
     * @return The JasperReportsContext instance.
     */
    public JasperReportsContext getContext() {
        return context;
    }

    /**
     * Retrieves an InputStream for the specified URI.
     * This method handles compiled reports, styles, and data adapters.
     *
     * @param uri The URI of the resource to retrieve.
     * @return An InputStream for the requested resource, or null if not found.
     */
    @Override
    public InputStream getInputStream(String uri) {
        LOG.debugf("Loading getInputStream %s", uri);
        String logType = null;
        String filePath = null;

        if (uri.endsWith(EXT_COMPILED) || uri.endsWith(EXT_STYLE)) {
            logType = uri.endsWith(EXT_COMPILED) ? "report" : "style";
            filePath = ClassPathUtils.toResourceName(Path.of(this.destinationPath.toString(), uri));
        } else if (uri.endsWith(EXT_DATA_ADAPTER)) {
            logType = "data adapter";
        }

        if (logType != null) {
            try {
                InputStream is = JRLoader.getLocationInputStream(filePath);
                LOG.debugf("Loading %s file %s %s", logType, filePath, is);
                return is;
            } catch (JRException ex) {
                LOG.warnf("Failed to load %s - %s", logType, ex.getMessage());
                LOG.debug(ex);
            }
        }

        return null;
    }

    /**
     * This method is not supported in this read-only implementation.
     *
     * @param uri The URI of the resource.
     * @return This method always throws an IllegalStateException.
     * @throws IllegalStateException Always thrown as this repository is read-only.
     */
    @Override
    public OutputStream getOutputStream(String uri) {
        LOG.warnf("Read-Only repository called getOutputStream: %s", uri);
        throw new IllegalStateException("This repository is read only");
    }

    /**
     * This method is not supported in this implementation.
     *
     * @param uri The URI of the resource.
     * @return This method always throws an IllegalStateException.
     * @throws IllegalStateException Always thrown as this method is not supported.
     */
    @Override
    public Resource getResource(String uri) {
        LOG.warnf("Read-Only repository called getResource: %s", uri);
        throw new IllegalStateException("Can only return an InputStream");
    }

    /**
     * This method is not supported in this read-only implementation.
     *
     * @param uri The URI of the resource.
     * @param resource The resource to save.
     * @throws IllegalStateException Always thrown as this repository is read-only.
     */
    @Override
    public void saveResource(String uri, Resource resource) {
        LOG.warnf("Read-Only repository called saveResource: %s", uri);
        throw new IllegalStateException("This repository is read only");
    }

    /**
     * Retrieves a resource of the specified type for the given URI.
     *
     * @param <T> The type of resource to retrieve.
     * @param uri The URI of the resource.
     * @param resourceType The class of the resource type.
     * @return The requested resource, or null if not found or if no appropriate PersistenceService is available.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Resource> T getResource(String uri, Class<T> resourceType) {
        LOG.debugf("getResource %s type %s", uri, resourceType);
        final PersistenceService persistenceService = PersistenceUtil.getInstance(context).getService(
                FileRepositoryService.class,
                resourceType);

        if (null != persistenceService) {
            return (T) persistenceService.load(SimpleRepositoryContext.of(context), uri, this);
        }

        return null;
    }

}