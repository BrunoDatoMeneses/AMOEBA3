package experiments.simpleLauncherExternalSource;

import experiments.mathematicalModels.Model_Manager;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.RAND_REPEATABLE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;


/**
 * The Class BadContextLauncherEasy.
 */
public class LauncherUI extends Application implements Serializable {

	private HashMap<String, Double> perceptions = new HashMap<String, Double>();

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
		RAND_REPEATABLE.setSeed(0);



		StudiedSystem studiedSystem = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
		VUIMulti amoebaVUI = new VUIMulti("2D");
		EllsaMultiUIWindow amoebaUI = new EllsaMultiUIWindow("ELLSA", amoebaVUI, studiedSystem);
		ELLSA ellsa = new ELLSA(amoebaUI,  amoebaVUI);
		ellsa.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(ellsa);
		File file = new File("resources/"+ PARAMS.configFile);
		backupSystem.load(file);
		ellsa.getEnvironment().setSeed(0);


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

		ellsa.data.PARAM_perceptionsGenerationCoefficient = PARAMS.perceptionsGenerationCoefficient;
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
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setIncompetenceDetection;
		ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubIncompetencedDetection;
		ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
		ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
		ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setRangeAmbiguityDetection;
		ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setModelAmbiguityDetection;
		ellsa.data.PARAM_NCS_isFusionResolution = PARAMS.setCompleteRedundancyDetection;
		ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setPartialRedundancyDetection;

		ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


		ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setCooperativeNeighborhoodLearning;
		ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
		ellsa.data.PARAM_isDream = PARAMS.setDream;
        ellsa.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;


		ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;

		ellsa.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS.isAllContextSearchAllowedForLearning;
		ellsa.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS.isAllContextSearchAllowedForExploitation;

		ellsa.data.PARAM_probabilityOfRangeAmbiguity = PARAMS.probabilityOfRangeAmbiguity;

		ellsa.data.PARAM_isExploitationActive = PARAMS.setActiveExploitation;

		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;



		ellsa.setSubPercepts(experiments.roboticArmDistributedControl.PARAMS.subPercepts);



		ellsa.data.STOP_UI = PARAMS.STOP_UI;
		ellsa.data.STOP_UI_cycle = PARAMS.STOP_UI_cycle;

		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;

		amoebaUI.toggleRender.setSelected(false);

		for (int i=0;i<PARAMS.nbLearningCycle;i++){

			studiedSystem.playOneStep();
			perceptions = studiedSystem.getOutput();
			ellsa.learn(perceptions);


		}

		for (int i=0;i<PARAMS.nbExploitationCycle;i++){

			studiedSystem.playOneStep();
			perceptions = studiedSystem.getOutput();
			ellsa.request(perceptions);


		}


		
	}

	
	




	
}
