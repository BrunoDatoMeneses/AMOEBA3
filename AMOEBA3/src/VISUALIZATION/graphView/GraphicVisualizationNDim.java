package VISUALIZATION.graphView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

// TODO: Auto-generated Javadoc
/**
 * The Class GraphicVisualizationNDim.
 */
public class GraphicVisualizationNDim extends JFrame {
	
	/** The cols. */
	private List<String> cols = new ArrayList<>();
	
	/** The jfreechart. */
	private JFreeChart jfreechart;
	
	/** The n dim data set. */
	private DefaultCategoryDataset nDimDataSet;
	
	/** The plot. */
	private SpiderWebPlot plot;
	
	/** The context ID. */
	private String contextID;
	
	/**
	 * Instantiates a new graphic visualization N dim.
	 */
	public GraphicVisualizationNDim() {
		super("Visualization of graph for N Dimesions");
	}
	
	/**
	 * Inits the.
	 */
	public void init() {
		nDimDataSet = new DefaultCategoryDataset();
		plot = new SpiderWebPlot(nDimDataSet);
	    plot.setAxisLinePaint(Color.black);
	    jfreechart = new JFreeChart("", TextTitle.DEFAULT_FONT, plot, true);
		JPanel jpanel = new ChartPanel(jfreechart);
		add(jpanel, BorderLayout.CENTER);	
		setSize(640, 480);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		// To show label
		plot.setLabelGenerator(new StandardCategoryItemLabelGenerator() {

		    @Override
		    public String generateColumnLabel(CategoryDataset dataset, int col) {
		        return dataset.getColumnKey(col) + " " + dataset.getValue(0, col) + ", " + dataset.getValue(1, col);
		    }
		});
	}
	
	/**
	 * Update dataset.
	 *
	 * @param setTitle the set title
	 * @param value the value
	 * @param col the col
	 */
	public void updateDataset(String setTitle, double value, int col) {
		nDimDataSet.addValue(value, setTitle, cols.get(col));
    }
	 
 	/**
	  * Initialize column.
	  *
	  * @param elements the elements
	  */
	 public void initializeColumn(List<String> elements) {
 		for (int i=0; i<elements.size(); i++) {
 			cols.add(elements.get(i));
 		}
 	}
 	
 	/**
	  * Sets the context ID.
	  *
	  * @param contextID the new context ID
	  */
	 public void setContextID(String contextID) {
 		this.contextID = contextID;
		jfreechart.setTitle("Context : " + contextID);
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
