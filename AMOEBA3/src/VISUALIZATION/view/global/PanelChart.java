package VISUALIZATION.view.global;

import java.awt.FlowLayout;

import javax.swing.JPanel;

import MAS.kernel.World;
import MAS.ncs.NCS;

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

import VISUALIZATION.view.system.ScheduledItem;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelChart.
 */
public class PanelChart extends JPanel implements ScheduledItem {

	/** The chart panel agents. */
	/* Agents chart */
	ChartPanel chartPanelAgents;
	
	/** The chart agents. */
	JFreeChart chartAgents;
	
	/** The data set agents. */
	XYSeriesCollection dataSetAgents;

	/** The chart panel NCS. */
	/* NCS chart */
	ChartPanel chartPanelNCS;
	
	/** The chart NCS. */
	JFreeChart chartNCS;
	
	/** The data set NCS. */
	XYSeriesCollection dataSetNCS;
	
	/** The world. */
	World world;

	/**
	 * Instantiates a new panel chart.
	 *
	 * @param world the world
	 */
	public PanelChart(World world) {

		this.setLayout(new FlowLayout());
		this.world = world;

		/* Create Agent chart */
		dataSetAgents = createDataSetAgents();
		JFreeChart chart = createChart();
		chartPanelAgents = new ChartPanel(chart);
		chartPanelAgents.setPreferredSize(new java.awt.Dimension(600, 400));
		this.add(chartPanelAgents);

		/* Create NCS chart */
		dataSetNCS = createDataSetNCS();
		JFreeChart chartNCS = createChartNCS();
		chartPanelNCS = new ChartPanel(chartNCS);
		chartPanelNCS.setPreferredSize(new java.awt.Dimension(600, 400));
		this.add(chartPanelNCS);

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

		collection.addSeries(new XYSeries("Context"));
		collection.addSeries(new XYSeries("Percept"));

		return collection;

	}

	/**
	 * Creates the data set NCS.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDataSetNCS() {

		XYSeriesCollection collection = new XYSeriesCollection();

		
		for (NCS ncs : NCS.values()) {
			collection.addSeries(new XYSeries(ncs.toString()));
		}
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

	/**
	 * Creates the chart NCS.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChartNCS() {

		// create subplot 1...
		final XYDataset data1 = dataSetNCS;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("NCS");
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
		return new JFreeChart("Total of NCS",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}

	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		
		dataSetAgents.getSeries("Context").add(
				tick, world.getNumberOfAgents().get("Context"));
		dataSetAgents.getSeries("Percept").add(
				tick, world.getNumberOfAgents().get("Percept"));


		
		for (NCS ncs : NCS.values()) {
			dataSetNCS.getSeries(ncs.toString()).add(tick, world.getThisLoopNCS().get(ncs));
		}

	}

	// }

}
