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
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.VUIMulti;
import fr.irit.smac.amak.ui.drawables.Drawable;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.World;
import kernel.backup.SaveHelperDummy;
import utils.Pair;
import utils.RandomUtils;
import utils.TRACE_LEVEL;
import utils.XmlConfigGenerator;


/**
 * The Class BadContextLauncherEasy.
 */
public class ReinforcementMultiUI2D extends Application implements Serializable {


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
	
	ELLSA amoebaSpatialReward;
	VUIMulti amoebaSpatialRewardVUI;
	EllsaMultiUIWindow amoebaSpatialRewardUI;
	
	ELLSA amoebaActionModel1;
	VUIMulti amoebaActionModel1VUI;
	EllsaMultiUIWindow amoebaActionModel1UI;
	
	ELLSA amoebaActionModel2;
	VUIMulti amoebaActionModel2VUI;
	EllsaMultiUIWindow amoebaActionModel2UI;
	
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
		amoebaSpatialRewardUI = new EllsaMultiUIWindow("SPATIAL REWARD", amoebaSpatialRewardVUI, null);
		
		amoebaActionModel1VUI = new VUIMulti("2D");
		amoebaActionModel1UI = new EllsaMultiUIWindow("ACTION 1 MODEL", amoebaActionModel1VUI, null);
		
		amoebaActionModel2VUI = new VUIMulti("2D");
		amoebaActionModel2UI = new EllsaMultiUIWindow("ACTION 2 MODEL", amoebaActionModel2VUI, null);
		
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
				
		
		agent = new AmoebaRewardAndControl();
    	env = new TwoDimensionEnv(10);
		
		state = env.reset(); 
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
		        				action.put("p2", state.get("p2")); //add previous state to action
		        				
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
		public double lr = 0.8;
		public double gamma = 0.9;
		private Random rand = new Random();
		
		public AmoebaRewardAndControl() {
			amoebaSpatialReward = setupSpatialReward();
			//amoebaActionModel1 = setupControlModel("1", amoebaActionModel1UI, amoebaActionModel1VUI);
			//amoebaActionModel2 = setupControlModel("2", amoebaActionModel2UI, amoebaActionModel2VUI);
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
			
			HashMap<String, Double> previousStateCurrentStateAction1 = new HashMap<>();
			previousStateCurrentStateAction1.put("p1Current", action.get("p1"));
			previousStateCurrentStateAction1.put("p2Current", action.get("p2"));
			previousStateCurrentStateAction1.put("p1Goal", positionAndReward.get("p1"));
			previousStateCurrentStateAction1.put("oracle", action.get("a1"));
			
			HashMap<String, Double> previousStateCurrentStateAction2 = new HashMap<>();
			previousStateCurrentStateAction2.put("p1Current", action.get("p1"));
			previousStateCurrentStateAction2.put("p2Current", action.get("p2"));
			previousStateCurrentStateAction2.put("p2Goal", positionAndReward.get("p2"));
			previousStateCurrentStateAction2.put("oracle", action.get("a2"));
			

			
			//System.out.println("ControlModel " + previousStateCurrentStateAction + "                  ---------------- SIMPLE REIN XP 149");
			//System.out.println("SpatialReward " + positionAndReward + "                  ---------------- SIMPLE REIN XP 149");
			
			amoebaSpatialReward.learn(positionAndReward);
			//amoebaActionModel1.learn(previousStateCurrentStateAction1);
			//amoebaActionModel2.learn(previousStateCurrentStateAction2);
			
		}

		@Override
		public HashMap<String, Double> explore(HashMap<String, Double> state, Environment env) {
			return env.randomAction();
		}
	}
	
	
	
	
	
	public static class TwoDimensionEnv implements Environment {
		private Random rand = new Random();
		private double x = 0;
		private double y = 0;
		private double reward = 0;
		private double size;
		private Drawable pos;
		
		public TwoDimensionEnv(double envSize) {
			
			size = envSize;
			
			
		}
		
		@Override
		public HashMap<String, Double> reset(){
			x = RandomUtils.nextDouble(rand, -size, Math.nextUp(size));
			x = Math.round(x);
			y = RandomUtils.nextDouble(rand, -size, Math.nextUp(size));
			y = Math.round(y);
			reward = 0.0;
			//pos.move(x+0.5, 0.5);
			
			HashMap<String, Double> ret = new HashMap<>();
			ret.put("p1", x);
			ret.put("p2", y);
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
			
			double action2 = actionMap.get("a2");
			//if(action2 == 0.0) action2 = rand.nextDouble();
			if(action2 > 0.0) action2 = Math.ceil(action2);
			if(action2 < 0.0 ) action2 = Math.floor(action2);
			if(action2 > 1.0) action2 = 1.0;
			if(action2 < -1.0) action2 = -1.0;
			double oldY = y;
			y = y + action2;
			
			//System.out.println("ACTIONS " + " a1 " +action + " " + " a2 " + action2);
			if(x < -size || x > size || y < -size || y > size) {
				reward = -1000.0;
			} else if((x == 0.0 && y == 0.0) || (sign(oldX) != sign(x) && sign(oldY) != sign(y) )) {
				// win !
				reward = 1000.0;
			} else {
				reward = -1.0;
			}
			HashMap<String, Double> ret = new HashMap<>();
			ret.put("p1", x);
			ret.put("p2", y);
			ret.put("oracle", reward);
			//pos.move(x+0.5, 0.5);
			return ret;
		}

		@Override
		public List<String> actionSpace() {
			ArrayList<String> l = new ArrayList<>();
			l.add("a1 enum:true {-1, 0, 1}");
			l.add("a2 enum:true {-1, 0, 1}");
			return l;
		}

		@Override
		public List<String> perceptionSpace() {
			ArrayList<String> l = new ArrayList<>();
			l.add("p1 enum:false [-"+size+", "+size+"]");
			l.add("p2 enum:false [-"+size+", "+size+"]");
			return l;
		}

		@Override
		public HashMap<String, Double> randomAction() {
			double a1 = rand.nextInt(3) - 1;
			double a2 = (a1 == 0.0) ? (rand.nextBoolean() ? -1 : 1) : (rand.nextInt(3) - 1);
						
//			double a1 =  rand.nextBoolean() ? -1 : 1;
//			double a2 =  rand.nextBoolean() ? -1 : 1;
			HashMap<String, Double> action = new HashMap<String, Double>();
			action.put("a1", a1);
			action.put("a2", a2);
			return action;
			}
		
	}
	
	
	
	
	
	private ELLSA setupSpatialReward() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		sensors.add(new Pair<String, Boolean>("p2", false));
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
		World.minLevel = TRACE_LEVEL.DEBUG;
		ELLSA amoeba = new ELLSA(amoebaSpatialRewardUI, amoebaSpatialRewardVUI, config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		
		

		
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		amoeba.setReinforcement(true);
		
		
		return amoeba;
	}
	

	private ELLSA setupControlModel(String action, EllsaMultiUIWindow window, VUIMulti VUI) {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1Current", false));
		sensors.add(new Pair<String, Boolean>("p2Current", false));
		sensors.add(new Pair<String, Boolean>("p"+action+"Goal", false));
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
		ELLSA amoeba = new ELLSA(window, VUI, config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		
		
		
		
		amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
		amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		
		return amoeba;
	}


	private static int sign(double x) {
		return x < 0 ? -1 : 1;
	}
	
	
	
}
