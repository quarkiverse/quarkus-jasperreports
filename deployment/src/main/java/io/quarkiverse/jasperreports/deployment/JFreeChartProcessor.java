package io.quarkiverse.jasperreports.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.logging.Log;

/**
 * A build step class responsible for registering classes related to the JFreeChart library
 * for reflection and runtime initialization during the Quarkus build process.
 */
class JFreeChartProcessor extends AbstractJandexProcessor {

    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-chart-customizers"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-charts"));
        index.produce(new IndexDependencyBuildItem("org.jfree", "jfreechart"));
    }

    @BuildStep
    void runtimeChartInitializedClasses(CombinedIndexBuildItem combinedIndex,
            BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        List<String> classes = new ArrayList<>(
                Stream.of(
                        net.sf.jasperreports.charts.fill.DefaultChartTheme.class.getName(),
                        net.sf.jasperreports.charts.util.ChartUtil.class.getPackage().getName(),
                        org.jfree.chart.ChartFactory.class.getName(),
                        org.jfree.chart.ChartHints.class.getName(),
                        org.jfree.chart.JFreeChart.class.getName(),
                        org.jfree.chart.LegendItem.class.getName(),
                        org.jfree.chart.block.BlockBorder.class.getName(),
                        org.jfree.chart.plot.DefaultDrawingSupplier.class.getName()
                ).toList());
        classes.addAll(collectSubclasses(combinedIndex, org.jfree.chart.axis.Axis.class.getName()));
        classes.addAll(collectSubclasses(combinedIndex, org.jfree.chart.plot.Plot.class.getName()));
        classes.addAll(collectSubclasses(combinedIndex, org.jfree.chart.renderer.AbstractRenderer.class.getName()));
        classes.addAll(collectSubclasses(combinedIndex, org.jfree.chart.title.Title.class.getName()));
        //@formatter:on
        Log.debugf("JFreeChart Runtime: %s", classes);
        classes.stream()
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
    }

    @BuildStep
    void registerChartForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>(
                collectSubclasses(combinedIndex, net.sf.jasperreports.charts.base.JRBaseChartDataset.class.getName()));
        classNames.addAll(collectClassesInPackage(combinedIndex,
                net.sf.jasperreports.charts.util.DrawChartRendererFactory.class.getPackageName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.charts.JRChart.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.charts.JRChartPlot.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.charts.JRChartPlot.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.charts.JRXySeries.class.getName()));
        classNames.addAll(
                collectSubclasses(combinedIndex, net.sf.jasperreports.charts.JRAbstractChartCustomizer.class.getName()));

        Log.debugf("Chart Reflection: %s", classNames);
        // methods and fields
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().serialization().build());
    }

    @BuildStep
    void registerChartResources(BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem) {
        //@formatter:off
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_ca"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_cs"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_de"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_es"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_fr"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_it"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_ja"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_nl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_pl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_pt_BR"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_pt_PT"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_ru"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_xh_CN"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.LocalizationBundle_xh_TW"));

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_ca"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_cs"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_de"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_es"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_fr"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_it"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_ja"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_nl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_pl"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_pt_PT"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_ru"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.plot.LocalizationBundle_xh_CN"));

        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.ui.LocalizationBundle"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.ui.LocalizationBundle_de"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.ui.LocalizationBundle_es"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.ui.LocalizationBundle_fr"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("org.jfree.chart.ui.LocalizationBundle_pt_PT"));
        //@formatter:on
    }
}
