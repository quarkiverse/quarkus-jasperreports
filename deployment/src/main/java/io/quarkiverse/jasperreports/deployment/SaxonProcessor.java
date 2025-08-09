package io.quarkiverse.jasperreports.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

/**
 * A build step class responsible for registering classes related to the Saxon library
 * for reflection and runtime initialization during the Quarkus build process.
 * <p>
 * Borrowed from Camel Quarkus XSLT Saxon extension.
 * </p>
 */
class SaxonProcessor extends AbstractJandexProcessor {

    /**
     * Registers the required Saxon and XML resolver classes for reflection and runtime initialization.
     *
     * @param reflectiveClasses the build producer for registering classes for reflection
     */
    @BuildStep
    void registerSaxon(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        //@formatter:off
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(
                        net.sf.saxon.Configuration.class,
                        net.sf.saxon.functions.String_1.class,
                        net.sf.saxon.functions.Tokenize_1.class,
                        net.sf.saxon.functions.StringJoin.class)
                .build());
        reflectiveClasses.produce(ReflectiveClassBuildItem.builder(org.xmlresolver.loaders.XmlLoader.class).build());
        //@formatter:on
    }
}
