package experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

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
public class SimpleReinforcement {
	/* Learn and Test */
	public static final int MAX_STEP_PER_EPISODE = 200;
	public static final int N_LEARN = 200;
	public static final int N_TEST = 10;
	
	/* Exploration */
	public static final int N_EXPLORE_LINE = 0;
	public static final double MIN_EXPLO_RATE = 0.02;
	public static final double EXPLO_RATE_DIMINUTION_FACTOR = 0.0;
	public static final double EXPLO_RATE_BASE = 1;
	public static final String EXPLORATION_STRATEGY = "random"; // can be "random" or "line"
	private static int exploreLine;
	
	private Random rand = new Random();
	private double x = 0;
	private double reward = 0;
	private Drawable pos;
	
	/**
	 * Wrapper for any kind of learning agent
	 * @author Hugo
	 *
	 */
	public interface LearningAgent {
		public double choose(HashMap<String, Double> state);
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2, HashMap<String, Double> action, boolean done);
	}
	
	/**
	 * Wrapper for AMOEBA
	 * @author Hugo
	 *
	 */
	public static class Amoeba implements LearningAgent {
		public AMOEBA amoeba;
		private Random rand = new Random();
		
		public Amoeba() {
			amoeba = setup();
			amoeba.setLocalModel(TypeLocalModel.COOP_MILLER_REGRESSION);
			amoeba.getEnvironment().setMappingErrorAllowed(0.009);
		}
		
		@Override
		public double choose(HashMap<String, Double> state) {
			double a = amoeba.maximize(state).getOrDefault("a1", 0.0);
			if(a == 0.0) {
				a = rand.nextBoolean() ? -1 : 1;
			}
			return a;
		}

		@Override
		public void learn(HashMap<String, Double> state, HashMap<String, Double> state2,
				HashMap<String, Double> action, boolean done) {
			amoeba.learn(action);
		}
		
	}
	
	/**
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
		public double choose(HashMap<String, Double> state) {
			int p = state.get("p1").intValue()+50;
			double a;
			if(Q[p][0] == Q[p][1]) {
				a = rand.nextBoolean() ? -1 : 1;
			} else {
				a = Q[p][0] > Q[p][1] ? -1 : 1;
			}
			return a;
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
			double q = reward + gamma * max - Q[p][a];
			Q[p][a] += lr * q;
		}
		
	}
	

	public static void main(String[] args) {
		//poc(true);
		//Configuration.commandLineMode = true;
		System.out.println("----- AMOEBA -----");
		learning(new Amoeba());
		System.out.println("----- END AMOEBA -----");
		/*System.out.println("\n\n----- QLEARNING -----");
		learning(new QLearning());
		System.out.println("----- END QLEARNING -----");*/
		/*ArrayList<ArrayList<Double>> results = new ArrayList<>();
		LearningAgent agent = new QLearning();
		for(int i = 0; i < 100; i++) {
			results.add(learning(agent));
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
		*/
		//System.exit(0);
	}
	
	/**
	 * Setup an amoeba for the SimpleReinforcement problem
	 * @return
	 */
	private static AMOEBA setup() {
		ArrayList<Pair<String, Boolean>> sensors = new ArrayList<>();
		sensors.add(new Pair<String, Boolean>("p1", false));
		sensors.add(new Pair<String, Boolean>("a1", true));
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
	public static ArrayList<Double> learning(LearningAgent agent){
		SimpleReinforcement env = new SimpleReinforcement();
		ArrayList<Double> averageRewards = new ArrayList<Double>();
		
		Random r = new Random();
		HashMap<String, Double> state = env.reset();
		HashMap<String, Double> state2;
		double explo = EXPLO_RATE_BASE;
		for(int i = 0; i < N_LEARN; i++) {
			int nbStep = 0;
			state = env.reset();
			exploreLine = N_EXPLORE_LINE;
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
				double lastAction = action.getOrDefault("a1", 0.0);
				
				action = new HashMap<String, Double>();
				
				if(exploreLine < N_EXPLORE_LINE && lastAction != 0.0) {
					action.put("a1", lastAction);
					exploreLine++;
				} else {
					action.put("a1", agent.choose(state));
					explore(r, explo, action, lastAction);
				}
				
				
				state2 = env.step(action.get("a1"));
				if(state2.get("oracle") != -1.0) {
					done = true;
				}
				action.put("p1", state.get("p1"));
				action.put("oracle", state2.get("oracle"));
				
				agent.learn(state, state2, action, done);
				totReward += action.get("oracle");
				
				state = state2;
			}
			
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

	private static double test(LearningAgent agent, SimpleReinforcement env, Random r, int nbTest) {
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
				double a = agent.choose(state);
				
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
		SimpleReinforcement env = new SimpleReinforcement();
		
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
		// increase precision of model near objective
		// right now it make things worst
		/*
		for(int n = 0; n < 5; n++) {
			for(double pos = 2.0; pos > 0.02; pos -= 0.1) {
				double reward = 100 - Math.abs(pos);
				HashMap<String, Double> action = new HashMap<String, Double>();
				
				action.put("p1", pos);
				action.put("a1", -1.0);
				action.put("oracle", reward);
				amoeba.learn(action);
				
				if(learnMalus) {
					reward = -150 + Math.abs(pos);
					action.put("p1", pos);
					action.put("a1", 1.0);
					action.put("oracle", reward);
					amoeba.learn(action);
				}
				
				action.put("p1", -pos);
				action.put("a1", 1.0);
				action.put("oracle", reward);
				amoeba.learn(action);
				
				if(learnMalus) {
					reward = -150 + Math.abs(pos);
					action.put("p1", -pos);
					action.put("a1", -1.0);
					action.put("oracle", reward);
					amoeba.learn(action);
				}
			}
		}
		*/
		
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
				
				state2 = env.step(action.get("a1"));
				
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
		AmoebaWindow.instance().point.move(100, 100);
		AmoebaWindow.instance().mainVUI.updateCanvas();
	}

	/**
	 * Possibly overwrite the action and explore instead. 
	 * @param r A rng
	 * @param explo Current exploration rate
	 * @param action The current action
	 * @param lastAction The action of the last simulation cycle
	 */
	private static void explore(Random r, double explo, HashMap<String, Double> action, double lastAction) {
		if(r.nextDouble() < explo) {
			switch (EXPLORATION_STRATEGY) {
			case "line":
				exploreLine = 0;
				action.put("a1", (r.nextBoolean() ? 1.0 : -1.0));
				break;
			case "random":
				action.put("a1", (r.nextBoolean() ? 1.0 : -1.0));
				break;
			default:
				throw new IllegalArgumentException("Unkown exploration strategy : "+EXPLORATION_STRATEGY);
			}
		}
	}
	
	/**
	 * Must be called AFTER an AMOEBA with GUI
	 */
	public SimpleReinforcement() {
		if(!Configuration.commandLineMode) {
			AmoebaWindow instance = AmoebaWindow.instance();
			//pos = new DrawableOval(0.5, 0.5, 1, 1);
			//pos.setColor(new Color(0.5, 0.0, 0.0, 0.5));
			//instance.mainVUI.add(pos);
			instance.mainVUI.createAndAddRectangle(-50, -0.25, 100, 0.5);
			instance.mainVUI.createAndAddRectangle(-0.25, -1, 0.5, 2);
			instance.point.hide();
			//instance.rectangle.hide();
		}
		
		
	}
	
	public HashMap<String, Double> step(double action){
		if(action == 0.0) action = rand.nextDouble();
		if(action > 0.0) action = Math.ceil(action);
		if(action < 0.0 ) action = Math.floor(action);
		if(action > 1.0) action = 1.0;
		if(action < -1.0) action = -1.0;
		double oldX = x;
		x = x + action;
		if(x < -50.0 || x > 50.0) {
			reward = -100.0;
		} else if(x == 0.0 || sign(oldX) != sign(x)) {
			// win !
			reward = 100.0;
		} else {
			reward = -1.0;
		}
		HashMap<String, Double> ret = new HashMap<>();
		ret.put("p1", x);
		ret.put("oracle", reward);
		//pos.move(x+0.5, 0.5);
		return ret;
	}
	
	public HashMap<String, Double> reset(){
		x = RandomUtils.nextDouble(rand, -50.0, Math.nextUp(50.0));
		x = Math.round(x);
		reward = 0.0;
		//pos.move(x+0.5, 0.5);
		
		HashMap<String, Double> ret = new HashMap<>();
		ret.put("p1", x);
		ret.put("oracle", reward);
		return ret;
	}
	
	private int sign(double x) {
		return x < 0 ? -1 : 1;
	}

}
