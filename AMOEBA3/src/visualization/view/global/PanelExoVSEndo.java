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


	ChartPanel chartPanelCriticity;
	JFreeChart chartCriticity;
	XYSeriesCollection dataSetCriticity;
	
	ChartPanel chartPanelErrors;
	JFreeChart chartErrors;
	XYSeriesCollection dataSetErrors;
	
	ChartPanel chartPanelAverageErrors;
	JFreeChart chartAverageErrors;
	XYSeriesCollection dataAverageErrors;

	ChartPanel chartPanelTotalErrors;
	JFreeChart chartTotalErrors;
	XYSeriesCollection dataTotalErrors;
	
	ChartPanel chartPanelTotalErrors_;
	JFreeChart chartTotalErrors_;
	XYSeriesCollection dataTotalErrors_;
	
	Double totalEndoError = 0.0;
	Double totalEndoError2 = 0.0;
	Double totalEndoError3 = 0.0;
	Double totalEndoError4 = 0.0;
	Double totalEndoError5 = 0.0;
	Double totalExoError = 0.0;
	
	Double totalEndoError_ = 0.0;
	Double totalEndoError_2 = 0.0;
	Double totalEndoError_3 = 0.0;
	Double totalEndoError_4 = 0.0;
	Double totalEndoError_5 = 0.0;
	Double totalExoError_ = 0.0;
	
	int exoWasRight;
	int endoWasRight;
	
	double endoTotalError;
	double exoTotalError;
	
	String endoType;
	
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
		this.endoType = typeOfEndo;

		/* Create Criticity chart */
		dataSetCriticity = createDataSet();
		JFreeChart chart0 = createChart(dataSetCriticity,"Criticities");
		chartPanelCriticity = new ChartPanel(chart0);
		chartPanelCriticity.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));
		
		/* Create Errors chart */
		dataSetErrors = createDataSet();
		JFreeChart chart = createChart(dataSetErrors,"Oracle Proportional errors");
		chartPanelErrors = new ChartPanel(chart);
		chartPanelErrors.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));
		
		/* Create Average Errors chart */
		dataAverageErrors = createDataSet();
		JFreeChart chart2 = createChart(dataAverageErrors,"Average Mean Criticities");
		chartPanelAverageErrors = new ChartPanel(chart2);
		chartPanelAverageErrors.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));
		
		/* Create Total Errors chart */
		dataTotalErrors = createDataSet();
		JFreeChart chart3 = createChart(dataTotalErrors,"Sum of Proportional Errors");
		chartPanelTotalErrors = new ChartPanel(chart3);
		chartPanelTotalErrors.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));
		
		/* Create Total Errors chart */
		dataTotalErrors_ = createDataSet();
		JFreeChart chart4 = createChart(dataTotalErrors_,"Sum of Criticities");
		chartPanelTotalErrors_ = new ChartPanel(chart4);
		chartPanelTotalErrors_.setPreferredSize(new java.awt.Dimension(world.getXgraphSize(), world.getYgraphSize()));

		
		this.add(chartPanelCriticity);
		this.add(chartPanelErrors);
		this.add(chartPanelAverageErrors);
		this.add(chartPanelTotalErrors);
		this.add(chartPanelTotalErrors_);

		endoWasRight = 0;
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

	
	
	private XYSeriesCollection createDataSet() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("Error Exo"));
		
		if(endoType.equals("Exo Vs Endo Overlap NCS")) {
			collection.addSeries(new XYSeries("All dim + infl + conf"));
			collection.addSeries(new XYSeries("Worst dim + infl + conf"));
			collection.addSeries(new XYSeries("All dim + infl + conf + vol"));
			collection.addSeries(new XYSeries("Worst dim + infl"));
			collection.addSeries(new XYSeries("Worst dim + infl + vol"));
		}
		else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
			collection.addSeries(new XYSeries("Model similarity"));
			}
		else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
			collection.addSeries(new XYSeries("undefined"));
		}
		
		
		
		//collection.addSeries(new XYSeries("Oracle"));

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
		Double endoError = 0.0;
		Double endoError2 = 0.0;
		Double endoError3 = 0.0;
		Double endoError4 = 0.0;
		Double endoError5 = 0.0;
		Double exoError = 0.0;
		
		
		if(world.getScheduler().getHeadAgent().severalActivatedContexts()) {
			
			
			
			
			
			
			// Error calculation | p - o | / |o|
			
			exoError = Math.abs(world.getScheduler().getHeadAgent().getPrediction() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1 );
			if(!exoError.isNaN()) {
				dataSetErrors.getSeries("Error Exo").add(tick, normalizePositiveValues(100, 5, exoError));
				dataAverageErrors.getSeries("Error Exo").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityCopy());
			}
			
			
			if(endoType.equals("Exo Vs Endo Overlap NCS")) {
				endoError = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlaps() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
				endoError2 = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
				endoError3 = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
				endoError4 = Math.abs(world.getScheduler().getHeadAgent().getendogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
				endoError5 = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
			
			}
			else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
				endoError = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsSharedIncompetence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
			}
			else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
				//endoError = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionNContextsByInfluence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
			}
			
			// Total error from previous calculation
			
			totalExoError += exoError;
			totalEndoError += endoError;
			totalEndoError2 += endoError2;
			totalEndoError3 += endoError3;
			totalEndoError4 += endoError4;
			totalEndoError5 += endoError5;
			
			
			
			// Total error from classical criticity
			
			totalExoError_ += world.getScheduler().getHeadAgent().getCriticity();
			
			if(endoType.equals("Exo Vs Endo Overlap NCS")) {
				totalEndoError_ += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsCriticity();
				totalEndoError_2 += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceCriticity();
				totalEndoError_3 += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidenceCriticity();
				totalEndoError_4 += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidenceCriticity();
				totalEndoError_5 += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumeCriticity();
			}
			else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
				totalEndoError_ += world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsSharedIncompetenceCriticity();
			}
			else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
				//endoError = Math.abs(world.getScheduler().getHeadAgent().getEndogenousPredictionNContextsByInfluence() - world.getScheduler().getHeadAgent().getOracleValue()) / (Math.abs(world.getScheduler().getHeadAgent().getOracleValue()) + 1);
			}
			
			
			// Displays
			
			
			if(!endoError.isNaN()) {
				if(endoType.equals("Exo Vs Endo Overlap NCS")) {
					dataSetErrors.getSeries("All dim + infl + conf").add(tick,  normalizePositiveValues(100, 5, endoError));
					dataSetErrors.getSeries("Worst dim + infl + conf").add(tick,  normalizePositiveValues(100, 5, endoError2));
					dataSetErrors.getSeries("All dim + infl + conf + vol").add(tick,  normalizePositiveValues(100, 5, endoError3));
					dataSetErrors.getSeries("Worst dim + infl").add(tick,  normalizePositiveValues(100, 5, endoError4));
					dataSetErrors.getSeries("Worst dim + infl + vol").add(tick,  normalizePositiveValues(100, 5, endoError5));
					
					dataAverageErrors.getSeries("All dim + infl + conf").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsOverlaps());
					dataAverageErrors.getSeries("Worst dim + infl + conf").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluence());
					dataAverageErrors.getSeries("All dim + infl + conf + vol").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsOverlapsInfluenceWithoutConfidence());
					dataAverageErrors.getSeries("Worst dim + infl").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence());
					dataAverageErrors.getSeries("Worst dim + infl + vol").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithVolume());
				
					dataTotalErrors.getSeries("Error Exo").add(tick,  totalExoError);
					dataTotalErrors.getSeries("All dim + infl + conf").add(tick,  totalEndoError);
					dataTotalErrors.getSeries("Worst dim + infl + conf").add(tick,  totalEndoError2);
					dataTotalErrors.getSeries("All dim + infl + conf + vol").add(tick,  totalEndoError3);
					dataTotalErrors.getSeries("Worst dim + infl").add(tick,  totalEndoError4);
					dataTotalErrors.getSeries("Worst dim + infl + vol").add(tick,  totalEndoError5);
				
				}
				else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
					dataSetErrors.getSeries("Model similarity").add(tick,  normalizePositiveValues(100, 5, endoError));
					
					dataAverageErrors.getSeries("Model similarity").add(tick,  world.getScheduler().getHeadAgent().getAveragePredictionCriticityEndoActivatedContextsSharedIncompetence());
					
					dataTotalErrors.getSeries("Error Exo").add(tick,  totalExoError);
					dataTotalErrors.getSeries("Model similarity").add(tick,  totalEndoError);
					}
				else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
					dataSetErrors.getSeries("undefined").add(tick,  -1.0);
				}
				
			}
			else {
				dataSetErrors.getSeries(endoType).add(tick, -10.0);
			}
			
			
			if(endoType.equals("Exo Vs Endo Overlap NCS")) {
				dataSetCriticity.getSeries("Error Exo").add(tick,   world.getScheduler().getHeadAgent().getCriticity());
				dataSetCriticity.getSeries("All dim + infl + conf").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsCriticity());
				dataSetCriticity.getSeries("Worst dim + infl + conf").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceCriticity());
				dataSetCriticity.getSeries("All dim + infl + conf + vol").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidenceCriticity());
				dataSetCriticity.getSeries("Worst dim + infl").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidenceCriticity());
				dataSetCriticity.getSeries("Worst dim + infl + vol").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumeCriticity());
			
			}
			else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
				
				dataSetCriticity.getSeries("Error Exo").add(tick,  world.getScheduler().getHeadAgent().getCriticity());
				dataSetCriticity.getSeries("Model similarity").add(tick,  world.getScheduler().getHeadAgent().getEndogenousPredictionActivatedContextsSharedIncompetenceCriticity());
				}
			else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
				
			}
			
			if(endoType.equals("Exo Vs Endo Overlap NCS")) {
				dataTotalErrors_.getSeries("Error Exo").add(tick,  totalExoError_);
				dataTotalErrors_.getSeries("All dim + infl + conf").add(tick,  totalEndoError_);
				dataTotalErrors_.getSeries("Worst dim + infl + conf").add(tick,  totalEndoError_2);
				dataTotalErrors_.getSeries("All dim + infl + conf + vol").add(tick,  totalEndoError_3);
				dataTotalErrors_.getSeries("Worst dim + infl").add(tick,  totalEndoError_4);
				dataTotalErrors_.getSeries("Worst dim + infl + vol").add(tick,  totalEndoError_5);
			
			}
			else if(endoType.equals("Exo Vs Endo Shared Incompetence NCS")) {
				
				dataTotalErrors_.getSeries("Error Exo").add(tick,  totalExoError_);
				dataTotalErrors_.getSeries("Model similarity").add(tick,  totalEndoError_);
				}
			else if(endoType.equals("Exo Vs Endo Incompetence NCS")){
				//dataSetErrors.getSeries("undefined").add(tick,  -1.0);
			}

			if( endoError != exoError) {
				if(endoError > exoError) {
					exoWasRight ++;
				}
				else if(endoError < exoError )  {
					endoWasRight ++;
				}
			}
			
			
			endoTotalError += endoError;
			exoTotalError += exoError;
			
			System.out.println(endoType + " -> EXO :" + exoWasRight + " ( " + exoError + " , " + (exoTotalError) +" )"  + " ENDO :" + endoWasRight + " ( " + endoError + " , "  + (endoTotalError) +" )");
		}
		
		
	}
	




	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		//System.out.println("getPoint " + mouseEvent.getPoint() + " " + this.getSize());
		//graph.getNode("origin").
		
		Point2D p = chartPanelErrors.translateScreenToJava2D(event.getTrigger().getPoint());
		Rectangle2D plotArea = chartPanelErrors.getScreenDataArea();
		XYPlot plot = (XYPlot) chartErrors.getPlot(); // your plot
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
