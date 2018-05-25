package visualization.graphView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import visualization.graphView.annotations.XYDomainValueAnnotation;

// TODO: Auto-generated Javadoc
/**
 * The Class GraphicVisualization2Dim.
 */
public class GraphicVisualization2Dim extends JFrame {
	
	/** The series. */
	ArrayList<XYSeries> series = new ArrayList<>();
	
	/** The chart. */
	JFreeChart chart;
	
	/** The chart title. */
	String chartTitle = "History of context : ";
	
	/** The context ID. */
	private String contextID;
	
	/** The last tick. */
	private int lastTick = 0;
	
	/** The last message. */
	private String lastMessage = null;
	
	/**
	 * Instantiates a new graphic visualization 2 dim.
	 */
	public GraphicVisualization2Dim() {
		super("Visualization of graph");
	}
	
	/**
	 * Inits the.
	 */
	public void init() {

		String xAxisLabel = "Tick";
		String yAxisLabel = "Value";
		
		XYDataset dataset = createDataset();
		
		chart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dataset);
		JPanel chartPanel = new ChartPanel(chart);
		add(chartPanel, BorderLayout.CENTER);
		
		setSize(640, 480);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setSeriesColor();
		
  	}
	
	/**
	 * Creates the temp marker.
	 *
	 * @param tick the tick
	 * @param message the message
	 */
	public void createTempMarker(int tick, String message) {
		String text;
		if (lastTick != tick) {
			text = tick + ": " + message;
		} else {
			text = lastMessage + "; " + message;
		}
		final XYPlot plot = chart.getXYPlot();
		XYDomainValueAnnotation tempMarker = new XYDomainValueAnnotation();
		tempMarker.setValue(tick);
		tempMarker.setToolTipText(text);
		tempMarker.setPaint(Color.red);
		
		tempMarker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
		tempMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
		tempMarker.setRotationAnchor(TextAnchor.TOP_LEFT);
		
		plot.addAnnotation(tempMarker);
		lastTick = tick;
		lastMessage = text;
	}
	
	/**
	 * Sets the XY series.
	 *
	 * @param elements the new XY series
	 */
	public void setXYSeries(List<String> elements) {
		if (series.size() > 0) {
			series.clear();
		}
		for (int i=0; i<elements.size(); i++) {
			XYSeries xySeries_min = new XYSeries(elements.get(i) + "_min");
			XYSeries xySeries_max = new XYSeries(elements.get(i) + "_max");
			series.add(xySeries_min);
			series.add(xySeries_max);
		}
	}
	
	/**
	 * Sets the series color.
	 */
	private void setSeriesColor() {
		final XYPlot plot = chart.getXYPlot();
		for (int i=0; i<series.size(); i+=2) {
			Color color = randomColor();
			plot.getRendererForDataset(plot.getDataset()).setSeriesPaint(i, color);
			plot.getRendererForDataset(plot.getDataset()).setSeriesPaint(i+1, color);
		}
	}
	
	/**
	 * Creates the dataset.
	 *
	 * @return the XY dataset
	 */
	private XYDataset createDataset() {
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i=0; i<series.size(); i++) {
			dataset.addSeries(series.get(i));
		}
	    return dataset;
	}
	
	/**
	 * Update data.
	 *
	 * @param indexSeries the index series
	 * @param tick the tick
	 * @param value the value
	 */
	public void updateData(int indexSeries, double tick, double value) {
		series.get(indexSeries).addOrUpdate(tick, value);
		repaint();
	}
	
	/**
	 * Sets the context ID.
	 *
	 * @param contextID the new context ID
	 */
	public void setContextID(String contextID) {
		this.contextID = contextID;
		chart.setTitle(chartTitle + contextID);
	}
	
	/**
	 * Gets the context ID.
	 *
	 * @return the context ID
	 */
	public String getContextID() {
		return contextID;
	}

	/**
	 * Random color.
	 *
	 * @return the color
	 */
	private Color randomColor() {
		int r = new Random().nextInt(255);
		int g = new Random().nextInt(255);
		int b = new Random().nextInt(255);
		return new Color(r, g, b);
	}
	
}
