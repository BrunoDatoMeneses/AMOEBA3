package chartVisualization;

import java.util.HashMap;
import java.util.List;
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

public class PlotLineChart {
	/**
	 * The max item it can have
	 */
	private int max_item;

	/**
	 * The actual number of items
	 */
	private int cur_items = 0;

	/**
	 * Number of series to plot
	 */
	private int n_series;

	/**
	 * To know if there are series not updated this loop
	 */
	private int cur_serie = 0;

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
	 * The map that link data serie to the name of this serie
	 */
	private Map<String, XYChart.Series<Number, Number>> serieData = new HashMap<>();

	/**
	 * Create the chart with the given name
	 * 
	 * @param title:
	 *            the title of the chart
	 * @param max_item:
	 *            the max item it can have
	 * @param n_series:
	 *            the number of series to plot
	 */
	public PlotLineChart(String title, int max_item, int n_series, String yLabel, List<String> serieNames) {
		this.max_item = max_item;
		this.n_series = n_series;

		xAxis = new NumberAxis("Cycle", 0, max_item, max_item / 10);
		xAxis.setForceZeroInRange(false);
		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel(yLabel);
		yAxis.setAnimated(false);

		chart = new LineChart<Number, Number>(xAxis, yAxis);
		chart.setTitle(title);
		chart.setCreateSymbols(false);

		/**
		 * @note (Rollafon) Be aware that for more than 13 series, the color cycle will
		 *       be repeated. That means that the 14th color will be the same as the
		 *       1st, the 15th as the 2nd, and so on. If the need is to have more color,
		 *       check and modify the chart.css file.
		 */
		for (String s : serieNames) {
			Series<Number, Number> serie = new Series<>();
			serie.setName(s);
			chart.getData().add(serie);
			serieData.put(s, serie);
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
	 * @param serieName:
	 *            the name of the serie to be updated
	 * @param numX:
	 *            the x value to be added
	 * @param numY:
	 *            the corresponding y value
	 */
	public void addData(String serieName, int numX, double numY) {
		lock.lock();
		Platform.runLater(() -> {
			if (cur_items < max_item) {
				++cur_serie;
				if (cur_serie == n_series) {
					++cur_items;
					cur_serie = 0;
				}
			} else {
				serieData.get(serieName).getData().remove(0);
			}
			serieData.get(serieName);
			serieData.get(serieName).getData();
			serieData.get(serieName).getData().add(new Data<Number, Number>(numX, numY));
			if (numX % max_item == 0) {
				xAxis.setLowerBound(numX);
				xAxis.setUpperBound(numX + max_item);
			}
			// This case is possible when the rendering was previously off
			else if (numX > xAxis.getUpperBound()) {
				int min = numX - (numX % max_item);
				xAxis.setLowerBound(min);
				xAxis.setUpperBound(min + max_item);
			}
		});
		lock.unlock();
	}
}
