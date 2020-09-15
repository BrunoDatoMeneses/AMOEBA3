package experiments.roboticDistributedArm;


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


        ellsas = new ELLSA[1];
        studiedSystems = new StudiedSystem[PARAMS.nbJoints];
        vuiMultis = new VUIMulti[PARAMS.nbJoints];
        ellsaMultiUIWindows = new EllsaMultiUIWindow[PARAMS.nbJoints];



        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = false;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = true;
        Configuration.plotMilliSecondsUpdate = 20000;

        for(int i=0;i<1;i++){


            studiedSystems[i] = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
            vuiMultis[i] = new VUIMulti("2D");
            ellsaMultiUIWindows[i] = new EllsaMultiUIWindow("ELLSA Theta "+i, vuiMultis[i], studiedSystems[i]);
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
        AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[jointsNb];
        double incLength = PARAMS.armBaseSize/jointsNb;

        double sum = 0.0;
        for(int i = 0;i<jointsNb;i++){
            distances[i] = incLength-(i*5);
            sum += distances[i];
        }



        RobotController robotController = new RobotController(jointsNb);
        RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
        robotArmManager.maxError = sum*2;

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
