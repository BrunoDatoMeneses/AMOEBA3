package experiments.reinforcement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import agents.context.localModel.TypeLocalModel;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
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
 * Train an amoeba on a simple reinforcement task.
 * The goal of the task is to get to the center. When the position of the agent cross 0, it gets a reward of 100.
 * The agent can only moves in 2 directions, of a distance of 1. Moving give a reward of -1.
 * If the agent moves outside of the allowed range, it gets a reward of -100. 
 * @author Hugo
 *
 */
public abstract class SimpleReinforcement1DSpatialRewardAndActionMltiUI extends Application implements Serializable{
	
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
	
	AMOEBA amoeba;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	AmoebaMultiUIWindow amoebaUI;
	
	
	
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
		
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI, null);
		
//		amoebaSpatialRewardVUI = new VUIMulti("2D");
//		amoebaSpatialRewardUI = new AmoebaMultiUIWindow("SPATIAL_REWARD", amoebaSpatialRewardVUI);
//		
//		amoebaControlModelVUI = new VUIMulti("2D");
//		amoebaControlModelUI = new AmoebaMultiUIWindow("CONTROL_MODEL", amoebaControlModelVUI);
//		
//	
		
		//startTask(100, 1000);


		
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
	
	public void startTask2(long wait, int cycles) 
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask2(wait, cycles);
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
                	
                	ArrayList<ArrayList<Double>> results = new ArrayList<>();
        			//LearningAgent agent = new QLearning();
        			LearningAgent agent = new AmoebaQL();
        			//LearningAgent agent = new AmoebaCoop();
        			Environment env = new OneDimensionEnv(10);
        			results.add(learning(agent, env));
 
            		
            		int nbEpisodes = results.get(0).size();
            		for(int i = 0; i < nbEpisodes; i++) {
            			double average = 0;
            			for(int j = 0; j < results.size(); j++) {
            				average += results.get(j).get(i);
            			}
            			average /= results.size();
            			System.out.println(""+i+"\t"+average);
            		}
                }
            });
     
            Thread.sleep(wait);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
		
		
		
//        for(int i = 0; i < cycles; i++) 
//        {
//            try
//            {
//                // Get the Status
//                final String status = "Processing " + i + " of " + cycles;
//                 
//                // Update the Label on the JavaFx Application Thread        
//                Platform.runLater(new Runnable() 
//                {
//                    @Override
//                    public void run() 
//                    {
//                    	///
//                    }
//                });
//         
//                Thread.sleep(wait);
//            }
//            catch (InterruptedException e) 
//            {
//                e.printStackTrace();
//            }
//        }
    }   
	
	public void runTask2(long wait, int cycles) 
    {
		
		try
        {
             
            // Update the Label on the JavaFx Application Thread        
            Platform.runLater(new Runnable() 
            {
                @Override
                public void run() 
                {
                	///
            		
                }
            });
     
            Thread.sleep(wait);
        }
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
		
		
		
        for(int i = 0; i < cycles; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + cycles;
                 
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	///
                    }
                });
         
                Thread.sleep(wait);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   
	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
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
	
	/**
	 * Compatible only with OneDimensionEnv 
	 * @author Hugo
	 *
	 */
	public static class AmoebaQL implements LearningAgent {
		public AMOEBA amoebaSpatialReward;
		//public AMOEBA amoebaControlModel;
		public double lr = 0.8;
		public double gamma = 0.9;
		private Random rand = new Random();
		
		public AmoebaQL() {
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
			
			if(!Configuration.commandLineMode) {
				AmoebaWindow instance = AmoebaWindow.instance();
				//pos = new DrawableOval(0.5, 0.5, 1, 1);
				//pos.setColor(new Color(0.5, 0.0, 0.0, 0.5));
				//instance.mainVUI.add(pos);
				//instance.mainVUI.createAndAddRectangle(-50, -0.25, 100, 0.5);
				//instance.mainVUI.createAndAddRectangle(-0.25, -1, 0.5, 2);
				instance.point.hide();
				//instance.rectangle.hide();
			}
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
	
	/**
	 * Setup an amoeba for the SimpleReinforcement problem
	 * @return
	 */
	private static AMOEBA setup() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		File config;
		try {
			config = File.createTempFile("config", "xml");
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
		
	

		
		return amoeba;
	}
	
	
	
	private static AMOEBA setupSpatialReward() {
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
		AMOEBA amoeba = new AMOEBA(null, null, config.getAbsolutePath(), null);
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

	private static double test(LearningAgent agent, Environment env, Random r, int nbTest) {
		HashMap<String, Double> state;
		HashMap<String, Double> state2;
		double nbPositiveReward = 0.0;
		double tot_reward = 0.0;
		for(int i = 0; i < nbTest; i++) {
			double reward = 0.0;
			state = env.reset();
			
			// execute simulation cycles
			boolean done = false;
			int nbStep = 0;
			while(!done) {
				nbStep++;
				if(nbStep > 200) {
					done = true;
				}
				state.remove("oracle");
				 HashMap<String, Double> a = agent.choose(state, env);
				
				state2 = env.step(a);
				
				if(state2.get("oracle") != -1.0) {
					done = true;
				}
				
				reward += state2.get("oracle");
				
				state = state2;
			}
			if(reward > 0) {
				nbPositiveReward += 1.0;
			}
			tot_reward += reward;
		}
		double averageReward = tot_reward/nbTest;
		System.out.println("Test average reward : "+averageReward+"  Positive reward %: "+(nbPositiveReward/nbTest));
		
		return averageReward;
	}
	
	
	
	private static int sign(double x) {
		return x < 0 ? -1 : 1;
	}

}