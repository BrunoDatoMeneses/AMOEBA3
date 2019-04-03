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

public class LoopNCS {
	/**
	 * The max item it can have
	 */
	private int max_item;

	/**
	 * The actual number of items
	 */
	private int cur_items = 0;

	/**
	 * The chart itself
	 */
	private LineChart<Number, Number> chart;

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
	public LoopNCS(String title, int max_item) {
		this.max_item = max_item;

		NumberAxis xAxis = new NumberAxis();
		xAxis.setLabel("Cycle");
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Number of NCS");

		chart = new LineChart<Number, Number>(xAxis, yAxis);
		chart.setTitle(title);

		for (NCS ncs : NCS.values()) {
			Series<Number, Number> serie = new Series<>();
			serie.setName(ncs.toString());
			chart.getData().add(serie);
			ncsData.put(ncs, serie);
		}

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
				ncsData.get(ncsName).getData().add(new Data<Number, Number>(numCycle, numNCS));
			} else {
				Data<Number, Number> data = ncsData.get(ncsName).getData().get(cur_items % max_item);
				data.setXValue(numCycle);
				data.setYValue(numNCS);
			}
			++cur_items;
		});
		lock.unlock();
	}
}
