package fr.irit.smac.amak.examples.philosophers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import fr.irit.smac.amak.ui.MainWindow;
import javafx.application.Platform;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;

public class PhilosophersStatistics {
	private Map<String, Double> philosophers = new HashMap<>();
	private XYChart.Series<String, Number> dataSeries;
	private BarChart<String, Number> barChart;
	private ReentrantLock lock = new ReentrantLock();
	private int n_updated = 0;
	private int max_updated = 0;

	public PhilosophersStatistics(String title) {
		CategoryAxis xAxis = new CategoryAxis();

		NumberAxis yAxis = new NumberAxis();
		yAxis.setLabel("Number of eaten pastas");

		barChart = new BarChart<>(xAxis, yAxis);
		barChart.setAnimated(false);

		dataSeries = new XYChart.Series<>();
		dataSeries.setName("Philosophers");

		barChart.setTitle(title);
		barChart.getData().add(dataSeries);

		Pane p = new Pane();
		p.getChildren().add(barChart);
		barChart.prefHeightProperty().bind(p.heightProperty());
		barChart.prefWidthProperty().bind(p.widthProperty());
		MainWindow.addTabbedPanel("Eaten pastas", p);
	}

	public void addPhilosopher(int id, double eatenPastas) {
		lock.lock();
		if (id > max_updated)
			max_updated = id;
		String str_id = Integer.toString(id);
		dataSeries.getData().add(new XYChart.Data<>(str_id, new Double(eatenPastas)));
		philosophers.put(str_id, eatenPastas);
		lock.unlock();
	}

	public void updatePhilosopher(int id, double eatenPastas) {
		lock.lock();
		++n_updated;
		String str_id = Integer.toString(id);
		philosophers.replace(str_id, eatenPastas);

		if (n_updated == max_updated) {
			Platform.runLater(() -> {
				barChart.getData().remove(0);
				dataSeries = new XYChart.Series<>();
				for (String s : philosophers.keySet())
					dataSeries.getData().add(new XYChart.Data<>(s, philosophers.get(s)));
				dataSeries.setName("Philosophers");
				barChart.getData().add(dataSeries);
			});
			n_updated = 0;
		}
		lock.unlock();
	}
}
