package experiments.roboticArm.launchers;


import experiments.managers.F_N_Manager;
import experiments.roboticArm.simulation.RobotArmManager;
import experiments.roboticArm.simulation.RobotController;
import experiments.roboticArm.simulation.RobotWorlExampleMultiUI;
import experiments.roboticArm.simulation.WorldExampleMultiUI;
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

public class RobotLaunchExampleWithUI extends Application{


	public static void main (String[] args) {

        TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;
		Application.launch(args);
	
	}

	@Override
	public void start(Stage primaryStage) throws Exception {


        Configuration.multiUI=true;
        Configuration.commandLineMode = false;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = true;
        Configuration.plotMilliSecondsUpdate = 20000;



        StudiedSystem studiedSystemTheta0 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        VUIMulti amoebaVUITheta0 = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUITheta0 = new EllsaMultiUIWindow("Multi-Agent System", amoebaVUITheta0, studiedSystemTheta0);
        ELLSA ellsaTheta0 = new ELLSA(amoebaUITheta0,  amoebaVUITheta0);
        ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
        File file = new File("resources/"+PARAMS.configFile);
        backupSystem.load(file);


        ellsaTheta0.allowGraphicalScheduler(true);
        ellsaTheta0.setRenderUpdate(false);
        ellsaTheta0.data.nameID = "ellsaTheta0";
        ellsaTheta0.data.learningSpeed = PARAMS.learningSpeed;
        ellsaTheta0.data.numberOfPointsForRegression = PARAMS.regressionPoints;
        ellsaTheta0.data.isActiveLearning = PARAMS.setActiveLearning;
        ellsaTheta0.data.isSelfLearning = PARAMS.setSelfLearning;
        ellsaTheta0.data.isAutonomousMode = PARAMS.setAutonomousMode;
        ellsaTheta0.data.isConflictDetection = PARAMS.setConflictDetection;
        ellsaTheta0.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsaTheta0.data.isVoidDetection2 = PARAMS.setVoidDetection2;
        ellsaTheta0.data.isSubVoidDetection = PARAMS.setSubVoidDetection;
        ellsaTheta0.data.isConflictResolution = PARAMS.setConflictResolution;
        ellsaTheta0.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsaTheta0.data.isFrontierRequest = PARAMS.setFrontierRequest;
        ellsaTheta0.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
        ellsaTheta0.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

        ellsaTheta0.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        ellsaTheta0.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsaTheta0.data.isDream = PARAMS.setDream;
        ellsaTheta0.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsaTheta0.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        ellsaTheta0.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
        ellsaTheta0.data.neighborhoodMultiplicator = PARAMS.neighborhoodMultiplicator;
        ellsaTheta0.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
        ellsaTheta0.getEnvironment().minLevel = TRACE_LEVEL.OFF;


        ellsaTheta0.setSubPercepts(PARAMS.subPercepts);

        int jointsNb = PARAMS.nbJoints;
        AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm Simulation");
        WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        double incLength = PARAMS.extendedArmLength /jointsNb;

        for(int i = 0;i<jointsNb;i++){
            distances[i] = incLength;
        }


        ELLSA ellsas[] = new ELLSA[2];
        ellsas[0] = ellsaTheta0;
        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
        robotArmManager.maxError = PARAMS.extendedArmLength *2;

        RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(window, vui, env, robotController, robotArmManager, jointsNb);


			
	}

	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
