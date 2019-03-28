package fr.irit.smac.amak.examples.asyncrandomants;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Class aiming at starting the mas-less ants system
 * 
 * @author perles
 *
 */
public class AsyncAntsLaunchExample {

	/**
	 * Launch method
	 * 
	 * @param args
	 *            Main arguments
	 */
	public static void main(String[] args) {
		Configuration.allowedSimultaneousAgentsExecution = 5;
		AsyncWorldExample env = new AsyncWorldExample();
		AsyncAntsAMASExample amas = new AsyncAntsAMASExample(env, Scheduling.UI);
		for (int i = 0; i < 50; i++)
			new AsyncAntExample(amas, 0, 0);
		
		Pane panel = new Pane();
		String content = "Async AntHill simulation\n\n" + "Ants move randomly.\n"
				+ "This demo is here to show AMAK asynchronous agent capacities.";
		Label label = new Label(content);
		label.setStyle("-fx-font-weight: bold;");
		panel.getChildren().add(label);
		MainWindow.setLeftPanel(panel);
	}
}
