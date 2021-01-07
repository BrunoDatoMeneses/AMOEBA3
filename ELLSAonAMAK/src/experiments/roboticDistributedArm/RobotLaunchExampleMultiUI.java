package experiments.roboticDistributedArm;


import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;

public class RobotLaunchExampleMultiUI extends Application{

    ELLSA[] ellsas;
    StudiedSystem[] studiedSystems;
    VUIMulti[] vuiMultis;
    EllsaMultiUIWindow[] ellsaMultiUIWindows;


	public static void main (String[] args) {

        TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;
		Application.launch(args);
		
	
	}

	@Override
	public void start(Stage primaryStage) throws Exception {


        ellsas = new ELLSA[PARAMS.nbJoints];
        studiedSystems = new StudiedSystem[PARAMS.nbJoints];
        vuiMultis = new VUIMulti[PARAMS.nbJoints];
        ellsaMultiUIWindows = new EllsaMultiUIWindow[PARAMS.nbJoints];



        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = false;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = true;
        Configuration.plotMilliSecondsUpdate = 20000;

        for(int i=0;i<PARAMS.nbJoints;i++){


            studiedSystems[i] = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
            vuiMultis[i] = new VUIMulti("2D");
            ellsaMultiUIWindows[i] = new EllsaMultiUIWindow("ELLSA Joint "+i, vuiMultis[i], studiedSystems[i]);
            ellsas[i] = new ELLSA(ellsaMultiUIWindows[i],  vuiMultis[i]);
            ellsas[i].setStudiedSystem(studiedSystems[i]);
            IBackupSystem backupSystem = new BackupSystem(ellsas[i]);
            File file;
            /*if(i==0) file = new File("resources/1jointRobotOrigin2DimensionsLauncher.xml");
            else file = new File("resources/1jointRobot4DimensionsLauncher.xml");*/
            //else file = new File("resources/1jointRobot3DimensionsLauncher.xml");
            file = new File("resources/1jointRobotOrigin2DimensionsLauncher.xml");

            backupSystem.load(file);

            //ellsaTheta0.saver = new SaveHelperImpl(ellsaTheta0, amoebaUITheta0);

            ellsas[i].allowGraphicalScheduler(true);
            ellsas[i].setRenderUpdate(false);
            ellsas[i].data.nameID = "ellsaTheta"+i;
            ellsas[i].data.PARAM_learningSpeed = PARAMS.learningSpeed;
            ellsas[i].data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;
            ellsas[i].data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
            ellsas[i].data.PARAM_isSelfLearning = PARAMS.setSelfLearning;
            ellsas[i].data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;
            ellsas[i].data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
            ellsas[i].data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
            ellsas[i].data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection2;
            ellsas[i].data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
            ellsas[i].data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
            ellsas[i].data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
            ellsas[i].data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
            ellsas[i].data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
            ellsas[i].data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS.setCoopLearning;

            ellsas[i].data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
            ellsas[i].data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
            ellsas[i].data.PARAM_isDream = PARAMS.setDream;
            ellsas[i].data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
            ellsas[i].data.PARAM_nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

            ellsas[i].getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
            ellsas[i].data.PARAM_initRegressionPerformance = PARAMS.setRegressionPerformance;
            ellsas[i].getEnvironment().PARAM_minTraceLevel = TRACE_LEVEL.OFF;
            ellsas[i].setSubPercepts(PARAMS.subPercepts);
        }









        int jointsNb = PARAMS.nbJoints;
        AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm Simulation");
        WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        double incLength = PARAMS.armBaseSize/jointsNb;

        double sum = 0.0;
        for(int i = 0;i<jointsNb;i++){
            distances[i] = incLength-(i*(incLength/(jointsNb*2)));
            sum += distances[i];
        }



        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbLearningCycle, PARAMS.nbExploitationCycle);
        robotArmManager.maxError = sum*2;

        robotArmManager.propagationControlWaves = PARAMS.requestControlCycles;
        robotArmManager.isOrientationGoal = PARAMS.isOrientationGoal;
        RobotWorldExampleMultiUI robot = new RobotWorldExampleMultiUI(window, vui, env, robotController, robotArmManager, jointsNb);




	}


	


	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
