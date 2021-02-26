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
import fr.irit.smac.amak.tools.Log;
import kernel.ELLSA;
import kernel.StudiedSystem;
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


		Log.enabled = false;
	}
	


	public static void start() throws Exception {

		String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
		String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
		xpCSV = new CSVWriter(dateAndHour+"_Dim_"+PARAMS.dimension
				+"_LearningCycles_" + PARAMS.nbLearningCycle
				+"_ExplotationCycles_" + PARAMS.nbExploitationCycle
				+"_Episodes_" + PARAMS.nbEpisodes
				+"_ActiveLearning_" + PARAMS.setActiveLearning
				+"_SelfLearning_" + PARAMS.setSelfLearning
				+"_Notes_" + PARAMS.model

		);





		writeParams(PARAMS.model);


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
		for (int i = 0; i < PARAMS.nbEpisodes; ++i) {
			System.out.print(i + " ");
			ellsaTest(data);
		}
		System.out.println("");
		double total = (System.currentTimeMillis()- start)/1000;
		double mean = total/ PARAMS.nbEpisodes;
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
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning cycles",PARAMS.nbLearningCycle +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Testting cycles",PARAMS.nbExploitationCycle +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning episodes",PARAMS.nbEpisodes +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Space size",PARAMS.spaceSize*4+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Mapping error",PARAMS.validityRangesPrecision +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Active Learning",PARAMS.setActiveLearning+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Self Learning",PARAMS.setSelfLearning+"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Init regression performance",PARAMS.modelErrorMargin +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

		xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Noise",PARAMS.noiseRange +"")));
		xpCSV.write(new ArrayList<>(Arrays.asList("Learning speed",PARAMS.exogenousLearningWeight +"")));
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
		xpCSV.write(new ArrayList<>(Arrays.asList("Incompetences",PARAMS.setVoidDetection +"")));
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
		ELLSA ellsa = new ELLSA(null,  null);
		StudiedSystem studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
		ellsa.setStudiedSystem(studiedSystem);
		IBackupSystem backupSystem = new BackupSystem(ellsa);
		File file = new File("resources/"+PARAMS.configFile);
		backupSystem.load(file);


		ellsa.allowGraphicalScheduler(false);
		ellsa.setRenderUpdate(false);
		ellsa.data.PARAM_exogenousLearningWeight = PARAMS.exogenousLearningWeight;
		ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;
		ellsa.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
		ellsa.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;
		ellsa.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
		ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
		ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection;
		ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
		ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
		ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
		ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
		ellsa.data.PARAM_NCS_isFusionResolution = PARAMS.setFusionResolution;
		ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setRestructureResolution;
		ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


		ellsa.data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS.setCoopLearningASUPPRIMER;

		ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
		ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
		ellsa.data.PARAM_isDream = PARAMS.setDream;
		ellsa.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;
		ellsa.data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
		ellsa.data.PARAM_creationNeighborNumberForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

		ellsa.getEnvironment().setMappingErrorAllowed(PARAMS.validityRangesPrecision);
		ellsa.data.PARAM_modelErrorMargin = PARAMS.modelErrorMargin;
		ellsa.data.PARAM_neighborhoodRadiusCoefficient = PARAMS.neighborhoodRadiusCoefficient;
		ellsa.data.PARAM_influenceRadiusCoefficient = PARAMS.influenceRadiusCoefficient;


		ellsa.data.PARAM_bootstrapCycle = PARAMS.setbootstrapCycle;

		ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;


		ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;

		ellsa.setSubPercepts(experiments.roboticDistributedArm.PARAMS.subPercepts);

		
		
		/*for (int i = 0; i < PARAMS.nbLearningCycle; ++i) {
			ellsa.cycle();
		}*/
		int count = 0;
		boolean bug = false;
		int agents = -2;
		int oldAgents = 0;
		while(!bug){
			ellsa.cycle();
			oldAgents = agents;
			agents = ellsa.getContexts().size();
			if(agents == oldAgents){
				count++;
			}else{
				count = 0;
			}

			if(count>50){
				bug=true;
			}
		}

		for(int k=0;k<25;k++){
			System.out.println(k + " " + ellsa.data.executionTimesSums[k]);
		}

		/*while(ellsa.getContexts().size()>5 || ellsa.getCycle()<50){
			ellsa.cycle();
		}
		System.out.println(ellsa.getCycle());*/

		/*while(ellsa.data.STATE_DreamCompleted!=1){
			ellsa.cycle();
		}*/


		double errorsMean = 0;

		for (int i = 0; i < PARAMS.nbExploitationCycle; ++i) {
			double currentError = studiedSystem.getErrorOnRequest(ellsa);
			errorsMean += currentError;

		}
		errorsMean = errorsMean/(PARAMS.nbExploitationCycle);

		HashMap<String, Double> mappingScores = ellsa.getHeadAgent().getMappingScores();
		System.out.println(mappingScores);
		HashMap<REQUEST, Integer> requestCounts = ellsa.data.requestCounts;
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
		data.get("nbAgents").add((double) ellsa.getContexts().size());
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
