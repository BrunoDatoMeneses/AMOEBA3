package experiments;

import java.util.HashMap;

import experiments.benchmark.NDimCube;
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
public class TestingMain {

	public static void main(String[] args) throws InterruptedException {
		Configuration.commandLineMode = false;
		Log.defaultMinLevel = Log.Level.INFORM;
		// create a system to be studied
		StudiedSystem studiedSystem = new NDimCube(50.0, 3);
		// create the amoeba
		// Make sure the path to the config file is correct.
		AMOEBA amoeba = new AMOEBA(null,null,"resources/threeDimensionsLauncher.xml", studiedSystem);
		amoeba.saver = new SaveHelperDummy();
		// a window should have appeared, allowing you to control and visualize the amoeba.
		
		Thread.sleep(5000);
		
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			studiedSystem.playOneStep();
			amoeba.learn(studiedSystem.getOutput());
			if(i%100 == 99) {
				end = System.currentTimeMillis();
				System.out.println("Time for 100 learn: "+(end-start)/1000.0);
				start = System.currentTimeMillis();
			}
		}
		
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			studiedSystem.playOneStep();
			amoeba.request(studiedSystem.getOutput());
			if(i%100 == 99) {
				end = System.currentTimeMillis();
				System.out.println("Time for 100 request: "+(end-start)/1000.0);
				start = System.currentTimeMillis();
			}
		}
		
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {
			studiedSystem.playOneStep();
			HashMap<String, Double> req = studiedSystem.getOutput();
			req.remove("px1");
			req.remove("px2");
			req.remove("px3");
			req.remove("oracle");
			amoeba.maximize(req);
			if(i%100 == 99) {
				end = System.currentTimeMillis();
				System.out.println("Time for 100 maximize: "+(end-start)/1000.0);
				start = System.currentTimeMillis();
			}
		}
		
		System.exit(0);
		
	}

}
