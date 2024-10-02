package io.quarkiverse.jasperreports.deployment;

import java.util.stream.Stream;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
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
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        Stream.of(
                javax.swing.plaf.metal.MetalIconFactory.class.getPackageName(),
                net.sf.jasperreports.data.http.HttpDataService.class.getPackageName(),
                net.sf.jasperreports.engine.SimpleReportContext.class.getPackageName(),
                net.sf.jasperreports.engine.design.JRAbstractCompiler.class.getPackageName(),
                net.sf.jasperreports.engine.export.oasis.JROdtExporter.class.getPackageName(),
                net.sf.jasperreports.engine.export.ooxml.DocxRunHelper.class.getPackageName(),
                net.sf.jasperreports.engine.fonts.AwtFontManager.class.getPackageName(),
                net.sf.jasperreports.engine.print.JRPrinterAWT.class.getPackageName(),
                net.sf.jasperreports.engine.type.ColorEnum.class.getPackageName(),
                net.sf.jasperreports.engine.util.json.DefaultJsonQLExecuter.class.getPackageName(),
                net.sf.jasperreports.engine.util.ExifUtil.class.getPackageName(),
                net.sf.jasperreports.renderers.AbstractSvgDataToGraphics2DRenderer.class.getPackageName(),
                net.sf.jasperreports.renderers.util.SvgFontProcessor.class.getPackageName()
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
}