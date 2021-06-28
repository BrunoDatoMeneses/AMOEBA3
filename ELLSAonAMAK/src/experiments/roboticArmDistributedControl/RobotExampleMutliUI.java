package experiments.roboticArmDistributedControl;



import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.drawables.DrawableCircle;
import fr.irit.smac.amak.ui.drawables.DrawableLine;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import utils.Pair;

public class RobotExampleMutliUI extends Agent<RobotWorldExampleMultiUI, WorldExampleMultiUI> {



	public int jointsNumber;

	public double xStart;
	public double yStart;

	public boolean isUIStopped;

	private DrawableCircle circleBase;


	public Pair<Double,Double> starts[] ;
	public Pair<Double,Double> ends[] ;
	public double angles[];
	public double anglesBase[];
	public DrawableLine lines[];
	public DrawableLine goalLines[];
	public DrawableLine errorGoalLines[];
	public DrawableCircle circles[];
	public DrawableCircle goalCircle;
	public DrawableCircle subGoalCircle;
	public DrawableLine goalOrientationLine;

	public RobotArmManager robotArmManager;
	public RobotController robotController;

	RobotWorldExampleMultiUI robotWorldExampleMultiUI;

	/**
	 * Constructor of the ant
	 * 
	 * @param amas
	 *            the amas the ant belongs to
	 * @param startX
	 *            Initial X coordinate
	 * @param startY
	 *            Initial Y coordinate
	 */
	public RobotExampleMutliUI(AmasMultiUIWindow window, RobotWorldExampleMultiUI amas, double startX, double startY, int joints, RobotController robotCtrl, RobotArmManager robotArmMgr) {
		super(window, amas, startX, startY);

		robotWorldExampleMultiUI = amas;

		xStart = startX;
		yStart = startY;

		isUIStopped = false;

		jointsNumber = joints;
		double distances[] = new double[jointsNumber];
		for(int i = 0;i<jointsNumber;i++){
			distances[i] = 100;
		}


		robotArmManager = robotArmMgr;
		robotController = robotCtrl;

		starts = new Pair[jointsNumber];
		ends = new Pair[jointsNumber];
		angles = new double[jointsNumber];
		anglesBase = new double[jointsNumber];
		lines = new DrawableLine[jointsNumber];
		circles = new DrawableCircle[jointsNumber];
		goalLines = new DrawableLine[2];

		if(!Configuration.commandLineMode){
			goalOrientationLine = new DrawableLine(0.0,0.0,0.0,0.0);
		}

		errorGoalLines = new DrawableLine[2];





		Double xPos = 0.0;
		Double yPos = 0.0;

		for (int i = 0;i<jointsNumber;i++){
			/*angles[i] = Math.random() * Math.PI * 2 ;*/

			angles[i] = 0.0 ;
			anglesBase[i] = 0.0 ;

			starts[i] = new Pair<>(xPos,yPos);

			double[] position = robotArmManager.forwardKinematics(angles,i+1);

			/*if (i==0){
				xSum = 100*Math.cos(angles[i]);
				ySum = 100*Math.sin(angles[i]);
			}else{
				xSum = 100*Math.cos(angles[i])*Math.cos(angles[i-1]) - 100*Math.sin(angles[i])*Math.sin(angles[i-1]) + 100*Math.cos(angles[i-1]);
				ySum = 100*Math.cos(angles[i])*Math.sin(angles[i-1]) + 100*Math.sin(angles[i])*Math.cos(angles[i-1]) + 100*Math.sin(angles[i-1]);
			}*/

			xPos = position[0];
			yPos = position[1];
			ends[i] = new Pair<>(xPos,yPos);

		}






		if(!Configuration.commandLineMode){
			circleBase = getAmas().getVUIMulti().createAndAddCircle(xStart, yStart,6);

			for (int i = 0;i<jointsNumber;i++){
				lines[i] = getAmas().getVUIMulti().createAndAddLine(starts[i].getA(), starts[i].getB(),ends[i].getA(),ends[i].getB());
				circles[i] = getAmas().getVUIMulti().createAndAddCircle(ends[i].getA(),ends[i].getB(),4);

			}

			goalCircle = getAmas().getVUIMulti().createAndAddCircle(0.0,0.0,5);
			subGoalCircle  = getAmas().getVUIMulti().createAndAddCircle(0.0,0.0,5);

			goalCircle.setColor(new Color(0.0,1.0,0.0,1.0));
			subGoalCircle.setColor(new Color(0.0,0.0,1.0,1.0));

			goalLines[0] =  getAmas().getVUIMulti().createAndAddLine(-10000.0, 0.0,10000.0,0.0);
			goalLines[1] =  getAmas().getVUIMulti().createAndAddLine(0.0, -10000.0,0.0,10000.0);
			goalOrientationLine = getAmas().getVUIMulti().createAndAddLine(0.0, 0.0,0.0,0.0);

			/*errorGoalLines[0] = getAmas().getVuiErrorDispersion().createAndAddLine(-10000.0, 0.0,10000.0,0.0);
			errorGoalLines[1] = getAmas().getVuiErrorDispersion().createAndAddLine(0.0, -10000.0,0.0,10000.0);*/


			getAmas().getVuiErrorDispersion().createAndAddCircle(0.0, 0.0,5.0);
		}




	}
	@Override
	public void onInitialization() {





	}

