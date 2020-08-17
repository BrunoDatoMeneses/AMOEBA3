package experiments.roboticDistributedArm;


import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;

public class RobotLaunchExample{



	public static void main (String[] args)  {

	    TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;
		
		start();
		
	
	}


	public static void start() {


        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;



        StudiedSystem studiedSystemTheta0 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        ELLSA ellsaTheta0 = new ELLSA(null,  null);
        ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
        File file = new File("resources/"+ PARAMS.configFile);
        backupSystem.load(file);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        ellsaTheta0.allowGraphicalScheduler(false);
        ellsaTheta0.setRenderUpdate(false);

        StudiedSystem studiedSystemTheta1 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        ELLSA ellsaTheta1 = new ELLSA(null,  null);
        ellsaTheta1.setStudiedSystem(studiedSystemTheta1);
        IBackupSystem backupSystem1 = new BackupSystem(ellsaTheta1);
        File file1 = new File("resources/"+ PARAMS.configFile);
        backupSystem1.load(file1);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        ellsaTheta1.allowGraphicalScheduler(false);
        ellsaTheta1.setRenderUpdate(false);

        ellsaTheta0.data.nameID = "ellsaTheta0";
        ellsaTheta0.data.learningSpeed = PARAMS.learningSpeed;
        ellsaTheta0.data.numberOfPointsForRegression = PARAMS.regressionPoints;
        ellsaTheta0.data.isActiveLearning = PARAMS.setActiveLearning;
        ellsaTheta0.data.isSelfLearning = PARAMS.setSelfLearning;
        ellsaTheta0.data.isAutonomousMode = PARAMS.setAutonomousMode;
        ellsaTheta0.data.isConflictDetection = PARAMS.setConflictDetection;
        ellsaTheta0.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsaTheta0.data.isVoidDetection2 = PARAMS.setVoidDetection2;
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
        ellsaTheta0.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
        ellsaTheta0.getEnvironment().minLevel = TRACE_LEVEL.ERROR;



        ellsaTheta1.data.nameID = "ellsaTheta1";
        ellsaTheta1.data.learningSpeed = PARAMS.learningSpeed;
        ellsaTheta1.data.numberOfPointsForRegression = PARAMS.regressionPoints;
        ellsaTheta1.data.isActiveLearning = PARAMS.setActiveLearning;
        ellsaTheta1.data.isSelfLearning = PARAMS.setSelfLearning;
        ellsaTheta1.data.isAutonomousMode = PARAMS.setAutonomousMode;
        ellsaTheta1.data.isConflictDetection = PARAMS.setConflictDetection;
        ellsaTheta1.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsaTheta1.data.isVoidDetection2 = PARAMS.setVoidDetection2;
        ellsaTheta1.data.isConflictResolution = PARAMS.setConflictResolution;
        ellsaTheta1.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsaTheta1.data.isFrontierRequest = PARAMS.setFrontierRequest;
        ellsaTheta1.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
        ellsaTheta1.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

        ellsaTheta1.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        ellsaTheta1.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsaTheta1.data.isDream = PARAMS.setDream;
        ellsaTheta1.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsaTheta1.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        ellsaTheta1.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
        ellsaTheta1.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
        ellsaTheta1.getEnvironment().minLevel = TRACE_LEVEL.ERROR;


        ellsaTheta1.setSubPercepts(PARAMS.subPercepts);
        ellsaTheta0.setSubPercepts(PARAMS.subPercepts);

        int jointsNb = PARAMS.nbJoints;
        //AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        //WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        //VUIMulti vui = new VUIMulti("Robot");


            double distances[] = new double[jointsNb];
            double incLength = PARAMS.armBaseSize/jointsNb;

            for(int i = 0;i<jointsNb;i++){
                    distances[i] = incLength;
            }



        ELLSA ellsas[] = new ELLSA[2];
        ellsas[0] = ellsaTheta0;
        ellsas[1] = ellsaTheta1;
        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
        robotArmManager.maxError = PARAMS.armBaseSize*2;
        RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(null, null, null, robotController, robotArmManager, jointsNb);

        while(!robotArmManager.finished){
            robot.cycleCommandLine();
        }

        TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.finished);
        TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.averageError.getAsDouble() + " [ " + Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size()) + " ]      -    " + robotArmManager.goalErrors);

			
	}
	



	

}
