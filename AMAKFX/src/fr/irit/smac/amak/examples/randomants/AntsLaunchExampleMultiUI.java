package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AntsLaunchExampleMultiUI extends Application{

	
	
	
	
	
	
	public static void main (String[] args) {
		
		
		Application.launch(args);
		
		
		
		//MainWindow.instance();		
		
		
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Configuration.multiUI=true;
		
		AmasMultiUIWindow window = new AmasMultiUIWindow();
		AmasMultiUIWindow window2 = new AmasMultiUIWindow();
		
		WorldExampleMultiUI env = new WorldExampleMultiUI(window);
		WorldExampleMultiUI env2 = new WorldExampleMultiUI(window2);
		

		new AntHillExampleMultiUI(window, env);
		new AntHillExampleMultiUI(window2, env2);
		
		Pane panel = new Pane();
		panel.getChildren().add(new Label("AntHill simulation\n"
				+ "Ants move randomly.\n"
				+ "This demo is here to show AMAK rendering capacities.\n"));
		window.setLeftPanel(panel);
		
		Pane panel2 = new Pane();
		panel2.getChildren().add(new Label("AntHill simulation\n"
				+ "Ants move randomly.\n"
				+ "This demo is here to show AMAK rendering capacities.\n"));
		window2.setLeftPanel(panel2);
	}
}