	@Override
	protected void onRenderingInitialization() {




	}

	/**
	 * Move in a random direction
	 */
	@Override
	protected void onDecideAndAct() {

		Pair<Pair<Double,Double>[],Pair<Double,Double>[]> startEndEnds = robotArmManager.decideAndAct(getAmas().getCycle(), anglesBase, angles);
		starts = startEndEnds.getA();
		ends = startEndEnds.getB();


		if (robotArmManager.learningCycle == PARAMS.nbLearningCycle && !isUIStopped){
			robotWorldExampleMultiUI.getScheduler().stop();
			isUIStopped = true;
		}

		if (robotArmManager.finished){
			robotWorldExampleMultiUI.getScheduler().stop();
		}



	}



	@Override
	public void onUpdateRender() {

		if(amas.isRenderUpdate){

			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{
					double[] goal = robotArmManager.getGoal();
					if(getAmas().getCycle()<robotArmManager.trainingCycles){
						if(ends[jointsNumber-1].getB()>0.0 || true){
							getAmas().getVUIMulti().createAndAddCircle(ends[jointsNumber-1].getA(), ends[jointsNumber-1].getB(),0.75);
						}

					}





					circleBase.move(xStart, yStart);



					for(int i = 0; i<jointsNumber; i++){
						lines[i].move(starts[i].getA(), starts[i].getB(),ends[i].getA(),ends[i].getB());
						circles[i].move(ends[i].getA(),ends[i].getB());
						if(robotArmManager.jointIndiceForRequests!=-1){
							if(robotArmManager.jointIndiceForRequests==i){
								circles[i].setColor(new Color(0.0,0.0,1.0,1.0));
							}else{
								circles[i].setColor(new Color(0.0,0.0,0.0,1.0));
							}
						}
					}


					goalCircle.move(goal[0], goal[1]);
					subGoalCircle.move(robotArmManager.xSubGoal, robotArmManager.ySubGoal);

					goalLines[0].move(goal[0]-10000.0,goal[1],goal[0]+10000.0,goal[1]);
					goalLines[1].move(goal[0],goal[1]-10000.0,goal[0],goal[1]+10000.0);

					goalOrientationLine.move(goal[0], goal[1], goal[0]+20*Math.cos(robotArmManager.angleGoal), goal[1]+20*Math.sin(robotArmManager.angleGoal));





					if(robotArmManager.plotRequestError){
						getAmas().getVuiErrorDispersion().createAndAddCircle(ends[jointsNumber-1].getA()-goal[0], ends[jointsNumber-1].getB()-goal[1],0.25);
						robotArmManager.plotRequestError = false;


					}

				/*errorGoalLines[0].delete();
				errorGoalLines[1].delete();
				errorGoalLines[0] = getAmas().getVuiErrorDispersion().createAndAddLine(-10000.0, 0.0,10000.0,0.0);
				errorGoalLines[1] = getAmas().getVuiErrorDispersion().createAndAddLine(0.0, -10000.0,0.0,10000.0);*/

				}
			});

		}



	}
}