package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.ui.AmasWindow;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class AntsLaunchExample {

	public static void main(String[] args) {
		
		AmasWindow amasWindowAntHillExample = new AmasWindow();
		
		
		WorldExample env = new WorldExample(amasWindowAntHillExample);
		
		
		
		new AntHillExample(amasWindowAntHillExample, env);
		
		Pane panel = new Pane();
		panel.getChildren().add(new Label("AntHill simulation\n"
				+ "Ants move randomly.\n"
				+ "This demo is here to show AMAK rendering capacities.\n"));
		amasWindowAntHillExample.setLeftPanel(panel);
	}
}
