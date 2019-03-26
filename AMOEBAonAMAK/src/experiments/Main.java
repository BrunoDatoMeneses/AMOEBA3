package experiments;

import java.io.File;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) {
		File file = new File("resources/twoDimensionsLauncher.xml");
		System.out.println(file);
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		Configuration.commandLineMode = false;

		AMOEBA amoeba = new AMOEBA(world, studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		backupSystem.loadXML(file);

		// Example for using the learn method
		amoeba.setRenderUpdate(false);
		amoeba.allowGraphicalScheduler(false);
		int nbCycle = 1000;
		for (int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}
		amoeba.setRenderUpdate(true);
		amoeba.allowGraphicalScheduler(true);
		System.out.println("End main");
	}
}
