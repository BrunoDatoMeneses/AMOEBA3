package experiments.roboticArm;


import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;

public class RobotLaunchExample{



	public static void main (String[] args)  {

	    TRACE.minLevel = TRACE_LEVEL.ERROR;
		
		start();
		
	
	}


	public static void start() {


        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;



        StudiedSystem studiedSystemTheta0 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        AMOEBA amoebaTheta0 = new AMOEBA(null,  null);
        amoebaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(amoebaTheta0);
        File file = new File("resources/"+PARAMS.configFile);
        backupSystem.load(file);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        amoebaTheta0.allowGraphicalScheduler(false);
        amoebaTheta0.setRenderUpdate(false);

        StudiedSystem studiedSystemTheta1 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        AMOEBA amoebaTheta1 = new AMOEBA(null,  null);
        amoebaTheta1.setStudiedSystem(studiedSystemTheta1);
        IBackupSystem backupSystem1 = new BackupSystem(amoebaTheta1);
        File file1 = new File("resources/"+PARAMS.configFile);
        backupSystem1.load(file1);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        amoebaTheta1.allowGraphicalScheduler(false);
        amoebaTheta1.setRenderUpdate(false);

        amoebaTheta0.data.nameID = "ellsaTheta0";
        amoebaTheta0.data.learningSpeed = PARAMS.learningSpeed;
        amoebaTheta0.data.numberOfPointsForRegression = PARAMS.regressionPoints;
        amoebaTheta0.data.isActiveLearning = PARAMS.setActiveLearning;
        amoebaTheta0.data.isSelfLearning = PARAMS.setSelfLearning;
        amoebaTheta0.data.isAutonomousMode = PARAMS.setAutonomousMode;
        amoebaTheta0.data.isConflictDetection = PARAMS.setConflictDetection;
        amoebaTheta0.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        amoebaTheta0.data.isVoidDetection2 = PARAMS.setVoidDetection2;
        amoebaTheta0.data.isConflictResolution = PARAMS.setConflictResolution;
        amoebaTheta0.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        amoebaTheta0.data.isFrontierRequest = PARAMS.setFrontierRequest;
        amoebaTheta0.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
        amoebaTheta0.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

        amoebaTheta0.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        amoebaTheta0.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        amoebaTheta0.data.isDream = PARAMS.setDream;
        amoebaTheta0.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        amoebaTheta0.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        amoebaTheta0.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
        amoebaTheta0.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
        World.minLevel = PARAMS.traceLevel;


        amoebaTheta1.data.nameID = "ellsaTheta1";
        amoebaTheta1.data.learningSpeed = PARAMS.learningSpeed;
        amoebaTheta1.data.numberOfPointsForRegression = PARAMS.regressionPoints;
        amoebaTheta1.data.isActiveLearning = PARAMS.setActiveLearning;
        amoebaTheta1.data.isSelfLearning = PARAMS.setSelfLearning;
        amoebaTheta1.data.isAutonomousMode = PARAMS.setAutonomousMode;
        amoebaTheta1.data.isConflictDetection = PARAMS.setConflictDetection;
        amoebaTheta1.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        amoebaTheta1.data.isVoidDetection2 = PARAMS.setVoidDetection2;
        amoebaTheta1.data.isConflictResolution = PARAMS.setConflictResolution;
        amoebaTheta1.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        amoebaTheta1.data.isFrontierRequest = PARAMS.setFrontierRequest;
        amoebaTheta1.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
        amoebaTheta1.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

        amoebaTheta1.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        amoebaTheta1.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        amoebaTheta1.data.isDream = PARAMS.setDream;
        amoebaTheta1.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        amoebaTheta1.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        amoebaTheta1.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
        amoebaTheta1.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
        World.minLevel = PARAMS.traceLevel;


        int jointsNb = PARAMS.nbJoints;
        //AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        //WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        //VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        for(int i = 0;i<jointsNb;i++){
            distances[i] = PARAMS.armBaseSize - (i*20);
        }

        AMOEBA amoebas[] = new AMOEBA[2];
        amoebas[0] = amoebaTheta0;
        amoebas[1] = amoebaTheta1;
        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, amoebas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);

        RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(null, null, null, robotController, robotArmManager, jointsNb);

        while(!robotArmManager.finished){
            robot.cycle();
        }

        TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.finished);
        TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.averageError.getAsDouble() + " [ " + Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size()) + " ]      -    " + robotArmManager.goalErrors);

			
	}
	



	

}
