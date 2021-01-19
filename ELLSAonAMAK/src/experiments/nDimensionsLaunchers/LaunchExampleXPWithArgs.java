package experiments.nDimensionsLaunchers;





import agents.head.REQUEST;
import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.CSVWriter;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class LaunchExampleXPWithArgs {

    private static CSVWriter xpCSV;

    public static void main (String[] args)  {

        TRACE.minLevel = TRACE_LEVEL.OFF;

        PARAMS.dimension = Integer.parseInt(args[0]);
        PARAMS.configFile = args[1] +".xml";

        PARAMS.nbLearningCycle = Integer.parseInt(args[2]);
        PARAMS.nbExploitationCycle = Integer.parseInt(args[3]);
        PARAMS.nbEpisodes = Integer.parseInt(args[4]);

        // Neighborhood
        PARAMS.mappingErrorAllowed =  Double.parseDouble(args[5]);
        PARAMS.setNeighborhoodMultiplicator = Integer.parseInt(args[6]);
        PARAMS.setExternalContextInfluenceRatio = Double.parseDouble(args[7]);
        PARAMS.setRegressionPerformance = Double.parseDouble(args[8]);

        // Learning
        PARAMS.setActiveLearning = Boolean.parseBoolean(args[9]);
        PARAMS.setSelfLearning = Boolean.parseBoolean(args[10]);

        //NCS
        PARAMS.setConflictDetection = Boolean.parseBoolean(args[11]);
        PARAMS.setConcurrenceDetection = Boolean.parseBoolean(args[12]);
        PARAMS.setVoidDetection = Boolean.parseBoolean(args[13]);
        PARAMS.setSubVoidDetection = Boolean.parseBoolean(args[14]);
        PARAMS.setFrontierRequest = Boolean.parseBoolean(args[15]);
        PARAMS.setSelfModelRequest = Boolean.parseBoolean(args[16]);
        PARAMS.setFusionResolution = Boolean.parseBoolean(args[17]);
        PARAMS.setRestructureResolution = Boolean.parseBoolean(args[18]);

        PARAMS.setDream = Boolean.parseBoolean(args[19]);
        PARAMS.setDreamCycleLaunch = Integer.parseInt(args[20]);

        PARAMS.setLearnFromNeighbors = Boolean.parseBoolean(args[21]);
        PARAMS.nbOfNeighborForLearningFromNeighbors = Integer.parseInt(args[22]);
        PARAMS.nbOfNeighborForContexCreationWithouOracle = Integer.parseInt(args[23]);
        PARAMS.nbOfNeighborForVoidDetectionInSelfLearning =  PARAMS.nbOfNeighborForContexCreationWithouOracle;




        PARAMS.model = args[24];
        PARAMS.setbootstrapCycle = Integer.parseInt(args[25]);
        PARAMS.extension = args[26];




        experimentation();

        System.out.print(" DONE");

        System.exit(1);
    }


    public static void experimentation() {

        xpCSV = new CSVWriter(PARAMS.model
                +"_Dim_" + PARAMS.dimension
                +"_Lrn_" + PARAMS.nbLearningCycle
                +"_Exp_" + PARAMS.nbExploitationCycle
                +"_Eps_" + PARAMS.nbEpisodes
                +"_PARAMS_" + PARAMS.extension

        );

        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

        HashMap<String, ArrayList<Double>> data = new HashMap<>();
        List<String> dataStringsVolumes = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol");

        List<String> dataStringsPrediction = Arrays.asList("predictionError", "predictionErrorDeviation");

        List<String> dataStringsEndoRequests = Arrays.asList("conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests","subvoidRequests", "modelRequests", "rdmRequests", "dreamRequests", "endogenousLearningSituations","fusionRequests","restructureRequests");

        //List<String> dataStringsNCS =

        List<String> dataStringsTimeExecution = Arrays.asList("learningCycleExecutionTime","exploitationCycleExecutionTime", "learningCycleExecutionTimeDeviation","exploitationCycleExecutionTimeDeviation",
                "perceptsTimeExecution", "contextsTimeExecution" , "headTimeExecution", "NCSTimeExecution"
                , "NCS_UselessnessTimeExecution", "NCS_IncompetendHeadTimeExecution", "NCS_ConcurrenceAndConflictTimeExecution", "NCS_Create_New_ContextTimeExecution", "NCS_OvermappingTimeExecution", "NCS_ChildContextTimeExecution", "NCS_PotentialRequestTimeExecution", "NCS_DreamPotentialRequestTimeExecution");

        List<String> dataStringsOther = Arrays.asList("localMinima","nbAgents","neighborsCounts");

        ArrayList<List<String>> dataStrings = new ArrayList<>(Arrays.asList(dataStringsVolumes, dataStringsEndoRequests, dataStringsTimeExecution, dataStringsOther, dataStringsPrediction ));

        for(List<String> dataString : dataStrings){
            for (String dataName : dataString){
                data.put(dataName, new ArrayList<>());
            }
        }

        double start = System.currentTimeMillis();

        for (int i = 0; i < PARAMS.nbEpisodes; ++i) {
            //System.out.print(i + " ");
            learningEpisode(data);
        }
        //System.out.println(" ");
        double total = (System.currentTimeMillis()- start)/1000;
        double mean = total/ PARAMS.nbEpisodes;
        System.out.println("[TIME MEAN] " + mean + " s");
        System.out.println("[TIME TOTAL] " + total + " s");

        writeData(data, dataStrings, total, mean);

        data = null;
    }

    private static void writeData(HashMap<String, ArrayList<Double>> data, ArrayList<List<String>> dataStrings, double total, double mean) {
        writeParams();

        xpCSV.write(new ArrayList<>(Arrays.asList("meanTime", ""+mean)));
        xpCSV.write(new ArrayList<>(Arrays.asList("totalTime",""+total )));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for(List<String> dataString : dataStrings){

            xpCSV.write(new ArrayList<>(Arrays.asList("#")));
            xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

            for (String dataName : dataString){

                OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
                Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();

                OptionalDouble minScore = data.get(dataName).stream().mapToDouble(a->a).min();
                OptionalDouble maxScore = data.get(dataName).stream().mapToDouble(a->a).max();

                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Average",averageScore.getAsDouble()+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Min" ,"" + minScore.getAsDouble())));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Max" ,"" + maxScore.getAsDouble())));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Deviation" ,"" + Math.sqrt(deviationScore/data.get(dataName).size()))));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

            }

            xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
        }

        /*for (String dataName : dataStringsVolumes){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",averageScore.getAsDouble()*100+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" ,"" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));



        }*/


        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        //Create the formatter for round the values of scores
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
        //System.out.println("ROUNDED");
        xpCSV.write(new ArrayList<>(Arrays.asList("ROUNDED")));

        for(List<String> dataString : dataStrings){

            xpCSV.write(new ArrayList<>(Arrays.asList("#")));
            xpCSV.write(new ArrayList<>(Arrays.asList(" ")));


            for (String dataName : dataString){

                OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
                Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
                OptionalDouble minScore = data.get(dataName).stream().mapToDouble(a->a).min();
                OptionalDouble maxScore = data.get(dataName).stream().mapToDouble(a->a).max();

                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Average_Rounded",df.format(averageScore.getAsDouble())+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Min" ,"" + df.format(minScore.getAsDouble()))));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Max" ,"" + df.format(maxScore.getAsDouble()))));
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"_Deviation_Rounded" , df.format(Math.sqrt(deviationScore/data.get(dataName).size())))));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

            }
        }


        /*for (String dataName : dataStringsVolumes){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //System.out.println(dataName +" [AVERAGE] " + df.format(averageScore.getAsDouble()*100) + " - " + "[DEVIATION] " +df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",df.format(averageScore.getAsDouble()*100)+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" , df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())))));


        }*/
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));



        xpCSV.close();
    }

    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {
        ELLSA ellsa = new ELLSA(null,  null);
        StudiedSystem studiedSystem = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File("resources/"+PARAMS.configFile);
        backupSystem.load(file);


        ellsa.allowGraphicalScheduler(false);
        ellsa.setRenderUpdate(false);
        ellsa.data.PARAM_learningSpeed = PARAMS.learningSpeed;
        ellsa.data.PARAM_numberOfPointsForRegression_ASUPPRIMER = PARAMS.regressionPoints;

        ellsa.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
        ellsa.data.PARAM_initRegressionPerformance = PARAMS.setRegressionPerformance;
        ellsa.data.PARAM_neighborhoodMultiplicator = PARAMS.setNeighborhoodMultiplicator;
        ellsa.data.PARAM_externalContextInfluenceRatio = PARAMS.setExternalContextInfluenceRatio;

        ellsa.data.PARAM_isActiveLearning = PARAMS.setActiveLearning;
        ellsa.data.PARAM_isSelfLearning = PARAMS.setSelfLearning;


        ellsa.data.PARAM_NCS_isConflictDetection = PARAMS.setConflictDetection;
        ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS.setConcurrenceDetection;
        ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
        ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setVoidDetection;
        ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubVoidDetection;
        ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setFrontierRequest;
        ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setSelfModelRequest;
        ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
        ellsa.data.PARAM_isDream = PARAMS.setDream;
        ellsa.data.PARAM_NCS_isFusionResolution = PARAMS.setFusionResolution;
        ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setRestructureResolution;

        ellsa.data.isCoopLearningWithoutOracle_ASUPPRIMER = PARAMS.setCoopLearningASUPPRIMER;


        ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsa.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;
        ellsa.data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsa.data.PARAM_nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

        ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;


        ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;

        ellsa.setSubPercepts(experiments.roboticDistributedArm.PARAMS.subPercepts);

        ellsa.data.PARAM_bootstrapCycle = PARAMS.setbootstrapCycle;


        ArrayList<Double> allLearningCycleTimes = new ArrayList<>();
        ArrayList<Double> allExploitationCycleTimes = new ArrayList<>();

        for (int i = 0; i < PARAMS.nbLearningCycle; ++i) {
            double start = System.currentTimeMillis();
            ellsa.cycle();
            allLearningCycleTimes.add(System.currentTimeMillis()- start);

        }
		/*while(ellsa.getContexts().size()>5 || ellsa.getCycle()<50){
			ellsa.cycle();
		}
		System.out.println(ellsa.getCycle());*/

		/*while(ellsa.data.STATE_DreamCompleted!=1){
			ellsa.cycle();
		}*/

        HashMap<String, Double> mappingScores = ellsa.getHeadAgent().getMappingScores();
        HashMap<REQUEST, Integer> requestCounts = ellsa.data.requestCounts;
        double[] executionTimes = ellsa.data.executionTimesSums;

        ArrayList<Double> allPredictionErrors = new ArrayList<>();

        for (int i = 0; i < PARAMS.nbExploitationCycle; ++i) {
            double start = System.currentTimeMillis();
            allPredictionErrors.add(new Double(studiedSystem.getErrorOnRequest(ellsa)));
            allExploitationCycleTimes.add(System.currentTimeMillis()- start);

        }

        OptionalDouble averageError = allPredictionErrors.stream().mapToDouble(a->a).average();
        Double errorDispersion = allPredictionErrors.stream().mapToDouble(a->Math.pow((a- averageError.getAsDouble()),2)).sum();
        double predictionError = averageError.getAsDouble();
        double predictionDispersion = Math.sqrt(errorDispersion /allPredictionErrors.size());

        OptionalDouble averageLearningCycleTime = allLearningCycleTimes.stream().mapToDouble(a->a).average();
        Double learningcycleTimeDispersion = allLearningCycleTimes.stream().mapToDouble(a->Math.pow((a- averageLearningCycleTime.getAsDouble()),2)).sum();
        double averageLearningCycleTimeDouble = averageLearningCycleTime.getAsDouble();
        double learningcycleTimeDispersionDouble = Math.sqrt(learningcycleTimeDispersion /allLearningCycleTimes.size());

        OptionalDouble averageExploitationCycleTime = allExploitationCycleTimes.stream().mapToDouble(a->a).average();
        Double ExploitationcycleTimeDispersion = allExploitationCycleTimes.stream().mapToDouble(a->Math.pow((a- averageExploitationCycleTime.getAsDouble()),2)).sum();
        double averageExploitationCycleTimeDouble = averageExploitationCycleTime.getAsDouble();
        double ExploitationcycleTimeDispersionDouble = Math.sqrt(ExploitationcycleTimeDispersion /allExploitationCycleTimes.size());

        System.out.println(mappingScores);
        System.out.println(requestCounts);
        System.out.println(predictionError*100 + " [+-" + predictionDispersion*100 + "]");
        System.out.println(ellsa.getContexts().size() + " Agents");

        // Volumes
        data.get("mappingScore").add(mappingScores.get("CTXT"));
        data.get("imprecisionScore").add(mappingScores.get("CONF") + mappingScores.get("CONC") + mappingScores.get("VOIDS"));
        data.get("conflictVol").add(mappingScores.get("CONF"));
        data.get("concurrenceVol").add(mappingScores.get("CONC"));
        data.get("voidVol").add(mappingScores.get("VOIDS"));

        // Predictions
        data.get("predictionError").add(predictionError);
        data.get("predictionErrorDeviation").add(predictionDispersion);

        // Endo Requests
        data.get("conflictRequests").add((double)requestCounts.get(REQUEST.CONFLICT));
        data.get("concurrenceRequests").add((double)requestCounts.get(REQUEST.CONCURRENCE));
        data.get("frontierRequests").add((double)requestCounts.get(REQUEST.FRONTIER));
        data.get("voidRequests").add((double)requestCounts.get(REQUEST.VOID));
        data.get("subvoidRequests").add((double)requestCounts.get(REQUEST.SUBVOID));
        data.get("modelRequests").add((double)requestCounts.get(REQUEST.MODEL));
        data.get("rdmRequests").add((double)requestCounts.get(REQUEST.RDM));
        data.get("dreamRequests").add((double)requestCounts.get(REQUEST.DREAM));
        data.get("endogenousLearningSituations").add((double)requestCounts.get(REQUEST.NEIGHBOR));
        data.get("fusionRequests").add((double)requestCounts.get(REQUEST.FUSION));
        data.get("restructureRequests").add((double)requestCounts.get(REQUEST.RESTRUCTURE));



        // Executions times
        data.get("learningCycleExecutionTime").add(averageLearningCycleTimeDouble);
        data.get("exploitationCycleExecutionTime").add(averageExploitationCycleTimeDouble);
        data.get("learningCycleExecutionTimeDeviation").add(learningcycleTimeDispersionDouble);
        data.get("exploitationCycleExecutionTimeDeviation").add(ExploitationcycleTimeDispersionDouble);

        data.get("perceptsTimeExecution").add(executionTimes[1]);
        data.get("contextsTimeExecution").add(executionTimes[2]);
        data.get("headTimeExecution").add(executionTimes[3]);

        data.get("NCSTimeExecution").add(executionTimes[8]);
        data.get("NCS_UselessnessTimeExecution").add(executionTimes[9]);
        data.get("NCS_IncompetendHeadTimeExecution").add(executionTimes[10]);
        data.get("NCS_ConcurrenceAndConflictTimeExecution").add(executionTimes[11]);
        data.get("NCS_Create_New_ContextTimeExecution").add(executionTimes[12]);
        data.get("NCS_OvermappingTimeExecution").add(executionTimes[13]);
        data.get("NCS_ChildContextTimeExecution").add(executionTimes[14]);
        data.get("NCS_PotentialRequestTimeExecution").add(executionTimes[15]);
        data.get("NCS_DreamPotentialRequestTimeExecution").add(executionTimes[16]);

        // Other
        data.get("nbAgents").add((double) ellsa.getContexts().size());
        data.get("localMinima").add((double) ellsa.data.countLocalMinina);
        data.get("neighborsCounts").add((double)ellsa.data.neighborsCounts/ellsa.getCycle());







        ellsa = null;
        studiedSystem = null;

    }


    private static void writeParams() {
        xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
        xpCSV.write(new ArrayList<>(Arrays.asList("SET")));
        xpCSV.write(new ArrayList<>(Arrays.asList("dimension", PARAMS.dimension+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("model",PARAMS.model)));
        xpCSV.write(new ArrayList<>(Arrays.asList("learningCycles", PARAMS.nbLearningCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("exploitatingCycles", PARAMS.nbExploitationCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("episodes", PARAMS.nbEpisodes +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("spaceSize", PARAMS.spaceSize*4+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("precisionRange", PARAMS.mappingErrorAllowed+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("neighborhoodSize", PARAMS.setNeighborhoodMultiplicator+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("influenceRatio", PARAMS.setExternalContextInfluenceRatio+"")));



        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isActiveLearning", PARAMS.setActiveLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isSelfLearning", PARAMS.setSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("goalXYError")));
        xpCSV.write(new ArrayList<>(Arrays.asList("errorMargin", PARAMS.setRegressionPerformance+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
        xpCSV.write(new ArrayList<>(Arrays.asList("noise", PARAMS.oracleNoiseRange+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("learningSpeed", PARAMS.learningSpeed+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("regressionPoints", PARAMS.regressionPoints+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("EXPLORATION")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isRandomExploration", PARAMS.randomExploration+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isContinuousExploration", PARAMS.continousExploration+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isLimitedToSpaceZone", PARAMS.limitedToSpaceZone+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("explorationIncrement", PARAMS.explorationIncrement+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("explorationWidth", PARAMS.explorationWidht+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("bootstrapCycle", PARAMS.setbootstrapCycle+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("NCS")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isConflictNCS", PARAMS.setConflictDetection+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isConcurenceNCS", PARAMS.setConcurrenceDetection+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isIncompetenceNCS", PARAMS.setVoidDetection +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isSubVoidDetection", PARAMS.setSubVoidDetection+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isAmbiguityNCS", PARAMS.setFrontierRequest+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isModelNCS", PARAMS.setSelfModelRequest+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isLearnFromNeighbors", PARAMS.setLearnFromNeighbors+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isDream", PARAMS.setDream+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("NCS PARAMS")));

        xpCSV.write(new ArrayList<>(Arrays.asList("dreamLaunch", PARAMS.setDreamCycleLaunch+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForLearningFromNeighbors", PARAMS.nbOfNeighborForLearningFromNeighbors+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForContexCreationWithouOracle", PARAMS.nbOfNeighborForContexCreationWithouOracle+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForVoidDetectionInSelfLearning", PARAMS.nbOfNeighborForVoidDetectionInSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
    }


}