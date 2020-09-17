package multiagent.framework.examples.randomants;

import multiagent.framework.Amas;
import multiagent.framework.Scheduling;
import multiagent.framework.tools.RunLaterHelper;
import multiagent.framework.ui.VUI;
import multiagent.framework.ui.drawables.DrawableString;

public class AntHillExample extends Amas<WorldExample> {

	private DrawableString antsCountLabel;

	public AntHillExample(WorldExample env) {
		super(env, Scheduling.DEFAULT);
	}

	@Override
	protected void onRenderingInitialization() {
		VUI.get().createAndAddImage(20, 20, "file:Resources/ant.png").setFixed().setLayer(10).setShowInExplorer(false);
		antsCountLabel = (DrawableString) VUI.get().createAndAddString(45, 25, "Ants count").setFixed().setLayer(10).setShowInExplorer(false);
	}

	@Override
	protected void onInitialAgentsCreation() {
		for (int i = 0; i < 50; i++)
			new AntExample(this, 0, 0);
	}

	@Override
	protected void onSystemCycleEnd() {
		RunLaterHelper.runLater(()->antsCountLabel.setText("Ants count: " + getAgents().size()));
	}
}
