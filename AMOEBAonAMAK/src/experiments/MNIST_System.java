package experiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import agents.context.localModel.TypeLocalModel;
import agents.percept.Percept;
import fr.irit.smac.amak.Configuration;
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
				mnist = new BufferedReader(new FileReader(path));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				System.exit(1);
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
	
	private static void test(String path, AMOEBA amoeba, int nbTests) {
		try {
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
				
				double res = amoeba.request(out);
				//System.out.println(nb+" Ora : "+oracle+"  res : "+res);
				if(Math.round(res/100) == oracle)
					correct += 1;
			}
			double p = (correct*1.0)/nb;
			System.out.println(correct+" success on "+nb+" try. "+p);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		StudiedSystem studiedSystem = new MNIST_System("D:\\hugor\\Documents\\mnist\\mnist_train.csv");
		File file = new File("Ressources\\mnist.xml");
		World world = new World();
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;

		AMOEBA amoeba = new AMOEBA(world, file, studiedSystem);
		for(Percept p : amoeba.getPercepts())
			p.setEnum(true);
		
		amoeba.setLocalModel(TypeLocalModel.AVERAGE);
		amoeba.setDataForErrorMargin(1000, 3, 0.4, 0.1, 40, 80);
		amoeba.setDataForInexactMargin(500, 2, 0.2, 0.05, 40, 80);
		
		//exemple for using the learn method
		//amoeba.setNoRenderUpdate(true);
		amoeba.allowGraphicalScheduler(false);
		long start = System.currentTimeMillis();
		int nbCycle = 500;
		for(int i = 0; i < nbCycle; ++i) {
			studiedSystem.playOneStep();
			//System.out.println(studiedSystem.getOutput());
			amoeba.learn(studiedSystem.getOutput());
		}
		long end = System.currentTimeMillis();
		System.out.println("Done in : "+(end-start)/1000.0);
		//amoeba.setNoRenderUpdate(false);
		amoeba.allowGraphicalScheduler(true);
		
		start = System.currentTimeMillis();
		test("D:\\hugor\\Documents\\mnist\\mnist_test.csv", amoeba, 1000);
		end = System.currentTimeMillis();
		System.out.println("Done in : "+(end-start)/1000.0);
		
		System.out.println("End main");
		
	}
}