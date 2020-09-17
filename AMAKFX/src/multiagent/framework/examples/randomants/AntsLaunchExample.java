package multiagent.framework.examples.randomants;

import multiagent.framework.ui.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class AntsLaunchExample {

	public static void main(String[] args) {
		WorldExample env = new WorldExample();
		
		new AntHillExample(env);
		
		Pane panel = new Pane();
		panel.getChildren().add(new Label("AntHill simulation\n"
				+ "Ants move randomly.\n"
				+ "This demo is here to show AMAK rendering capacities.\n"));
		MainWindow.setLeftPanel(panel);
	}
}
