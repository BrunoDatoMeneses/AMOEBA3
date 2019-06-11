package fr.irit.smac.amak.examples.asyncrandomants;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.examples.randomants.AntExample;
import fr.irit.smac.amak.examples.randomants.AntHillExample;
import fr.irit.smac.amak.examples.randomants.WorldExample;
import fr.irit.smac.amak.ui.MainWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

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
		Configuration.allowedSimultaneousAgentsExecution = 4;
		WorldExample env = new WorldExample();
		AntHillExample amas = new AntHillExample(env);
		for (int i = 0; i < 50; i++)
			new AntExample(amas, 0, 0);
		
		Pane panel = new Pane();
		String content = "Async AntHill simulation\n\n" + "Ants move randomly.\n"
				+ "This demo is here to show AMAK asynchronous agent capacities.";
		Label label = new Label(content);
		label.setStyle("-fx-font-weight: bold;");
		panel.getChildren().add(label);
		MainWindow.setLeftPanel(panel);
	}
}