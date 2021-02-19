package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

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


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_LauncherUI  extends Application implements Serializable {



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
		


		StudiedSystem studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		VUIMulti amoebaVUI = new VUIMulti("2D");
		EllsaMultiUIWindow amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, studiedSystem);
		ELLSA ellsa = new ELLSA(amoebaUI,  amoebaVUI);
		ellsa.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(ellsa);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);

		//ellsa.saver = new SaveHelperImpl(ellsa, amoebaUI);
		
		ellsa.allowGraphicalScheduler(true);
		ellsa.setRenderUpdate(false);

		ellsa.getEnvironment().setMappingErrorAllowed(PARAMS.validityRangesPrecision);
		ellsa.data.PARAM_modelErrorMargin = PARAMS.modelErrorMargin;
		ellsa.data.PARAM_bootstrapCycle = PARAMS.setbootstrapCycle;
		ellsa.data.PARAM_exogenousLearningWeight = PARAMS.exogenousLearningWeight;
		ellsa.data.PARAM_endogenousLearningWeight = PARAMS.endogenousLearningWeight;

		ellsa.data.PARAM_neighborhoodRadiusCoefficient = PARAMS.neighborhoodRadiusCoefficient;
		ellsa.data.PARAM_influenceRadiusCoefficient = PARAMS.influenceRadiusCoefficient;
		ellsa.data.PARAM_maxRangeRadiusCoefficient = PARAMS.maxRangeRadiusCoefficient;
		ellsa.data.PARAM_rangeSimilarityCoefficient = PARAMS.rangeSimilarityCoefficient;
		ellsa.data.PARAM_minimumRangeCoefficient = PARAMS.minimumRangeCoefficient;

		ellsa.data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
		ellsa.data.PARAM_creationNeighborNumberForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

		ellsa.data.PARAM_perceptionsGenerationCoefficient = PARAMS.perceptionsGenerationCoefficient
		;
		ellsa.data.PARAM_modelSimilarityThreshold = PARAMS.modelSimilarityThreshold;

		ellsa.data.PARAM_LEARNING_WEIGHT_ACCURACY = PARAMS.LEARNING_WEIGHT_ACCURACY;
		ellsa.data.PARAM_LEARNING_WEIGHT_PROXIMITY = PARAMS.LEARNING_WEIGHT_PROXIMITY;
		ellsa.data.PARAM_LEARNING_WEIGHT_EXPERIENCE = PARAMS.LEARNING_WEIGHT_EXPERIENCE;
		ellsa.data.PARAM_LEARNING_WEIGHT_GENERALIZATION = PARAMS.LEARNING_WEIGHT_GENERALIZATION;

		ellsa.data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = PARAMS.EXPLOITATION_WEIGHT_PROXIMITY;
		ellsa.data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE;
		ellsa.data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION;


		ellsa.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
		ellsa.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;

		ellsa.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
		ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection;
		ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
		ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
		ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
		ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
		ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
		ellsa.data.PARAM_NCS_isFusionResolution = PARAMS.setFusionResolution;
		ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setRestructureResolution;

		ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


		ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
		ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
		ellsa.data.PARAM_isDream = PARAMS.setDream;
        ellsa.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;


		ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;

		ellsa.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS.isAllContextSearchAllowedForLearning;
		ellsa.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS.isAllContextSearchAllowedForExploitation;

		ellsa.data.PARAM_probabilityOfRangeAmbiguity = PARAMS.probabilityOfRangeAmbiguity;

		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;



		ellsa.setSubPercepts(experiments.roboticDistributedArm.PARAMS.subPercepts);



		ellsa.data.STOP_UI = PARAMS.STOP_UI;
		ellsa.data.STOP_UI_cycle = PARAMS.STOP_UI_cycle;

		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;
		ellsa.data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS.setCoopLearningASUPPRIMER;

		//ellsa.setSubPercepts(new ArrayList<>(Collections.singleton("px2")));
		
		/*for (int i = 0; i < PARAMS.nbCycle; ++i) {
			amoeba.cycle();
		}*/
		
		
		// Exemple for adding a tool in the toolbar
//		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
//		slider.setShowTickLabels(true);
//		slider.setShowTickMarks(true);
//		
//		slider.valueProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				System.out.println("new Value "+newValue);
//				mappingErrorAllowed = (double)newValue;
//				amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
//			}
//		});
//		amoebaUI.addToolbar(slider);
		
		//studiedSystem.playOneStep();
		//amoeba.learn(studiedSystem.getOutput());
		
		
		
		
		/* AUTOMATIC */
//				long start = System.currentTimeMillis();
//				for (int i = 0; i < nbCycle; ++i) {
//					studiedSystem.playOneStep();
//					amoeba.learn(studiedSystem.getOutput());
//				}
//				long end = System.currentTimeMillis();
//				System.out.println("Done in : " + (end - start) );
//				
//				start = System.currentTimeMillis();
//				for (int i = 0; i < nbCycle; ++i) {
//					studiedSystem.playOneStep();
//					amoeba.request(studiedSystem.getOutput());
//				}
//				end = System.currentTimeMillis();
//				System.out.println("Done in : " + (end - start) );
		
		
//				/* XP PIERRE */
//				
//				String fileName = fileName(new ArrayList<String>(Arrays.asList("GaussiennePierre")));
//				
//				FILE Pierrefile = new FILE("Pierre",fileName);
//				for (int i = 0; i < nbCycle; ++i) {
//					studiedSystem.playOneStep();
//					amoeba.learn(studiedSystem.getOutput());
//					if(amoeba.getHeadAgent().isActiveLearning()) {
//						studiedSystem.setActiveLearning(true);
//						studiedSystem.setSelfRequest(amoeba.getHeadAgent().getSelfRequest());
//						 
//					}
//				}
//				
//				for (int i = 0; i < 10; ++i) {
//					studiedSystem.playOneStep();
//					System.out.println(studiedSystem.getOutput());
//					System.out.println(amoeba.request(studiedSystem.getOutput()));
//					
//					
//				}
//				
//				Pierrefile.write(new ArrayList<String>(Arrays.asList("ID contexte","Coeff Cte","Coeff X0","Coeff X1","Min Value","Max Value")));
//				
//				for(Context ctxt : amoeba.getContexts()) {
//					
//					writeMessage(Pierrefile, ctxt.toStringArrayPierre());
//
//				}
//				
//				
//				Pierrefile.close();
		
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
