package experiments.nDimensionsLaunchers;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;

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
public class F_N_Launcher  extends Application implements Serializable {


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
	public static final int nbTest = 10;
	

	
	public static void main(String[] args) throws IOException {
		
		
		Application.launch(args);


	}
	

	@Override
	public void start(Stage arg0) throws Exception {


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = false;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		HashMap<String, ArrayList<Double>> data = new HashMap<String, ArrayList<Double>>();
		
		List<String> dataStrings = Arrays.asList("mappingScore", "randomRequests", "activeRequests","nbAgents");
		
		for (String dataName : dataStrings){
			data.put(dataName, new ArrayList<Double>());
		}
		
		for (int i = 0; i < nbTest; ++i) {
			System.out.print(i + " ");
			ellsaTest( data);
		}
		System.out.println("");
		
		
		
		
		
		
		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			System.out.println("[" + dataName +" AVERAGE] " + averageScore.getAsDouble());
			System.out.println("[" + dataName +" DEVIATION] " +Math.sqrt(deviationScore/data.get(dataName).size()));
		}
		
		
		
	}


	private void ellsaTest(HashMap<String, ArrayList<Double>> data) {
		AMOEBA amoeba = new AMOEBA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(spaceSize, dimension, nbOfModels, normType, randomExploration, explorationIncrement,explorationWidht,limitedToSpaceZone, oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/twoDimensionsLauncher.xml");
		backupSystem.load(file);
		
		
		amoeba.allowGraphicalScheduler(false);
		amoeba.setRenderUpdate(false);		
		amoeba.data.learningSpeed = learningSpeed;
		amoeba.data.numberOfPointsForRegression = regressionPoints;
		amoeba.data.isActiveLearning = setActiveLearning;
		amoeba.data.isSelfLearning = setSelfLearning;
		amoeba.getEnvironment().setMappingErrorAllowed(mappingErrorAllowed);
		
		amoeba.setRenderUpdate(false);
		
		World.minLevel = TRACE_LEVEL.ERROR;
		
		

		
		
		for (int i = 0; i < nbCycle; ++i) {
			amoeba.cycle();
		}
		
		
		data.get("mappingScore").add(amoeba.getHeadAgent().criticalities.getCriticality("spatialCriticality"));
		data.get("randomRequests").add(studiedSystem.getRandomRequestCounts());
		data.get("activeRequests").add(studiedSystem.getActiveRequestCounts());
		data.get("nbAgents").add((double)amoeba.getContexts().size());
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
