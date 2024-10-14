package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.logging.Log;

/**
 * A build step class responsible for registering classes related to the Jaxen library
 * for reflection and runtime initialization during the Quarkus build process.
 * <p>
 * Jaxen supports 4 XML libs DOM, DOM4J, JDOM, XOM but we want to use native DOM so we exclude all the other 3rd party libs from
 * classloading.
 * </p>
 */
class JaxenProcessor extends AbstractJandexProcessor {

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("jaxen", "jaxen"));
    }

    @BuildStep
    void registerJaxenForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.jaxen.util.xml.JaxenXPathExecuterFactory.class.getPackageName()));
        classNames.add(net.sf.jasperreports.jaxen.data.JaxenXmlDataSource.class.getName());
        classNames.add(org.jaxen.saxpath.base.XPathReader.class.getName());

        Log.debugf("Jaxen Reflection: %s", classNames);
        // methods and fields
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().build());
    }
}