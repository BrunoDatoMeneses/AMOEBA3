package experiments.UnityLauncher.view3d;

import agents.context.Context;
import experiments.UnityLauncher.Sender;
import experiments.UnityLauncher.SocketServer;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
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
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherUI_Unity extends Application implements Serializable {

	ELLSA ellsa;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	EllsaMultiUIWindow amoebaUI;
	Sender sender;
	boolean test = true;

	private static ServerSocket socketserver = null;
	private static  Socket socket = null;
	private SocketServer server;

	public static void main(String[] args) throws IOException {
		
		
		Application.launch(args);


	}
	

	@Override
	public void start(Stage arg0) throws Exception {


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;

		studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, studiedSystem);


		startTask(100, PARAMS.nbCycle);
		

		
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
		try {
			socketserver = new ServerSocket(2009);
			System.out.println("Server ready...");
			socket = socketserver.accept(); // Un client se connecte on
			// l'accepte
			System.out.println("Client connected...");
			server = new SocketServer(socketserver, socket);

		} catch (IOException e) {
			System.err.println("Le port " + socket.getLocalPort() + " est déjà utilisé !");
			e.printStackTrace();
		}

		try
		{

			// Update the Label on the JavaFx Application Thread
			Platform.runLater(new Runnable()
			{
				@Override
				public void run()
				{

					ellsa = new ELLSA(amoebaUI,  amoebaVUI);

					ellsa.setStudiedSystem(studiedSystem);
					IBackupSystem backupSystem = new BackupSystem(ellsa);
					File file = new File("resources/"+PARAMS.configFile);
					backupSystem.load(file);

					//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

					ellsa.allowGraphicalScheduler(true);
					ellsa.setRenderUpdate(true);
					ellsa.data.learningSpeed = PARAMS.learningSpeed;
					ellsa.data.numberOfPointsForRegression = PARAMS.regressionPoints;
					ellsa.data.isActiveLearning = PARAMS.setActiveLearning;
					ellsa.data.isSelfLearning = PARAMS.setSelfLearning;
					ellsa.data.isConflictDetection = PARAMS.setConflictDetection;
					ellsa.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
					ellsa.data.isVoidDetection2 = PARAMS.setVoidDetection2;
					ellsa.data.isConflictResolution = PARAMS.setConflictResolution;
					ellsa.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
					ellsa.data.isFrontierRequest = PARAMS.setFrontierRequest;
					ellsa.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
					ellsa.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

					ellsa.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
					ellsa.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
					ellsa.data.isDream = PARAMS.setDream;
					ellsa.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
					ellsa.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

					ellsa.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
					ellsa.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
					ellsa.getEnvironment().minLevel = PARAMS.traceLevel;

					sender = new Sender(server, ellsa);

				}
			});

			Thread.sleep(wait);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		int i = 0;

		while(test)
		//for(int i = 0; i < cycles; i++)
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
						//test = amoeba.data.normalizedCriticality < 100000;
						//System.out.println(amoeba.data.normalizedCriticality);
						updateContextsOnUnity(ellsa, sender);

					}
				});



				Thread.sleep(wait);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			i+=1;
		}
		System.out.println(i-1);
		System.out.println(ellsa.data.normalizedCriticality);
		System.out.println(ellsa.getHeadAgent().getBestContext().getName());


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


	
}
