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
		
		//amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);
		
		ellsa.allowGraphicalScheduler(true);
		ellsa.setRenderUpdate(false);
		ellsa.data.PARAM_learningSpeed = PARAMS.learningSpeed;
		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;
		ellsa.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
		ellsa.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;
		ellsa.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
		ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection2;
		ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
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
		ellsa.data.PARAM_neighborhoodMultiplicator = PARAMS.setNeighborhoodMultiplicator;
		ellsa.data.PARAM_externalContextInfluenceRatio = PARAMS.setExternalContextInfluenceRatio;

		ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;
		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;

		ellsa.setSubPercepts(experiments.roboticDistributedArm.PARAMS.subPercepts);


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
