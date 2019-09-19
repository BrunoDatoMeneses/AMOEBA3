package fr.irit.smac.amak.examples.randomantsMultiUi;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AntsLaunchExampleMultiUI extends Application{

	
	public static void main (String[] args) {
		
		
		Application.launch(args);
		
	
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		Configuration.multiUI=true;
		
		
		AmasMultiUIWindow window = new AmasMultiUIWindow("Random Ants Multi UI 1");
		//AmasMultiUIWindow window2 = new AmasMultiUIWindow("Random Ants Multi UI 2");
		
		
		WorldExampleMultiUI env = new WorldExampleMultiUI(window);
		//WorldExampleMultiUI env2 = new WorldExampleMultiUI(window2);
		

		AntHillExampleMultiUI ants = new AntHillExampleMultiUI(window, VUIMulti.get("Ants VUI 1"), env);
		//new AntHillExampleMultiUI(window2, VUIMulti.get("Ants VUI 2"), env2);
		
		for(int i=0;i<1000;i++)
			ants.cycle();
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
