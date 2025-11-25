package io.quarkiverse.jasperreports.deployment;

import static io.quarkus.deployment.pkg.PackageConfig.JarConfig.JarType.UBER_JAR;
import static org.jboss.jandex.AnnotationTarget.Kind.CLASS;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.quarkiverse.jasperreports.JasperReportsBeanProducer;
import io.quarkiverse.jasperreports.JasperReportsRecorder;
import io.quarkiverse.jasperreports.config.Constants;
import io.quarkiverse.jasperreports.config.ReportBuildTimeConfig;
import io.quarkiverse.jasperreports.deployment.item.CompiledReportFileBuildItem;
import io.quarkiverse.jasperreports.deployment.item.ReportFileBuildItem;
import io.quarkiverse.jasperreports.deployment.item.ReportRootBuildItem;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.IsProduction;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.deployment.pkg.NativeConfig;
import io.quarkus.deployment.pkg.PackageConfig;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;
import io.quarkus.jackson.deployment.IgnoreJsonDeserializeClassBuildItem;
import io.quarkus.logging.Log;
import net.sf.jasperreports.compilers.ReportExpressionEvaluationData;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRReportCompileData;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Processor class for JasperReports integration in Quarkus.
 * This class handles various build steps required for JasperReports functionality,
 * including feature registration, resource merging, dependency indexing,
 * and reflection configuration.
 */
class JasperReportsProcessor extends AbstractJandexProcessor {

    private static final String FEATURE = "jasperreports";
    private static final String EXTENSIONS_FILE = "jasperreports_extension.properties";
    private static final DotName JSON_DESERIALIZE = DotName.createSimple(JsonDeserialize.class.getName());

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /**
     * Merges specified JSON/Properties files if the package type is UBER_JAR and generates them
     * as resources in the Uber JAR.
     *
     * @param generatedResourcesProducer the producer to add generated resources
     * @param packageConfig the package configuration to check for UBER_JAR type
     */
    @BuildStep(onlyIf = IsProduction.class)
    void uberJarFiles(BuildProducer<GeneratedResourceBuildItem> generatedResourcesProducer,
            BuildProducer<UberJarMergedResourceBuildItem> uberJarMergedProducer,
            PackageConfig packageConfig) {
        if (packageConfig.jar().type() == UBER_JAR) {
            mergeAndGenerateJson("properties-metadata.json", generatedResourcesProducer);
        }

        // Merges the JasperReports extension properties file into the Uber JAR.
        uberJarMergedProducer.produce(new UberJarMergedResourceBuildItem(EXTENSIONS_FILE));
        uberJarMergedProducer.produce(new UberJarMergedResourceBuildItem("metadata_messages-defaults.properties"));
    }

