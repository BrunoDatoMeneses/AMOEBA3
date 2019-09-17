package fr.irit.smac.amak.examples.randomants;

import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmasWindow;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.DrawableString;

public class AntHillExample extends Amas<WorldExample> {


	
	private DrawableString antsCountLabel;

	public AntHillExample(AmasWindow window, WorldExample env) {
		super(window, env, Scheduling.DEFAULT);
	}

	@Override
	protected void onRenderingInitialization() {
		VUI.get(amasWindow).createAndAddImage(20, 20, "file:Resources/ant.png").setFixed().setLayer(10).setShowInExplorer(false);
		antsCountLabel = (DrawableString) VUI.get(amasWindow).createAndAddString(45, 25, "Ants count").setFixed().setLayer(10).setShowInExplorer(false);
		
		
	}

	@Override
	protected void onInitialAgentsCreation() {
		for (int i = 0; i < 50; i++)
			new AntExample(amasWindow, this, 0, 0);
	}

	@Override
	protected void onSystemCycleEnd() {
		RunLaterHelper.runLater(()->antsCountLabel.setText("Ants count: " + getAgents().size()));
	}
}
