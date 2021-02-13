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
import java.text.SimpleDateFormat;
import java.util.*;

public class LaunchExampleXPWithArgsManualy {

    private static CSVWriter xpCSV;

    public static void main (String[] args)  {





        PARAMS.dimension = 2;
        PARAMS.configFile =  "twoDimensionsLauncher" +".xml";

        /*PARAMS.dimension = 3;
        PARAMS.configFile =  "threeDimensionsLauncher" +".xml";*/

        /*PARAMS.dimension = 10;
        PARAMS.configFile =  "tenDimensionsLauncher" +".xml";*/

        /*PARAMS.dimension = 4;
        PARAMS.configFile =  "fourDimensionsLauncher" +".xml";*/

        /*PARAMS.dimension = 5;
        PARAMS.configFile =  "fiveDimensionsLauncher" +".xml";*/

        PARAMS.nbLearningCycle = 1000;
        PARAMS.nbExploitationCycle = (int)(PARAMS.nbLearningCycle * 0.25);
        PARAMS.nbEpisodes = 2;

        // Neighborhood
        PARAMS.validityRangesPrecision =  0.08;
        PARAMS.neighborhoodRadiusCoefficient = 2;
        PARAMS.influenceRadiusCoefficient = 0.50;
        PARAMS.modelErrorMargin = 1;

        // Learning
        PARAMS.setActiveLearning = true;
        PARAMS.setSelfLearning = false;

        //NCS

        PARAMS.setConflictDetection = true;
        PARAMS.setConcurrenceDetection = true;
        PARAMS.setVoidDetection = true;
        PARAMS.setSubVoidDetection = false;
        PARAMS.setFrontierRequest = true;
        PARAMS.setSelfModelRequest = true;
        PARAMS.setFusionResolution = true;
        PARAMS.setRestructureResolution = true;

        PARAMS.setDream = false;
        PARAMS.setDreamCycleLaunch = 1500;

        PARAMS.setLearnFromNeighbors = false;
        PARAMS.setisCreationWithNeighbor = true;

        PARAMS.nbOfNeighborForLearningFromNeighbors = 1;
        PARAMS.nbOfNeighborForContexCreationWithouOracle = 5000;
        PARAMS.nbOfNeighborForVoidDetectionInSelfLearning =  PARAMS.nbOfNeighborForContexCreationWithouOracle;


//        PARAMS.model = "multi";
//        PARAMS.model = "disc";
//        PARAMS.model = "square";
        PARAMS.model = "squareFixed";
//        PARAMS.model = "triangle";
//        PARAMS.model = "gaussian";
//        PARAMS.model = "polynomial";
//        PARAMS.model = "gaussianCos2";
//        PARAMS.model = "cosX";
//        PARAMS.model = "cosSinX";
//        PARAMS.model = "rosenbrock";
//        PARAMS.model = "squareSplitTriangle";
//        PARAMS.model = "squareSplitFixed";


        String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        PARAMS.extension = dateAndHour;

        PARAMS.setbootstrapCycle = 10;

        PARAMS.exogenousLearningWeight = 0.1;
        PARAMS.endogenousLearningWeight = 0.1;

        PARAMS.LEARNING_WEIGHT_ACCURACY = 1.0;
        PARAMS.LEARNING_WEIGHT_PROXIMITY = 0.0;
        PARAMS.LEARNING_WEIGHT_EXPERIENCE = 1.0;
        PARAMS.LEARNING_WEIGHT_GENERALIZATION = 1.0;

        PARAMS.EXPLOITATION_WEIGHT_PROXIMITY = 1.0;
        PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE = 1.0;
        PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION = 1.0;

        PARAMS.perceptionsGenerationCoefficient = 0.1;

        PARAMS.modelSimilarityThreshold = 0.001;

        PARAMS.maxRangeRadiusCoefficient = 2.0;
        PARAMS.rangeSimilarityCoefficient = 0.375;
        PARAMS.minimumRangeCoefficient = 0.25;

        PARAMS.isAllContextSearchAllowedForLearning = true;
        PARAMS.isAllContextSearchAllowedForExploitation = true;

        PARAMS.probabilityOfRangeAmbiguity = 0.1;

        TRACE.minLevel = TRACE_LEVEL.OFF;

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

        WRITER.writeData(xpCSV,data, dataStrings, total, mean);

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
            //System.out.println(ellsa.getCycle() + " " + ellsa.getContexts().size());

            if(ellsa.getCycle()%200 == 0){
                // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
                long heapMaxSize = (long)Runtime.getRuntime().maxMemory();
                System.out.println("heapMaxSize\t\t" + heapMaxSize);

                // Get current size of heap in bytes
                long heapSize = (Runtime.getRuntime().totalMemory());
                System.out.println("heapSize\t\t" + heapSize);

                // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
                long heapFreeSize = Runtime.getRuntime().freeMemory();;
                System.out.println("heapFreeSize\t" + heapFreeSize + "\n");
            }
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

        System.out.println(ellsa.getContexts().get(0).getVolume() + " Vol");
        System.out.println(ellsa.getHeadAgent().getMinMaxVolume()+ " MinMaxVol");

        System.out.println(ellsa.data.minMaxPerceptsStatesAfterBoostrap);


        WRITER.setData(data, ellsa, mappingScores, requestCounts, executionTimes, predictionError, predictionDispersion, averageLearningCycleTimeDouble, learningcycleTimeDispersionDouble, averageExploitationCycleTimeDouble, ExploitationcycleTimeDispersionDouble);








        ellsa = null;
        studiedSystem = null;

    }





}
