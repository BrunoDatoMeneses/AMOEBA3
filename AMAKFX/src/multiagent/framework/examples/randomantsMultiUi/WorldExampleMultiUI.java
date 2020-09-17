package multiagent.framework.examples.randomantsMultiUi;

import multiagent.framework.Environment;
import multiagent.framework.Scheduling;
import multiagent.framework.ui.AmasMultiUIWindow;

public class WorldExampleMultiUI extends Environment {
	public WorldExampleMultiUI(AmasMultiUIWindow window, Object...params) {
		super(window, Scheduling.DEFAULT, params);
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
