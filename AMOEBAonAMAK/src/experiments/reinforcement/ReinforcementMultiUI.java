package experiments.reinforcement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import agents.context.localModel.TypeLocalModel;
import experiments.FILE;
import experiments.reinforcement.SimpleReinforcement1DSpatialRewardAndActionMltiUI.Environment;
import experiments.reinforcement.SimpleReinforcement1DSpatialRewardAndActionMltiUI.LearningAgent;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.VUIMulti;
import fr.irit.smac.amak.ui.drawables.Drawable;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperDummy;
import kernel.backup.SaveHelperImpl;
import utils.Pair;
import utils.RandomUtils;
import utils.TRACE_LEVEL;
import utils.XmlConfigGenerator;


/**
 * The Class BadContextLauncherEasy.
 */
public class ReinforcementMultiUI extends Application implements Serializable {


	public static final double oracleNoiseRange = 0.5;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 3	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = true;
	//public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
	public static double mappingErrorAllowed = 0.03; // MULTI
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	
	public static final int nbCycle = 1000;
	
	/* Learn and Test */
	public static final int MAX_STEP_PER_EPISODE = 200;
	public static final int N_LEARN = 1000;//400
	public static final int N_TEST = 100;
	
	/* Exploration */
	public static final double MIN_EXPLO_RATE = 0.02;
	public static final double EXPLO_RATE_DIMINUTION_FACTOR = 0.01;
	public static final double EXPLO_RATE_BASE = 1;
	
	AMOEBA amoebaSpatialReward;
	VUIMulti amoebaSpatialRewardVUI;
	AmoebaMultiUIWindow amoebaSpatialRewardUI;
	
	AMOEBA amoebaControlModel;
	VUIMulti amoebaControlModelVUI;
	AmoebaMultiUIWindow amoebaControlModelUI;
	
	LearningAgent agent;
	Environment env;
	
	int nbStep;
	boolean done;
	boolean invalid;
	HashMap<String, Double> action;
	HashMap<String, Double> state ;
	HashMap<String, Double> state2;
	double totReward;
	
	public static void main(String[] args) throws IOException {
		
		
		Application.launch(args);


	}
	
	@Override
	public void start(Stage arg0) throws Exception, IOException {

		Configuration.multiUI=true;
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		amoebaSpatialRewardVUI = new VUIMulti("2D");
		amoebaSpatialRewardUI = new AmoebaMultiUIWindow("SPATIAL REWARD", amoebaSpatialRewardVUI);
		
		amoebaControlModelVUI = new VUIMulti("2D");
		amoebaControlModelUI = new AmoebaMultiUIWindow("CONTROL MODEL", amoebaControlModelVUI);
		
		startTask(100, 0);		
	}
	
