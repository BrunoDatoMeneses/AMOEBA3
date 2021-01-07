package experiments.nDimensionsLaunchers;

import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherUIJFSMA2020 extends Application implements Serializable {




	ELLSA ellsa;
	StudiedSystem studiedSystem;
	VUIMulti amoebaVUI;
	EllsaMultiUIWindow amoebaUI;
	
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
		


		ellsa = new ELLSA(null,  null);
		studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		ellsa.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(ellsa);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);

		ellsa.saver = new SaveHelperImpl(ellsa);

		ellsa.allowGraphicalScheduler(true);
		ellsa.setRenderUpdate(false);
		ellsa.data.PARAM_learningSpeed = PARAMS.learningSpeed;
		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;
		ellsa.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
		ellsa.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;
		ellsa.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
		ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection2;
		ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
		ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
		ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
		ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
		ellsa.data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS.setCoopLearning;

		ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
		ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
		ellsa.data.PARAM_isDream = PARAMS.setDream;
		ellsa.data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
		ellsa.data.PARAM_nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

		ellsa.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
		ellsa.data.PARAM_initRegressionPerformance = PARAMS.setRegressionPerformance;
		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;

		for(int i=0;i<PARAMS.nbCycle;i++){
			ellsa.cycle();
			if(i%100 ==0){
				System.out.print(i+",");
			}

		}

		ellsa.getHeadAgent().getMappingScoresAndPrint();
		System.out.println("RDM REQUESTS " + studiedSystem.getRandomRequestCounts());
		System.out.println("ACT REQUESTS " + studiedSystem.getActiveRequestCounts());
		System.out.println("CTXT NB " + ellsa.getContexts().size());
		System.out.println("REQUEST TYPES");
		System.out.println(ellsa.data.requestCounts);

		System.out.println(ellsa.getHeadAgent().getMappingScores());

		/*double errorsMean = 0;
		for (int i = 0; i < nbCycle/4; ++i) {
			errorsMean += studiedSystem.getErrorOnRequest(amoeba);
		}
		errorsMean = errorsMean/(nbCycle/4);
		System.out.println("PREDICTION MEAN ERROR " + errorsMean);*/

		//amoeba.saver.newManualSave( dimension + "_" + nbCycle +"_TestManualSave", "saves/");
		ellsa.saver.newManualSave("TestManualSave", "saves/");

		Configuration.commandLineMode = false;
		amoebaVUI = new VUIMulti("2D");
		amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, null);
		ELLSA ellsa2 = new ELLSA(amoebaUI,  amoebaVUI);
		StudiedSystem studiedSystem2 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		ellsa2.setStudiedSystem(studiedSystem2);
		IBackupSystem backupSystem2 = new BackupSystem(ellsa2);
		File file2 = new File("resources/"+PARAMS.configFile);
		backupSystem2.load(file2);

		ellsa2.saver = new SaveHelperImpl(ellsa2, amoebaUI);

		ellsa2.saver.load("saves/"+PARAMS.nbCycle +"_TestManualSave.xml");
		//amoeba2.saver.load("saves/"+amoeba.getCycle()+ "_" +dimension + "_" + nbCycle +"_TestManualSave.xml");
		ellsa2.setRenderUpdate(true);

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
