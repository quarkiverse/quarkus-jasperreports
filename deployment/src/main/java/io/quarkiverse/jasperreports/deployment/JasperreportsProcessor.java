package io.quarkiverse.jasperreports.deployment;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

class JasperreportsProcessor {

    private static final String FEATURE = "jasperreports";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void runtimeInits(BuildProducer<RuntimeInitializedClassBuildItem> producer) {
        // TODO: Test what is really needed for native to work
        // see also https://github.com/quarkusio/quarkus/issues/31224
        producer.produce(new RuntimeInitializedClassBuildItem("net.sf.jasperreports.engine.SimpleReportContext"));
        producer.produce(new RuntimeInitializedClassBuildItem("net.sf.jasperreports.engine.design.JRAbstractCompiler"));
    }
}
