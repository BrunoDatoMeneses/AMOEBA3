package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;
import utils.TRACE_LEVEL;


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
		AmoebaMultiUIWindow amoebaUI = new AmoebaMultiUIWindow("ELLSA", amoebaVUI, studiedSystem);
		AMOEBA amoeba = new AMOEBA(amoebaUI,  amoebaVUI);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);
		
		amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);
		
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
		World.minLevel = TRACE_LEVEL.CYCLE;
		
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
