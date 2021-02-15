package experiments.nDimensionsLaunchers;





import agents.head.REQUEST;
import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import utils.CSVWriter;
import utils.Pair;
import utils.TRACE;
import utils.TRACE_LEVEL;

import java.io.File;
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
        PARAMS.validityRangesPrecision =  Double.parseDouble(args[5]);
        PARAMS.neighborhoodRadiusCoefficient = Integer.parseInt(args[6]);
        PARAMS.influenceRadiusCoefficient = Double.parseDouble(args[7]);
        PARAMS.modelErrorMargin = Double.parseDouble(args[8]);

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

        PARAMS.setisCreationWithNeighbor = Boolean.parseBoolean(args[24]);




        PARAMS.model = args[25];
        PARAMS.setbootstrapCycle = Integer.parseInt(args[26]);



        PARAMS.exogenousLearningWeight = Double.parseDouble(args[27]);
        PARAMS.endogenousLearningWeight = Double.parseDouble(args[28]);

        PARAMS.LEARNING_WEIGHT_ACCURACY = Double.parseDouble(args[29]);
        PARAMS.LEARNING_WEIGHT_PROXIMITY = Double.parseDouble(args[30]);
        PARAMS.LEARNING_WEIGHT_EXPERIENCE = Double.parseDouble(args[31]);
        PARAMS.LEARNING_WEIGHT_GENERALIZATION = Double.parseDouble(args[32]);

        PARAMS.EXPLOITATION_WEIGHT_PROXIMITY = Double.parseDouble(args[33]);
        PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE = Double.parseDouble(args[34]);
        PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION = Double.parseDouble(args[35]);

        PARAMS.perceptionsGenerationCoefficient = Double.parseDouble(args[36]);

        PARAMS.modelSimilarityThreshold = Double.parseDouble(args[37]);

        PARAMS.maxRangeRadiusCoefficient = Double.parseDouble(args[38]);
        PARAMS.rangeSimilarityCoefficient = Double.parseDouble(args[39]);
        PARAMS.minimumRangeCoefficient = Double.parseDouble(args[40]);

        PARAMS.isAllContextSearchAllowedForLearning = Boolean.parseBoolean(args[41]);
        PARAMS.isAllContextSearchAllowedForExploitation = Boolean.parseBoolean(args[42]);

        PARAMS.probabilityOfRangeAmbiguity = Double.parseDouble(args[43]);

        PARAMS.extension = args[44];


        experimentation();

        System.out.print(" DONE");

        System.exit(1);
    }


    public static void experimentation() {

        xpCSV = new CSVWriter(
                PARAMS.model
                +"_PARAMS_" + PARAMS.extension

        );

        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

        Pair<ArrayList<List<String>>,HashMap<String, ArrayList<Double>>> dataPair = WRITER.getData();
        ArrayList<List<String>> dataStrings = dataPair.getA();
        HashMap<String, ArrayList<Double>> data = dataPair.getB();

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

        WRITER.writeData(xpCSV, data, dataStrings, total, mean);

        data = null;
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

        /*System.out.println(mappingScores);
        System.out.println(requestCounts);
        System.out.println(predictionError*100 + " [+-" + predictionDispersion*100 + "]");
        System.out.println(ellsa.getContexts().size() + " Agents");*/

        WRITER.setData(data, ellsa, mappingScores, requestCounts, executionTimes, predictionError, predictionDispersion, averageLearningCycleTimeDouble, learningcycleTimeDispersionDouble, averageExploitationCycleTimeDouble, ExploitationcycleTimeDispersionDouble);


        ellsa = null;
        studiedSystem = null;

    }




}