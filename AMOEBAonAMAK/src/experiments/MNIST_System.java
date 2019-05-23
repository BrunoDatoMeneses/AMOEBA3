package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import agents.context.localModel.TypeLocalModel;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import kernel.AMOEBA;
import kernel.BackupSystem;
import kernel.IBackupSystem;
import kernel.StudiedSystem;
import kernel.World;

/**
 * A system using the MNIST database in the CSV format.
 * You'll need to get it here : https://pjreddie.com/projects/mnist-in-csv/ 
 */
public class MNIST_System implements StudiedSystem {
	BufferedReader mnist; 
	String path;
	String line;

	public MNIST_System(String path) {
		this.path = path;
		try {
			mnist = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void playOneStep() {
		try {
			line = mnist.readLine();
			if(line == null) {
				try {
					mnist = new BufferedReader(new FileReader(path));
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
					System.exit(1);
				}
				playOneStep();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Looping on the file ...");
			try {
				mnist.close();
				mnist = new BufferedReader(new FileReader(path));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				System.exit(1);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		String[] s = line.split(",");
		for(int i = 1; i < s.length; ++i) {
			out.put(""+i, Double.parseDouble(s[i]));
		}
		out.put("oracle", Double.parseDouble(s[0])*100);
		return out;
	}
	
	/**
	 * Evaluate an AMOEBA against a test dataset
	 * @param path to the test dataset
	 * @param the trained amoeba
	 * @param the number of test
	 * @return a list with the success rate and the execution time
	 */
	public static List<Double> test(String path, AMOEBA amoeba, int nbTests) {
		try {
			long start = 0;
			long end = 0;
			long t = 0;
			BufferedReader test = new BufferedReader(new FileReader(path));
			String line;
			int nb = 0;
			int correct = 0;
			while((line = test.readLine()) != null && nb < nbTests) {
				nb += 1;
				HashMap<String, Double> out = new HashMap<String, Double>();
				String[] s = line.split(",");
				for(int i = 1; i < s.length; ++i) {
					out.put(""+i, Double.parseDouble(s[i]));
				}
				double oracle = Double.parseDouble(s[0]); 
				out.put("oracle", oracle*100);
				
				start = System.currentTimeMillis();
				double res = amoeba.request(out);
				end = System.currentTimeMillis();
				t += end-start;
				if(Math.round(res/100) == oracle)
					correct += 1;
			}
			double p = (correct*1.0)/nb;
			//System.out.println(correct+" success on "+nb+" try. "+p);
			test.close();
			
			ArrayList<Double> ret = new ArrayList<>();
			ret.add(p);
			ret.add(t/1000.0);
			return ret;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		boolean benchmark = true;
		if(benchmark) { 
			System.out.println("AMOEBA benchmark with MNIST");
			Log.minLevel = Log.Level.FATAL;
			Configuration.commandLineMode = true;
			
			File file = new File("resources\\mnist.xml");
			
			// setup cache --- (very important to reduce impact of the 1st measure)
			System.out.println("setup...");
			Configuration.allowedSimultaneousAgentsExecution = 1;
			StudiedSystem learnSystem = new MNIST_System("../../mnist/mnist_train.csv");
			StudiedSystem requestSystem = new MNIST_System("../../mnist/mnist_test.csv");
			World world = new World();
			AMOEBA amoeba = new AMOEBA(world, null);
			amoeba.setLocalModel(TypeLocalModel.AVERAGE);
			IBackupSystem backupSystem = new BackupSystem(amoeba);
			backupSystem.load(file);
			Benchmark.benchmark(amoeba, learnSystem, requestSystem, 100, 100, 50, null);
			System.out.println("Done. Starting benchmark.");
			// ---------------
			List<List<List<Double>>> results = new ArrayList<>();
			for(int thd = 1; thd <= 8; thd *= 2) {
				Configuration.allowedSimultaneousAgentsExecution = thd;
				learnSystem = new MNIST_System("../../mnist/mnist_train.csv");
				requestSystem = new MNIST_System("../../mnist/mnist_test.csv");
				world = new World();
				amoeba = new AMOEBA(world, null);
				amoeba.setLocalModel(TypeLocalModel.AVERAGE);
				backupSystem = new BackupSystem(amoeba);
				backupSystem.load(file);
				List<List<Double>> bench = Benchmark.benchmark(amoeba, learnSystem, requestSystem, 1000, 500, 100, null);
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
		else {
			System.out.println("To start press a key.");
			System.in.read();
			//Non benchmark usage :
			StudiedSystem studiedSystem = new MNIST_System("..\\..\\mnist\\mnist_train.csv");
			File file = new File("resources\\mnist.xml");
			World world = new World();
			Configuration.commandLineMode = false;
			Configuration.allowedSimultaneousAgentsExecution = 8;
	
			AMOEBA amoeba = new AMOEBA(world, studiedSystem);
			IBackupSystem backupSystem = new BackupSystem(amoeba);
			backupSystem.load(file);
			
			amoeba.setLocalModel(TypeLocalModel.AVERAGE);
			
			//Example for using the learn method
			amoeba.setRenderUpdate(true);
			amoeba.allowGraphicalScheduler(false);
			
			long start = System.currentTimeMillis();
			int nbCycle = 100;
			for(int i = 0; i < nbCycle; ++i) {
				studiedSystem.playOneStep();
				amoeba.learn(studiedSystem.getOutput());
			}
			long end = System.currentTimeMillis();
			System.out.println("Learning done in "+(end-start)/1000.0);
			
			start = System.currentTimeMillis();
			List<Double> ret = test("..\\..\\mnist\\mnist_test.csv", amoeba, 500);
			end = System.currentTimeMillis();
			System.out.println("Accuracy of "+ret.get(0)+" . Done in "+(end-start)/1000.0);
			
			amoeba.setRenderUpdate(true);
			amoeba.allowGraphicalScheduler(true);
			
			System.out.println("End main");
		}
		
	}

	@Override
	public double requestOracle(HashMap<String, Double> request) {
		return 0;
	}
}