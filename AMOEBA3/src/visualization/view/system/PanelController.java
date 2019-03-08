package visualization.view.system;

import java.awt.FlowLayout;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import mas.kernel.Config;
import mas.kernel.World;

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

import mas.agents.head.Head;

// TODO: Auto-generated Javadoc
/**
 * The Class PanelController.
 */
public class PanelController extends JPanel implements ScheduledItem {

	/** The chart panel criticity. */
	/* Criticity chart */
	ChartPanel chartPanelCriticity;
	
	/** The chart criticity. */
	JFreeChart chartCriticity;
	
	/** The dataset criticity. */
	XYSeriesCollection datasetCriticity;

	/** The chart panel value. */
	/* Value chart */
	ChartPanel chartPanelValue;
	
	/** The chart value. */
	JFreeChart chartValue;
	
	/** The dataset value. */
	XYSeriesCollection datasetValue;

	/** The chart panel average crit. */
	/* Average criticity chart */
	ChartPanel chartPanelAverageCrit;
	
	/** The chart average crit. */
	JFreeChart chartAverageCrit;
	
	/** The dataset average crit. */
	XYSeriesCollection datasetAverageCrit;
	
	/** The chart panel binary. */
	/* Binary chart */
	ChartPanel chartPanelBinary;
	
	/** The chart binary. */
	JFreeChart chartBinary;
	
	/** The dataset binary. */
	XYSeriesCollection datasetBinary;
	
	/** The chart panel spatial criticality. */
	/* spatial criticality chart */
	ChartPanel chartPanelSpatialCriticality;
	
	/** The chart binary. */
	JFreeChart chartSpatialCriticality;
	
	/** The dataset binary. */
	XYSeriesCollection datasetSpatialCriticality;
	
	/** The controller. */
	Head controller;
	
	/** The world. */
	World world;
	
	/** The size X. */
	private int sizeX ;
	
	/** The size Y. */
	private int sizeY ;
	
	/** The tool bar. */
	private JToolBar toolBar;
	
	/** The button export prediction. */
	private JButton buttonExportPrediction;

	/**
	 * Instantiates a new panel controller.
	 *
	 * @param controller the controller
	 * @param world the world
	 */
	public PanelController(Head controller, World world) {

		this.sizeX = world.getXgraphSize();
		this.sizeY = world.getYgraphSize();
		
		this.setLayout(new FlowLayout());
		this.controller = controller;
		this.world = world;
		
		toolBar = new JToolBar();
		this.add(toolBar);
		toolBar.addSeparator();
		
		buttonExportPrediction = new JButton(Config.getIcon("terminal.png"));
		buttonExportPrediction.addActionListener(e -> {generateCsvFile("output.csv");});
		buttonExportPrediction.setToolTipText("Export the output in csv format");
		toolBar.add(buttonExportPrediction);
		
		/* Create criticity chart */
		datasetCriticity = createDataset();
		JFreeChart chart = createChart();
		chartPanelCriticity = new ChartPanel(chart);
		chartPanelCriticity.setPreferredSize(new java.awt.Dimension(sizeX, sizeY));
		this.add(chartPanelCriticity);

		/* Create value chart */
		datasetValue = createDatasetValue();
		JFreeChart chartValue = createChartValue();
		chartPanelValue = new ChartPanel(chartValue);
		chartPanelValue.setPreferredSize(new java.awt.Dimension(sizeX,sizeY));
		this.add(chartPanelValue);

		/* Create average criticity chart */
		datasetAverageCrit = createDatasetAverageCrit();
		JFreeChart chartAverageCrit = createChartAverageCrit();
		chartPanelAverageCrit = new ChartPanel(chartAverageCrit);
		chartPanelAverageCrit.setPreferredSize(new java.awt.Dimension(sizeX,sizeY));
		this.add(chartPanelAverageCrit);
		
		/* Create binary chart */
		datasetBinary = createDatasetBinary();
		JFreeChart chartBinary = createChartBinary();
		chartPanelBinary = new ChartPanel(chartBinary);
		chartPanelBinary.setPreferredSize(new java.awt.Dimension(sizeX, sizeY));
		this.add(chartPanelBinary);
		
		/* Create spatial criticality chart */
		datasetSpatialCriticality = createDatasetSpatialCriticality();
		JFreeChart chartSpatialCriticality = createChartSpatialCriticality();
		chartPanelSpatialCriticality = new ChartPanel(chartSpatialCriticality);
		chartPanelSpatialCriticality.setPreferredSize(new java.awt.Dimension(sizeX, sizeY));
		this.add(chartPanelSpatialCriticality);

	}

	
	   /**
   	 * Generate csv file.
   	 *
   	 * @param sFileName the s file name
   	 */
   	private void generateCsvFile(String sFileName)
	   {
		try
		{
		    FileWriter writer = new FileWriter(sFileName);
			 
		    writer.append("Tick");
		    writer.append(',');
		    writer.append("Oracle");
		    writer.append(",");
		    writer.append("Output");
		    writer.append('\n');

		    for (int i = 0 ; i < datasetValue.getSeries("prediction").getItemCount() ; i++) {
			    writer.append(""+i);
			    writer.append(',');
			    writer.append(""+datasetValue.getSeries("variable").getY(i));
			    writer.append(",");
			    writer.append(""+datasetValue.getSeries("prediction").getY(i));
			    writer.append('\n');
		    }


				
		    writer.flush();
		    writer.close();
		}
		catch(IOException e)
		{
		     e.printStackTrace();
		} 
	  }
	

