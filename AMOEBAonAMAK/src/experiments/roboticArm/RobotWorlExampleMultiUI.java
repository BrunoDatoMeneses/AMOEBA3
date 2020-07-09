package experiments.roboticArm;

import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import fr.irit.smac.amak.ui.drawables.DrawableString;

public class RobotWorlExampleMultiUI extends Amas<WorldExampleMultiUI> {

	public RobotExampleMutliUI robotExampleMutliUI;

	private DrawableString antsCountLabel;

	public RobotWorlExampleMultiUI(AmasMultiUIWindow window, VUIMulti vui, WorldExampleMultiUI env, RobotController robotController, RobotArmManager robotArmManager, int jointsNb) {
		super(window, vui, env, Scheduling.DEFAULT);
		robotExampleMutliUI = new RobotExampleMutliUI(amasMultiUIWindow, this, 0, 0, jointsNb, robotController, robotArmManager);




	}

	@Override
	public void cycle() {
		cycle++;
		robotExampleMutliUI.onDecideAndAct();
	}

	@Override
	protected void onRenderingInitialization() {

	}

	@Override
	protected void onInitialAgentsCreation() {



			
	}

	@Override
	protected void onSystemCycleEnd() {

	}
}
