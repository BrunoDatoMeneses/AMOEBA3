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
		studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);

		amoeba.saver = new SaveHelperImpl(amoeba);

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
		amoeba.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
		amoeba.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
		World.minLevel = TRACE_LEVEL.ERROR;

		for(int i=0;i<PARAMS.nbCycle;i++){
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

		/*double errorsMean = 0;
		for (int i = 0; i < nbCycle/4; ++i) {
			errorsMean += studiedSystem.getErrorOnRequest(amoeba);
		}
		errorsMean = errorsMean/(nbCycle/4);
		System.out.println("PREDICTION MEAN ERROR " + errorsMean);*/

		//amoeba.saver.newManualSave( dimension + "_" + nbCycle +"_TestManualSave", "saves/");
		amoeba.saver.newManualSave("TestManualSave", "saves/");

		Configuration.commandLineMode = false;
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI);
		AMOEBA amoeba2 = new AMOEBA(amoebaUI,  amoebaVUI);
		StudiedSystem studiedSystem2 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		amoeba2.setStudiedSystem(studiedSystem2);
		IBackupSystem backupSystem2 = new BackupSystem(amoeba2);
		File file2 = new File("resources/"+PARAMS.configFile);
		backupSystem2.load(file2);

		amoeba2.saver = new SaveHelperImpl(amoeba2, amoebaUI);

		amoeba2.saver.load("saves/"+PARAMS.nbCycle +"_TestManualSave.xml");
		//amoeba2.saver.load("saves/"+amoeba.getCycle()+ "_" +dimension + "_" + nbCycle +"_TestManualSave.xml");
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






}
