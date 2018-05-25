package visualization.graphView.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

// TODO: Auto-generated Javadoc
/**
 * The Class XYDomainValueAnnotation.
 *
 * @author peter
 */
public class XYDomainValueAnnotation extends ValueAnnotation implements XYAnnotation{
    
    /**
     * Draws the annotation.
     *
     * @param g2  the graphics device.
     * @param plot  the plot.
     * @param dataArea  the data area.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param rendererIndex  the renderer index.
     * @param info  an optional info object that will be populated with
     *              entity information.
     */
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int rendererIndex,
                     PlotRenderingInfo info){
        int index = plot.getDomainAxisIndex(domainAxis);
        if(index < 0) return;
        AxisLocation dal = plot.getDomainAxisLocation(index);
        RectangleEdge axisEdge = Plot.resolveDomainAxisLocation(dal, plot.getOrientation());
        double value = domainAxis.valueToJava2D(getValue(), dataArea, axisEdge);
        Line2D line = this.getLine(dataArea, axisEdge, value);
        if(line == null) return;
        g2.setPaint(getPaint());
        g2.setStroke(getStroke());
        g2.draw(line);
        drawLabel(g2, dataArea, line.getBounds2D(), axisEdge);
        addEntity(info, ShapeUtilities.createLineRegion(line, 6), rendererIndex, getToolTipText(), getURL());
    }

}
