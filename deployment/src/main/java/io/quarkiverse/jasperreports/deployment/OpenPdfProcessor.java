package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.NativeImageEnableAllCharsetsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.logging.Log;

public class OpenPdfProcessor extends AbstractJandexProcessor {

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("com.github.librepdf", "openpdf"));
    }

    @BuildStep
    NativeImageEnableAllCharsetsBuildItem enableAllCharsetsBuildItem() {
        return new NativeImageEnableAllCharsetsBuildItem();
    }

    @BuildStep
    void registerOpenPdfFonts(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns) {
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/pdf/fonts/**");
        builder.includeGlob("**/font-fallback/**");
        nativeImageResourcePatterns.produce(builder.build());
    }

    @BuildStep
    void registerOpenPdfResources(BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem) {
        // Register resource bundles
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("com/lowagie/text/error_messages/en.lng"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("com/lowagie/text/error_messages/nl.lng"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("com/lowagie/text/error_messages/pt.lng"));
    }

    /**
     * Registers classes and packages that need to be initialized at runtime.
     *
     * @param runtimeInitializedPackages Producer for runtime initialized packages
     */
    @BuildStep
    void runtimeOpenPdfInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        List<String> classes = new ArrayList<>();
        classes.add(com.lowagie.text.pdf.PdfPublicKeySecurityHandler.class.getName());

        Log.debugf("OpenPdf Runtime: %s", classes);
        classes.stream()
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
        //@formatter:on
    }

    @BuildStep
    void registerOpenPdfForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>(
                collectSubclasses(combinedIndex, com.lowagie.text.Image.class.getName()));
        classNames.add(com.lowagie.text.PageSize.class.getName());
        classNames.add(com.lowagie.text.Utilities.class.getName());
        classNames.add(com.lowagie.text.pdf.PdfName.class.getName());

        Log.debugf("OpenPdf Reflection: %s", classNames);
        // methods and fields
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().build());
    }

}