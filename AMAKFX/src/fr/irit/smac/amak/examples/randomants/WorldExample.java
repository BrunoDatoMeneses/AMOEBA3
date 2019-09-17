package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.ui.AmasWindow;
import fr.irit.smac.amak.ui.MainWindow;

public class WorldExample extends Environment {
	
	public WorldExample(AmasWindow amasWindow, Object...params) {
		super(amasWindow, Scheduling.DEFAULT, params);
	}

	private int width;
	private int height;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public void onInitialization() {
		this.width = 800;
		this.height = 600;
	}

}
