package chartVisualization;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import fr.irit.smac.amak.ui.MainWindow;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Pane;
import ncs.NCS;

public class NCSChart {
	/**
	 * The max item it can have
	 */
	private int max_item;

	/**
	 * The actual number of items
	 */
	private int cur_items = 0;

	/**
	 * Number of NCS
	 */
	private final int n_ncs = NCS.values().length;

	/**
	 * Number of NCS added this loop
	 */
	private int cur_ncs = 0;

	/**
	 * The chart itself
	 */
	private LineChart<Number, Number> chart;

	/**
	 * The x axis
	 */
	private NumberAxis xAxis;

	/**
	 * The lock to avoid parallel issues
	 */
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * The map that link data serie to a NCS type
	 */
	private Map<NCS, XYChart.Series<Number, Number>> ncsData = new HashMap<>();

	/**
	 * Create the chart with the given name
	 * 
	 * @param title:
	 *            the title of the chart
	 * @param max_item:
	 *            the max item it can have
	 */
	public NCSChart(String title, int max_item) {
		this.max_item = max_item;

		xAxis = new NumberAxis("Cycle", 0, max_item, max_item/10);
		xAxis.setForceZeroInRange(false);
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Number of NCS");
		yAxis.setAnimated(false);

		chart = new LineChart<Number, Number>(xAxis, yAxis);
		chart.setTitle(title);

		/**
		 * @note (Rollafon)
		 * Be aware that for more than 13 NCS kinds, the color cycle will be repeated.
		 * That means that the 14th color will be the same as the 1st, the 15th as the 2nd, and so on. 
		 * If the need is to have more color, check and modify the chart.css file.
		 */
		for (NCS ncs : NCS.values()) {
			Series<Number, Number> serie = new Series<>();
			serie.setName(ncs.toString());
			chart.getData().add(serie);
			ncsData.put(ncs, serie);
		}
		
		chart.getStylesheets().add("chartVisualization/chart.css");
		
		Pane p = new Pane();
		p.getChildren().add(chart);
		chart.prefWidthProperty().bind(p.widthProperty());
		chart.prefHeightProperty().bind(p.heightProperty());
		MainWindow.addTabbedPanel(title, p);
	}

	/**
	 * Add a data to the chart
	 * 
	 * @param ncsName:
	 *            The name of the ncs
	 * @param numCycle:
	 *            The cycle numero
	 * @param numNCS:
	 *            the number of NCS on this cycle
	 */
	public void addData(NCS ncsName, int numCycle, int numNCS) {
		lock.lock();
		Platform.runLater(() -> {
			if (cur_items < max_item) {
				++cur_ncs;
				if (cur_ncs == n_ncs) {
					++cur_items;
					cur_ncs = 0;
				}
			} else {
				ncsData.get(ncsName).getData().remove(0);
			}
			ncsData.get(ncsName).getData().add(new Data<Number, Number>(numCycle, numNCS));
			if (numCycle % max_item == 0) {
				xAxis.setLowerBound(numCycle);
				xAxis.setUpperBound(numCycle + max_item);
			}

		});
		lock.unlock();
	}
}
