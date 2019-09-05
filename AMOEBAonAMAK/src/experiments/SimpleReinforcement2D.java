package experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import agents.context.localModel.TypeLocalModel;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.drawables.Drawable;
import gui.AmoebaWindow;
import kernel.AMOEBA;
import kernel.World;
import kernel.backup.SaveHelperDummy;
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
public abstract class SimpleReinforcement2D {
	/* Learn and Test */
	public static final int MAX_STEP_PER_EPISODE = 200;
	public static final int N_LEARN = 400;//400
	public static final int N_TEST = 100;
	
	/* Exploration */
	public static final double MIN_EXPLO_RATE = 0.02;
	public static final double EXPLO_RATE_DIMINUTION_FACTOR = 0.01;
	public static final double EXPLO_RATE_BASE = 1;
	
	public static void main(String[] args) {
		//poc(true);
		Configuration.commandLineMode = false;
		Configuration.plotMilliSecondsUpdate = 20000;
		/*System.out.println("----- AMOEBA -----");
		learning(new QLearning(), new OneDimensionEnv());
		System.out.println("----- END AMOEBA -----");
		System.out.println("\n\n----- QLEARNING -----");
		learning(new QLearning());
		System.out.println("----- END QLEARNING -----");*/
		ArrayList<ArrayList<Double>> results = new ArrayList<>();
		for(int i = 0; i < 1; i++) {
			//LearningAgent agent = new QLearning();
			LearningAgent agent = new AmoebaQL();
			//LearningAgent agent = new AmoebaCoop();
			Environment env = new TwoDimensionEnv(10);
			results.add(learning(agent, env));
			System.out.println(i);
		}
		
		int nbEpisodes = results.get(0).size();
		for(int i = 0; i < nbEpisodes; i++) {
			double average = 0;
			for(int j = 0; j < results.size(); j++) {
				average += results.get(j).get(i);
			}
			average /= results.size();
			System.out.println(""+i+"\t"+average);
		}
		
		//System.exit(0);
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
		public AMOEBA amoeba;
		public double lr = 0.8;
		public double gamma = 0.9;
		private Random rand = new Random();
		
		public AmoebaQL() {
			amoeba = setup();
			amoeba.setLocalModel(TypeLocalModel.MILLER_REGRESSION);
			amoeba.getEnvironment().setMappingErrorAllowed(0.025);
		}
		
		@Override
		public HashMap<String, Double> choose(HashMap<String, Double> state, Environment env) {
			
			HashMap<String, Double> bestActions =  amoeba.maximize(state);
			double a1 = bestActions.getOrDefault("a1", 0.0);
			double a2 = bestActions.getOrDefault("a2", 0.0);
//			if(a1 == 0.0) {
//				a1 = rand.nextBoolean() ? -1 : 1;
//			}
//			if(a2 == 0.0) {
//				a2 = rand.nextBoolean() ? -1 : 1;
//			}
			
			HashMap<String, Double> action = new HashMap<String, Double>();
			action.put("a1", a1);
			action.put("a2", a2);
			return action;
		}

		@Override
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2,
				HashMap<String, Double> action, boolean done) {
			
			// state : previous position and associated reward
			// state2 : new position with current reward
			// action : previous state, current actions and current reward
			
			HashMap<String, Double> state2Copy = new HashMap<>(state2);
			state2Copy.remove("oracle"); //reward
			
			double reward = state2.get("oracle");
			double q;
			if(!done) {
				double expectedReward = amoeba.request(action);
				HashMap<String, Double> futureState = this.choose(state2Copy, null);
				futureState.putAll(state2);
				double futureReward = amoeba.request(futureState);
				//double futureAction = this.choose(state2Copy, null).get("a1");
				
				q = reward + gamma * futureReward - expectedReward;
			} else {
				q = reward;
			}
			HashMap<String, Double> learn = new HashMap<>(action);
			
			//learn.put("oracle", lr * q);
			learn.put("oracle", reward);
			// learn : previous state, current action and current Q learning reward
			System.out.println(learn);
			amoeba.learn(learn);
			
		}

