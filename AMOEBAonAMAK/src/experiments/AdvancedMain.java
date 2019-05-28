package experiments;

import java.io.File;
import java.io.IOException;

import agents.context.Context;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import gui.AmoebaWindow;
import gui.NoneRenderer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.SaveHelper;
import kernel.StudiedSystem;
import kernel.World;

/**
 * A more advanced and complete main.
 * @author Hugo
 *
 */
public class AdvancedMain {

	public static void main(String[] args) throws IOException {
		// Instantiating the MainWindow before usage.
		// It also allows you to change some of its behavior before creating an AMOEBA.
		// If you use Configuration.commandLineMode = True , then you should skip it. 
		AmoebaWindow.instance();
		example();
	}

	private static void example() throws IOException {

		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 8;
		Configuration.waitForGUI = true;

		// Create a World, a Studied System, and an AMOEBA
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoeba = new AMOEBA(world, studiedSystem);
		// A window appeared, allowing to control the simulation, but if you try to run it
		// it will crash (there's no percepts !). We need to load a configuration :
		
		// Change how new Context are rendered.
		//Context.defaultRenderStrategy = NoneRenderer.class;
		
		// Create a backup system for the AMOEBA
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		// Load a configuration matching the studied system
		File file = new File("resources\\twoDimensionsLauncher.xml");
		backupSystem.load(file);
		// Note : if you intend to use a SaveHelper, you can use SaveHelper.load instead of a BackupSystem
		
		// We add an optional saver, allowing us to autosave the amoeba at each cycle.
		amoeba.saver = new SaveHelper(amoeba);
		// Autosave slow execution, if you want fast training, set saver to null.

		// The amoeba is ready to be used.
		// Next we show how to control it with code :

		// We deny the possibility to change simulation speed with the UI
		amoeba.allowGraphicalScheduler(false);
		// We allow rendering
		amoeba.setRenderUpdate(true);
		long start = System.currentTimeMillis();
		// We run some learning cycles
		int nbCycle = 1000;
		for (int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : " + (end - start) / 1000.0);
		
		// We create a manual save point
		amoeba.saver.newManualSave("TestManualSave");
		
		// We set the log level to INFORM, to avoid debug logs that slow down simulation
		Log.defaultMinLevel = Log.Level.INFORM;
		
		// We deactivate rendering
		amoeba.setRenderUpdate(false);
		// Do some more learning
		start = System.currentTimeMillis();
		for (int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}
		end = System.currentTimeMillis();
		System.out.println("Done in : " + (end - start) / 1000.0);
		
		
		// Activate rendering back
		amoeba.setRenderUpdate(true);
		// After activating rendering we need to update agent's visualization
		amoeba.updateAgentsVisualisation();
		// We allow simulation control with the UI
		amoeba.allowGraphicalScheduler(true);
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0, 10, 0);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
			}
		});
		AmoebaWindow.addToolbar(slider);

		System.out.println("End main");
	}
}