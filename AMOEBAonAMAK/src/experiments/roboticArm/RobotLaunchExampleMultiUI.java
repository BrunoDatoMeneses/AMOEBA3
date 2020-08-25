package experiments.roboticArm;


import experiments.nDimensionsLaunchers.F_N_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.AmasMultiUIWindow;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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



        StudiedSystem studiedSystemTheta0 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        VUIMulti amoebaVUITheta0 = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUITheta0 = new EllsaMultiUIWindow("ELLSA Theta 0", amoebaVUITheta0, studiedSystemTheta0);
        ELLSA ellsaTheta0 = new ELLSA(amoebaUITheta0,  amoebaVUITheta0);
        ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
        File file = new File("resources/"+PARAMS.configFile);
        backupSystem.load(file);

        //ellsaTheta0.saver = new SaveHelperImpl(ellsaTheta0, amoebaUITheta0);

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



        StudiedSystem studiedSystemTheta1 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        VUIMulti amoebaVUITheta1 = new VUIMulti("2D");
        EllsaMultiUIWindow amoebaUITheta1 = new EllsaMultiUIWindow("ELLSA Theta 1", amoebaVUITheta1, studiedSystemTheta1);
        ELLSA ellsaTheta1 = new ELLSA(amoebaUITheta1,  amoebaVUITheta1);
        ellsaTheta1.setStudiedSystem(studiedSystemTheta1);
        IBackupSystem backupSystem1 = new BackupSystem(ellsaTheta1);
        File file1 = new File("resources/"+PARAMS.configFile);
        backupSystem1.load(file1);



        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        ellsaTheta1.allowGraphicalScheduler(true);
        ellsaTheta1.setRenderUpdate(false);
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
        ellsaTheta1.getEnvironment().minLevel = TRACE_LEVEL.OFF;



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
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
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