		@Override
		public HashMap<String, Double> explore(HashMap<String, Double> state, Environment env) {
			return env.randomAction();
		}
	}
	
	/**
	 * Wrapper for AMOEBA
	 * @author Hugo
	 *
	 */
	public static class AmoebaCoop implements LearningAgent {
		public AMOEBA amoeba;
		
		public AmoebaCoop() {
			amoeba = setup();
			amoeba.setLocalModel(TypeLocalModel.COOP_MILLER_REGRESSION);
			amoeba.getEnvironment().setMappingErrorAllowed(0.009);
		}
		
		@Override
		public HashMap<String, Double> choose(HashMap<String, Double> state, Environment env) {
			HashMap<String, Double> action = amoeba.maximize(state);
			if(action.get("oracle") == Double.NEGATIVE_INFINITY) {
				action = env.randomAction();
			}
			return action;
		}

		@Override
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2,
				HashMap<String, Double> action, boolean done) {
			amoeba.learn(action);
		}

		@Override
		public HashMap<String, Double> explore(HashMap<String, Double> state, Environment env) {
			return env.randomAction();
		}
		
	}
	
	/**
	 * Compatible only with OneDimensionEnv.<br/>
	 * An extremely crude and quick implementation of Q learning.
	 * Not expected to perform well, but should be better than random.
	 * @author Hugo
	 *
	 */
	public static class QLearning implements LearningAgent {
		public double[][] Q = new double[102][2];
		public double lr = 0.8;
		public double gamma = 0.9;
		private Random rand = new Random();
		
		@Override
		public HashMap<String, Double> choose(HashMap<String, Double> state, Environment env) {
			int p = state.get("p1").intValue()+50;
			double a;
			if(Q[p][0] == Q[p][1]) {
				a = rand.nextBoolean() ? -1 : 1;
			} else {
				a = Q[p][0] > Q[p][1] ? -1 : 1;
			}
			HashMap<String, Double> action = new HashMap<String, Double>();
			action.put("a1", a);
			return action;
		}

		@Override
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2,
				HashMap<String, Double> action, boolean done) {
			int p = state.get("p1").intValue()+50;
			int p2 = state2.get("p1").intValue()+50;
			int a = action.get("a1").intValue() == -1 ? 0 : 1;
			double reward = state2.get("oracle");
			double max = Double.NEGATIVE_INFINITY;
			if(!done) {
				for(Double v : Q[p2]) {
					max = Math.max(max, v);
				}
			} else {
				max = reward;
			}
			// 
			double q = reward + gamma * max - Q[p][a];
			Q[p][a] += lr * q;
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
			y = RandomUtils.nextDouble(rand, -size, Math.nextUp(size));
			y = Math.round(x);
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
	
	/**
	 * Setup an amoeba for the SimpleReinforcement problem
	 * @return
	 */
	private static AMOEBA setup() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		sensors.add(new Pair<String, Boolean>("a1", true));
		sensors.add(new Pair<String, Boolean>("p2", false));
		sensors.add(new Pair<String, Boolean>("a2", true));
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
		AMOEBA amoeba = new AMOEBA(config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
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
				action.put("p2", state.get("p2")); //add previous state to action
				
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
			double testAR = test(agent, env, r, N_TEST);
			averageRewards.add(testAR);
			
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
	
	/**
	 * This is a proof of concept, showing that if amoeba learn the correct model of the reward,
	 * it can produce a good solution.
	 * The expected average reward for the optimal solution is 75.
	 * The main cause of negative reward is infinite loop (usually near the objective). In such case, the reward is -200
	 */
	public static void poc(boolean learnMalus) {
		AMOEBA amoeba = setup();
		Environment env = new TwoDimensionEnv(50);
		
		// train
		for(double n = 0.0; n < 0.5; n+=0.1) {
			double pos = 50.0-n;
			for(int i = 0; i < 49; i++) {
				double reward = 100 - Math.abs(pos);
				HashMap<String, Double> action = new HashMap<String, Double>();
				action.put("p1", pos);
				action.put("a1", -1.0);
				action.put("oracle", reward);
				amoeba.learn(action);
				
				if(learnMalus) {
					reward = -150 + Math.abs(pos);
					action.put("a1", 1.0);
					action.put("oracle", reward);
					amoeba.learn(action);
				}
				
				pos -= 1.0;
			}
			
			pos = -50.0-n;
			for(int i = 0; i < 49; i++) {
				double reward = 100 - Math.abs(pos);
				HashMap<String, Double> action = new HashMap<String, Double>();
				action.put("p1", pos);
				action.put("a1", 1.0);
				action.put("oracle", reward);
				amoeba.learn(action);
				
				if(learnMalus) {
					reward = -150 + Math.abs(pos);
					action.put("a1", -1.0);
					action.put("oracle", reward);
					amoeba.learn(action);
				}
				
				pos += 1.0;
			}
		}
		
		// tests
		Random r = new Random();
		HashMap<String, Double> state = env.reset();
		HashMap<String, Double> state2;
		double tot_reward = 0.0;
		int nbTest = 100;
		double nbPositiveReward = 0;
		for(int i = 0; i < nbTest; i++) {
			double reward = 0.0;
			state = env.reset();
			HashMap<String, Double> action = new HashMap<String, Double>();
			
			// execute simulation cycles
			boolean done = false;
			int nbStep = 0;
			while(!done) {
				nbStep++;
				if(nbStep > 200) {
					done = true;
				}
				state.remove("oracle");
				action = amoeba.maximize(state);
				// random action if no proposition from amoeba
				if(action.get("oracle").equals(Double.NEGATIVE_INFINITY) ) {
					action.put("a1", (r.nextBoolean() ? 1.0 : -1.0));
				}
				//System.out.println("action "+action);
				
				state2 = env.step(action);
				
				if(state2.get("oracle") != -1.0) {
					done = true;
				}
				
				reward += state2.get("oracle");
				
				//System.out.println("state2 "+state2+"  reward "+reward);
				
				state = state2;
			}
			if(reward > 0) {
				nbPositiveReward += 1.0;
			}
			tot_reward += reward;
			//System.out.println("-----------------------------\nTot reward "+tot_reward+"\n-----------------------------");
		}
		System.out.println("Average reward : "+tot_reward/nbTest+"  Positive reward %: "+(nbPositiveReward/nbTest));
	}
	
	private static int sign(double x) {
		return x < 0 ? -1 : 1;
	}

}