	public void startTask(long wait, int cycles) 
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask(wait, cycles);
            }
        };
 
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
        
     
    }
	
	
	public void runTask(long wait, int cycles) 
    {
		
		try
        {
             
            // Update the Label on the JavaFx Application Thread        
            Platform.runLater(new Runnable() 
            {
                @Override
                public void run() 
                {
                	agent = new AmoebaRewardAndControl();
                	env = new OneDimensionEnv(10);
                }
            });
     
            Thread.sleep(wait);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
		
		
		state = env.reset(); // BUG LAAAAAAAAAAAAAAAA
		double explo = EXPLO_RATE_BASE;
		for(int i = 0; i < N_LEARN; i++) {
			nbStep = 0;
			state = env.reset();
			action = new HashMap<String, Double>();
			totReward = 0.0;
			
			// execute simulation cycles
			done = false;
			invalid = false;
			
			
			while(!done && !invalid) {
				
				 try
		            {
		                 
		                // Update the Label on the JavaFx Application Thread        
		                Platform.runLater(new Runnable() 
		                {
		                    @Override
		                    public void run() 
		                    {
		                    	nbStep++;
		        				if(nbStep > MAX_STEP_PER_EPISODE) {
		        					invalid = true;
		        				}
		        				state.remove("oracle");
		        				
		        				action = new HashMap<String, Double>();
		        				
		        				action = agent.explore(state, env);
//		        				if(rand.nextDouble() < explo) {
//		        					action = agent.explore(state, env);
//		        				} else {
//		        					action = agent.choose(state, env);
//		        				}
		        				
		        				
		        				state2 = env.step(action);  // new position with associated reward
		        				
		        				if(state2.get("oracle") != -1.0) { //if goal or end of world
		        					done = true;
		        				}
		        				action.put("p1", state.get("p1")); //add previous state to action
		        				
		        				action.put("oracle", state2.get("oracle")); //add current reward to action
		        				
		        				// state : previous position and associated reward
		        				// state2 : new position with current reward
		        				// action : previous state, current action and current reward
		        				
		        				agent.learn(state, state2, action, done);
		        				totReward += action.get("oracle");
		        				
		        				state = state2;
		                    }
		                });
		         
		                Thread.sleep(wait);
		            }
		            catch (InterruptedException e) 
		            {
		                e.printStackTrace();
		            }
				
				
				
			}
			
			System.out.println("-----------------------------------------------------------------------");
			
			// update exploration rate
			if(explo > MIN_EXPLO_RATE) {
				explo -= EXPLO_RATE_DIMINUTION_FACTOR;
				if(explo < MIN_EXPLO_RATE)
					explo = MIN_EXPLO_RATE;
			}
			
			System.out.println("Episode "+i+"  reward : "+totReward+"  explo : "+explo);
			//double testAR = test(agent, env, r, N_TEST);
			//averageRewards.add(testAR);
			
			//Scanner scan = new Scanner(System.in);
			//scan.nextLine();
		}
		
        
    }   
	
	
	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}

	
	
	public static String fileName(ArrayList<String> infos) {
		String fileName = "";
		
		for(String info : infos) {
			fileName += info + "_";
		}
		
		return fileName;
	}
	
	public static void writeMessage(FILE file, ArrayList<String> message) {
		
		file.initManualMessage();
		
		for(String m : message) {
			file.addManualMessage(m);
		}
		
		file.sendManualMessage();
		
	}
	
	/**
	 * An environment in which a LearningAgent reside
	 * @author Hugo
	 *
	 */
	public interface Environment {
		public List<String> actionSpace();
		public List<String> perceptionSpace();
		public HashMap<String, Double> reset();
		public HashMap<String, Double> step(HashMap<String, Double> action);
		public HashMap<String, Double> randomAction();
	}
	
	/**
	 * Wrapper for any kind of learning agent
	 * @author Hugo
	 *
	 */
	public interface LearningAgent {
		public HashMap<String, Double> choose(HashMap<String, Double> state, Environment env);
		public HashMap<String, Double> explore(HashMap<String, Double> state, Environment env);
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2, HashMap<String, Double> action, boolean done);
	}
	
	public class AmoebaRewardAndControl implements LearningAgent {
		public AMOEBA amoebaSpatialReward;
		//public AMOEBA amoebaControlModel;
		public double lr = 0.8;
		public double gamma = 0.9;
		private Random rand = new Random();
		
		public AmoebaRewardAndControl() {
			amoebaSpatialReward = setupSpatialReward();
			//amoebaControlModel = setupControlModel();
		}
		
		@Override
		public HashMap<String, Double> choose(HashMap<String, Double> state, Environment env) {
			
//			HashMap<String, Double> stateWithVizuAdded = new HashMap<String, Double>(state);
//			stateWithVizuAdded.put("p2", 0.0);
//			stateWithVizuAdded.put("oracle", 0.0);
//			HashMap<String, Double> bestFuturePosition =  amoebaSpatialReward.reinforcementRequest(stateWithVizuAdded);
//			
//			HashMap<String, Double> action = new HashMap<String, Double>();
//			if(bestFuturePosition!=null) {
//				HashMap<String, Double> requestForControlModel = new HashMap<String, Double>();
//				requestForControlModel.put("pCurrent", state.get("p1"));
//				requestForControlModel.put("pGoal", bestFuturePosition.get("p1"));
//				
//				double bestAction = amoebaControlModel.request(requestForControlModel);
//				
//				
//				action.put("a1", bestAction);
//			}
//			action = env.randomAction();
//			
//			return action;
			return null;
		}

		@Override
		public void learn(HashMap<String, Double> state, HashMap<String, Double> positionAndReward,
				HashMap<String, Double> action, boolean done) {
			
			// state : previous position and associated reward
			// state2 : new position with current reward
			// action : previous state, current actions and current reward
			
			HashMap<String, Double> previousStateCurrentStateAction = new HashMap<>();
			previousStateCurrentStateAction.put("pCurrent", action.get("p1"));
			previousStateCurrentStateAction.put("pGoal", positionAndReward.get("p1"));
			previousStateCurrentStateAction.put("oracle", action.get("a1"));
			

			
			//System.out.println("ControlModel " + previousStateCurrentStateAction + "                  ---------------- SIMPLE REIN XP 149");
			//System.out.println("SpatialReward " + positionAndReward + "                  ---------------- SIMPLE REIN XP 149");
			
			amoebaSpatialReward.learn(positionAndReward);
			//amoebaControlModel.learn(previousStateCurrentStateAction);
			
		}

		@Override
		public HashMap<String, Double> explore(HashMap<String, Double> state, Environment env) {
			return env.randomAction();
		}
	}
	
	
	
	
	
	public static class OneDimensionEnv implements Environment {
		private Random rand = new Random();
		private double x = 0;
		private double reward = 0;
		private double size;
		private Drawable pos;
		
		public OneDimensionEnv(double envSize) {
			
			size = envSize;
			
			
		}
		
		@Override
		public HashMap<String, Double> reset(){
			x = RandomUtils.nextDouble(rand, -size, Math.nextUp(size));
			x = Math.round(x);
			reward = 0.0;
			//pos.move(x+0.5, 0.5);
			
			HashMap<String, Double> ret = new HashMap<>();
			ret.put("p1", x);
			ret.put("oracle", reward);
			return ret;
		}
		
		@Override
		public HashMap<String, Double> step(HashMap<String, Double> actionMap){
			double action = actionMap.get("a1");
			//if(action == 0.0) action = rand.nextDouble();
			if(action > 0.0) action = Math.ceil(action);
			if(action < 0.0 ) action = Math.floor(action);
			if(action > 1.0) action = 1.0;
			if(action < -1.0) action = -1.0;
			double oldX = x;
			x = x + action;
			
			
			//System.out.println("ACTIONS " + " a1 " +action + " " + " a2 " + action2);
			if(x < -size || x > size) {
				reward = -1000.0;
			} else if((x == 0.0) || (sign(oldX) != sign(x) )) {
				// win !
				reward = 1000.0;
			} else {
				reward = -1.0;
			}
			HashMap<String, Double> ret = new HashMap<>();
			ret.put("p1", x);
			ret.put("oracle", reward);
			//pos.move(x+0.5, 0.5);
			return ret;
		}

		@Override
		public List<String> actionSpace() {
			ArrayList<String> l = new ArrayList<>();
			l.add("a1 enum:true {-1, 0, 1}");
			return l;
		}

		@Override
		public List<String> perceptionSpace() {
			ArrayList<String> l = new ArrayList<>();
			l.add("p1 enum:false [-"+size+", "+size+"]");
			return l;
		}

		@Override
		public HashMap<String, Double> randomAction() {
			double a1 = rand.nextBoolean() ? -1 : 1;
			
						

			HashMap<String, Double> action = new HashMap<String, Double>();
			action.put("a1", a1);
			return action;
			}
		
	}
	
	
	
	
	
	private AMOEBA setupSpatialReward() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		File config;
		try {
			config = File.createTempFile("configSpatialReward", "xml");
			XmlConfigGenerator.makeXML(config, sensors);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null; // now compilator know config is initialized
		}
		//File config = new File("resources/simpleReinManualTrained.xml");
		
		Log.defaultMinLevel = Log.Level.INFORM;
		World.minLevel = TRACE_LEVEL.ERROR;
		AMOEBA amoeba = new AMOEBA(amoebaSpatialRewardUI, amoebaSpatialRewardVUI, config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		
		

		
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		//amoeba.setReinforcement(true);
		
		
		return amoeba;
	}
	

	private static AMOEBA setupControlModel() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("pCurrent", false));
		sensors.add(new Pair<String, Boolean>("pGoal", false));
		File config;
		try {
			config = File.createTempFile("configControlModel", "xml");
			XmlConfigGenerator.makeXML(config, sensors);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
			return null; // now compilator know config is initialized
		}
		//File config = new File("resources/simpleReinManualTrained.xml");
		
		Log.defaultMinLevel = Log.Level.INFORM;
		World.minLevel = TRACE_LEVEL.ERROR;
		AMOEBA amoeba = new AMOEBA(null, null, config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		
		
		
		
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		
		return amoeba;
	}


	private static int sign(double x) {
		return x < 0 ? -1 : 1;
	}
	
	/**
	 * Teach a learning agent on the SimpleReinforcement problem
	 * @param agent
	 * @return
	 */
	public static ArrayList<Double> learning(LearningAgent agent, Environment env){
		ArrayList<Double> averageRewards = new ArrayList<Double>();
		Random rand = new Random();
		
		Random r = new Random();
		HashMap<String, Double> state = env.reset();
		HashMap<String, Double> state2;
		double explo = EXPLO_RATE_BASE;
		for(int i = 0; i < N_LEARN; i++) {
			int nbStep = 0;
			state = env.reset();
			HashMap<String, Double> action = new HashMap<String, Double>();
			double totReward = 0.0;
			
			// execute simulation cycles
			boolean done = false;
			boolean invalid = false;
			
			
			while(!done && !invalid) {
				nbStep++;
				if(nbStep > MAX_STEP_PER_EPISODE) {
					invalid = true;
				}
				state.remove("oracle");
				
				action = new HashMap<String, Double>();
				
				action = agent.explore(state, env);
//				if(rand.nextDouble() < explo) {
//					action = agent.explore(state, env);
//				} else {
//					action = agent.choose(state, env);
//				}
				
				
				state2 = env.step(action);  // new position with associated reward
				
				if(state2.get("oracle") != -1.0) { //if goal or end of world
					done = true;
				}
				action.put("p1", state.get("p1")); //add previous state to action
				
				action.put("oracle", state2.get("oracle")); //add current reward to action
				
				// state : previous position and associated reward
				// state2 : new position with current reward
				// action : previous state, current action and current reward
				
				agent.learn(state, state2, action, done);
				totReward += action.get("oracle");
				
				state = state2;
			}
			
			System.out.println("-----------------------------------------------------------------------");
			
			// update exploration rate
			if(explo > MIN_EXPLO_RATE) {
				explo -= EXPLO_RATE_DIMINUTION_FACTOR;
				if(explo < MIN_EXPLO_RATE)
					explo = MIN_EXPLO_RATE;
			}
			
			System.out.println("Episode "+i+"  reward : "+totReward+"  explo : "+explo);
			//double testAR = test(agent, env, r, N_TEST);
			//averageRewards.add(testAR);
			
			//Scanner scan = new Scanner(System.in);
			//scan.nextLine();
		}
		
		return averageRewards;
	}
	
}
