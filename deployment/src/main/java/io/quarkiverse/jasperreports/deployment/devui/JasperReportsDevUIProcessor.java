package io.quarkiverse.jasperreports.deployment.devui;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.ExternalPageBuilder;
import io.quarkus.devui.spi.page.Page;
import net.sf.jasperreports.engine.JasperReport;

public class JasperReportsDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer) {
        final CardPageBuildItem card = new CardPageBuildItem();

        final ExternalPageBuilder versionPage = Page.externalPageBuilder("JasperReports Version")
                .icon("font-awesome-regular:file-pdf")
                .url("https://community.jaspersoft.com/download-jaspersoft/community-edition/")
                .doNotEmbed()
                .staticLabel(JasperReport.class.getPackage().getSpecificationVersion());

        card.addPage(versionPage);

        card.setCustomCard("qwc-jasperreports-card.js");

        cardPageBuildItemBuildProducer.produce(card);
    }
}