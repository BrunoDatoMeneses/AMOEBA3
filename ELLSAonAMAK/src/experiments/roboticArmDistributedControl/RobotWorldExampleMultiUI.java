package experiments.roboticArmDistributedControl;

import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import javafx.scene.control.ToggleButton;

public class RobotWorldExampleMultiUI extends Amas<WorldExampleMultiUI> {

	public RobotExampleMutliUI robotExampleMutliUI;
	VUIMulti vuiErrorDispersion;
	AmakPlot plotsDistanceToGoal;
	public ToggleButton toggleRender;
	public boolean isRenderUpdate;


	public RobotWorldExampleMultiUI(AmasMultiUIWindow window, VUIMulti vui, WorldExampleMultiUI env, RobotController robotController, RobotArmManager robotArmManager, int jointsNb) {
		super(window, vui, env, Scheduling.DEFAULT);

		if(!Configuration.commandLineMode){
			vuiErrorDispersion = new VUIMulti("Error Dispersion");
			amasMultiUIWindow.addTabbedPanel(vuiErrorDispersion.title, vuiErrorDispersion.getPanel());
			plotsDistanceToGoal = new AmakPlot(amasMultiUIWindow, "Distance to Goal", AmakPlot.ChartType.LINE, "Cycle", "Distance");



		}

		if(!Configuration.commandLineMode){
			vuiErrorDispersion = new VUIMulti("Error Dispersion");
			amasMultiUIWindow.addTabbedPanel(vuiErrorDispersion.title, vuiErrorDispersion.getPanel());

			// update render button
			toggleRender = new ToggleButton("Allow Rendering");
			toggleRender.setOnAction(evt -> {
				isRenderUpdate = toggleRender.isSelected();

			});
			toggleRender.setSelected(isRenderUpdate);
			amasMultiUIWindow.addToolbar(toggleRender);
		}

		robotExampleMutliUI = new RobotExampleMutliUI(amasMultiUIWindow, this, 0, 0, jointsNb, robotController, robotArmManager);

		robotArmManager.setRobotWorldExampleMultiUI(this);



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
