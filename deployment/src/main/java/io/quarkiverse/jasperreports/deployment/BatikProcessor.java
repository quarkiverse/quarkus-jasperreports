package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.logging.Log;

public class BatikProcessor {

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-bridge"));
        index.produce(new IndexDependencyBuildItem("org.apache.xmlgraphics", "batik-gvt"));
    }

    /**
     * Registers classes and packages that need to be initialized at runtime.
     *
     * @param runtimeInitializedPackages Producer for runtime initialized packages
     */
    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
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
        Log.debugf("Runtime: %s", classes);
        classes.stream()
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
    }

    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>(
                collectClassesInPackage(combinedIndex, org.apache.batik.gvt.font.AWTGVTFont.class.getPackageName()));

        // methods and fields
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().build());
    }

    private List<String> collectClassesInPackage(CombinedIndexBuildItem combinedIndex, String packageName) {
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
        Log.tracef("Package: %s", classes);
        return classes;
    }

}