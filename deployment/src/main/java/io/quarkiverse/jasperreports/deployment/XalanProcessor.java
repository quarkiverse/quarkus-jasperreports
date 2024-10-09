package io.quarkiverse.jasperreports.deployment;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import io.quarkiverse.jasperreports.XalanTransformerFactory;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.nativeimage.ExcludeConfigBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ServiceProviderBuildItem;

/**
 * Xalan native processor from Camel.
 *
 * @see <a href=
 *      "https://github.com/apache/camel-quarkus/blob/ff092e50666465f93c0ac71f607886e397e81598/extensions-support/xalan/deployment/src/main/java/org/apache/camel/quarkus/support/xalan/deployment/XalanNativeImageProcessor.java">XalanNativeImageProcessor.java</a>
 */
class XalanProcessor {
    private static final String TRANSFORMER_FACTORY_SERVICE_FILE_PATH = "META-INF/services/javax.xml.transform.TransformerFactory";

    @BuildStep
    List<ReflectiveClassBuildItem> reflectiveClasses() {
        return Arrays.asList(
                ReflectiveClassBuildItem.builder(io.quarkiverse.jasperreports.XalanTransformerFactory.class.getName(),
                        "org.apache.xalan.xsltc.dom.ObjectFactory",
                        "org.apache.xalan.xsltc.dom.XSLTCDTMManager",
                        "org.apache.xalan.xsltc.trax.ObjectFactory",
                        "org.apache.xalan.xsltc.trax.TransformerFactoryImpl",
                        "org.apache.xml.dtm.ObjectFactory",
                        "org.apache.xml.dtm.ref.DTMManagerDefault",
                        "org.apache.xml.serializer.OutputPropertiesFactory",
                        "org.apache.xml.serializer.CharInfo",
                        "org.apache.xml.utils.FastStringBuffer").methods().build(),
                ReflectiveClassBuildItem.builder("org.apache.xml.serializer.ToHTMLStream",
                        "org.apache.xml.serializer.ToTextStream",
                        "org.apache.xml.serializer.ToXMLStream").build());
    }

    @BuildStep
    List<NativeImageResourceBundleBuildItem> resourceBundles() {
        return Arrays.asList(
                new NativeImageResourceBundleBuildItem("org.apache.xalan.xsltc.compiler.util.ErrorMessages"),
                new NativeImageResourceBundleBuildItem("org.apache.xml.serializer.utils.SerializerMessages"),
                new NativeImageResourceBundleBuildItem("org.apache.xml.serializer.XMLEntities"),
                new NativeImageResourceBundleBuildItem("org.apache.xml.res.XMLErrorResources"));
    }

    @BuildStep
    void resources(BuildProducer<NativeImageResourceBuildItem> resource) {

        Stream.of(
                "html",
                "text",
                "xml",
                "unknown")
                .map(s -> "org/apache/xml/serializer/output_" + s + ".properties")
                .map(NativeImageResourceBuildItem::new)
                .forEach(resource::produce);

    }

    @BuildStep
    void installTransformerFactory(
            BuildProducer<ExcludeConfigBuildItem> excludeConfig,
            BuildProducer<ServiceProviderBuildItem> serviceProvider) {

        excludeConfig
                .produce(new ExcludeConfigBuildItem("xalan\\.xalan-.*\\.jar", "/" + TRANSFORMER_FACTORY_SERVICE_FILE_PATH));
        serviceProvider.produce(new ServiceProviderBuildItem("javax.xml.transform.TransformerFactory",
                XalanTransformerFactory.class.getName()));

    }

}