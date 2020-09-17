package experiments.roboticArm.simulation;

import multiagent.framework.Amas;
import multiagent.framework.Configuration;
import multiagent.framework.Scheduling;
import multiagent.framework.ui.AmasMultiUIWindow;
import multiagent.framework.ui.VUIMulti;

public class RobotWorlExampleMultiUI extends Amas<WorldExampleMultiUI> {

	public RobotExampleMutliUI robotExampleMutliUI;
	VUIMulti vuiErrorDispersion;


	public RobotWorlExampleMultiUI(AmasMultiUIWindow window, VUIMulti vui, WorldExampleMultiUI env, RobotController robotController, RobotArmManager robotArmManager, int jointsNb) {
		super(window, vui, env, Scheduling.DEFAULT);

		if(!Configuration.commandLineMode){
			vuiErrorDispersion = new VUIMulti("Error Dispersion");
			amasMultiUIWindow.addTabbedPanel(vuiErrorDispersion.title, vuiErrorDispersion.getPanel());
		}

		robotExampleMutliUI = new RobotExampleMutliUI(amasMultiUIWindow, this, 0, 0, jointsNb, robotController, robotArmManager);


	}

	public VUIMulti getVuiErrorDispersion(){
		return vuiErrorDispersion;
	}

	public void cycleCommandLine() {
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
