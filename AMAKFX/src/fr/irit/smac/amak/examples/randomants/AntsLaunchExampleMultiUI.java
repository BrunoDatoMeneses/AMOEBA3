package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.ui.AmasWindow;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class AntsLaunchExampleMultiUI {


	
	
	
	public static void main(String[] args) {
		
		
		MainWindow mainWindow = new MainWindow();
		mainWindow.instance();
		
		AmasWindow mainWindowAntHillExample = new AmasWindow();
		
		

		//MainWindow mainWindowAntHillExample2 = new MainWindow();
		
		
		WorldExample env = new WorldExample(mainWindowAntHillExample);
		
		
		
		new AntHillExample(mainWindowAntHillExample, env);
		
		Pane panel = new Pane();
		panel.getChildren().add(new Label("AntHill simulation\n"
				+ "Ants move randomly.\n"
				+ "This demo is here to show AMAK rendering capacities.\n"));
		mainWindowAntHillExample.setLeftPanel(panel);
		
		
		
		
	}
	
	
	
}













