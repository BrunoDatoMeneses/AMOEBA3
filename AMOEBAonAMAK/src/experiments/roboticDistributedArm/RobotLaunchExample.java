package experiments.roboticDistributedArm;


import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
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

        ELLSA[] ellsas;
        StudiedSystem[] studiedSystems;
    ellsas = new ELLSA[PARAMS.nbJoints];
    studiedSystems = new StudiedSystem[PARAMS.nbJoints];

            for(int i=0;i<PARAMS.nbJoints;i++){


                    studiedSystems[i] = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
                    ellsas[i] = new ELLSA(null,  null);
                    ellsas[i].setStudiedSystem(studiedSystems[i]);
                    IBackupSystem backupSystem = new BackupSystem(ellsas[i]);
                    File file;
                    if(i==0) file = new File("resources/1jointRobotOrigin2DimensionsLauncher.xml");
                    else file = new File("resources/1jointRobot4DimensionsLauncher.xml");
                    //else file = new File("resources/1jointRobot3DimensionsLauncher.xml");

                    backupSystem.load(file);

                    //ellsaTheta0.saver = new SaveHelperImpl(ellsaTheta0, amoebaUITheta0);

                    ellsas[i].allowGraphicalScheduler(true);
                    ellsas[i].setRenderUpdate(false);
                    ellsas[i].data.nameID = "ellsaTheta"+i;
                    ellsas[i].data.learningSpeed = PARAMS.learningSpeed;
                    ellsas[i].data.numberOfPointsForRegression = PARAMS.regressionPoints;
                    ellsas[i].data.isActiveLearning = PARAMS.setActiveLearning;
                    ellsas[i].data.isSelfLearning = PARAMS.setSelfLearning;
                    ellsas[i].data.isAutonomousMode = PARAMS.setAutonomousMode;
                    ellsas[i].data.isConflictDetection = PARAMS.setConflictDetection;
                    ellsas[i].data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
                    ellsas[i].data.isVoidDetection2 = PARAMS.setVoidDetection2;
                    ellsas[i].data.isSubVoidDetection = PARAMS.setSubVoidDetection;
                    ellsas[i].data.isConflictResolution = PARAMS.setConflictResolution;
                    ellsas[i].data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
                    ellsas[i].data.isFrontierRequest = PARAMS.setFrontierRequest;
                    ellsas[i].data.isSelfModelRequest = PARAMS.setSelfModelRequest;
                    ellsas[i].data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

                    ellsas[i].data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
                    ellsas[i].data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
                    ellsas[i].data.isDream = PARAMS.setDream;
                    ellsas[i].data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
                    ellsas[i].data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

                    ellsas[i].getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
                    ellsas[i].data.initRegressionPerformance = PARAMS.setRegressionPerformance;
                    ellsas[i].getEnvironment().minLevel = TRACE_LEVEL.OFF;
                    ellsas[i].setSubPercepts(PARAMS.subPercepts);
            }

        int jointsNb = PARAMS.nbJoints;
        //AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        //WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        //VUIMulti vui = new VUIMulti("Robot");


            double distances[] = new double[jointsNb];
            double incLength = PARAMS.armBaseSize/jointsNb;

            for(int i = 0;i<jointsNb;i++){
                    distances[i] = incLength;
            }




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
