package visualization.view.global;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import mas.kernel.World;
import mas.ncs.NCS;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import visualization.view.system.ScheduledItem;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelChart.
 */
public class PanelExoVSEndo extends JPanel implements ScheduledItem, ChartMouseListener {

	/** The chart panel agents. */
	/* Agents chart */
	ChartPanel chartPanelAgents;
	
	/** The chart agents. */
	JFreeChart chartAgents;
	
	/** The data set agents. */
	XYSeriesCollection dataSetAgents;

	
	
	
	double exoTotalError;
	
	
	/** The world. */
	World world;

	/**
	 * Instantiates a new panel chart.
	 *
	 * @param world the world
	 */
	public PanelExoVSEndo(World world, String typeOfEndo) {

		this.setLayout(new FlowLayout());
		this.world = world;

		/* Create Agent chart */
		dataSetAgents = createDataSetAgents();
		JFreeChart chart = createChart();
		chartPanelAgents = new ChartPanel(chart);
		chartPanelAgents.setPreferredSize(new java.awt.Dimension(2560, 1440));
//		chartPanelAgents.setMinimumDrawHeight(0);
//		chartPanelAgents.setMinimumDrawWidth(0);
//		chartPanelAgents.setMaximumDrawHeight(1440);
//		chartPanelAgents.setMaximumDrawWidth(2560);
//		chartPanelAgents.setMouseWheelEnabled(true);
//		chartPanelAgents.setMinimumSize(new Dimension(1920, 1080));
		
		this.add(chartPanelAgents);

		exoTotalError = 0;

	}

	/*
	 * private JFreeChart createChart(DefaultCategoryDataset dataset2) { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 * private DefaultCategoryDataset createDataset() { // TODO Auto-generated
	 * method stub return null; }
	 */

	/**
	 * Creates the data set agents.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDataSetAgents() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("Error Predictions"));
		
		
		//collection.addSeries(new XYSeries("Oracle"));

		return collection;

	}



	/**
	 * Creates the chart.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChart() {

		// create subplot 1...
		final XYDataset data1 = dataSetAgents;
		final XYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setSeriesPaint(0, new Color(200, 0, 0)); 
		renderer.setSeriesPaint(1, new Color(0, 0, 200)); 
		renderer.setSeriesPaint(2, new Color(0, 200, 0)); 
		renderer.setSeriesPaint(3, new Color(200, 200, 0)); 
		final NumberAxis rangeAxis1 = new NumberAxis("Error Predictions");
		final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer);
		subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
;
		
		// parent plot...
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(
				new NumberAxis("Tick"));
		plot.setGap(10.0);

		// add the subplots...
		plot.add(subplot1, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart("Error Predictions",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}



	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		Double exoError = 0.0;
		
		exoError = Math.abs(world.getScheduler().getHeadAgent().getPrediction() - world.getScheduler().getHeadAgent().getOracleValue()) / Math.abs(world.getScheduler().getHeadAgent().getOracleValue());
		if(!exoError.isNaN()) {
			dataSetAgents.getSeries("Error Predictions").add(tick, normalizePositiveValues(100, 20, exoError));
		}
		
		
		

		exoTotalError += exoError;
		
	}
	




	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		//System.out.println("getPoint " + mouseEvent.getPoint() + " " + this.getSize());
		//graph.getNode("origin").
		
		Point2D p = chartPanelAgents.translateScreenToJava2D(event.getTrigger().getPoint());
		Rectangle2D plotArea = chartPanelAgents.getScreenDataArea();
		XYPlot plot = (XYPlot) chartAgents.getPlot(); // your plot
		double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
		double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
		
		
		System.out.println("{ "+ chartX + " ; " + chartY +" } ");
		
		
		
		//this.world.getAmoeba().request(request(requestPosition));
		
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	public double normalize(double lowerBound, double upperBound, double value) {
		return lowerBound + (upperBound - lowerBound)/(1+Math.exp(-value));
	}
	
	public double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound*2*(- 0.5 + 1/(1+Math.exp(-value/dispersion)));
	}

	// }

}
