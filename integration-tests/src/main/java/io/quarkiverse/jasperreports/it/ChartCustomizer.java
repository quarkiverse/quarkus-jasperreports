package io.quarkiverse.jasperreports.it;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;

import net.sf.jasperreports.charts.JRAbstractChartCustomizer;
import net.sf.jasperreports.charts.JRChart;

public class ChartCustomizer extends JRAbstractChartCustomizer {

    @Override
    public void customize(final JFreeChart freeChart, final JRChart jasperChart) {
        final XYPlot plot = freeChart.getXYPlot();
        final XYItemRenderer renderer = plot.getRenderer();
        final Range range = renderer.findDomainBounds(plot.getDataset());
        final Range yRange = plot.getDataRange(plot.getRangeAxis());

        final double startX = range.getLowerBound() - 1.0d;
        final double endX = range.getUpperBound() + 1.0d;

        final Color green = new Color(0, 192, 0, 64);

        renderer.addAnnotation(
                new XYBoxAnnotation(startX, 0.0d, endX, yRange.getUpperBound() / 2.0d, new BasicStroke(), green, green));
    }

}
