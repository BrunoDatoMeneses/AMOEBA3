package experiments.roboticArmCentralizedControl;


import experiments.mathematicalModels.Model_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.RAND_REPEATABLE;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;

public class RobotLaunchExampleMultiUI extends Application{



	public static void main (String[] args) {

        TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;
		Application.launch(args);
		
	
	}

	@Override
	public void start(Stage primaryStage) throws Exception {


        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = false;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = true;
        Configuration.plotMilliSecondsUpdate = 20000;
        RAND_REPEATABLE.setSeed(0);



        StudiedSystem studiedSystemTheta0 = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
        VUIMulti amoebaVUITheta0 = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUITheta0 = new EllsaMultiUIWindow("ELLSA Theta 0", amoebaVUITheta0, studiedSystemTheta0);
        ELLSA ellsaTheta0 = new ELLSA(amoebaUITheta0,  amoebaVUITheta0);
        ellsaTheta0.getEnvironment().setSeed(0);
        ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
        File file = new File("resources/"+ PARAMS.configFile);
        backupSystem.load(file);

        //ellsaTheta0.saver = new SaveHelperImpl(ellsaTheta0, amoebaUITheta0);

        ellsaTheta0.allowGraphicalScheduler(true);
        ellsaTheta0.setRenderUpdate(false);
        ellsaTheta0.data.nameID = "ellsaTheta0";

        ellsaTheta0.getEnvironment().setMappingErrorAllowed(PARAMS.validityRangesPrecision);
        ellsaTheta0.data.PARAM_modelErrorMargin = PARAMS.modelErrorMargin;
        ellsaTheta0.data.PARAM_bootstrapCycle = PARAMS.setbootstrapCycle;
        ellsaTheta0.data.PARAM_exogenousLearningWeight = PARAMS.exogenousLearningWeight;
        ellsaTheta0.data.PARAM_endogenousLearningWeight = PARAMS.endogenousLearningWeight;

        ellsaTheta0.data.PARAM_neighborhoodRadiusCoefficient = PARAMS.neighborhoodRadiusCoefficient;
        ellsaTheta0.data.PARAM_influenceRadiusCoefficient = PARAMS.influenceRadiusCoefficient;
        ellsaTheta0.data.PARAM_maxRangeRadiusCoefficient = PARAMS.maxRangeRadiusCoefficient;
        ellsaTheta0.data.PARAM_rangeSimilarityCoefficient = PARAMS.rangeSimilarityCoefficient;
        ellsaTheta0.data.PARAM_minimumRangeCoefficient = PARAMS.minimumRangeCoefficient;

        ellsaTheta0.data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsaTheta0.data.PARAM_creationNeighborNumberForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        ellsaTheta0.data.PARAM_perceptionsGenerationCoefficient = PARAMS.perceptionsGenerationCoefficient;
        ellsaTheta0.data.PARAM_modelSimilarityThreshold = PARAMS.modelSimilarityThreshold;

        ellsaTheta0.data.PARAM_LEARNING_WEIGHT_ACCURACY = PARAMS.LEARNING_WEIGHT_ACCURACY;
        ellsaTheta0.data.PARAM_LEARNING_WEIGHT_PROXIMITY = PARAMS.LEARNING_WEIGHT_PROXIMITY;
        ellsaTheta0.data.PARAM_LEARNING_WEIGHT_EXPERIENCE = PARAMS.LEARNING_WEIGHT_EXPERIENCE;
        ellsaTheta0.data.PARAM_LEARNING_WEIGHT_GENERALIZATION = PARAMS.LEARNING_WEIGHT_GENERALIZATION;

        ellsaTheta0.data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = PARAMS.EXPLOITATION_WEIGHT_PROXIMITY;
        ellsaTheta0.data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE;
        ellsaTheta0.data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION;


        ellsaTheta0.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
        ellsaTheta0.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;

        ellsaTheta0.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
        ellsaTheta0.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsaTheta0.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection;
        ellsaTheta0.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
        ellsaTheta0.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
        ellsaTheta0.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsaTheta0.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
        ellsaTheta0.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
        ellsaTheta0.data.PARAM_NCS_isFusionResolution = PARAMS.setFusionResolution;
        ellsaTheta0.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setRestructureResolution;

        ellsaTheta0.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


        ellsaTheta0.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        ellsaTheta0.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsaTheta0.data.PARAM_isDream = PARAMS.setDream;
        ellsaTheta0.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;


        ellsaTheta0.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;

        ellsaTheta0.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS.isAllContextSearchAllowedForLearning;
        ellsaTheta0.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS.isAllContextSearchAllowedForExploitation;

        ellsaTheta0.data.PARAM_probabilityOfRangeAmbiguity = PARAMS.probabilityOfRangeAmbiguity;



        ellsaTheta0.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;



        ellsaTheta0.setSubPercepts(PARAMS.subPercepts);



        StudiedSystem studiedSystemTheta1 = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
        VUIMulti amoebaVUITheta1 = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUITheta1 = new EllsaMultiUIWindow("ELLSA Theta 1", amoebaVUITheta1, studiedSystemTheta1);
        ELLSA ellsaTheta1 = new ELLSA(amoebaUITheta1,  amoebaVUITheta1);
        ellsaTheta1.getEnvironment().setSeed(0);
        ellsaTheta1.setStudiedSystem(studiedSystemTheta1);
        IBackupSystem backupSystem1 = new BackupSystem(ellsaTheta1);
        File file1 = new File("resources/"+ PARAMS.configFile);
        backupSystem1.load(file1);



        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        ellsaTheta1.allowGraphicalScheduler(true);
        ellsaTheta1.setRenderUpdate(false);
        ellsaTheta1.data.nameID = "ellsaTheta1";


        ellsaTheta1.getEnvironment().setMappingErrorAllowed(PARAMS.validityRangesPrecision);
        ellsaTheta1.data.PARAM_modelErrorMargin = PARAMS.modelErrorMargin;
        ellsaTheta1.data.PARAM_bootstrapCycle = PARAMS.setbootstrapCycle;
        ellsaTheta1.data.PARAM_exogenousLearningWeight = PARAMS.exogenousLearningWeight;
        ellsaTheta1.data.PARAM_endogenousLearningWeight = PARAMS.endogenousLearningWeight;

        ellsaTheta1.data.PARAM_neighborhoodRadiusCoefficient = PARAMS.neighborhoodRadiusCoefficient;
        ellsaTheta1.data.PARAM_influenceRadiusCoefficient = PARAMS.influenceRadiusCoefficient;
        ellsaTheta1.data.PARAM_maxRangeRadiusCoefficient = PARAMS.maxRangeRadiusCoefficient;
        ellsaTheta1.data.PARAM_rangeSimilarityCoefficient = PARAMS.rangeSimilarityCoefficient;
        ellsaTheta1.data.PARAM_minimumRangeCoefficient = PARAMS.minimumRangeCoefficient;

        ellsaTheta1.data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsaTheta1.data.PARAM_creationNeighborNumberForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        ellsaTheta1.data.PARAM_perceptionsGenerationCoefficient = PARAMS.perceptionsGenerationCoefficient;
        ellsaTheta1.data.PARAM_modelSimilarityThreshold = PARAMS.modelSimilarityThreshold;

        ellsaTheta1.data.PARAM_LEARNING_WEIGHT_ACCURACY = PARAMS.LEARNING_WEIGHT_ACCURACY;
        ellsaTheta1.data.PARAM_LEARNING_WEIGHT_PROXIMITY = PARAMS.LEARNING_WEIGHT_PROXIMITY;
        ellsaTheta1.data.PARAM_LEARNING_WEIGHT_EXPERIENCE = PARAMS.LEARNING_WEIGHT_EXPERIENCE;
        ellsaTheta1.data.PARAM_LEARNING_WEIGHT_GENERALIZATION = PARAMS.LEARNING_WEIGHT_GENERALIZATION;

        ellsaTheta1.data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = PARAMS.EXPLOITATION_WEIGHT_PROXIMITY;
        ellsaTheta1.data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE;
        ellsaTheta1.data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION;


        ellsaTheta1.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
        ellsaTheta1.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;

        ellsaTheta1.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
        ellsaTheta1.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsaTheta1.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection;
        ellsaTheta1.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
        ellsaTheta1.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
        ellsaTheta1.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsaTheta1.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
        ellsaTheta1.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
        ellsaTheta1.data.PARAM_NCS_isFusionResolution = PARAMS.setFusionResolution;
        ellsaTheta1.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setRestructureResolution;

        ellsaTheta1.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


        ellsaTheta1.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        ellsaTheta1.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsaTheta1.data.PARAM_isDream = PARAMS.setDream;
        ellsaTheta1.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;


        ellsaTheta1.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;

        ellsaTheta1.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS.isAllContextSearchAllowedForLearning;
        ellsaTheta1.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS.isAllContextSearchAllowedForExploitation;

        ellsaTheta1.data.PARAM_probabilityOfRangeAmbiguity = PARAMS.probabilityOfRangeAmbiguity;



        ellsaTheta1.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;



        ellsaTheta1.setSubPercepts(PARAMS.subPercepts);



        ellsaTheta1.setSubPercepts(PARAMS.subPercepts);
        ellsaTheta0.setSubPercepts(PARAMS.subPercepts);

        int jointsNb = PARAMS.nbJoints;
        AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        double incLength = PARAMS.armBaseSize/jointsNb;

        for(int i = 0;i<jointsNb;i++){
            distances[i] = incLength;
        }


        ELLSA ellsas[] = new ELLSA[2];
        ellsas[0] = ellsaTheta0;
        ellsas[1] = ellsaTheta1;
        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbLearningCycle, PARAMS.nbExploitationCycle);
        robotArmManager.maxError = PARAMS.armBaseSize*2;

        RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(window, vui, env, robotController, robotArmManager, jointsNb);



			
	}
	
	public void startTask(RobotWorlExampleMultiUI amas, long wait, int cycles)
    {
        // Create a Runnable
        Runnable task = new Runnable()
        {
            public void run()
            {
                runTask(amas, wait, cycles);
            }
        };
 
        // Run the task in a background thread
        Thread backgroundThread = new Thread(task);
        // Terminate the running thread if the application exits
        backgroundThread.setDaemon(true);
        // Start the thread
        backgroundThread.start();
    }
	
	public void runTask(RobotWorlExampleMultiUI amas, long wait, int cycles)
    {
        for(int i = 0; i < cycles; i++) 
        {
            try
            {
                // Get the Status
                final String status = "Processing " + i + " of " + cycles;
                 
                // Update the Label on the JavaFx Application Thread        
                Platform.runLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                    	amas.cycle();
                    	System.out.println(status);
                    }
                });
         
                Thread.sleep(wait);
            }
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }
        }
    }   

	
	@Override
	public void stop() throws Exception {
		super.stop();
		System.exit(0);
	}
}
