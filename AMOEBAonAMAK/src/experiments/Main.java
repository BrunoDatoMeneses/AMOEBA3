package experiments;

import java.io.File;
import java.io.IOException;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.MainWindow;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) throws IOException {
		MainWindow.instance();
		experiment();
	}

	private static void experiment() throws IOException {
	
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 8;

		// Create a World, a Studied System, and an AMOEBA
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		AMOEBA amoeba = new AMOEBA(world, studiedSystem);
		// Create a backup system for the AMOEBA
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		// Load a configuration matching the studied system
		File file = new File("resources\\twoDimensionsLauncher.xml");
		backupSystem.loadXML(file);
		/*
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
		// After activating rendering we need to run a cycle to update agents
		// We use a request call to avoid change in context
		amoeba.request(studiedSystem.getOutput());
		// We allow simulation control with the UI
		amoeba.allowGraphicalScheduler(true);
		System.out.println("End main");
		*/
	}
}
