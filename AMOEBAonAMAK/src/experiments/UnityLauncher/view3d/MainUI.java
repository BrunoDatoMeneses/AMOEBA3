package experiments.UnityLauncher.view3d;

import agents.context.Context;
import experiments.FILE;
import experiments.UnityLauncher.Sender;
import experiments.UnityLauncher.SocketServer;
import experiments.nDimensionsLaunchers.F_N_Manager;

import fr.irit.smac.amak.Configuration;
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

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;

public class MainUI extends Application{

	ELLSA amoeba;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	EllsaMultiUIWindow amoebaUI;
	Sender sender;

	private SocketServer server;
	private static Socket socket = null;

	/* GUI or not */
	public static final boolean viewer = false;
	private String message = "";
	private Boolean shutDown;



	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage arg0) throws Exception {


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;

		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, null);



		/*startTask(100, PARAMS_UNITY.nbCycle);*/
		startTask(100, 0);



	}

	private void updateContextsOnUnity(ELLSA amoeba, Sender sender) {
		ArrayList<Context> spatiallyAlteredContexts = amoeba.getSpatiallyAlteredContextForUnityUI();
		ArrayList<Context> toKillContexts = amoeba.getToKillContextsForUnityUI();

		if(spatiallyAlteredContexts.size()>0) {

			sender.sendContexts(spatiallyAlteredContexts);

			while (!sender.acq("CTXTS", amoeba.getCycle())) {
				try
				{
					Thread.sleep(10);
				}
				catch(InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
		}

		if(toKillContexts.size()>0) {

			sender.sendContextsToKill(toKillContexts);

			while (!sender.acq("KILL", amoeba.getCycle())) {
				try
				{
					Thread.sleep(10);
				}
				catch(InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
		}
	}


	public void startTask(long wait, int cycles)
	{
		// Create a Runnable
		Runnable task = new Runnable()
		{
			public void run()
			{
				runTask(wait, cycles);
			}
		};

		// Run the task in a background thread
		Thread backgroundThread = new Thread(task);
		// Terminate the running thread if the application exits
		backgroundThread.setDaemon(true);
		// Start the thread
		backgroundThread.start();


	}

	public void runTask(long wait, int cycles)
	{

		try
		{

			// Update the Label on the JavaFx Application Thread
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{

					amoeba = new ELLSA(amoebaUI,  amoebaVUI);
					studiedSystem = new F_N_Manager(PARAMS_UNITY.spaceSize, PARAMS_UNITY.dimension, PARAMS_UNITY.nbOfModels, PARAMS_UNITY.normType, PARAMS_UNITY.randomExploration, PARAMS_UNITY.explorationIncrement,PARAMS_UNITY.explorationWidht,PARAMS_UNITY.limitedToSpaceZone, PARAMS_UNITY.oracleNoiseRange);
					amoeba.setStudiedSystem(studiedSystem);
					IBackupSystem backupSystem = new BackupSystem(amoeba);
					File file = new File("resources/"+PARAMS_UNITY.configFile);
					backupSystem.load(file);

					//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

					amoeba.allowGraphicalScheduler(true);
					amoeba.setRenderUpdate(false);
					amoeba.data.learningSpeed = PARAMS_UNITY.learningSpeed;
					amoeba.data.numberOfPointsForRegression = PARAMS_UNITY.regressionPoints;
					amoeba.data.isActiveLearning = PARAMS_UNITY.setActiveLearning;
					amoeba.data.isSelfLearning = PARAMS_UNITY.setSelfLearning;
					amoeba.data.isConflictDetection = PARAMS_UNITY.setConflictDetection;
					amoeba.data.isConcurrenceDetection = PARAMS_UNITY.setConcurrenceDetection;
					amoeba.data.isVoidDetection2 = PARAMS_UNITY.setVoidDetection2;
					amoeba.data.isConflictResolution = PARAMS_UNITY.setConflictResolution;
					amoeba.data.isConcurrenceResolution = PARAMS_UNITY.setConcurrenceResolution;
					amoeba.data.isFrontierRequest = PARAMS_UNITY.setFrontierRequest;
					amoeba.data.isSelfModelRequest = PARAMS_UNITY.setSelfModelRequest;
					amoeba.data.isCoopLearningWithoutOracle = PARAMS_UNITY.setCoopLearning;

					amoeba.data.isLearnFromNeighbors = PARAMS_UNITY.setLearnFromNeighbors;
					amoeba.data.nbOfNeighborForLearningFromNeighbors = PARAMS_UNITY.nbOfNeighborForLearningFromNeighbors;
					amoeba.data.isDream = PARAMS_UNITY.setDream;
					amoeba.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS_UNITY.nbOfNeighborForVoidDetectionInSelfLearning;
					amoeba.data.nbOfNeighborForContexCreationWithouOracle = PARAMS_UNITY.nbOfNeighborForContexCreationWithouOracle;

					amoeba.getEnvironment().setMappingErrorAllowed(PARAMS_UNITY.mappingErrorAllowed);
					amoeba.data.initRegressionPerformance = PARAMS_UNITY.setRegressionPerformance;
					World.minLevel = PARAMS_UNITY.traceLevel;

					//sender = new Sender(server, amoeba);

				}
			});

			Thread.sleep(wait);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}



		for(int i = 0; i < cycles; i++)
		{
			if(i%100==0){
				System.out.print(i + " ");
			}
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
						amoeba.cycle();
						updateContextsOnUnity(amoeba, sender);


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
	
	public static String fileName(ArrayList<String> infos) {
		String fileName = "";
		
		for(String info : infos) {
			fileName += info + "_";
		}
		
		return fileName;
	}
	
	public static void writeMessage(FILE file, ArrayList<String> message) {
		
		file.initManualMessage();
		
		for(String m : message) {
			file.addManualMessage(m);
		}
		
		file.sendManualMessage();
		
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	private void quit(){
		
		server.close();		
		shutDown = true;
	}
	
	private void ack(){
		//System.out.println("ACK...");
		//message = readMessage();
		//System.out.println(message);
		if(!message.contentEquals("")){
				server.sendMessage("ACK_" + Integer.toString(server.getMessageCounter())); 
				//System.out.println("ACK");
		}
		else{
			server.sendMessage("ERR_"+Integer.toString(server.getMessageCounter()));
		}
	}
	
	
	

}

