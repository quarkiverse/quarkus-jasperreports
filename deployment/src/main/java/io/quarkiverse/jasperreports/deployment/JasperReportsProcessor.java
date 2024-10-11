package io.quarkiverse.jasperreports.deployment;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import io.quarkiverse.jasperreports.deployment.config.ReportConfig;
import io.quarkiverse.jasperreports.deployment.item.CompiledReportFileBuildItem;
import io.quarkiverse.jasperreports.deployment.item.ReportFileBuildItem;
import io.quarkiverse.jasperreports.deployment.item.ReportRootBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedClassBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageProxyDefinitionBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBundleBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourcePatternsBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedPackageBuildItem;
import io.quarkus.deployment.pkg.builditem.OutputTargetBuildItem;
import io.quarkus.deployment.pkg.builditem.UberJarMergedResourceBuildItem;
import io.quarkus.logging.Log;
import net.sf.jasperreports.compilers.ReportExpressionEvaluationData;
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

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    /**
     * Merges the JasperReports extension properties file into the Uber JAR.
     * <p>
     * This build step ensures that the JasperReports extension properties file
     * is included in the final Uber JAR, allowing JasperReports to properly
     * load its extensions at runtime.
     *
     * @return A {@link UberJarMergedResourceBuildItem} representing the merged resource
     */
    @BuildStep
    UberJarMergedResourceBuildItem mergeResource() {
        return new UberJarMergedResourceBuildItem(EXTENSIONS_FILE);
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
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-jaxen"));
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
     * @param combinedIndex The CombinedIndexBuildItem containing information about indexed classes.
     */
    @BuildStep
    void registerForReflection(BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
            CombinedIndexBuildItem combinedIndex) {
        //@formatter:off
        final List<String> classNames = new ArrayList<>();
        // By Implementors: jasper interfaces/abstract classes that are created with Class.forName
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
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.governors.GovernorExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.jackson.util.JacksonUtil.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.parts.PartComponentsExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.pdf.classic.ClassicPdfProducerFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.renderers.AbstractSvgDataToGraphics2DRenderer.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.renderers.util.SvgFontProcessor.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.repo.DefaultRepositoryExtensionsRegistryFactory.class.getPackageName()));
        classNames.addAll(collectClassesInPackage(combinedIndex, net.sf.jasperreports.util.JsonLoader.class.getPackageName()));

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
     * @param combinedIndex Combined index of classes in the application
     */
    @BuildStep
    void runtimeInitializedClasses(BuildProducer<RuntimeInitializedPackageBuildItem> runtimeInitializedPackages,
            CombinedIndexBuildItem combinedIndex) {
        //@formatter:off
        List<String> classes = collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistryFactory.class.getName());
        classes.addAll(Stream.of("javax.swing",
                "javax.swing.plaf.metal",
                "javax.swing.text.html",
                "javax.swing.text.rtf",
                "sun.datatransfer",
                "sun.swing",
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

        // Register resource patterns for OOXML export and font icons
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/export/ooxml/docx/**");
        builder.includeGlob("**/export/ooxml/pptx/**");
        builder.includeGlob("**/export/ooxml/xlsx/**");
        builder.includeGlob("**/sf/jasperreports/fonts/icons/**");
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
        builder.includeGlob("**/*." + ReportFileBuildItem.EXT_REPORT);
        builder.includeGlob("**/*." + ReportFileBuildItem.EXT_COMPILED);
        builder.includeGlob("**/*." + ReportFileBuildItem.EXT_STYLE);
        builder.includeGlob("**/*." + ReportFileBuildItem.EXT_DATA_ADAPTER);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_REPORT);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_COMPILED);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_STYLE);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_DATA_ADAPTER);
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

        // only care about compiled - .jasper files
        for (CompiledReportFileBuildItem reportFile : compiledReports) {
            Log.debugf("Jasper compiled report: %s", reportFile.getPath());
            try {
                String jasperFilePath = reportFile.getPath().toFile().getAbsolutePath();
                JasperReport report = (JasperReport) JRLoader.loadObject(JRLoader.getLocationInputStream(jasperFilePath));
                JRReportCompileData reportData = (JRReportCompileData) report.getCompileData();
                ReportExpressionEvaluationData mainData = (ReportExpressionEvaluationData) reportData
                        .getMainDatasetCompileData();
                String reportDataClass = mainData.getCompileName();
                if (StringUtils.isNotBlank(reportDataClass)) {
                    byte[] bytes = (byte[]) mainData.getCompileData();
                    Log.debugf("JasperReport Data Class: %s Size: %d", reportDataClass, bytes.length);

                    reflectiveClassProducer
                            .produce(ReflectiveClassBuildItem.builder(reportDataClass).constructors().methods()
                                    .serialization()
                                    .build());
                    additionalClasses.produce(new GeneratedClassBuildItem(true, reportDataClass, bytes));
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
     * @param combinedIndex The combined index of classes for the build
     */
    @BuildStep
    void registerJasperReportsProxies(BuildProducer<NativeImageProxyDefinitionBuildItem> proxyDefinitions,
            CombinedIndexBuildItem combinedIndex) {
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
        nativeImageResourcePatterns.produce(builder.build());
    }

    /**
     * Determines the default report root directory based on the provided configuration.
     *
     * @param config The ReportConfig containing build configuration settings.
     * @return A ReportRootBuildItem representing the default report root directory.
     *         If build is enabled and a source is specified in the config, it returns that source.
     *         Otherwise, it returns the default source path defined in ReportConfig.
     */
    @BuildStep
    ReportRootBuildItem defaultReportRoot(ReportConfig config) {
        if (config.build().enable()) {
            return new ReportRootBuildItem(config.build().source().toString());
        }
        return new ReportRootBuildItem(ReportConfig.DEFAULT_SOURCE_PATH);
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
            Path startDir = findProjectRoot(outputTarget.getOutputDirectory(), Paths.get(reportRoot.getPath()));

            if (!Files.exists(startDir)) {
                Log.warnf("JasperReport Source Directory: %s does not exist!", startDir);
                continue;
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
                                if (filePath.endsWith(ReportFileBuildItem.EXT_COMPILED)) {
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

        // Print the found files
        reportFiles.forEach((file) -> {
            Log.tracef("Watching report file %s", file);
            watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(file.getFileName()));
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
     * @param compiledReportProducer A producer for GeneratedClassBuildItem to handle compiled report classes.
     * @param compiledReportFileProducer A producer for CompiledReportFileBuildItem to handle compiled report files.
     * @param outputTarget The OutputTargetBuildItem containing information about the build output directory.
     */
    @BuildStep
    void compileReports(ReportConfig config, List<ReportFileBuildItem> reportFiles,
            BuildProducer<GeneratedClassBuildItem> compiledReportProducer,
            BuildProducer<CompiledReportFileBuildItem> compiledReportFileProducer,
            OutputTargetBuildItem outputTarget) {

        if (!config.build().enable()) {
            Log.debug("Automatic report compilation disabled");
        }

        Log.debugf("Found %s report(s) to compile", reportFiles.size());
        try {
            // make sure the destination path exists before we try to write files to it
            Path outputDirectoryPath = Path.of(outputTarget.getOutputDirectory().toString(), "classes",
                    config.build().destination().toString());

            if (!Files.exists(outputDirectoryPath)) {
                Files.createDirectories(outputDirectoryPath);
            }

            // TODO - only compile if the report file has changed?
            for (ReportFileBuildItem item : reportFiles) {
                try (InputStream inputStream = JRLoader.getLocationInputStream(item.getPath().toString())) {
                    Path outputFilePath = Path.of(outputDirectoryPath.toString(),
                            item.getFileName().replace("." + ReportFileBuildItem.EXT_REPORT,
                                    "." + ReportFileBuildItem.EXT_COMPILED));
                    String outputFile = outputFilePath.toString();

                    Log.infof("Compiling %s into %s", item.getPath().toString(), outputFile);

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    JasperCompileManager.compileReportToStream(inputStream, outputStream);

                    Log.debugf("Compiled size is %s", outputStream.size());

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        outputStream.writeTo(fos);
                    }

                    // allow dynamically compiled files to be processed
                    compiledReportFileProducer.produce(new CompiledReportFileBuildItem(outputFilePath));
                    compiledReportProducer
                            .produce(new GeneratedClassBuildItem(true, item.getFileName(), outputStream.toByteArray()));
                } catch (JRException ex) {
                    Log.fatalf("JasperReports error while compiling reports: %s", ex.getMessage());
                    Log.debug(ex);
                }
            }
        } catch (IOException ex) {
            Log.fatalf("I/O Error while compiling reports: %s", ex.getMessage());
            Log.debug(ex);
        }
    }

    /**
     * Finds the project root directory. It starts with build target and searches for a directory under that and if that is not
     * found it uses the start directory.
     *
     * @param outputDirectory The output directory path.
     * @param startDirectory The starting directory path to search from.
     * @return The path to the project root directory.
     */
    static Path findProjectRoot(Path outputDirectory, Path startDirectory) {
        Path currentPath = outputDirectory.getParent();
        if (Files.exists(currentPath.resolve(startDirectory))) {
            return currentPath.resolve(startDirectory).normalize();
        } else {
            return startDirectory;
        }
    }
}