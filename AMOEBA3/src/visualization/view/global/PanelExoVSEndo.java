package visualization.view.global;

import java.awt.AWTException;
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

	
	
	int endoWasRight;
	int exoWasRight;
	
	double endoTotalError;
	double exoTotalError;
	

	
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
		chartPanelAgents.setPreferredSize(new java.awt.Dimension(1800, 1000));
		this.add(chartPanelAgents);

		endoWasRight = 0;
		exoWasRight = 0;
		
		endoTotalError = 0;
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

		collection.addSeries(new XYSeries("Error Exo"));
		collection.addSeries(new XYSeries("Error Endo"));
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
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("ERRORS EXO AND ENDO");
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
		return new JFreeChart("ERRORS EXO AND ENDO",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}



	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {

		int tick = world.getScheduler().getTick();
		Double endoError;
		Double exoError;
		
		//double exoError = 100*Math.abs(world.getScheduler().getHeadAgent().getPrediction() - world.getScheduler().getHeadAgent().getOracleValue())/Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) ;
		exoError = Math.abs(world.getScheduler().getHeadAgent().getPrediction() - world.getScheduler().getHeadAgent().getOracleValue()) / Math.abs(world.getScheduler().getHeadAgent().getOracleValue());
		//double endoError =  100*Math.abs(world.getScheduler().getHeadAgent().getEndogenousPrediction() - world.getScheduler().getHeadAgent().getOracleValue())/Math.abs(world.getScheduler().getHeadAgent().getOracleValue());
		endoError = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPrediction() - world.getScheduler().getHeadAgent().getOracleValue()) / Math.abs(world.getScheduler().getHeadAgent().getOracleValue());
//		if(exoError==0.0) {
//			endoError = 300;
//		}
//		else if(exoError < endoError) {
//			endoError = normalize(100, 200, endoError/exoError);
//		}
//		else {
//			endoError = 100*endoError/exoError;
//		}
		
		if(!exoError.isNaN() && !endoError.isNaN()) {
			dataSetAgents.getSeries("Error Exo").add(
					tick, normalizePositiveValues(100, 20, exoError));
			dataSetAgents.getSeries("Error Endo").add(
					tick, normalizePositiveValues(100, 20, endoError));
		}
		
//		if(!exoError.isNaN() && !endoError.isNaN()) {
//			dataSetAgents.getSeries("Error Exo").add(
//					tick,  exoError);
//			dataSetAgents.getSeries("Error Endo").add(
//					tick, endoError);
//		}
		
		
		//dataSetAgents.getSeries("Oracle").add(tick, world.getScheduler().getHeadAgent().getOracleValue());


		if( endoError != exoError) {
			if(endoError>exoError) {
				exoWasRight ++;
			}
			else {
				endoWasRight ++;
			}
		}
		
		endoTotalError += endoError;
		exoTotalError += exoError;
		
		//System.out.println("EXO :" + exoWasRight + " ( " + exoError + " , " + (exoTotalError/world.getScheduler().getTick()) +" )"  + " ENDO :" + endoWasRight + " ( " + endoError + " , "  + (endoTotalError/world.getScheduler().getTick()) +" )");
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
