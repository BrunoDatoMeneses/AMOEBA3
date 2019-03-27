package experiments;

import java.io.File;
import java.io.IOException;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) throws IOException {
		experiment();
		// benchmark();
	}

	private static void experiment() throws IOException {
		System.out.println("To start press a key.");
		System.in.read();
		File file = new File("resources\\twoDimensionsLauncher.xml");
		System.out.println(file);
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 8;

		AMOEBA amoeba = new AMOEBA(world, studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		backupSystem.loadXML(file);

		// Example for using the learn method
		/*amoeba.setRenderUpdate(false);
		amoeba.allowGraphicalScheduler(false);
		long start = System.currentTimeMillis();
		int nbCycle = 2000;
		for (int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			// System.out.println(studiedSystem.getOutput());
			amoeba.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : " + (end - start) / 1000.0);
		amoeba.setRenderUpdate(true);
		amoeba.allowGraphicalScheduler(true);*/
		System.out.println("End main");
	}
}
