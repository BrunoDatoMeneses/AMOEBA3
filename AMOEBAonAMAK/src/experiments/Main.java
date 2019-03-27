package experiments;

import java.io.File;
import java.io.IOException;

import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;

public class Main {

	public static void main(String[] args) throws IOException {
		experiment();
		//benchmark();
	}
	
	private static void experiment() throws IOException {
		System.out.println("To start press a key.");
		System.in.read();
		File file = new File("Ressources\\twoDimensionsLauncher.xml");
		System.out.println(file);
		World world = new World();
		StudiedSystem studiedSystem = new F_XY_System(50.0);
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 8;

		AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
		amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);

		//exemple for using the learn method
		/*amoeba.setNoRenderUpdate(true);
		amoeba.allowGraphicalScheduler(false);
		long start = System.currentTimeMillis();
		int nbCycle = 2000;
		for(int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			//System.out.println(studiedSystem.getOutput());
			amoeba.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : "+(end-start)/1000.0);
		amoeba.setNoRenderUpdate(false);
		amoeba.allowGraphicalScheduler(true);*/
		System.out.println("End main");
	}
	
	private static void benchmark() {
		File file = new File("Ressources\\twoDimensionsLauncher.xml");
		Configuration.commandLineMode = true;
		
		int nbCycle = 10000;
		
		Configuration.allowedSimultaneousAgentsExecution = 8;
		double min = Double.POSITIVE_INFINITY;
		double mean = 0;
		for(int t = 0; t < 100; ++t) {
			System.out.println("\nt="+t);
			World world = new World();
			StudiedSystem studiedSystem = new F_XY_System(50.0);
			AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
			amoeba.setDataForErrorMargin(1000, 5, 0.4, 0.1, 40, 80);
			amoeba.setDataForInexactMargin(500, 2.5, 0.2, 0.05, 40, 80);

			//exemple for using the learn method
			//amoeba.setNoRenderUpdate(true);
			//amoeba.allowGraphicalScheduler(false);
			long start = System.currentTimeMillis();
			for(int i = 0; i < nbCycle; ++i) {
				studiedSystem.playOneStep();
				amoeba.learn(studiedSystem.getOutput());
			}
			long end = System.currentTimeMillis();
			if((end-start) < min)
				min = (end-start);
			mean += (end-start);
			//amoeba.setNoRenderUpdate(false);
			//amoeba.allowGraphicalScheduler(true);
		}
		System.out.println("\n"+Configuration.allowedSimultaneousAgentsExecution+" Thread min : "+min+", mean : "+(mean/100));


	}
}
