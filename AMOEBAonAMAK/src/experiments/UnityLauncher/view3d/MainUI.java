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

	ELLSA ellsa;
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

	private void updateContextsOnUnity(ELLSA ellsa, Sender sender) {
		ArrayList<Context> spatiallyAlteredContexts = ellsa.getSpatiallyAlteredContextForUnityUI();
		ArrayList<Context> toKillContexts = ellsa.getToKillContextsForUnityUI();

		if(spatiallyAlteredContexts.size()>0) {

			sender.sendContexts(spatiallyAlteredContexts);

			while (!sender.acq("CTXTS", ellsa.getCycle())) {
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

			while (!sender.acq("KILL", ellsa.getCycle())) {
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

					ellsa = new ELLSA(amoebaUI,  amoebaVUI);
					studiedSystem = new F_N_Manager(PARAMS_UNITY.spaceSize, PARAMS_UNITY.dimension, PARAMS_UNITY.nbOfModels, PARAMS_UNITY.normType, PARAMS_UNITY.randomExploration, PARAMS_UNITY.explorationIncrement,PARAMS_UNITY.explorationWidht,PARAMS_UNITY.limitedToSpaceZone, PARAMS_UNITY.oracleNoiseRange);
					ellsa.setStudiedSystem(studiedSystem);
					IBackupSystem backupSystem = new BackupSystem(ellsa);
					File file = new File("resources/"+PARAMS_UNITY.configFile);
					backupSystem.load(file);

					//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

					ellsa.allowGraphicalScheduler(true);
					ellsa.setRenderUpdate(false);
					ellsa.data.learningSpeed = PARAMS_UNITY.learningSpeed;
					ellsa.data.numberOfPointsForRegression = PARAMS_UNITY.regressionPoints;
					ellsa.data.isActiveLearning = PARAMS_UNITY.setActiveLearning;
					ellsa.data.isSelfLearning = PARAMS_UNITY.setSelfLearning;
					ellsa.data.isConflictDetection = PARAMS_UNITY.setConflictDetection;
					ellsa.data.isConcurrenceDetection = PARAMS_UNITY.setConcurrenceDetection;
					ellsa.data.isVoidDetection2 = PARAMS_UNITY.setVoidDetection2;
					ellsa.data.isConflictResolution = PARAMS_UNITY.setConflictResolution;
					ellsa.data.isConcurrenceResolution = PARAMS_UNITY.setConcurrenceResolution;
					ellsa.data.isFrontierRequest = PARAMS_UNITY.setFrontierRequest;
					ellsa.data.isSelfModelRequest = PARAMS_UNITY.setSelfModelRequest;
					ellsa.data.isCoopLearningWithoutOracle = PARAMS_UNITY.setCoopLearning;

					ellsa.data.isLearnFromNeighbors = PARAMS_UNITY.setLearnFromNeighbors;
					ellsa.data.nbOfNeighborForLearningFromNeighbors = PARAMS_UNITY.nbOfNeighborForLearningFromNeighbors;
					ellsa.data.isDream = PARAMS_UNITY.setDream;
					ellsa.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS_UNITY.nbOfNeighborForVoidDetectionInSelfLearning;
					ellsa.data.nbOfNeighborForContexCreationWithouOracle = PARAMS_UNITY.nbOfNeighborForContexCreationWithouOracle;

					ellsa.getEnvironment().setMappingErrorAllowed(PARAMS_UNITY.mappingErrorAllowed);
					ellsa.data.initRegressionPerformance = PARAMS_UNITY.setRegressionPerformance;
					ellsa.getEnvironment().minLevel = PARAMS_UNITY.traceLevel;

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
						ellsa.cycle();
						updateContextsOnUnity(ellsa, sender);


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

