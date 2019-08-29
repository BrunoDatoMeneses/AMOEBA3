package experiments.nDimensionsLaunchers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import gui.AmoebaWindow;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Slider;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import kernel.backup.SaveHelperImpl;


// TODO: Auto-generated Javadoc
/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_Launcher implements Serializable {


	public static final double oracleNoiseRange = 0.5;
	public static final double learningSpeed = 0.01;
	public static final int regressionPoints = 100;
	public static final int dimension = 2;
	public static final double spaceSize = 50.0	;
	public static final int nbOfModels = 3	;
	public static final int normType = 2	;
	public static final boolean randomExploration = true;
	public static final boolean limitedToSpaceZone = true;
	//public static final double mappingErrorAllowed = 0.07; // BIG SQUARE
	public static double mappingErrorAllowed = 0.03; // MULTI
	public static final double explorationIncrement = 1.0	;
	public static final double explorationWidht = 0.5	;
	
	public static final int nbCycle = 1000;
	

	
	public static void main(String[] args) throws IOException {
		// Instantiating the MainWindow before usage.
		// It also allows you to change some of its behavior before creating an AMOEBA.
		// If you use Configuration.commandLineMode = True , then you should skip it. 
		AmoebaWindow.instance();
		launch();
	}
	


	public static void launch() throws IOException{
		
		
		
		
		// Set AMAK configuration before creating an AMOEBA
		Configuration.commandLineMode = false;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = true;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		AMOEBA amoeba = new AMOEBA();
		StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);
		
		amoeba.saver = new SaveHelperImpl(amoeba);
		amoeba.allowGraphicalScheduler(true);
		amoeba.setRenderUpdate(true);		
		amoeba.data.learningSpeed = learningSpeed;
		amoeba.data.numberOfPointsForRegression = regressionPoints;
		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		
		// Exemple for adding a tool in the toolbar
		Slider slider = new Slider(0.01, 0.1, mappingErrorAllowed);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		
		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				System.out.println("new Value "+newValue);
				mappingErrorAllowed = (double)newValue;
				amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
			}
		});
		AmoebaWindow.addToolbar(slider);
		
		studiedSystem.playOneStep();
		amoeba.learn(studiedSystem.getOutput());
		
		/* AUTOMATIC */
//		long start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
//		
//		start = System.currentTimeMillis();
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.request(studiedSystem.getOutput());
//		}
//		end = System.currentTimeMillis();
//		System.out.println("Done in : " + (end - start) );
		
		
//		/* XP PIERRE */
//		
//		String fileName = fileName(new ArrayList<String>(Arrays.asList("GaussiennePierre")));
//		
//		FILE Pierrefile = new FILE("Pierre",fileName);
//		for (int i = 0; i < nbCycle; ++i) {
//			studiedSystem.playOneStep();
//			amoeba.learn(studiedSystem.getOutput());
//			if(amoeba.getHeadAgent().isActiveLearning()) {
//				studiedSystem.setActiveLearning(true);
//				studiedSystem.setSelfRequest(amoeba.getHeadAgent().getSelfRequest());
//				 
//			}
//		}
//		
//		for (int i = 0; i < 10; ++i) {
//			studiedSystem.playOneStep();
//			System.out.println(studiedSystem.getOutput());
//			System.out.println(amoeba.request(studiedSystem.getOutput()));
//			
//			
//		}
//		
//		Pierrefile.write(new ArrayList<String>(Arrays.asList("ID contexte","Coeff Cte","Coeff X0","Coeff X1","Min Value","Max Value")));
//		
//		for(Context ctxt : amoeba.getContexts()) {
//			
//			writeMessage(Pierrefile, ctxt.toStringArrayPierre());
//
//		}
//		
//		
//		Pierrefile.close();
		
	
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
