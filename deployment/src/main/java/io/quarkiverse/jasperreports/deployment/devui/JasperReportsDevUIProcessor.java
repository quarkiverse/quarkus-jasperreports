package io.quarkiverse.jasperreports.deployment.devui;

import java.util.List;

import io.quarkiverse.jasperreports.deployment.item.ReportFileBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.ExternalPageBuilder;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.devui.spi.page.PageBuilder;
import io.quarkus.devui.spi.page.TableDataPageBuilder;
import net.sf.jasperreports.engine.JasperReport;

public class JasperReportsDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            List<ReportFileBuildItem> reportFiles) {
        final CardPageBuildItem card = new CardPageBuildItem();

        final ExternalPageBuilder versionPage = Page.externalPageBuilder("JasperReports Version")
                .icon("font-awesome-solid:tag")
                .url("https://community.jaspersoft.com/download-jaspersoft/community-edition/")
                .doNotEmbed()
                .staticLabel(JasperReport.class.getPackage().getSpecificationVersion());

        card.addPage(versionPage);

        final PageBuilder<TableDataPageBuilder> reportsPage = Page.tableDataPageBuilder("Reports")
                .icon("font-awesome-solid:file-pdf")
                .staticLabel(String.valueOf(reportFiles.size()))
                .showColumn("fileName")
                .showColumn("type")
                .showColumn("parent")
                .buildTimeDataKey("reports");

        card.addPage(reportsPage);

        card.addBuildTimeData("reports", reportFiles);

        card.setCustomCard("qwc-jasperreports-card.js");

        cardPageBuildItemBuildProducer.produce(card);
    }
}