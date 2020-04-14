package experiments.UnityLauncher.view3d;

import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
import fr.irit.smac.amak.Configuration;
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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherUI_Unity extends Application implements Serializable {

	AMOEBA amoeba;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	AmoebaMultiUIWindow amoebaUI;
	boolean test = true;

	private ServerSocket socketserver = null;
	private  Socket socket = null;

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
		amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI, studiedSystem);


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

					amoeba = new AMOEBA(amoebaUI,  amoebaVUI);

					amoeba.setStudiedSystem(studiedSystem);
					IBackupSystem backupSystem = new BackupSystem(amoeba);
					File file = new File("resources/"+PARAMS.configFile);
					backupSystem.load(file);

					//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

					amoeba.allowGraphicalScheduler(true);
					amoeba.setRenderUpdate(false);
					amoeba.data.learningSpeed = PARAMS.learningSpeed;
					amoeba.data.numberOfPointsForRegression = PARAMS.regressionPoints;
					amoeba.data.isActiveLearning = PARAMS.setActiveLearning;
					amoeba.data.isSelfLearning = PARAMS.setSelfLearning;
					amoeba.data.isConflictDetection = PARAMS.setConflictDetection;
					amoeba.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
					amoeba.data.isVoidDetection2 = PARAMS.setVoidDetection2;
					amoeba.data.isConflictResolution = PARAMS.setConflictResolution;
					amoeba.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
					amoeba.data.isFrontierRequest = PARAMS.setFrontierRequest;
					amoeba.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
					amoeba.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

					amoeba.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
					amoeba.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
					amoeba.data.isDream = PARAMS.setDream;
					amoeba.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
					amoeba.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

					amoeba.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
					amoeba.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
					World.minLevel = PARAMS.traceLevel;

				}
			});

			Thread.sleep(wait);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		int i = 0;

		/*while(test)
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
						amoeba.cycle();
						test = amoeba.data.normalizedCriticality < 100000;
						//System.out.println(amoeba.data.normalizedCriticality);



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
		System.out.println(amoeba.data.normalizedCriticality);
		System.out.println(amoeba.getHeadAgent().getBestContext().getName());*/


	}
	
	




	
}
