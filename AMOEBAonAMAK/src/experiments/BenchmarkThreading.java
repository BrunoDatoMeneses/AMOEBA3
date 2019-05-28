package experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.DoubleBinaryOperator;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IAMOEBA;
import kernel.IBackupSystem;
import kernel.StudiedSystem;
import kernel.World;

public class BenchmarkThreading {

	/**
	 * Benchmark an AMOEBA configured by the user.
	 * What is measured is execution time of learn and request.
	 * A measure is composed of : the number of cycle, the time it took to reach it,
	 * the time it took to make the nbRequest after that number of cycle, 
	 * and the mean error value of these request.
	 * 
	 * @param amoeba a configured AMOEBA, ready to be used
	 * @param learnSystem the studied system used to learn (can be null if nbLeatn = 0)
	 * @param requestSystem the studied system used for request (can be the same as learn, can be null if nbRequest = 0)
	 * @param nbLearn the maximum number of learn cycle to be done, if 0 no learn will be done, but a measure will be taken nonetheless
	 * @param nbRequest the number of request that will be done at each measure, if 0 no request will be done
	 * @param measureEveryNLearn how many learn between measure
	 * @param error the function used to measure error, if null, will use squared differences
	 * @return a list containing each measure, a measure is a list with this format : [numLearn, timeLearn, timeRequest, meanErrorRequest] 
	 */
	public static List<List<Double>> benchmark(IAMOEBA amoeba, StudiedSystem learnSystem, StudiedSystem requestSystem,
												int nbLearn, int nbRequest, int measureEveryNLearn, DoubleBinaryOperator error) {
		if(error == null) {
			error = (a,b) -> (a-b)*(a-b);
		}
		
		long start = 0;
		long end = 0;
		long t = 0;
		ArrayList<List<Double>> ret = new ArrayList<>();
		for(int i = 0; i < nbLearn; ++i) {
			
			// learn one cycle
			learnSystem.playOneStep();
			learnSystem.playOneStep();
			HashMap<String, Double> out = learnSystem.getOutput();
			start = System.currentTimeMillis();
			amoeba.learn(out);
			end = System.currentTimeMillis();
			t += end-start;
			
			// take a measure, but not if it's the end
			if((i+1) < nbLearn && (i+1)%measureEveryNLearn == 0) {
				ArrayList<Double> measure = new ArrayList<>();
				measure.add((double)i+1);
				measure.add(t/1000.0);
				
				long tRequest = 0;
				double meanError = 0;
				for(int j = 0; j < nbRequest; ++j) {
					requestSystem.playOneStep();
					requestSystem.playOneStep();
					HashMap<String, Double> reqOut = requestSystem.getOutput();
					
					start = System.currentTimeMillis();
					double res = amoeba.request(reqOut);
					end = System.currentTimeMillis();
					tRequest += end-start;
					meanError += error.applyAsDouble(reqOut.get("oracle"), res);
				}
				measure.add(tRequest/1000.0);
				measure.add(meanError/nbRequest);
				ret.add(measure);
			}
		}
		// take at least one measure, even if there's no learn 
		ArrayList<Double> measure = new ArrayList<>();
		measure.add((double)nbLearn);
		measure.add(t/1000.0);
		
		long tRequest = 0;
		double meanError = 0;
		for(int j = 0; j < nbRequest; ++j) {
			requestSystem.playOneStep();
			HashMap<String, Double> reqOut = requestSystem.getOutput();
			
			start = System.currentTimeMillis();
			double res = amoeba.request(reqOut);
			end = System.currentTimeMillis();
			tRequest += end-start;
			meanError += error.applyAsDouble(reqOut.get("oracle"), res);
		}
		measure.add(tRequest/1000.0);
		measure.add(meanError/nbRequest);
		ret.add(measure);
		
		
		return ret;
	}
	
	/**
	 * Example for using benchmark
	 * @param args
	 */
	public static void main(String[] args) {
		Log.defaultMinLevel = Log.Level.FATAL;
		Configuration.commandLineMode = true;
		
		File file = new File("resources\\100DimensionsLauncherTrained1000.xml");

		
		// setup cache --- (very important to reduce impact of the 1st measure)
		Configuration.allowedSimultaneousAgentsExecution = 1;
		StudiedSystem learnSystem = new NDimCube(50.0, 100);
		World world = new World();
		AMOEBA amoeba = new AMOEBA(world, learnSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		backupSystem.load(file);
		benchmark(amoeba, learnSystem, learnSystem, 100, 100, 100, null);
		// ---------------
		System.out.println("Starting benchmark.");
		List<List<List<Double>>> results = new ArrayList<>();
		for(int thd = 1; thd <= 8; thd *= 2) {
			Configuration.allowedSimultaneousAgentsExecution = thd;
			learnSystem = new NDimCube(50.0, 100);
			world = new World();
			amoeba = new AMOEBA(world, null);
			backupSystem = new BackupSystem(amoeba);
			backupSystem.load(file);
			List<List<Double>> bench = benchmark(amoeba, learnSystem, learnSystem, 0, 10000, 1000, null);
			System.out.println("Thd "+thd+" "+bench);
			results.add(bench);
		}
		String outLearn = "Cycle 1Thd 2Thd 4Thd 8Thd\n";
		String outRequest = "Cycle 1Thd 2Thd 4Thd 8Thd\n";
		for(int i = 0; i < results.get(0).size(); ++i) {
			outLearn += results.get(0).get(i).get(0) +", "+ results.get(0).get(i).get(1) +", "+ results.get(1).get(i).get(1)
					 +", "+ results.get(2).get(i).get(1) +", "+ results.get(3).get(i).get(1) +"\n";
			outRequest += results.get(0).get(i).get(0) +", "+ results.get(0).get(i).get(2) +", "+ results.get(1).get(i).get(2)
					 +", "+ results.get(2).get(i).get(2) +", "+ results.get(3).get(i).get(2) +"\n";
		}
		System.out.println("Learn : ");
		System.out.println(outLearn);
		System.out.println("Request : ");
		System.out.println(outRequest);
		System.out.println("Done.");
		System.exit(0);
	}

}
