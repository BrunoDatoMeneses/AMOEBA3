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
public class TimeExecutionCharts extends JPanel implements ScheduledItem, ChartMouseListener {


	ChartPanel chartPanelTimeExecution;
	JFreeChart chartTimeExecution;
	XYSeriesCollection dataSetTimeExecution;
	
	

	
	String endoType;
	
	/** The world. */
	World world;

	/**
	 * Instantiates a new panel chart.
	 *
	 * @param world the world
	 */
	public TimeExecutionCharts(World world, String typeOfEndo) {

		this.setLayout(new FlowLayout());
		this.world = world;
		this.endoType = typeOfEndo;

		/* Create Criticity chart */
		dataSetTimeExecution = createDataSet();
		JFreeChart chart0 = createChart(dataSetTimeExecution,"Time Execution");
		chartPanelTimeExecution = new ChartPanel(chart0);
		chartPanelTimeExecution.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));
		
		
		this.add(chartPanelTimeExecution);




	}

	/*
	 * private JFreeChart createChart(DefaultCategoryDataset dataset2) { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 * private DefaultCategoryDataset createDataset() { // TODO Auto-generated
	 * method stub return null; }
	 */

	
	
	private XYSeriesCollection createDataSet() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("Agents"));
		collection.addSeries(new XYSeries("Percepts"));
		collection.addSeries(new XYSeries("Contexts"));
		collection.addSeries(new XYSeries("Head"));
		collection.addSeries(new XYSeries("UI"));
		
		


		return collection;

	}



	/**
	 * Creates the chart.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChart(XYSeriesCollection dataSet, String title) {

		// create subplot 1...
		final XYDataset data1 = dataSet;
		final XYItemRenderer renderer = new StandardXYItemRenderer();
		renderer.setSeriesPaint(0, new Color(0, 0, 0, 200)); 
		renderer.setSeriesPaint(1, new Color(200, 0, 0)); 
		renderer.setSeriesPaint(2, new Color(0, 0, 200)); 
		renderer.setSeriesPaint(3, new Color(0, 200, 0)); 
		renderer.setSeriesPaint(4, new Color(200, 200, 0)); 
		renderer.setSeriesPaint(5, new Color(0, 200, 200));
		renderer.setSeriesPaint(6, new Color(200, 0, 200));
		final NumberAxis rangeAxis1 = new NumberAxis(title);
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
		return new JFreeChart(title,
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}



	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		
		
		dataSetTimeExecution.getSeries("Agents").add(tick, world.getScheduler().agentsExecutionTime);
		dataSetTimeExecution.getSeries("Percepts").add(tick, world.getScheduler().perceptsExecutionTime);
		dataSetTimeExecution.getSeries("Contexts").add(tick, world.getScheduler().contextsExecutionTime);
		dataSetTimeExecution.getSeries("Head").add(tick, world.getScheduler().headExecutionTime);
		dataSetTimeExecution.getSeries("UI").add(tick, world.getScheduler().previousUIExecutionTime);
		

		

		
		
	}
	




	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		////system.out.println("getPoint " + mouseEvent.getPoint() + " " + this.getSize());
		//graph.getNode("origin").
		
		
		
		
		//system.out.println("{ "+ chartX + " ; " + chartY +" } ");
		
		
		
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

