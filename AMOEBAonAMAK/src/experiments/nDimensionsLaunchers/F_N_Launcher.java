package experiments.nDimensionsLaunchers;


import java.io.File;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

import agents.head.REQUEST;
import experiments.FILE;
import fr.irit.smac.amak.Configuration;
import kernel.AMOEBA;
import kernel.StudiedSystem;
import kernel.World;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.CSVWriter;


/**
 * The Class BadContextLauncherEasy.
 */
public class F_N_Launcher implements Serializable {


	private static CSVWriter xpCSV;



	public static void main(String[] args) throws Exception {


		start();



	}
	


	public static void start() throws Exception {

		String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
		String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
		xpCSV = new CSVWriter(date,dateAndHour+"_Dim_"+PARAMS.dimension);

		String model = "Square";

		writeParams(model);


		// Set AMAK configuration before creating an AMOEBA
		Configuration.multiUI=true;
		Configuration.commandLineMode = true;
		Configuration.allowedSimultaneousAgentsExecution = 1;
		Configuration.waitForGUI = false;
		Configuration.plotMilliSecondsUpdate = 20000;
		
		HashMap<String, ArrayList<Double>> data = new HashMap<>();
		
		List<String> dataStrings = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol","nbAgents", "randomRequests", "activeRequests", "selfRequests","conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests", "modelRequests","neighborRequests","fusionRequests","restructureRequests", "prediction");



		for (String dataName : dataStrings){
			data.put(dataName, new ArrayList<>());
		}

		double start = System.currentTimeMillis();
		for (int i = 0; i < PARAMS.nbTest; ++i) {
			System.out.print(i + " ");
			ellsaTest(data);
		}
		System.out.println("");
		double total = (System.currentTimeMillis()- start)/1000;
		double mean = total/ PARAMS.nbTest;
		System.out.println("[TIME MEAN] " + mean + " s");
		System.out.println("[TIME TOTAL] " + total + " s");


		xpCSV.write(new ArrayList<>(Arrays.asList("TIME MEAN", mean + " s","TIME TOTAL",total + " s" )));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
		
		
		
		
		
		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()<1){
				System.out.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
				xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()*100+"" ,"DEVIATION","" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));
			}else{
				System.out.println(dataName +" [AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get(dataName).size()));
				xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()+"" ,"DEVIATION","" + Math.sqrt(deviationScore/data.get(dataName).size()))));
			}


		}

		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		//Create the formatter for round the values of scores
		Locale currentLocale = Locale.getDefault();
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
		otherSymbols.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
		System.out.println("ROUNDED");
		xpCSV.write(new ArrayList<>(Arrays.asList("ROUNDED")));
		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()<1){
				System.out.println(dataName +" [AVERAGE] " + df.format(averageScore.getAsDouble()*100) + " - " + "[DEVIATION] " +df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())));
				xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,df.format(averageScore.getAsDouble()*100)+"" ,"DEVIATION","" + df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())))));
			}


		}
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		for (String dataName : dataStrings){
			OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
			Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
			if(averageScore.getAsDouble()>=1){
				System.out.println(dataName +" [AVERAGE] " + Math.round(averageScore.getAsDouble()) + " - " + "[DEVIATION] " +Math.round(Math.sqrt(deviationScore/data.get(dataName).size())));
				xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,df.format(averageScore.getAsDouble())+"" ,"DEVIATION","" + df.format(Math.sqrt(deviationScore/data.get(dataName).size())))));
			}


		}

		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		OptionalDouble averageScore = data.get("prediction").stream().mapToDouble(a->a).average();
		Double deviationScore = data.get("prediction").stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
		System.out.println("[PREDICTION AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get("prediction").size()));
		xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION AVERAGE" , ""+averageScore.getAsDouble() ,"DEVIATION" ,""+Math.sqrt(deviationScore/data.get("prediction").size()))));

		System.out.println("[PREDICTION AVERAGE %] " + df.format(100*averageScore.getAsDouble()) + " - " + "[DEVIATION %] " +df.format(100*Math.sqrt(deviationScore/data.get("prediction").size())));
		xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION AVERAGE %" , ""+df.format(100*averageScore.getAsDouble()) ,"DEVIATION %" ,""+df.format(100*Math.sqrt(deviationScore/data.get("prediction").size())))));
		xpCSV.close();
		
	}

	private static void writeParams(String model) {
		xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
		xpCSV.write(new ArrayList<>(Arrays.asList("SET")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Dim", PARAMS.dimension+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Model",model)));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning cycles",PARAMS.nbCycle+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Testting cycles",PARAMS.nbCycleTest+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning episodes",PARAMS.nbTest+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Space size",PARAMS.spaceSize*4+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Mapping error",PARAMS.mappingErrorAllowed+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Active Learning",PARAMS.setActiveLearning+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Self Learning",PARAMS.setSelfLearning+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Init regression performance",PARAMS.setRegressionPerformance+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Noise",PARAMS.oracleNoiseRange+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning speed",PARAMS.learningSpeed+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Regression points",PARAMS.regressionPoints+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("EXPLORATION")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Random Exploration",PARAMS.randomExploration+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Continous Exploration",PARAMS.continousExploration+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Limited To SpaceZone",PARAMS.limitedToSpaceZone+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Exploration Increment",PARAMS.explorationIncrement+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Exploration Widht",PARAMS.explorationWidht+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("NCS")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Conflicts",PARAMS.setConflictDetection+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Concurrences",PARAMS.setConcurrenceDetection+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Incompetences",PARAMS.setVoidDetection2+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Ambiguities",PARAMS.setFrontierRequest+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Model",PARAMS.setSelfModelRequest+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learn From Neighbors",PARAMS.setLearnFromNeighbors+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Dream",PARAMS.setDream+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("OTHER")));
		xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForLearningFromNeighbors",PARAMS.nbOfNeighborForLearningFromNeighbors+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForContexCreationWithouOracle",PARAMS.nbOfNeighborForContexCreationWithouOracle+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForVoidDetectionInSelfLearning",PARAMS.nbOfNeighborForVoidDetectionInSelfLearning+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
	}


	private static void ellsaTest(HashMap<String, ArrayList<Double>> data) {
		AMOEBA amoeba = new AMOEBA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
		amoeba.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(amoeba);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);


		amoeba.allowGraphicalScheduler(false);
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
		amoeba.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
		amoeba.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

		amoeba.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
		amoeba.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
		amoeba.data.isDream = PARAMS.setDream;
		amoeba.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
		amoeba.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

		amoeba.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
		amoeba.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
		amoeba.data.isAutonomousMode = PARAMS.setAutonomousMode;


		World.minLevel = PARAMS.traceLevel;
		
		

		
		
		for (int i = 0; i < PARAMS.nbCycle; ++i) {
			amoeba.cycle();
		}

		double errorsMean = 0;

		for (int i = 0; i < PARAMS.nbCycleTest; ++i) {
			double currentError = studiedSystem.getErrorOnRequest(amoeba);
			errorsMean += currentError;

		}
		errorsMean = errorsMean/(PARAMS.nbCycleTest);

		HashMap<String, Double> mappingScores = amoeba.getHeadAgent().getMappingScores();
		System.out.println(mappingScores);
		HashMap<REQUEST, Integer> requestCounts = amoeba.data.requestCounts;
		System.out.println(requestCounts);
		System.out.println(errorsMean*100);

		data.get("mappingScore").add(mappingScores.get("CTXT"));
		data.get("imprecisionScore").add(mappingScores.get("CONF") + mappingScores.get("CONC") + mappingScores.get("VOIDS"));
		data.get("conflictVol").add(mappingScores.get("CONF"));
		data.get("concurrenceVol").add(mappingScores.get("CONC"));
		data.get("voidVol").add(mappingScores.get("VOIDS"));
		data.get("randomRequests").add(studiedSystem.getRandomRequestCounts());
		data.get("activeRequests").add(studiedSystem.getActiveRequestCounts());
		data.get("selfRequests").add(studiedSystem.getSelfRequestCounts());
		data.get("conflictRequests").add((double)requestCounts.get(REQUEST.CONFLICT));
		data.get("concurrenceRequests").add((double)requestCounts.get(REQUEST.CONCURRENCE));
		data.get("frontierRequests").add((double)requestCounts.get(REQUEST.FRONTIER));
		data.get("voidRequests").add((double)requestCounts.get(REQUEST.VOID));
		data.get("modelRequests").add((double)requestCounts.get(REQUEST.MODEL));
		data.get("neighborRequests").add((double)requestCounts.get(REQUEST.NEIGHBOR));
		data.get("fusionRequests").add((double)requestCounts.get(REQUEST.FUSION));
		data.get("restructureRequests").add((double)requestCounts.get(REQUEST.RESTRUCTURE));
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
