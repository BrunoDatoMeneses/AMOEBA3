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
        AmoebaMultiUIWindow amoebaUITheta0 = new AmoebaMultiUIWindow("ELLSA Theta 0", amoebaVUITheta0, studiedSystemTheta0);
        AMOEBA amoebaTheta0 = new AMOEBA(amoebaUITheta0,  amoebaVUITheta0);
        amoebaTheta0.setStudiedSystem(studiedSystemTheta0);
        IBackupSystem backupSystem = new BackupSystem(amoebaTheta0);
        File file = new File("resources/"+PARAMS.configFile);
        backupSystem.load(file);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        amoebaTheta0.allowGraphicalScheduler(true);
        amoebaTheta0.setRenderUpdate(false);
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

        StudiedSystem studiedSystemTheta1 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        VUIMulti amoebaVUITheta1 = new VUIMulti("2D");
        AmoebaMultiUIWindow amoebaUITheta1 = new AmoebaMultiUIWindow("ELLSA Theta 1", amoebaVUITheta1, studiedSystemTheta1);
        AMOEBA amoebaTheta1 = new AMOEBA(amoebaUITheta1,  amoebaVUITheta1);
        amoebaTheta1.setStudiedSystem(studiedSystemTheta1);
        IBackupSystem backupSystem1 = new BackupSystem(amoebaTheta1);
        File file1 = new File("resources/"+PARAMS.configFile);
        backupSystem1.load(file1);

        //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

        amoebaTheta1.allowGraphicalScheduler(true);
        amoebaTheta1.setRenderUpdate(false);
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
        AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        for(int i = 0;i<jointsNb;i++){
            distances[i] = PARAMS.armBaseSize - (i*20);
        }

        AMOEBA amoebas[] = new AMOEBA[2];
        amoebas[0] = amoebaTheta0;
        amoebas[1] = amoebaTheta1;
        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, amoebas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);

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
