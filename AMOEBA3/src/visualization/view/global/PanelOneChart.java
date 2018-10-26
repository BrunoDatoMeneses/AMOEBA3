package visualization.view.global;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import mas.kernel.World;
import mas.ncs.NCS;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import visualization.view.system.ScheduledItem;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelChart.
 */
public class PanelOneChart extends JPanel implements ScheduledItem {

	/** The chart panel agents. */
	/* Agents chart */
	ChartPanel chartPanel;
	
	/** The chart agents. */
	JFreeChart chart;
	
	/** The data set agents. */
	XYSeriesCollection dataSetPredictions;

	
	
	/** The world. */
	World world;

	/**
	 * Instantiates a new panel chart.
	 *
	 * @param world the world
	 */
	public PanelOneChart(World world) {

		this.setLayout(new FlowLayout());
		this.world = world;

		/* Create prediction chart */
		dataSetPredictions = createDataSetPredictions();
		JFreeChart chart = createChart();
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(600, 400));



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
	private XYSeriesCollection createDataSetPredictions() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("Predictions"));
		collection.addSeries(new XYSeries("Endogenous Predictions"));

		return collection;

	}



	/**
	 * Creates the chart.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChart() {

		// create subplot 1...
		final XYDataset data1 = dataSetPredictions;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Agents in AMAS");
		final XYPlot subplot1 = new XYPlot(data1, null, rangeAxis1, renderer1);
		subplot1.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

		// parent plot...
		final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(
				new NumberAxis("Tick"));
		plot.setGap(10.0);

		// add the subplots...
		plot.add(subplot1, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);

		// return a new chart containing the overlaid plot...
		return new JFreeChart(" Agents in AMAS",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}

	

	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		
		dataSetPredictions.getSeries("Predictions").add(
				tick, world.getNumberOfAgents().get("Context"));
		dataSetPredictions.getSeries("Endogenous Predictions").add(
				tick, world.getNumberOfAgents().get("Percept"));


		
		

	}

	// }

}
