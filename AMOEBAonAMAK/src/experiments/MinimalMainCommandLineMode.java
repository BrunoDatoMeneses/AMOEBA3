package experiments;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.StudiedSystem;

/**
 * The most minimal main possible producing a functioning amoeba.
 * @author Hugo
 *
 */
public class MinimalMainCommandLineMode {

	public static void main(String[] args) throws InterruptedException {
		
		Configuration.commandLineMode = true;
		
		// create a system to be studied
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		// create the amoeba
		// Make sure the path to the config file is correct.
		AMOEBA amoeba = new AMOEBA(null,null,"resources/twoDimensionsLauncher.xml", studiedSystem);
		// a window should have appeared, allowing you to control and visualize the amoeba.
		
		// Learning and Request example
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1001; ++i) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : " + (end - start)  + " ms");
		
		for (int i = 0; i < 10; ++i) {
			studiedSystem.playOneStep();
			System.out.println(amoeba.request(studiedSystem.getOutput()));
		}
	}

}
