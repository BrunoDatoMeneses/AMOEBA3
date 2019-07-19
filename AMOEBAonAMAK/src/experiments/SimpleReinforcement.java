package experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Random;

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
	public static final int N_EXPLORE_LINE = 100;
	public static final double MIN_EXPLO_RATE = 0.02;
	public static final double EXPLO_RATE_DIMINUTION_FACTOR = 0.01;
	public static final double EXPLO_RATE_BASE = 2;
	public static final String EXPLORATION_STRATEGY = "line"; // can be "random" or "line"
	private static int exploreLine;
	
	private Random rand = new Random();
	private double x = 0;
	private double reward = 0;
	private Drawable pos;

	public static void main(String[] args) {
		//poc(true);
		exp1();
		
	}
	
	public static void exp1() {
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
			return; // now compilator know config is initialized
		}
		
		//Configuration.commandLineMode = true;
		Log.defaultMinLevel = Log.Level.INFORM;
		World.minLevel = TRACE_LEVEL.ERROR;
		AMOEBA amoeba = new AMOEBA(config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
		SimpleReinforcement env = new SimpleReinforcement();
		
		Random r = new Random();
		HashMap<String, Double> state = env.reset();
		HashMap<String, Double> state2;
		double explo = EXPLO_RATE_BASE;
		for(int i = 0; i < 200; i++) {
			Deque<HashMap<String, Double>> actions = new ArrayDeque<>();
			//System.out.println("Explore "+i);
			int nbStep = 0;
			state = env.reset();
			exploreLine = N_EXPLORE_LINE;
			HashMap<String, Double> action = new HashMap<String, Double>();
			
			// execute simulation cycles
			boolean done = false;
			boolean invalid = false;
			while(!done && !invalid) {
				nbStep++;
				if(nbStep > 200) {
					invalid = true;
				}
				state.remove("oracle");
				double lastAction = action.getOrDefault("a1", 0.0);
				
				action = new HashMap<String, Double>();
				
				if(exploreLine < N_EXPLORE_LINE && lastAction != 0.0) {
					action.put("a1", lastAction);
					exploreLine++;
				} else {
					action = amoeba.maximize(state);
					explore(r, explo, action, lastAction);
				}
				
				
				state2 = env.step(action.get("a1"));
				
				if(state2.get("oracle") != -1.0) {
					done = true;
				}
				
				action.put("p1", state.get("p1"));
				action.put("oracle", state2.get("oracle"));
				actions.push(action);
				
				state = state2;
			}
			
			// build learning set 
			HashMap<String, Double> step = actions.pop();
			double reward = step.get("oracle");
			Deque<HashMap<String, Double>> learnSet = new ArrayDeque<>();
			learnSet.add(step);
			while(!actions.isEmpty()) {
				step = actions.pop();
				reward += step.get("oracle");
				step.put("oracle", reward);
				learnSet.push(step);
			}
			
			// learn
			while(!learnSet.isEmpty()) {
				HashMap<String, Double> a = learnSet.pop();
				//System.out.println("("+a.get("p1")+"\t, "+a.get("a1")+"\t, "+a.get("oracle")+")");
				amoeba.learn(a);
			}
			//System.exit(0);
			
			// update exploration rate
			if(explo > MIN_EXPLO_RATE) {
				explo -= EXPLO_RATE_DIMINUTION_FACTOR;
				if(explo < MIN_EXPLO_RATE)
					explo = MIN_EXPLO_RATE;
			}
			
			System.out.println("Episode "+i+"  reward : "+reward+"  explo : "+explo);
		}
		
		test(amoeba, env, r, 500);
	}

	private static void test(AMOEBA amoeba, SimpleReinforcement env, Random r, int nbTest) {
		HashMap<String, Double> state;
		HashMap<String, Double> state2;
		double nbPositiveReward = 0.0;
		double tot_reward = 0.0;
		for(int i = 0; i < nbTest; i++) {
			double reward = 0.0;
			state = env.reset();
			HashMap<String, Double> action = new HashMap<String, Double>();
			
			// execute simulation cycles
			boolean done = false;
			int nbStep = 0;
			while(!done) {
				nbStep++;
				if(nbStep > 1000) {
					done = true;
				}
				state.remove("oracle");
				action = amoeba.maximize(state);
				// random action if no proposition from amoeba
				if(action.get("oracle").equals(Double.NEGATIVE_INFINITY) ) {
					action.put("a1", (r.nextBoolean() ? 1.0 : -1.0));
				}
				
				state2 = env.step(action.get("a1"));
				
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
		System.out.println("Test average reward : "+tot_reward/nbTest+"  Positive reward %: "+(nbPositiveReward/nbTest));
		
		if(!Configuration.commandLineMode) {
			AmoebaWindow.instance().point.hide();
			AmoebaWindow.instance().rectangle.hide();
			AmoebaWindow.instance().mainVUI.updateCanvas();
		}
	}
	
	/**
	 * This is a proof of concept, showing that if amoeba learn the correct model of the reward,
	 * it can produce a good solution.
	 * The expected average reward for the optimal solution is 75.
	 * The main cause of negative reward is infinite loop (usually near the objective). In such case, the reward is -200
	 */
	public static void poc(boolean learnMalus) {
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
			return; // now compilator know config is initialized
		}
		
		Log.defaultMinLevel = Log.Level.INFORM;
		World.minLevel = TRACE_LEVEL.ERROR;
		AMOEBA amoeba = new AMOEBA(config.getAbsolutePath(), null);
		amoeba.saver = new SaveHelperDummy();
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
		if(r.nextDouble() < explo || action.get("oracle").equals(Double.NEGATIVE_INFINITY) ) {
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
