package experiments.nDimensionsLaunchers;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import agents.head.REQUEST;
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
public class F_N_Launcher implements Serializable {


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
	public static final int nbTest = 50;

	public static final boolean setVoidDetection = false ;


	public static final boolean setConflictDetection = true ;
	public static final boolean setConflictResolution = setConflictDetection ;

	public static final boolean setConcurrenceDetection = true ;
	public static final boolean setConcurrenceResolution = setConcurrenceDetection ;

	public static final boolean setVoidDetection2 = true ;

	public static final boolean setFrontierRequest = true ;


	public static void main(String[] args) throws Exception {


		start();


	}
	


	public static void start() throws Exception {


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = false;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		HashMap<String, ArrayList<Double>> data = new HashMap<>();
		
		List<String> dataStrings = Arrays.asList("mappingScore", "imprecisionScore", "randomRequests", "activeRequests","nbAgents", "conflictVol", "concurrenceVol", "voidVol", "conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests", "selfRequests", "prediction");



		for (String dataName : dataStrings){
			data.put(dataName, new ArrayList<>());
		}
		
		for (int i = 0; i < nbTest; ++i) {
			System.out.print(i + " ");
			ellsaTest(data);
		}
		System.out.println("");
		
		
		
		
		
		
		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()<1){
				System.out.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
			}else{
				System.out.println(dataName +" [AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get(dataName).size()));
			}


		}



		//Create the formatter for round the values of scores
		Locale currentLocale = Locale.getDefault();
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
		System.out.println("ROUNDED");

		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()<1){
				System.out.println(dataName +" [AVERAGE] " + df.format(averageScore.getAsDouble()*100) + " - " + "[DEVIATION] " +df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())));
			}


		}

		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()>=1){
				System.out.println(dataName +" [AVERAGE] " + Math.round(averageScore.getAsDouble()) + " - " + "[DEVIATION] " +Math.round(Math.sqrt(deviationScore/data.get(dataName).size())));
			}


		}

		OptionalDouble averageScore = data.get("prediction").stream().mapToDouble(a->a).average();
		Double deviationScore = data.get("prediction").stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
		System.out.println("[PREDICTION AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get("prediction").size()));
		System.out.println("[PREDICTION AVERAGE] " + df.format(100*averageScore.getAsDouble()) + " - " + "[DEVIATION] " +df.format(100*Math.sqrt(deviationScore/data.get("prediction").size())));
		
		
	}


	private static void ellsaTest(HashMap<String, ArrayList<Double>> data) {
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
		amoeba.data.isConflictDetection = setConflictDetection;
		amoeba.data.isConcurrenceDetection = setConcurrenceDetection;
		amoeba.data.isVoidDetection = setVoidDetection;
		amoeba.data.isConflictResolution = setConflictResolution;
		amoeba.data.isConcurrenceResolution = setConcurrenceResolution;
		amoeba.data.isVoidDetection2 = setVoidDetection2;
		amoeba.data.isFrontierRequest = setFrontierRequest;
		
		amoeba.setRenderUpdate(false);
		
		World.minLevel = TRACE_LEVEL.ERROR;
		
		

		
		
		for (int i = 0; i < nbCycle; ++i) {
			amoeba.cycle();
		}

		double errorsMean = 0;
		for (int i = 0; i < nbCycle/4; ++i) {
			errorsMean += studiedSystem.getErrorOnRequest(amoeba);
		}
		errorsMean = errorsMean/(nbCycle/4);

		HashMap<String, Double> mappingScores = amoeba.getHeadAgent().getMappingScores();
		HashMap<REQUEST, Integer> requestCounts = amoeba.data.requestCounts;

		data.get("mappingScore").add(mappingScores.get("CTXT"));
		data.get("imprecisionScore").add(mappingScores.get("CONF") + mappingScores.get("CONC") + mappingScores.get("VOIDS"));
		data.get("conflictVol").add(mappingScores.get("CONF"));
		data.get("concurrenceVol").add(mappingScores.get("CONC"));
		data.get("voidVol").add(mappingScores.get("VOIDS"));
		data.get("randomRequests").add(studiedSystem.getRandomRequestCounts());
		data.get("activeRequests").add(studiedSystem.getActiveRequestCounts());
		data.get("conflictRequests").add((double)requestCounts.get(REQUEST.CONFLICT));
		data.get("concurrenceRequests").add((double)requestCounts.get(REQUEST.CONCURRENCE));
		data.get("frontierRequests").add((double)requestCounts.get(REQUEST.FRONTIER));
		data.get("voidRequests").add((double)requestCounts.get(REQUEST.VOID));
		data.get("selfRequests").add((double)requestCounts.get(REQUEST.SELF));
		data.get("nbAgents").add((double)amoeba.getContexts().size());
		data.get("prediction").add(errorsMean);



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
