package multiagent.framework.examples.randomants;

import multiagent.framework.Environment;
import multiagent.framework.Scheduling;

public class WorldExample extends Environment {
	public WorldExample(Object...params) {
		super(Scheduling.DEFAULT, params);
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
