package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.logging.Log;

public class BatikProcessor extends AbstractJandexProcessor {

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-anim"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-awt-util"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-bridge"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-constants"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-css"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-dom"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-gvt"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-svg-dom"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-util"));
    }

    /**
     * Registers classes and packages that need to be initialized at runtime.
     *
     * @param runtimeInitializedPackages Producer for runtime initialized packages
     */
    @BuildStep
    void runtimeBatikInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        List<String> classes = new ArrayList<>(Stream.of("javax.swing",
                "javax.swing.plaf.metal",
                "javax.swing.text.html",
                "javax.swing.text.rtf",
                "sun.datatransfer",
                "sun.swing",
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
                org.apache.batik.script.jpython.JPythonInterpreter.class.getPackageName(),
                org.apache.batik.script.InterpreterPool.class.getName()).toList());
        //@formatter:on
        Log.debugf("Batik Runtime: %s", classes);
        classes.stream()
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
    }

    @BuildStep
    void registerBatikForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>();
        classNames.addAll(collectImplementors(combinedIndex, org.apache.batik.css.parser.ExtendedParser.class.getName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, org.apache.batik.gvt.font.AWTGVTFont.class.getPackageName()));

        Log.debugf("Batik Reflection: %s", classNames);
        // methods and fields
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().build());
    }

    @BuildStep
    void registerBatikResources(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns) {
        // Register all message bundles
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/apache/batik/**/resources/**");
        nativeImageResourcePatterns.produce(builder.build());
    }

}