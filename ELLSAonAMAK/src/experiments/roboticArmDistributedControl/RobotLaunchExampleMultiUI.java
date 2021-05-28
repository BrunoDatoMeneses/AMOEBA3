package experiments.roboticArmDistributedControl;


import experiments.mathematicalModels.Model_Manager;
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


            studiedSystems[i] = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
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
            ellsas[i].getEnvironment().setMappingErrorAllowed(experiments.roboticArmDistributedControl.PARAMS.validityRangesPrecision);
            ellsas[i].data.PARAM_modelErrorMargin = experiments.roboticArmDistributedControl.PARAMS.modelErrorMargin;
            ellsas[i].data.PARAM_bootstrapCycle = experiments.roboticArmDistributedControl.PARAMS.setbootstrapCycle;
            ellsas[i].data.PARAM_exogenousLearningWeight = experiments.roboticArmDistributedControl.PARAMS.exogenousLearningWeight;
            ellsas[i].data.PARAM_endogenousLearningWeight = experiments.roboticArmDistributedControl.PARAMS.endogenousLearningWeight;

            ellsas[i].data.PARAM_neighborhoodRadiusCoefficient = experiments.roboticArmDistributedControl.PARAMS.neighborhoodRadiusCoefficient;
            ellsas[i].data.PARAM_influenceRadiusCoefficient = experiments.roboticArmDistributedControl.PARAMS.influenceRadiusCoefficient;
            ellsas[i].data.PARAM_maxRangeRadiusCoefficient = experiments.roboticArmDistributedControl.PARAMS.maxRangeRadiusCoefficient;
            ellsas[i].data.PARAM_rangeSimilarityCoefficient = experiments.roboticArmDistributedControl.PARAMS.rangeSimilarityCoefficient;
            ellsas[i].data.PARAM_minimumRangeCoefficient = experiments.roboticArmDistributedControl.PARAMS.minimumRangeCoefficient;

            ellsas[i].data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = experiments.roboticArmDistributedControl.PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
            ellsas[i].data.PARAM_creationNeighborNumberForContexCreationWithouOracle = experiments.roboticArmDistributedControl.PARAMS.nbOfNeighborForContexCreationWithouOracle;

            ellsas[i].data.PARAM_perceptionsGenerationCoefficient = experiments.roboticArmDistributedControl.PARAMS.perceptionsGenerationCoefficient;
            ellsas[i].data.PARAM_modelSimilarityThreshold = experiments.roboticArmDistributedControl.PARAMS.modelSimilarityThreshold;

            ellsas[i].data.PARAM_LEARNING_WEIGHT_ACCURACY = experiments.roboticArmDistributedControl.PARAMS.LEARNING_WEIGHT_ACCURACY;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_PROXIMITY = experiments.roboticArmDistributedControl.PARAMS.LEARNING_WEIGHT_PROXIMITY;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_EXPERIENCE = experiments.roboticArmDistributedControl.PARAMS.LEARNING_WEIGHT_EXPERIENCE;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_GENERALIZATION = experiments.roboticArmDistributedControl.PARAMS.LEARNING_WEIGHT_GENERALIZATION;

            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = experiments.roboticArmDistributedControl.PARAMS.EXPLOITATION_WEIGHT_PROXIMITY;
            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = experiments.roboticArmDistributedControl.PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE;
            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = experiments.roboticArmDistributedControl.PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION;


            ellsas[i].data.PARAM_isActiveLearning = experiments.roboticArmDistributedControl.PARAMS.setActiveLearning;
            ellsas[i].data.PARAM_isSelfLearning = experiments.roboticArmDistributedControl.PARAMS.setSelfLearning;

            ellsas[i].data.PARAM_NCS_isConflictDetection = experiments.roboticArmDistributedControl.PARAMS.setConflictDetection;
            ellsas[i].data.PARAM_NCS_isConcurrenceDetection = experiments.roboticArmDistributedControl.PARAMS.setConcurrenceDetection;
            ellsas[i].data.PARAM_NCS_isVoidDetection = experiments.roboticArmDistributedControl.PARAMS.setVoidDetection;
            ellsas[i].data.PARAM_NCS_isSubVoidDetection = experiments.roboticArmDistributedControl.PARAMS.setSubVoidDetection;
            ellsas[i].data.PARAM_NCS_isConflictResolution = experiments.roboticArmDistributedControl.PARAMS.setConflictResolution;
            ellsas[i].data.PARAM_NCS_isConcurrenceResolution = experiments.roboticArmDistributedControl.PARAMS.setConcurrenceResolution;
            ellsas[i].data.PARAM_NCS_isFrontierRequest = experiments.roboticArmDistributedControl.PARAMS.setFrontierRequest;
            ellsas[i].data.PARAM_NCS_isSelfModelRequest = experiments.roboticArmDistributedControl.PARAMS.setSelfModelRequest;
            ellsas[i].data.PARAM_NCS_isFusionResolution = experiments.roboticArmDistributedControl.PARAMS.setFusionResolution;
            ellsas[i].data.PARAM_NCS_isRetrucstureResolution = experiments.roboticArmDistributedControl.PARAMS.setRestructureResolution;

            ellsas[i].data.PARAM_NCS_isCreationWithNeighbor = experiments.roboticArmDistributedControl.PARAMS.setisCreationWithNeighbor;


            ellsas[i].data.PARAM_isLearnFromNeighbors = experiments.roboticArmDistributedControl.PARAMS.setLearnFromNeighbors;
            ellsas[i].data.PARAM_nbOfNeighborForLearningFromNeighbors = experiments.roboticArmDistributedControl.PARAMS.nbOfNeighborForLearningFromNeighbors;
            ellsas[i].data.PARAM_isDream = experiments.roboticArmDistributedControl.PARAMS.setDream;
            ellsas[i].data.PARAM_DreamCycleLaunch = experiments.roboticArmDistributedControl.PARAMS.setDreamCycleLaunch;


            ellsas[i].data.PARAM_isAutonomousMode = experiments.roboticArmDistributedControl.PARAMS.setAutonomousMode;

            ellsas[i].data.PARAM_NCS_isAllContextSearchAllowedForLearning = experiments.roboticArmDistributedControl.PARAMS.isAllContextSearchAllowedForLearning;
            ellsas[i].data.PARAM_NCS_isAllContextSearchAllowedForExploitation = experiments.roboticArmDistributedControl.PARAMS.isAllContextSearchAllowedForExploitation;

            ellsas[i].data.PARAM_probabilityOfRangeAmbiguity = experiments.roboticArmDistributedControl.PARAMS.probabilityOfRangeAmbiguity;



            ellsas[i].getEnvironment().PARAM_minTraceLevel = experiments.roboticArmDistributedControl.PARAMS.traceLevel;



            ellsas[i].setSubPercepts(experiments.roboticArmDistributedControl.PARAMS.subPercepts);
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
