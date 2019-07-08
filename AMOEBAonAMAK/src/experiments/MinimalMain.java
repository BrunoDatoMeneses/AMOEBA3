package experiments;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.backup.SaveHelperDummy;

/**
 * The most minimal main possible producing a functioning amoeba.
 * @author Hugo
 *
 */
public class MinimalMain {

	public static void main(String[] args) throws InterruptedException {
		Configuration.commandLineMode = true;
		Log.defaultMinLevel = Log.Level.INFORM;
		// create a system to be studied
		StudiedSystem studiedSystem = new NDimCube(50.0, 100);
		// create the amoeba
		// Make sure the path to the config file is correct.
		AMOEBA amoeba = new AMOEBA("resources/100DimensionsLauncher.xml", studiedSystem);
		amoeba.saver = new SaveHelperDummy();
		// a window should have appeared, allowing you to control and visualize the amoeba.
		
		Thread.sleep(5000);
		
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		for(int i = 0; i < 10000; i++) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
			if(i%100 == 0) {
				end = System.currentTimeMillis();
				System.out.println("Time for 100 learn: "+(end-start)/1000.0);
				start = System.currentTimeMillis();
			}
		}
	}

}
