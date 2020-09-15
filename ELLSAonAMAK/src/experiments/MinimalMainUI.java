package experiments;

import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;

/**
 * The most minimal main possible producing a functioning amoeba.
 * @author Hugo
 *
 */
public class MinimalMainUI {//TODO

	public static void main(String[] args) throws InterruptedException {
		
		Configuration.commandLineMode = false;
		
		// create a system to be studied
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		// create the amoeba
		// Make sure the path to the config file is correct.
		ELLSA ellsa = new ELLSA(null,null,"resources/twoDimensionsLauncher.xml", studiedSystem);
		// a window should have appeared, allowing you to control and visualize the amoeba.
		
		// Learning and Request example
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1001; ++i) {
			studiedSystem.playOneStep();
			ellsa.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : " + (end - start)  + " ms");
		
		for (int i = 0; i < 10; ++i) {
			studiedSystem.playOneStep();
			System.out.println(ellsa.request(studiedSystem.getOutput()));
		}
	}

}
