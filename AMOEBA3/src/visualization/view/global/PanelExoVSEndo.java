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
public class PanelExoVSEndo extends JPanel implements ScheduledItem {

	/** The chart panel agents. */
	/* Agents chart */
	ChartPanel chartPanelAgents;
	
	/** The chart agents. */
	JFreeChart chartAgents;
	
	/** The data set agents. */
	XYSeriesCollection dataSetAgents;


	

	

	
	/** The world. */
	World world;

	/**
	 * Instantiates a new panel chart.
	 *
	 * @param world the world
	 */
	public PanelExoVSEndo(World world) {

		this.setLayout(new FlowLayout());
		this.world = world;

		/* Create Agent chart */
		dataSetAgents = createDataSetAgents();
		JFreeChart chart = createChart();
		chartPanelAgents = new ChartPanel(chart);
		chartPanelAgents.setPreferredSize(new java.awt.Dimension(1600, 400));
		this.add(chartPanelAgents);



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

		collection.addSeries(new XYSeries("Exo"));
		collection.addSeries(new XYSeries("Endo"));
		collection.addSeries(new XYSeries("Oracle"));

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
		final NumberAxis rangeAxis1 = new NumberAxis("EXO VS ENDO VS ORACLE predictions");
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
		return new JFreeChart("EXO VS ENDO VS ORACLE predictions",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}



	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		
		dataSetAgents.getSeries("Exo").add(
				tick, world.getScheduler().getHeadAgent().getPrediction());
		dataSetAgents.getSeries("Endo").add(
				tick, world.getScheduler().getHeadAgent().getEndogenousPrediction());
		dataSetAgents.getSeries("Oracle").add(
				tick, world.getScheduler().getHeadAgent().getOracleValue());


		
		

	}

	// }

}
