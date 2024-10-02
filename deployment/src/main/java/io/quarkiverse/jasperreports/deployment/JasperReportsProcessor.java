package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;

class JasperReportsProcessor {
    private static final Logger LOGGER = Logger.getLogger("JasperReports");
    private static final String FEATURE = "jasperreports";
    private static final String EXTENSIONS_FILE = "jasperreports_extension.properties";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    UberJarMergedResourceBuildItem mergeResource() {
        return new UberJarMergedResourceBuildItem(EXTENSIONS_FILE);
    }

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-fonts"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-bridge"));
        index.produce(new IndexDependencyBuildItem("com.ibm.icu", "icu4j"));
        index.produce(new IndexDependencyBuildItem("com.drewnoakes", "metadata-extractor"));
    }

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        Stream.of(
                javax.swing.plaf.metal.MetalIconFactory.class.getPackageName()
                )
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
        //@formatter:on
    }

    @BuildStep
    void substrateResourceBuildItems(BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
            BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem) {
        nativeImageResourceProducer.produce(new NativeImageResourceBuildItem(
                EXTENSIONS_FILE,
                "default.jasperreports.properties",
                "jasperreports_messages.properties",
                "metadata_messages.properties",
                "metadata_messages-defaults.properties",
                "properties-metadata.json"));

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jasperreports_messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("metadata_messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("metadata_messages-defaults"));
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {

        final List<String> classNames = new ArrayList<>();
        // All utilities
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.renderers.util.RendererUtil.class.getPackageName()));
        classNames.addAll(
                collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.util.ExifUtil.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.engine.util.json.DefaultJsonQLExecuter.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.renderers.WrappingSvgDataToGraphics2DRenderer.class.getPackageName()));

        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).constructors().methods().fields()
                        .serialization()
                        .build());
    }

    public List<String> collectClassesInPackage(CombinedIndexBuildItem combinedIndex, String packageName) {
        final List<String> classes = new ArrayList<>();
        final List<DotName> packages = new ArrayList<>(combinedIndex.getIndex().getSubpackages(packageName));
        packages.add(DotName.createSimple(packageName));
        for (DotName aPackage : packages) {
            final List<String> packageClasses = combinedIndex.getIndex()
                    .getClassesInPackage(aPackage)
                    .stream()
                    .map(ClassInfo::toString)
                    .toList();
            classes.addAll(packageClasses);
        }
        LOGGER.infof("%s Classes: %s", packageName, classes);
        return classes;
    }

    private List<String> collectSubclasses(CombinedIndexBuildItem combinedIndex, String className) {
        List<String> classes = combinedIndex.getIndex()
                .getAllKnownSubclasses(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toList());
        classes.add(className);
        return classes;
    }

    public List<String> collectImplementors(CombinedIndexBuildItem combinedIndex, String className) {
        List<String> classes = combinedIndex.getIndex()
                .getAllKnownImplementors(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toList());
        classes.add(className);
        return classes;
    }
}