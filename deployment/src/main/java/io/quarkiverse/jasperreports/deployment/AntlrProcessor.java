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
 * Processor for ANTLR-related build steps in Quarkus deployment.
 * This is specifically used in the JsonQL integration tests.
 */
class AntlrProcessor extends AbstractJandexProcessor {

    /**
     * Indexes transitive dependencies for ANTLR.
     *
     * @param index BuildProducer for IndexDependencyBuildItem
     */
    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("antlr", "antlr"));
    }

    /**
     * Registers ANTLR classes for reflection.
     *
     * @param reflectiveClass BuildProducer for ReflectiveClassBuildItem
     * @param combinedIndex CombinedIndexBuildItem containing the combined index
     */
    @BuildStep
    void registerJaxenForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        final List<String> classNames = new ArrayList<>(collectSubclasses(combinedIndex, antlr.Token.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, antlr.BaseAST.class.getName()));

        Log.debugf("ANTLR Reflection: %s", classNames);
        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(classNames.toArray(new String[0])).methods().fields().serialization().build());
    }
}