    /**
     * Indexes transitive dependencies required for JasperReports functionality.
     * <p>
     * This method adds various JasperReports modules and related dependencies
     * to the build index, ensuring they are properly processed during the build.
     *
     * @param index The BuildProducer for IndexDependencyBuildItem, used to add
     *        dependencies to the build index.
     */
    @BuildStep
    void indexTransitiveDependencies(BuildProducer<IndexDependencyBuildItem> index) {
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-data-adapters"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-excel-poi"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-functions"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-jdt"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-json"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-pdf"));
    }

    /**
     * Registers classes for reflection in the native image.
     * <p>
     * This method collects various classes used by JasperReports and registers them
     * for reflection. This is necessary for proper functioning in a native image context.
     *
     * @param reflectiveClass The BuildProducer for ReflectiveClassBuildItem, used to register
     *        classes for reflection.
     */
    @BuildStep
    void registerForReflection(CombinedIndexBuildItem combinedIndex, BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        //@formatter:off
        final List<String> classNames = new ArrayList<>();
        // By Implementors: jasper interfaces/abstract classes that are created with Class.forName
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.functions.FunctionSupport.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRDataSource.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRDataSourceProvider.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRTemplate.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRVisitor.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.design.JRCompiler.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.query.JRQueryExecuter.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.query.QueryExecuterFactory.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.util.xml.JRXPathExecuterFactory.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.xml.ReportLoader.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistry.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistryFactory.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, net.sf.jasperreports.dataadapters.AbstractDataAdapter.class.getName()));
        classNames.addAll(collectSubclasses(combinedIndex, net.sf.jasperreports.engine.JRAbstractExporter.class.getName()));

        // By Package (utilities etc)
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.compilers.ReportExpressionEvaluationData.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.components.ComponentsExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.data.xmla.XmlaDataAdapterImpl.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.JasperReport.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.base.ElementStore.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.design.JRDesignQuery.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.export.DefaultExporterFilterFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.export.ExporterNature.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.fill.JRFillVariable.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.fonts.SimpleFontFace.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.part.DefaultPartComponentsBundle.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.type.ColorEnum.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.util.ImageUtil.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.util.xml.JRXPathExecuter.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.engine.xml.ReportLoader.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.export.CsvExporterConfiguration.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.extensions.DefaultExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.functions.standard.MathFunctions.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.governors.GovernorExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.jackson.util.JacksonUtil.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.parts.PartComponentsExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.renderers.AbstractSvgDataToGraphics2DRenderer.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.renderers.util.SvgFontProcessor.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.repo.DefaultRepositoryExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.util.JsonLoader.class.getPackageName()));

        // PDF
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.pdf.classic.ClassicPdfProducerFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.pdf.type.PdfVersionEnum.class.getPackageName()));

        // basic Java classes found in reports for serialization
        classNames.add("java.util.Collections$CheckedCollection");
        classNames.add("java.util.Collections$CheckedList");
        classNames.add("java.util.Collections$CheckedMap");
        classNames.add("java.util.Collections$CheckedQueue");
        classNames.add("java.util.Collections$CheckedSet");
        classNames.add("java.util.Collections$SynchronizedCollection");
        classNames.add("java.util.Collections$SynchronizedList");
        classNames.add("java.util.Collections$SynchronizedMap");
        classNames.add("java.util.Collections$SynchronizedNavigableMap");
        classNames.add("java.util.Collections$SynchronizedRandomAccessList");
        classNames.add("java.util.Collections$SynchronizedSortedMap");
        classNames.add(byte.class.getName());
        classNames.add(byte[].class.getName());
        classNames.add(java.awt.Color.class.getName());
        classNames.add(java.awt.color.ColorSpace.class.getName());
        classNames.add(java.io.Serializable.class.getName());
        classNames.add(java.lang.Boolean.class.getName());
        classNames.add(java.lang.Byte.class.getName());
        classNames.add(java.lang.Byte.class.getName());
        classNames.add(java.lang.Character.class.getName());
        classNames.add(java.lang.Double.class.getName());
        classNames.add(java.lang.Enum.class.getName());
        classNames.add(java.lang.Float.class.getName());
        classNames.add(java.lang.Integer.class.getName());
        classNames.add(java.lang.Iterable.class.getName());
        classNames.add(java.lang.Long.class.getName());
        classNames.add(java.lang.Number.class.getName());
        classNames.add(java.lang.Object.class.getName());
        classNames.add(java.lang.Short.class.getName());
        classNames.add(java.lang.String.class.getName());
        classNames.add(java.lang.StringBuilder.class.getName());
        classNames.add(java.math.BigDecimal.class.getName());
        classNames.add(java.util.AbstractList.class.getName());
        classNames.add(java.util.AbstractMap.class.getName());
        classNames.add(java.util.ArrayList.class.getName());
        classNames.add(java.util.Calendar.class.getName());
        classNames.add(java.util.Collections.class.getName());
        classNames.add(java.util.Date.class.getName());
        classNames.add(java.util.GregorianCalendar.class.getName());
        classNames.add(java.util.HashMap.class.getName());
        classNames.add(java.util.HashSet.class.getName());
        classNames.add(java.util.Hashtable.class.getName());
        classNames.add(java.util.LinkedHashMap.class.getName());
        classNames.add(java.util.LinkedHashSet.class.getName());
        classNames.add(java.util.LinkedList.class.getName());
        classNames.add(java.util.List.class.getName());
        classNames.add(java.util.Map.class.getName());
        classNames.add(java.util.Set.class.getName());
        classNames.add(java.util.TreeMap.class.getName());
        classNames.add(java.util.TreeSet.class.getName());
        classNames.add(java.util.UUID.class.getName());
        classNames.add(java.util.Vector.class.getName());
        classNames.addAll(collectImplementors(combinedIndex, java.time.temporal.TemporalAccessor.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, java.util.Collection.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, java.util.Map.Entry.class.getName()));

        //@formatter:on
        final TreeSet<String> uniqueClasses = new TreeSet<>(classNames);
        Log.debugf("Jasper Reflection: %s", uniqueClasses);

        reflectiveClass.produce(
                ReflectiveClassBuildItem.builder(uniqueClasses.toArray(new String[0])).constructors().methods().fields()
                        .serialization().build());
    }

    /**
     * Registers classes and packages that need to be initialized at runtime.
     *
     * @param runtimeInitializedPackages Producer for runtime initialized packages
     */
    @BuildStep
    void runtimeInitializedClasses(CombinedIndexBuildItem combinedIndex,
            BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages) {
        //@formatter:off
        List<String> classes = collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistryFactory.class.getName());
        classes.addAll(Stream.of(
                "javax.swing",
                "javax.swing.plaf.metal",
                "javax.swing.text.html",
                "javax.swing.text.rtf",
                "sun.datatransfer",
                "sun.swing",
                "sun.lwawt.LWWindowPeer",
                net.sf.jasperreports.components.headertoolbar.HeaderToolbarElement.class.getPackageName(),
                net.sf.jasperreports.components.iconlabel.IconLabelElement.class.getPackageName(),
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
                net.sf.jasperreports.poi.query.PoiQueryExecuterFactoryBundle.class.getName()).toList());
        //@formatter:on
        Log.debugf("Jasper Runtime: %s", classes);
        classes.stream()
                .map(RuntimeInitializedPackageBuildItem::new)
                .forEach(runtimeInitializedPackages::produce);
    }

    /**
     * Registers various resource build items for native image compilation.
     *
     * @param nativeImageResourcePatterns Producer for native image resource patterns
     * @param nativeImageResourceProducer Producer for native image resources
     * @param resourceBundleBuildItem Producer for resource bundles
     */
    @BuildStep
    void registerResourceBuildItems(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns,
            BuildProducer<NativeImageResourceBuildItem> nativeImageResourceProducer,
            BuildProducer<NativeImageResourceBundleBuildItem> resourceBundleBuildItem) {
        // Register individual resource files
        nativeImageResourceProducer.produce(new NativeImageResourceBuildItem(
                "jasperreports.properties",
                EXTENSIONS_FILE,
                "default.jasperreports.properties",
                "jasperreports_messages.properties",
                "metadata_messages.properties",
                "metadata_messages-defaults.properties",
                "properties-metadata.json"));

        // Register resource bundles
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("jasperreports_messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("metadata_messages"));
        resourceBundleBuildItem.produce(new NativeImageResourceBundleBuildItem("metadata_messages-defaults"));

        // Register resource patterns for POI export and font icons
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("net/sf/jasperreports/engine/export/ooxml/docx/**");
        builder.includeGlob("net/sf/jasperreports/engine/export/ooxml/pptx/**");
        builder.includeGlob("net/sf/jasperreports/engine/export/ooxml/xlsx/**");
        builder.includeGlob("net/sf/jasperreports/fonts/icons/**");
        builder.includeGlob("net/sf/jasperreports/engine/images/**");
        nativeImageResourcePatterns.produce(builder.build());
    }

    /**
     * Registers report files for native image compilation and processes compiled report files.
     *
     * @param nativeImageResourcePatterns Producer for native image resource patterns
     */
    @BuildStep
    void registerReports(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns) {
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/*." + Constants.EXT_REPORT);
        builder.includeGlob("**/*." + Constants.EXT_COMPILED);
        builder.includeGlob("**/*." + Constants.EXT_STYLE);
        builder.includeGlob("**/*." + Constants.EXT_DATA_ADAPTER);
        builder.includeGlob("*." + Constants.EXT_REPORT);
        builder.includeGlob("*." + Constants.EXT_COMPILED);
        builder.includeGlob("*." + Constants.EXT_STYLE);
        builder.includeGlob("*." + Constants.EXT_DATA_ADAPTER);
        nativeImageResourcePatterns.produce(builder.build());
    }

    /**
     * Registers report files for native image compilation and processes compiled report files.
     *
     * @param additionalClasses Producer for additional generated classes
     * @param reflectiveClassProducer Producer for reflective classes
     * @param compiledReports The list of all found compiled report files
     */
    @BuildStep
    void registerCompiledReports(
            BuildProducer<GeneratedClassBuildItem> additionalClasses,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClassProducer,
            List<CompiledReportFileBuildItem> compiledReports) {

        final Set<String> uniqueReportClassNames = new LinkedHashSet<>();

        // only care about compiled - .jasper files
        for (CompiledReportFileBuildItem reportFile : compiledReports) {
            Log.debugf("Jasper compiled report: %s", reportFile.getPath());
            try {
                String jasperFilePath = reportFile.getPath().toFile().getAbsolutePath();
                JasperReport report = (JasperReport) JRLoader.loadObject(JRLoader.getLocationInputStream(jasperFilePath));
                JRReportCompileData compileData = (JRReportCompileData) report.getCompileData();

                // collect all compiled datasets in this report
                List<ReportExpressionEvaluationData> datasetClasses = new ArrayList<>();

                // add the main data set (should always found)
                ReportExpressionEvaluationData mainData = (ReportExpressionEvaluationData) compileData
                        .getMainDatasetCompileData();
                if (mainData != null) {
                    datasetClasses.add((ReportExpressionEvaluationData) compileData.getMainDatasetCompileData());
                }

                // add any sub datasets if found
                final JRDataset[] datasets = report.getDatasets();
                if (datasets != null) {
                    for (JRDataset dataset : datasets) {
                        ReportExpressionEvaluationData reportData = (ReportExpressionEvaluationData) compileData
                                .getDatasetCompileData(dataset);
                        datasetClasses.add(reportData);
                    }
                }

                Map<Integer, Serializable> crossTabs = compileData.getCrosstabsCompileData();
                if (crossTabs != null) {
                    for (Serializable value : crossTabs.values()) {
                        if (value instanceof ReportExpressionEvaluationData reportData) {
                            datasetClasses.add(reportData);
                        }
                    }
                }

                // produce all dataset classes so they are found in native mode
                final Set<String> uniqueReportClassNames = new LinkedHashSet<>(datasetClasses.size());
                for (ReportExpressionEvaluationData reportData : datasetClasses) {
                    final String reportDataClass = reportData.getCompileName();
                    if (StringUtils.isNotBlank(reportDataClass)) {
                        if (uniqueReportClassNames.contains(reportDataClass)) {
                            throw new IllegalStateException("Duplicate report class found: " + reportDataClass
                                    + " Please review all reports for duplicate names.");
                        }
                        uniqueReportClassNames.add(reportDataClass);
                        final byte[] bytes = (byte[]) reportData.getCompileData();
                        Log.debugf("JasperReport Data Class: %s Size: %d", reportDataClass, bytes.length);

                        reflectiveClassProducer
                                .produce(ReflectiveClassBuildItem.builder(reportDataClass).constructors().methods()
                                        .serialization()
                                        .build());
                        additionalClasses.produce(new GeneratedClassBuildItem(true, reportDataClass, bytes));
                    }
                }
            } catch (JRException e) {
                Log.error("Error loading JasperReport file class.", e);
            }
        }
    }

    /**
     * Registers JasperReports proxy classes for native image compilation.
     * <p>
     * This method collects classes from the CsvExporterConfiguration and PdfExporterConfiguration
     * packages and registers them as proxy definitions for the native image build process.
     *
     * @param proxyDefinitions The producer for NativeImageProxyDefinitionBuildItem
     */
    @BuildStep
    void registerJasperReportsProxies(CombinedIndexBuildItem combinedIndex,
            BuildProducer<NativeImageProxyDefinitionBuildItem> proxyDefinitions) {
        // Register the proxy for exporters
        List<String> classes = new ArrayList<>(collectInterfacesInPackage(combinedIndex,
                net.sf.jasperreports.export.CsvExporterConfiguration.class.getPackageName()));
        classes.addAll(collectInterfacesInPackage(combinedIndex,
                net.sf.jasperreports.pdf.PdfExporterConfiguration.class.getPackageName()));
        for (String proxyClassName : classes) {
            proxyDefinitions.produce(new NativeImageProxyDefinitionBuildItem(proxyClassName));
        }
    }

    /**
     * Registers font resources for inclusion in the native image.
     * <p>
     * This method adds a glob pattern to include DejaVu fonts in the native image.
     * These fonts are commonly used by JasperReports for PDF generation.
     *
     * @param nativeImageResourcePatterns The producer for NativeImageResourcePatternsBuildItem
     */
    @BuildStep
    void registerFonts(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns) {
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/fonts/dejavu/**");
        builder.includeGlob("fonts/**");
        nativeImageResourcePatterns.produce(builder.build());
    }

    /**
     * A build step that processes the `@JsonDeserialize` annotations in the application,
     * specifically focusing on classes within the `net.sf.jasperreports` package.
     * Any class annotated with `@JsonDeserialize` that belongs to this package will be ignored
     * for JSON deserialization purposes by producing an {@link IgnoreJsonDeserializeClassBuildItem}.
     *
     * @param combinedIndex The build item that contains the index of all annotations and classes in the application.
     * @param ignoredJsonDeserializationClasses The build producer used to generate {@link IgnoreJsonDeserializeClassBuildItem}
     *        instances for classes that should be ignored for deserialization.
     */
    @BuildStep
    void ignoreJacksonDeserialize(CombinedIndexBuildItem combinedIndex,
            BuildProducer<IgnoreJsonDeserializeClassBuildItem> ignoredJsonDeserializationClasses) {
        IndexView index = combinedIndex.getIndex();

        // handle the various @JsonDeserialize cases
        for (AnnotationInstance deserializeInstance : index.getAnnotations(JSON_DESERIALIZE)) {
            AnnotationTarget annotationTarget = deserializeInstance.target();
            if (CLASS.equals(annotationTarget.kind())) {
                DotName dotName = annotationTarget.asClass().name();
                if (dotName.packagePrefix().startsWith("net.sf.jasperreports")) {
                    Log.debugf("Ignore @JsonDeserialize: %s", dotName);
                    ignoredJsonDeserializationClasses.produce(new IgnoreJsonDeserializeClassBuildItem(dotName));
                }
            }
        }
    }

    /**
     * Determines the default report root directory based on the provided configuration.
     *
     * @param config The ReportConfig containing build configuration settings.
     * @return A ReportRootBuildItem representing the default report root directory.
     *         If a build is enabled and a source is specified in the config, it returns that source.
     *         Otherwise, it returns the default source path defined in ReportConfig.
     */
    @BuildStep
    ReportRootBuildItem defaultReportRoot(ReportBuildTimeConfig config) {
        if (config.build().enable()) {
            return new ReportRootBuildItem(config.build().source());
        }
        return new ReportRootBuildItem(Path.of(Constants.DEFAULT_SOURCE_PATH));
    }

    /**
     * Collects report files from specified report roots and produces build items for each file.
     * <p>
     * This method walks through the directory tree of each report root, identifying files
     * with extensions matching those defined in ReportFileBuildItem.EXTENSIONS. It produces
     * a ReportFileBuildItem for each matching file, and a CompiledReportFileBuildItem for
     * pre-compiled (.jasper) files.
     *
     * @param reportRoots A list of ReportRootBuildItem representing the root directories to search.
     * @param reportFilesProducer A BuildProducer for ReportFileBuildItem to handle found report files.
     * @param compiledReportFileProducer A BuildProducer for CompiledReportFileBuildItem to handle pre-compiled report files.
     * @param outputTarget The OutputTargetBuildItem containing information about the build output directory.
     */
    @BuildStep
    void collectReportFiles(List<ReportRootBuildItem> reportRoots, BuildProducer<ReportFileBuildItem> reportFilesProducer,
            BuildProducer<CompiledReportFileBuildItem> compiledReportFileProducer, OutputTargetBuildItem outputTarget) {
        final AtomicInteger count = new AtomicInteger(0);
        for (ReportRootBuildItem reportRoot : reportRoots) {
            Path startDir = null;
            // #167 allow an external directory so check if the core path exists first
            if (Files.exists(reportRoot.getOriginalPath())) {
                Log.debugf("JasperReport Source Directory: %s", reportRoot.getOriginalPath());
                startDir = reportRoot.getOriginalPath();
            }
            if (startDir == null) {
                final Path projectRoot = findProjectRoot(outputTarget.getOutputDirectory());
                if (projectRoot == null) {
                    Log.warnf("JasperReport Source Directory does not exist!");
                    continue;
                }
                startDir = projectRoot.resolve(Paths.get(reportRoot.getPath())).normalize();
                if (!Files.exists(startDir)) {
                    Log.warnf("JasperReport Source Directory: %s does not exist!", startDir);
                    continue;
                }
            }

            try {
                // reports - .jrxml
                // styles - .jrtx
                // compiled - .jasper
                Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        // Check if the file has one of the desired extensions
                        for (String ext : ReportFileBuildItem.EXTENSIONS) {
                            String filePath = file.toString();
                            if (filePath.endsWith(ext)) {
                                count.incrementAndGet();
                                reportFilesProducer.produce(new ReportFileBuildItem(file));

                                // allow pre-compiled files to be processed
                                if (filePath.endsWith(Constants.EXT_COMPILED)) {
                                    compiledReportFileProducer.produce(new CompiledReportFileBuildItem(file));
                                }
                                break;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Log.error("Error looking for JasperReport files.", e);
            }
        }

        Log.debugf("Collected %s report file(s)", count.get());
    }

    /**
     * Watches report files for hot deployment in development mode.
     * <p>
     * This method is only executed during development and is responsible for setting up
     * hot deployment watching for JasperReport files. It iterates through the list of
     * report files and adds each file to the list of watched paths for hot deployment.
     *
     * @param watchedPaths A BuildProducer for HotDeploymentWatchedFileBuildItem to register files for watching.
     * @param reportFiles A list of ReportFileBuildItem representing the report files to be watched.
     */
    @BuildStep(onlyIf = IsDevelopment.class)
    void watchReportFiles(BuildProducer<HotDeploymentWatchedFileBuildItem> watchedPaths,
            List<ReportFileBuildItem> reportFiles) {
        reportFiles.forEach((file) -> {

            if (Files.isRegularFile(file.getPath())) {
                Log.debugf("Watching report file %s", file);
                watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(file.getPath().toString()));
            }
        });
    }

    /**
     * Compiles JasperReports files during the build process.
     * <p>
     * This method is responsible for compiling JasperReports (.jrxml) files into their compiled (.jasper) format.
     * It processes all report files found in the project, compiles them, and produces the necessary build items.
     *
     * @param config The ReportConfig containing configuration settings for report compilation.
     * @param reportFiles A list of ReportFileBuildItem representing the report files to be compiled.
     * @param compiledReportFileProducer A producer for CompiledReportFileBuildItem to handle compiled report files.
     * @param outputTarget The OutputTargetBuildItem containing information about the build output directory.
     */
    @BuildStep
    void compileReports(ReportBuildTimeConfig config, List<ReportFileBuildItem> reportFiles,
            BuildProducer<CompiledReportFileBuildItem> compiledReportFileProducer,
            OutputTargetBuildItem outputTarget) {

        if (!config.build().enable()) {
            Log.debug("Automatic report compilation disabled");
            return; // Exit early if compilation is disabled
        }

        Log.debugf("Found %s report(s) to compile", reportFiles.size());

        try {
            // Ensure the destination directory exists
            Path outputDirectoryPath = Path.of(outputTarget.getOutputDirectory().toString(), "classes",
                    config.build().destination().toString());

            if (!Files.exists(outputDirectoryPath)) {
                Files.createDirectories(outputDirectoryPath);
            }

            for (ReportFileBuildItem item : reportFiles) {
                if (item.getFileName().endsWith("." + Constants.EXT_REPORT)) { // Handling .jrxml files
                    Path outputFilePath = Path.of(outputDirectoryPath.toString(),
                            item.getFileName().replace("." + Constants.EXT_REPORT, "." + Constants.EXT_COMPILED));
                    String outputFile = outputFilePath.toString();

                    try {
                        // Use the helper method to check if the output file needs updating
                        if (shouldUpdateFile(item.getPath(), outputFilePath)) {
                            Log.infof("Compiling %s into %s", item.getPath().toString(), outputFile);

                            try (InputStream inputStream = JRLoader
                                    .getLocationInputStream(item.getPath().toAbsolutePath().toString());
                                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                                // Compile the report
                                JasperCompileManager.compileReportToStream(inputStream, outputStream);
                                Log.debugf("Compiled size is %s", outputStream.size());

                                // Write the compiled output to the file
                                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                    outputStream.writeTo(fos);
                                }
                            }
                        }

                        // Allow dynamically compiled files to be processed
                        compiledReportFileProducer.produce(new CompiledReportFileBuildItem(outputFilePath));
                    } catch (JRException | IOException ex) {
                        Log.fatalf("Error while compiling report %s: %s", item.getPath(), ex.getMessage());
                        Log.debug(ex);
                    }
                } else if (item.getFileName().endsWith("." + Constants.EXT_STYLE)) { // Handling .jrtx files
                    Path outputFilePath = Path.of(outputDirectoryPath.toString(), item.getFileName());
                    String outputFile = outputFilePath.toString();

                    try {
                        // Use the helper method to check if the style file needs copying
                        if (shouldUpdateFile(item.getPath(), outputFilePath)) {
                            Log.infof("Copying %s into %s", item.getPath().toString(), outputFile);

                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                Files.copy(item.getPath(), fos);
                            }
                        }
                    } catch (IOException ex) {
                        Log.fatalf("I/O Error while copying styles: %s", ex.getMessage());
                        Log.debug(ex);
                    }
                }
            }
        } catch (IOException ex) {
            Log.fatalf("I/O Error while compiling reports: %s", ex.getMessage());
            Log.debug(ex);
        }
    }

    /**
     * Registers the JasperReportsBeanProducer as an unremovable bean.
     *
     * @return An AdditionalBeanBuildItem for the JasperReportsBeanProducer.
     */
    @BuildStep
    AdditionalBeanBuildItem registerBeanProducer() {
        return AdditionalBeanBuildItem.unremovableOf(JasperReportsBeanProducer.class);
    }

    /**
     * Initializes the JasperReports producer at runtime.
     *
     * @param recorder The JasperReportsRecorder to use for initialization.
     * @param beanContainer The BeanContainerBuildItem containing the bean container.
     * @param config The ReportBuildTimeConfig to use for initialization.
     */
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void initializeBeanProducer(JasperReportsRecorder recorder, BeanContainerBuildItem beanContainer,
            ReportBuildTimeConfig config) {
        recorder.initProducer(beanContainer.getValue(), config);
    }

    @BuildStep
    SystemPropertyBuildItem sysPropHeadless(NativeConfig nativeConfig) {
        if (nativeConfig.enabled()) {
            // see https://github.com/quarkiverse/quarkus-jasperreports/issues/156
            return new SystemPropertyBuildItem("java.awt.headless", "true");
        }
        return null;
    }

    /**
     * Merges multiple JSON resources with the specified filename from the classpath into a single JSON array
     * and produces the merged JSON as a resource for inclusion in the build.
     *
     * @param filename the name of the JSON files to search for and combine.
     * @param generatedResourcesProducer the producer responsible for outputting the generated combined JSON resource.
     */
    private static void mergeAndGenerateJson(String filename,
            BuildProducer<GeneratedResourceBuildItem> generatedResourcesProducer) {
        // Output stream for writing the final merged JSON array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

            // Retrieve all instances of the specified file in the resources
            List<URL> resources = Collections.list(Thread.currentThread().getContextClassLoader().getResources(filename));

            // Append each JSON resource file found to the JSON array
            for (URL resource : resources) {
                Log.debugf("Appending JSON: %s", resource);
                try (InputStream is = resource.openStream(); JsonReader reader = Json.createReader(is)) {
                    // Parse a JSON object from the resource and add it to the array
                    JsonObject json = reader.readObject();
                    arrayBuilder.add(json);
                }
            }

            // Write combined JSON array to output stream
            Map<String, Object> config = Collections.emptyMap();
            JsonWriterFactory writerFactory = Json.createWriterFactory(config);
            try (JsonWriter writer = writerFactory.createWriter(outputStream)) {
                writer.writeArray(arrayBuilder.build());
            }

            // Produce the merged resource for inclusion in the Uber JAR
            Log.warnf("JSON Combined: %s", filename);
            generatedResourcesProducer.produce(new GeneratedResourceBuildItem(filename, outputStream.toByteArray()));
        } catch (IOException ex) {
            // Log an error if an exception occurs during processing
            Log.errorf("Unexpected error combining %s", filename, ex);
        }
    }

    // Helper method to check if the file should be updated
    private static boolean shouldUpdateFile(Path inputFilePath, Path outputFilePath) throws IOException {
        if (Files.exists(outputFilePath)) {
            // Compare last modified times
            FileTime inputFileLastModifiedTime = Files.getLastModifiedTime(inputFilePath);
            FileTime outputFileLastModifiedTime = Files.getLastModifiedTime(outputFilePath);

            // Return false if the output file is newer or the same as the input file
            if (outputFileLastModifiedTime.compareTo(inputFileLastModifiedTime) >= 0) {
                Log.debugf("Skipping update, output file %s is up to date.", outputFilePath.toString());
                return false;
            }
        }
        // If the file doesn't exist or is older, we should update it
        return true;
    }

    /**
     * Finds the project root directory. It starts with a build target and searches for a directory under that, and if that is
     * not
     * found, it uses the start directory.
     *
     * @param outputDirectory The output directory path.
     * @return The path to the project root directory.
     */
    private static Path findProjectRoot(Path outputDirectory) {
        Path currentPath = outputDirectory;
        do {
            if (Files.exists(currentPath.resolve(Paths.get("src", "main")))
                    || Files.exists(currentPath.resolve(Paths.get("config", "application.properties")))
                    || Files.exists(currentPath.resolve(Paths.get("config", "application.yaml")))
                    || Files.exists(currentPath.resolve(Paths.get("config", "application.yml")))) {
                return currentPath.normalize();
            }
            if (currentPath.getParent() != null && Files.exists(currentPath.getParent())) {
                currentPath = currentPath.getParent();
            } else {
                return null;
            }
        } while (true);
    }
}
