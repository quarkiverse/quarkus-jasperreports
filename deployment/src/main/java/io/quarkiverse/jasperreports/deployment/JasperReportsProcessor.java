package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;

class JasperReportsProcessor {

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
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-jaxen"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-bridge"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-gvt"));
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {

        final List<String> classNames = new ArrayList<>();
        classNames.addAll(collectClassesInPackage(combinedIndex, org.apache.batik.gvt.font.AWTGVTFont.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.engine.fonts.FontExtensionsRegistry.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.engine.util.ImageUtil.class.getPackageName()));

        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods(true).serialization(true).build());
    }

    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        Stream.of(
                        "javax.swing",
                        "javax.swing.plaf.metal",
                        "javax.swing.text.html",
                        "javax.swing.text.rtf",
                        "org.apache.hc.client5.http.impl.auth.NTLMEngineImpl",
                        "sun.datatransfer",
                        "sun.swing",
                        net.sf.jasperreports.components.headertoolbar.HeaderToolbarElement.class.getPackageName(),
                        net.sf.jasperreports.components.list.UnusedSpaceImageRenderer.class.getName(),
                        net.sf.jasperreports.components.util.AbstractFieldComparator.class.getPackageName(),
                        net.sf.jasperreports.crosstabs.fill.calculation.MeasureDefinition.class.getPackageName(),
                        net.sf.jasperreports.engine.SimpleReportContext.class.getPackageName(),
                        net.sf.jasperreports.engine.base.ElementsBlock.class.getPackageName(),
                        net.sf.jasperreports.engine.convert.ReportConverter.class.getName(),
                        net.sf.jasperreports.engine.export.AbstractTextRenderer.class.getPackageName(),
                        net.sf.jasperreports.engine.fill.JRFillSubreport.class.getPackageName(),
                        net.sf.jasperreports.engine.fonts.AwtFontManager.class.getPackageName(),
                        net.sf.jasperreports.engine.type.ColorEnum.class.getPackageName(),
                        net.sf.jasperreports.engine.util.JRQueryExecuterUtils.class.getPackageName(),
                        org.apache.batik.anim.values.AnimatableTransformListValue.class.getPackageName(),
                        org.apache.batik.bridge.CSSUtilities.class.getPackageName(),
                        org.apache.batik.css.engine.SystemColorSupport.class.getPackageName(),
                        org.apache.batik.dom.svg.AbstractSVGTransform.class.getPackageName(),
                        org.apache.batik.ext.awt.MultipleGradientPaint.class.getPackageName(),
                        org.apache.batik.ext.awt.image.GraphicsUtil.class.getPackageName(),
                        org.apache.batik.ext.awt.image.rendered.TurbulencePatternRed.class.getPackageName(),
                        org.apache.batik.ext.awt.image.spi.DefaultBrokenLinkProvider.class.getName(),
                        org.apache.batik.gvt.CompositeGraphicsNode.class.getPackageName(),
                        org.apache.batik.gvt.renderer.MacRenderer.class.getPackageName(),
                        org.apache.batik.script.InterpreterPool.class.getName(),
                        org.apache.xmlbeans.impl.schema.TypeSystemHolder.class.getName()
                )
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
        //@formatter:on
    }

    @BuildStep
    void registerResourceBuildItems(BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
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
    void registerFonts(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns) {
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/fonts/dejavu/**");
        nativeImageResourcePatterns.produce(builder.build());
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
        return classes;
    }
}
