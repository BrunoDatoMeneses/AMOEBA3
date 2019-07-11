package experiments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.drawables.Drawable;
import fr.irit.smac.amak.ui.drawables.DrawableOval;
import gui.AmoebaWindow;
import javafx.scene.paint.Color;
import kernel.AMOEBA;
import utils.Pair;
import utils.RandomUtils;
import utils.XmlConfigGenerator;

public class SimpleReinforcement {
	
	private Random rand = new Random();
	private double x = 0;
	private double reward = 0;
	private Drawable pos;

	public static void main(String[] args) {
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
		
		AMOEBA amoeba = new AMOEBA(config.getAbsolutePath(), null);
		SimpleReinforcement env = new SimpleReinforcement();
		
		Random r = new Random();
		HashMap<String, Double> state = env.reset();
		HashMap<String, Double> state2;
		for(int i = 0; i < 10000; i++) {
			state.remove("oracle");
			HashMap<String, Double> action = amoeba.maximize(state);
			if(r.nextDouble() < 0.1 || action.get("oracle").equals(Double.NEGATIVE_INFINITY) ) {
				System.out.println("Random action");
				action.put("a1", (r.nextBoolean() ? 10.0 : -10.0));
			}
			state2 = env.step(action.get("a1"));
			action.put("oracle", state2.get("oracle"));
			action.put("p1", state.get("p1"));
			System.out.println(action);
			amoeba.learn(action);
			state = state2;
		}
	}
	
	/**
	 * Must be called AFTER an AMOEBA with GUI
	 */
	public SimpleReinforcement() {
		Configuration.commandLineMode = false;
		AmoebaWindow instance = AmoebaWindow.instance();
		pos = new DrawableOval(0.5, 0.5, 1, 1);
		pos.setColor(new Color(0.5, 0.0, 0.0, 0.5));
		instance.mainVUI.add(pos);
		instance.mainVUI.createAndAddRectangle(-50, -0.25, 100, 0.5);
		instance.mainVUI.createAndAddRectangle(-0.25, -1, 0.5, 2);
		
		
		
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
			x = RandomUtils.nextDouble(rand, -50.0, Math.nextUp(50.0));
			reward = -100.0;
		} else if(x == 0.0 || sign(oldX) != sign(x)) {
			// win !
			reward = 100.0;
			x = RandomUtils.nextDouble(rand, -50.0, Math.nextUp(50.0));
		} else {
			reward = -1.0;
		}
		HashMap<String, Double> ret = new HashMap<>();
		ret.put("p1", x);
		ret.put("oracle", reward);
		pos.move(x, 0);
		return ret;
	}
	
	public HashMap<String, Double> reset(){
		x = RandomUtils.nextDouble(rand, -50.0, Math.nextUp(50.0));
		reward = 0.0;
		
		HashMap<String, Double> ret = new HashMap<>();
		ret.put("p1", x);
		ret.put("oracle", reward);
		return ret;
	}
	
	private int sign(double x) {
		return x < 0 ? -1 : 1;
	}

}
