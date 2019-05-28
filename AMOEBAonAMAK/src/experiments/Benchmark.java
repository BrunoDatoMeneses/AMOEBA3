package experiments;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class Benchmark {

	/**
	 * 
	 * @param args nbThread nbLearnCycle XMLConfigFileLearn nbRequestCycle XMLConfigFileRequest 
	 */
	public static void main(String[] args) {
		if(args.length != 5) {
			System.err.println("Usage : nbThread nbLearnCycle XMLConfigFileLearn nbRequestCycle XMLConfigFileRequest");
			System.exit(0);
		}
		int nbThread = Integer.valueOf(args[0]);
		int nbLearnCycle = Integer.valueOf(args[1]);
		int nbRequestCycle = Integer.valueOf(args[3]);
		
		
		Configuration.commandLineMode = true;
		Log.defaultMinLevel = Log.Level.FATAL;
		Configuration.allowedSimultaneousAgentsExecution = nbThread;
		
		if(nbLearnCycle > 0) {
			execLearn(nbLearnCycle, args[2]);
		}
		
		if(nbRequestCycle > 0) {
			execRequest(nbRequestCycle, args[4]);
		}
		
		System.exit(0);
	}
	
	private static void execLearn(int nbCycle, String configFile) {
		System.out.println("Start "+nbCycle+" learning cycles.");
		
		AMOEBA amoeba = new AMOEBA(new World(), null);
		BackupSystem bs = new BackupSystem(amoeba);
		bs.load(new File(configFile));
		StudiedSystem ss = new NDimCube(50, amoeba.getPercepts().size());
		
		ArrayList<Double> criticity = new ArrayList<Double>(1000);
		
		double start = System.currentTimeMillis();
		for(int i = 0; i < nbCycle; i++) {
			ss.playOneStep();
			amoeba.learn(ss.getOutput());
			criticity.add(amoeba.getHeads().get(0).getCriticity());
		}
		double end = System.currentTimeMillis();
		
		bs.save(new File("benchmark_learning_out.xml"));
		
		System.out.println("Criticities :\n"+criticity);
		System.out.println("Learning done in "+((end-start)/1000));
	}
	
	private static void execRequest(int nbCycle, String configFile) {
		System.out.println("Start "+nbCycle+" request cycles.");
		
		AMOEBA amoeba = new AMOEBA(new World(), null);
		BackupSystem bs = new BackupSystem(amoeba);
		bs.load(new File(configFile));
		StudiedSystem ss = new NDimCube(50, amoeba.getPercepts().size());
		
		ArrayList<Double> criticity = new ArrayList<Double>(1000);
		
		double start = System.currentTimeMillis();
		for(int i = 0; i < nbCycle; i++) {
			ss.playOneStep();
			amoeba.request(ss.getOutput());
			criticity.add(amoeba.getHeads().get(0).getCriticity());
		}
		double end = System.currentTimeMillis();
		
		System.out.println("Criticities :\n"+criticity);
		System.out.println("Requests done in "+((end-start)/1000));
	}

}
