package io.quarkiverse.jasperreports.deployment;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

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
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRReportCompileData;
import net.sf.jasperreports.engine.util.JRLoader;

/**
 * Processor class for JasperReports integration in Quarkus.
 * This class handles various build steps required for JasperReports functionality,
 * including feature registration, resource merging, dependency indexing,
 * and reflection configuration.
 */
class JasperReportsProcessor {

    private static final String FEATURE = "jasperreports";
    private static final String EXTENSIONS_FILE = "jasperreports_extension.properties";
    private static final String DEFAULT_ROOT_PATH = "src/main/resources";

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
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-jdt"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-pdf"));
        index.produce(new IndexDependencyBuildItem("net.sf.jasperreports", "jasperreports-xalan"));
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
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.design.JRCompiler.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistry.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.extensions.ExtensionsRegistryFactory.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRDataSource.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRDataSourceProvider.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRVisitor.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.xml.ReportLoader.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.JRTemplate.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.query.QueryExecuterFactory.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.query.JRQueryExecuter.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, net.sf.jasperreports.engine.util.xml.JRXPathExecuterFactory.class.getName()));
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

        // basic Java classes found in reports
        classNames.addAll(collectImplementors(combinedIndex, java.util.Collection.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, java.time.temporal.TemporalAccessor.class.getName()));
        classNames.addAll(collectImplementors(combinedIndex, java.util.Map.Entry.class.getName()));
        classNames.add(byte.class.getName());
        classNames.add(byte[].class.getName());
        classNames.add(java.awt.color.ColorSpace.class.getName());
        classNames.add(java.awt.Color.class.getName());
        classNames.add(java.io.Serializable.class.getName());
        classNames.add(java.lang.Boolean.class.getName());
        classNames.add(java.lang.Byte.class.getName());
        classNames.add(java.lang.Enum.class.getName());
        classNames.add(java.lang.Byte.class.getName());
        classNames.add(java.lang.Character.class.getName());
        classNames.add(java.lang.Double.class.getName());
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

        //@formatter:on
        final TreeSet<String> uniqueClasses = new TreeSet<>(classNames);
        Log.debugf("Reflection: %s", uniqueClasses);

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
                net.sf.jasperreports.poi.query.PoiQueryExecuterFactoryBundle.class.getName(),
                net.sf.jasperreports.xalan.data.XalanXmlDataSource.class.getPackageName(),
                net.sf.jasperreports.xalan.util.XalanNsAwareXPathExecuter.class.getPackageName(),
                org.apache.batik.anim.values.AnimatableTransformListValue.class.getPackageName(),
                org.apache.xmlbeans.impl.schema.TypeSystemHolder.class.getName()).toList());
        //@formatter:on
        Log.debugf("Runtime: %s", classes);
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

        // Register resource patterns for OOXML export
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("**/export/ooxml/docx/**");
        builder.includeGlob("**/export/ooxml/pptx/**");
        builder.includeGlob("**/export/ooxml/xlsx/**");
        nativeImageResourcePatterns.produce(builder.build());
    }

    /**
     * Registers report files for native image compilation and processes compiled report files.
     *
     * @param nativeImageResourcePatterns Producer for native image resource patterns
     * @param additionalClasses Producer for additional generated classes
     * @param reflectiveClassProducer Producer for reflective classes
     * @param outputTarget The output target build item
     */
    @BuildStep
    void registerReports(BuildProducer<NativeImageResourcePatternsBuildItem> nativeImageResourcePatterns,
            BuildProducer<GeneratedClassBuildItem> additionalClasses,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClassProducer,
            OutputTargetBuildItem outputTarget) {
        final NativeImageResourcePatternsBuildItem.Builder builder = NativeImageResourcePatternsBuildItem.builder();
        builder.includeGlob("*." + ReportFileBuildItem.EXT_REPORT);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_COMPILED);
        builder.includeGlob("*." + ReportFileBuildItem.EXT_STYLE);
        nativeImageResourcePatterns.produce(builder.build());

        Path startDir = findProjectRoot(outputTarget.getOutputDirectory());
        Log.debugf("JasperReport Source Directory: %s", startDir);
        Set<Path> foundFiles = new HashSet<>();

        try {
            // compiled - .jasper
            Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    // Check if the file has one of the desired extensions
                    String filePath = file.toString();
                    if (filePath.endsWith(ReportFileBuildItem.EXT_COMPILED)) {
                        Log.debugf("Jasper compiled report: %s", filePath);
                        foundFiles.add(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Log.error("Error looking for JasperReport files.", e);
        }

        foundFiles.forEach((file) -> {
            try {
                String jasperFilePath = file.toFile().getAbsolutePath();
                JasperReport report = (JasperReport) JRLoader.loadObject(JRLoader.getLocationInputStream(jasperFilePath));
                JRReportCompileData reportData = (JRReportCompileData) report.getCompileData();
                ReportExpressionEvaluationData mainData = (ReportExpressionEvaluationData) reportData
                        .getMainDatasetCompileData();
                String reportDataClass = mainData.getCompileName();
                if (StringUtils.isNotBlank(reportDataClass)) {
                    byte[] bytes = (byte[]) mainData.getCompileData();
                    Log.debugf("JasperReport Data Class: %s Size: %d", reportDataClass, bytes.length);

                    reflectiveClassProducer
                            .produce(ReflectiveClassBuildItem.builder(reportDataClass).constructors().methods().serialization()
                                    .build());
                    additionalClasses.produce(new GeneratedClassBuildItem(true, reportDataClass, bytes));
                }

            } catch (JRException e) {
                Log.error("Error loading JasperReport file class.", e);
            }
        });
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

    @BuildStep(onlyIf = IsDevelopment.class)
    ReportRootBuildItem defaultReportRoot() {
        return new ReportRootBuildItem(DEFAULT_ROOT_PATH);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void watchReportFiles(BuildProducer<HotDeploymentWatchedFileBuildItem> watchedPaths,
            BuildProducer<ReportFileBuildItem> reportFiles,
            List<ReportRootBuildItem> reportRoots) {

        for (ReportRootBuildItem reportRoot : reportRoots) {
            Path startDir = Paths.get(reportRoot.getPath()); // Specify your starting directory
            List<Path> foundFiles = new ArrayList<>();

            try {
                // reports - .jrxml
                // styles - .jrtx
                // compiled - .jasper
                Files.walkFileTree(startDir, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        // Check if the file has one of the desired extensions
                        for (String ext : ReportFileBuildItem.EXTENSIONS) {
                            if (file.toString().endsWith(ext)) {
                                foundFiles.add(file);
                                break;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Log.error("Error looking for report files.", e);
            }

            // Print the found files
            foundFiles.forEach((file) -> {
                reportFiles.produce(new ReportFileBuildItem(file));
                watchedPaths.produce(new HotDeploymentWatchedFileBuildItem(file.toString()));
            });
        }
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

    private List<String> collectInterfacesInPackage(CombinedIndexBuildItem combinedIndex, String packageName) {
        final List<String> classes = new ArrayList<>();
        final List<DotName> packages = new ArrayList<>(combinedIndex.getIndex().getSubpackages(packageName));
        packages.add(DotName.createSimple(packageName));
        for (DotName aPackage : packages) {
            final List<String> packageClasses = combinedIndex.getIndex()
                    .getClassesInPackage(aPackage)
                    .stream()
                    .filter(ClassInfo::isInterface) // Filter only interfaces
                    .map(ClassInfo::toString)
                    .toList();
            classes.addAll(packageClasses);
        }
        Log.tracef("Package: %s", classes);
        return classes;
    }

    private List<String> collectSubclasses(CombinedIndexBuildItem combinedIndex, String className) {
        List<String> classes = combinedIndex.getIndex()
                .getAllKnownSubclasses(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toList());
        classes.add(className);
        Log.tracef("Subclasses: %s", classes);
        return classes;
    }

    public List<String> collectImplementors(CombinedIndexBuildItem combinedIndex, String className) {
        Set<String> classes = combinedIndex.getIndex()
                .getAllKnownImplementors(DotName.createSimple(className))
                .stream()
                .map(ClassInfo::toString)
                .collect(Collectors.toCollection(HashSet::new));
        classes.add(className);
        Set<String> subclasses = new HashSet<>();
        for (String implementationClass : classes) {
            subclasses.addAll(collectSubclasses(combinedIndex, implementationClass));
        }
        classes.addAll(subclasses);
        Log.tracef("Implementors: %s", classes);
        return new ArrayList<>(classes);
    }

    static Path findProjectRoot(Path outputDirectory) {
        Path currentPath = outputDirectory.getParent();
        Log.tracef("Current Directory: %s", currentPath);
        Path root = Paths.get(DEFAULT_ROOT_PATH);
        if (Files.exists(currentPath.resolve(root))) {
            return currentPath.resolve(root).normalize();
        } else {
            return outputDirectory;
        }
    }
}