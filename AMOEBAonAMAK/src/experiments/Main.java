package experiments;

import java.io.File;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) {
		File file = new File("Ressources\\twoDimensionsLauncher.xml");
		System.out.println(file);
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		Configuration.commandLineMode = false;

		AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
		BackupSystem backupSystem = new BackupSystem(amoeba);
		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);

		//exemple for using the learn method
		amoeba.setNoRenderUpdate(true);
		amoeba.allowGraphicalScheduler(false);
		int nbCycle = 1000;
		for(int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}
		backupSystem.save("./save_TEST.xml");
		amoeba.setNoRenderUpdate(false);
		amoeba.allowGraphicalScheduler(true);
		System.out.println("End main");
	}
}
