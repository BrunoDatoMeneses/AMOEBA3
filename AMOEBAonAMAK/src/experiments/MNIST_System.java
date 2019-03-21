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
import agents.percept.Percept;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;

/**
 * The Class BadContextManager.
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
	
	private static List<Double> test(String path, AMOEBA amoeba, int nbTests) {
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
	
	private static List<Double> benchmark(int nbThread, int nbCycle, int nbRequest){
		StudiedSystem studiedSystem = new MNIST_System("..\\..\\mnist\\mnist_train.csv");
		File file = new File("Ressources\\mnist.xml");
		World world = new World();
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = nbThread;
		
		ArrayList<Double> ret = new ArrayList<>();

		AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
		for(Percept p : amoeba.getPercepts())
			p.setEnum(true);
		
		amoeba.setLocalModel(TypeLocalModel.AVERAGE);
		amoeba.setDataForErrorMargin(1000, 3, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2, 0.2, 0.05, 40, 80);
		
		long start = 0;
		long end = 0;
		long t = 0;
		for(int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			HashMap<String, Double> output = studiedSystem.getOutput();
			start = System.currentTimeMillis();
			amoeba.learn(output);
			end = System.currentTimeMillis();
			t += end-start;
		}
		ret.add(t/1000.0);
		
		List<Double> testRet = test("..\\..\\mnist\\mnist_test.csv", amoeba, 500);
		ret.add(testRet.get(1));
				
		return ret;
	}
	
	public static void main(String[] args) throws IOException {
		boolean benchmark = true;
		if(benchmark) { 
			String outLearn = "Cycle 1Thd 2Thd 4Thd 8Thd";
			String outRequest = "Cycle 1Thd 2Thd 4Thd 8Thd";
			Log.minLevel = Log.Level.FATAL;
			Configuration.commandLineMode = true;
			int nbRequest = 500;
			benchmark(1,1,1); //set up memory.
			for(int nbCycle = 1; nbCycle < 502; nbCycle += 100) {
				System.out.println("Nb Cycle : "+nbCycle);
				outLearn += nbCycle + " ";
				outRequest += nbCycle + " ";
				for(int thd = 1; thd <= 8; thd *= 2) {
					List<Double> ret = benchmark(thd, nbCycle, nbRequest);
					outLearn += ret.get(0) + " ";
					outRequest += ret.get(1) + " ";
				}
				outLearn += "\n";
				outRequest += "\n";
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
			File file = new File("Ressources\\mnist.xml");
			World world = new World();
			Configuration.commandLineMode = false;
			Configuration.allowedSimultaneousAgentsExecution = 8;
	
			AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
			for(Percept p : amoeba.getPercepts())
				p.setEnum(true);
			
			amoeba.setLocalModel(TypeLocalModel.AVERAGE);
			amoeba.setDataForErrorMargin(1000, 3, 0.4, 0.1, 40, 80);
			amoeba.setDataForInexactMargin(500, 2, 0.2, 0.05, 40, 80);
			
			//exemple for using the learn method
			amoeba.setNoRenderUpdate(false);
			amoeba.allowGraphicalScheduler(false);
			long start = System.currentTimeMillis();
			int nbCycle = 100;
			for(int i = 0; i < nbCycle; ++i) {
				studiedSystem.playOneStep();
				//System.out.println(studiedSystem.getOutput());
				amoeba.learn(studiedSystem.getOutput());
			}
			long end = System.currentTimeMillis();
			System.out.println("Learning done in "+(end-start)/1000.0);
			amoeba.setNoRenderUpdate(false);
			amoeba.allowGraphicalScheduler(true);
			
			start = System.currentTimeMillis();
			List<Double> ret = test("..\\..\\mnist\\mnist_test.csv", amoeba, 500);
			end = System.currentTimeMillis();
			System.out.println("Accuracy of "+ret.get(0)+" . Done in "+(end-start)/1000.0);
			
			System.out.println("End main");
		}
		
	}
}