	/*
	 * private JFreeChart createChart(DefaultCategoryDataset dataset2) { // TODO
	 * Auto-generated method stub return null; }
	 * 
	 * private DefaultCategoryDataset createDataset() { // TODO Auto-generated
	 * method stub return null; }
	 */

	/**
	 * Creates the dataset.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDataset() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("criticity"));
		collection.addSeries(new XYSeries("variation"));

		return collection;
	}

	/**
	 * Creates the dataset value.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDatasetValue() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("variable"));
		collection.addSeries(new XYSeries("prediction"));
		collection.addSeries(new XYSeries("creation"));

		return collection;
	}

	/**
	 * Creates the dataset average crit.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDatasetAverageCrit() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("averageCriticity"));
		collection.addSeries(new XYSeries("errorAllowed"));
		collection.addSeries(new XYSeries("inexactAllowed"));

		return collection;
	}
	
	/**
	 * Creates the dataset binary.
	 *
	 * @return the XY series collection
	 */
	private XYSeriesCollection createDatasetBinary() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("consecutiveContext"));

		return collection;
	}
	
	private XYSeriesCollection createDatasetSpatialCriticality() {

		XYSeriesCollection collection = new XYSeriesCollection();

		collection.addSeries(new XYSeries("spatialCriticality"));
		collection.addSeries(new XYSeries("zero"));
		collection.addSeries(new XYSeries("spatialGeneralization"));
		

		return collection;
	}

	/**
	 * Creates the chart.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChart() {

		// create subplot 1...
		final XYDataset data1 = datasetCriticity;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Criticity level");
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
		return new JFreeChart(controller.getName() + " criticity over time",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}

	/**
	 * Creates the chart value.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChartValue() {

		// create subplot 1...
		final XYDataset data1 = datasetValue;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Value");
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
		return new JFreeChart("tracked variable over time",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}

	/**
	 * Creates the chart average crit.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChartAverageCrit() {

		// create subplot 1...
		final XYDataset data1 = datasetAverageCrit;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Average criticity");
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
		return new JFreeChart("Real error + Tolerated error",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}

	/**
	 * Creates the chart binary.
	 *
	 * @return the j free chart
	 */
	private JFreeChart createChartBinary() {

		// create subplot 1...
		final XYDataset data1 = datasetBinary;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Consecutive Context");
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
		return new JFreeChart("Other data",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}
	
	private JFreeChart createChartSpatialCriticality() {

		// create subplot 1...
		final XYDataset data1 = datasetSpatialCriticality;
		final XYItemRenderer renderer1 = new StandardXYItemRenderer();
		final NumberAxis rangeAxis1 = new NumberAxis("Spatial criticality");
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
		return new JFreeChart("Spatial criticality",
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
	}
	
	/* (non-Javadoc)
	 * @see view.system.ScheduledItem#update()
	 */
	@Override
	public void update() {
		

		datasetCriticity.getSeries("criticity").add(
				world.getScheduler().getTick(), Math.abs(controller.getCriticity())); //TODO
		datasetCriticity.getSeries("variation").add(
				world.getScheduler().getTick(), controller.getAveragePredictionCriticity());  //TODO
		datasetValue.getSeries("variable").add(world.getScheduler().getTick(),
				controller.getOracleValue());
		datasetValue.getSeries("creation").add(world.getScheduler().getTick(),
				controller.isNewContextWasCreated() ? 1 : 0);
		datasetValue.getSeries("prediction").add(world.getScheduler().getTick(),
				controller.getAction());
		datasetAverageCrit.getSeries("averageCriticity").add(world.getScheduler().getTick(),
				controller.getAveragePredictionCriticity());
		datasetAverageCrit.getSeries("errorAllowed").add(world.getScheduler().getTick(),
				controller.getErrorAllowed());
//		datasetAverageCrit.getSeries("inexactAllowed").add(world.getScheduler().getTick(),
//				controller.getInexactAllowed());
		datasetBinary.getSeries("consecutiveContext").add(world.getScheduler().getTick(),(controller.getLastUsedContext() == controller.getBestContext())?0:1);
		
		datasetSpatialCriticality.getSeries("spatialCriticality").add(world.getScheduler().getTick(),controller.getAverageSpatialCriticality());
		datasetSpatialCriticality.getSeries("zero").add(world.getScheduler().getTick(),0.0);
		//datasetSpatialCriticality.getSeries("spatialGeneralization").add(world.getScheduler().getTick(),controller.getSpatialGeneralizationScore());
		
	}

	// }

}
