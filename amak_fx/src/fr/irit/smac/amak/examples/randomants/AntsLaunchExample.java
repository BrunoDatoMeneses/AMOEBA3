package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.ui.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class AntsLaunchExample {

	public static void main(String[] args) {
		WorldExample env = new WorldExample();
		
		new AntHillExample(env);
		
		Pane panel = new Pane();
		panel.getChildren().add(new Label("<html><b>AntHill simulation</b><br/><br/>"
				+ "Ants move randomly.<br />"
				+ "This demo is here to show AMAK<br />rendering capacities.</html>"));
		MainWindow.setLeftPanel(panel);
	}
}
