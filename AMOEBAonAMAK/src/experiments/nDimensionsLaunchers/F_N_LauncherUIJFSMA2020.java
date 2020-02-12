package experiments.nDimensionsLaunchers;

import experiments.FILE;
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
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherUIJFSMA2020 extends Application implements Serializable {


	public static final double oracleNoiseRange = 0.5;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 2	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = true;
	//public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
	public static double mappingErrorAllowed = 0.05; // MULTI
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	public static final boolean setActiveLearning = true	;
	public static final boolean setSelfLearning = false	;
	public static final int nbCycle = 1000;
	public static final boolean setVoidDetection = false ;


	public static final boolean setConflictDetection = true ;
	public static final boolean setConflictResolution = true ;

	public static final boolean setConcurrenceDetection = true ;
	public static final boolean setConcurrenceResolution = true ;

	public static final boolean setVoidDetection2 = true ;

	public static final boolean setFrontierRequest = false ;

	AMOEBA amoeba;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	AmoebaMultiUIWindow amoebaUI;
	
	public static void main(String[] args) throws IOException {
		Application.launch(args);
	}
	

	@Override
	public void start(Stage arg0) throws Exception {


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;
		


		amoeba = new AMOEBA(null,  null);
		studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);

		amoeba.saver = new SaveHelperImpl(amoeba);

		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(false);
		amoeba.data.learningSpeed = learningSpeed;
		amoeba.data.numberOfPointsForRegression = regressionPoints;
		amoeba.data.isActiveLearning = setActiveLearning;
		amoeba.data.isSelfLearning = setSelfLearning;
		amoeba.data.isConflictDetection = setConflictDetection;
		amoeba.data.isConcurrenceDetection = setConcurrenceDetection;
		amoeba.data.isVoidDetection = setVoidDetection;
		amoeba.data.isConflictResolution = setConflictResolution;
		amoeba.data.isConcurrenceResolution = setConcurrenceResolution;
		amoeba.data.isVoidDetection2 = setVoidDetection2;
		amoeba.data.isFrontierRequest = setFrontierRequest;

		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		World.minLevel = TRACE_LEVEL.ERROR;

		for(int i=0;i<nbCycle;i++){
			amoeba.cycle();
			if(i%100 ==0){
				System.out.print(i+",");
			}

		}

		amoeba.getHeadAgent().getMappingScoresAndPrint();
		System.out.println("RDM REQUESTS " + studiedSystem.getRandomRequestCounts());
		System.out.println("ACT REQUESTS " + studiedSystem.getActiveRequestCounts());
		System.out.println("CTXT NB " + amoeba.getContexts().size());
		System.out.println("REQUEST TYPES");
		System.out.println(amoeba.data.requestCounts);

		amoeba.saver.newManualSave("TestManualSave", "saves/");

		Configuration.commandLineMode = false;
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI);
		AMOEBA amoeba2 = new AMOEBA(amoebaUI,  amoebaVUI);
		StudiedSystem studiedSystem2 = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
		amoeba2.setStudiedSystem(studiedSystem2);
		IBackupSystem backupSystem2 = new BackupSystem(amoeba2);
		File file2 = new File("resources/twoDimensionsLauncher.xml");
		backupSystem2.load(file2);

		amoeba2.saver = new SaveHelperImpl(amoeba2, amoebaUI);
		amoeba2.saver.load("saves/"+nbCycle +"_TestManualSave.xml");
		amoeba2.setRenderUpdate(true);

		amoebaUI.rectangle.delete();
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

					amoeba = new AMOEBA(amoebaUI,  amoebaVUI);
					studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
					amoeba.setStudiedSystem(studiedSystem);
					IBackupSystem backupSystem = new BackupSystem(amoeba);
					File file = new File("resources/twoDimensionsLauncher.xml");
					backupSystem.load(file);

					amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

					amoeba.allowGraphicalScheduler(true);
					amoeba.setRenderUpdate(true);
					amoeba.data.learningSpeed = learningSpeed;
					amoeba.data.numberOfPointsForRegression = regressionPoints;
					amoeba.data.isActiveLearning = setActiveLearning;
					amoeba.data.isSelfLearning = setSelfLearning;
					amoeba.data.isConflictDetection = setConflictDetection;
					amoeba.data.isConcurrenceDetection = setConcurrenceDetection;
					amoeba.data.isVoidDetection = setVoidDetection;
					amoeba.data.isConflictResolution = setConflictResolution;
					amoeba.data.isConcurrenceResolution = setConcurrenceResolution;

					amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
					World.minLevel = TRACE_LEVEL.ERROR;

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


}
