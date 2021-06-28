package experiments.mathematicalModels;





import agents.head.REQUEST;
import agents.head.SITUATION;
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

public class LaunchExampleXPWithArgsManualy_OLD {

    private static CSVWriter xpCSV;

    public static void main (String[] args)  {





        PARAMS_OLD.dimension = 2;
        PARAMS_OLD.configFile =  "twoDimensionsLauncher" +".xml";

        /*PARAMS_OLD.dimension = 3;
        PARAMS_OLD.configFile =  "threeDimensionsLauncher" +".xml";*/

//        PARAMS_OLD.dimension = 10;
//        PARAMS_OLD.configFile =  "tenDimensionsLauncher" +".xml";

        /*PARAMS_OLD.dimension = 4;
        PARAMS_OLD.configFile =  "fourDimensionsLauncher" +".xml";*/

        /*PARAMS_OLD.dimension = 5;
        PARAMS_OLD.configFile =  "fiveDimensionsLauncher" +".xml";*/

        PARAMS_OLD.nbLearningCycle = 1000;
        PARAMS_OLD.nbExploitationCycle = 250;
        PARAMS_OLD.nbEpisodes = 1;

        // Neighborhood
        PARAMS_OLD.validityRangesPrecision =  0.1;
        PARAMS_OLD.neighborhoodRadiusCoefficient = 2;
        PARAMS_OLD.influenceRadiusCoefficient = 0.50;
        PARAMS_OLD.modelErrorMargin = 1.0;

        // Learning
        PARAMS_OLD.setActiveLearning = false;
        PARAMS_OLD.setSelfLearning = true;
        PARAMS_OLD.setCooperativeNeighborhoodLearning = true;

//        PARAMS_OLD.setActiveLearning = true;
//        PARAMS_OLD.setSelfLearning = false;
//        PARAMS_OLD.setLearnFromNeighbors = false;

        //NCS

        PARAMS_OLD.setModelAmbiguityDetection = true;
        PARAMS_OLD.setConflictDetection = true;
        PARAMS_OLD.setConcurrenceDetection = true;
        PARAMS_OLD.setIncompetenceDetection = true;
        PARAMS_OLD.setCompleteRedundancyDetection = true;
        PARAMS_OLD.setPartialRedundancyDetection = true;
        PARAMS_OLD.setRangeAmbiguityDetection = true;

        PARAMS_OLD.setSubIncompetencedDetection = false;

        PARAMS_OLD.setDream = false;
        PARAMS_OLD.setDreamCycleLaunch = 1500;


        PARAMS_OLD.setisCreationWithNeighbor = true;

        PARAMS_OLD.nbOfNeighborForLearningFromNeighbors = 1;
        PARAMS_OLD.nbOfNeighborForContexCreationWithouOracle = 7;
        PARAMS_OLD.nbOfNeighborForVoidDetectionInSelfLearning =  PARAMS_OLD.nbOfNeighborForContexCreationWithouOracle;


//        PARAMS_OLD.model = "multi";
//        PARAMS_OLD.model = "disc";
//        PARAMS_OLD.model = "square";
        PARAMS_OLD.model = "squareFixed";
//        PARAMS_OLD.model = "triangle";
//        PARAMS_OLD.model = "gaussian";
//        PARAMS_OLD.model = "polynomial";
//        PARAMS_OLD.model = "gaussianCos2";
//        PARAMS_OLD.model = "cosX";
//        PARAMS_OLD.model = "cosSinX";
//        PARAMS_OLD.model = "rosenbrock";
//        PARAMS_OLD.model = "squareSplitTriangle";
//        PARAMS_OLD.model = "squareSplitFixed";
//        PARAMS_OLD.model = "squareDiscLos";


        String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
        PARAMS_OLD.extension = dateAndHour;

        PARAMS_OLD.setbootstrapCycle = 10;

        PARAMS_OLD.exogenousLearningWeight = 0.1;
        PARAMS_OLD.endogenousLearningWeight = 0.1;

        PARAMS_OLD.LEARNING_WEIGHT_ACCURACY = 1.0;
        PARAMS_OLD.LEARNING_WEIGHT_PROXIMITY = 0.0;
        PARAMS_OLD.LEARNING_WEIGHT_EXPERIENCE = 1.0;
        PARAMS_OLD.LEARNING_WEIGHT_GENERALIZATION = 1.0;

        PARAMS_OLD.EXPLOITATION_WEIGHT_PROXIMITY = 1.0;
        PARAMS_OLD.EXPLOITATION_WEIGHT_EXPERIENCE = 1.0;
        PARAMS_OLD.EXPLOITATION_WEIGHT_GENERALIZATION = 1.0;

        PARAMS_OLD.perceptionsGenerationCoefficient = 0.1;

        PARAMS_OLD.modelSimilarityThreshold = 0.001;

        PARAMS_OLD.maxRangeRadiusCoefficient = 2.0;
        PARAMS_OLD.rangeSimilarityCoefficient = 0.375;
        PARAMS_OLD.minimumRangeCoefficient = 0.25;

        PARAMS_OLD.isAllContextSearchAllowedForLearning = true;
        PARAMS_OLD.isAllContextSearchAllowedForExploitation = true;

        PARAMS_OLD.probabilityOfRangeAmbiguity = 0.1;

        PARAMS_OLD.transferCyclesRatio = 0.3;//0.429;

        PARAMS_OLD.nbEndoExploitationCycle = 0;
        PARAMS_OLD.setActiveExploitation = false;

        PARAMS_OLD.noiseRange = 0.0;

        TRACE.minLevel = TRACE_LEVEL.OFF;

        experimentation();

        System.out.print(" DONE");

        System.exit(1);
    }


    public static void experimentation() {

        xpCSV = new CSVWriter(
                PARAMS_OLD.model
                        +"_PARAMS_OLD_" + PARAMS_OLD.extension

        );

        // Set AMAK configuration before creating an ELLSA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;


        Pair<ArrayList<List<String>>,HashMap<String, ArrayList<Double>>> dataPair = WRITER.getData();
        ArrayList<List<String>> dataStrings = dataPair.getA();
        HashMap<String, ArrayList<Double>> data = dataPair.getB();

        double start = System.nanoTime();

        for (int i = 0; i < PARAMS_OLD.nbEpisodes; ++i) {
            //System.out.print(i + " ");
            learningEpisode(data);

        }
        //System.out.println(" ");
        double total = (System.nanoTime()- start)/1000000000;
        double mean = total/ PARAMS_OLD.nbEpisodes;
        System.out.println("[TIME MEAN] " + mean + " s");
        System.out.println("[TIME TOTAL] " + total + " s");

        WRITER.writeData(xpCSV,data, dataStrings, total, mean);

        data = null;
    }




    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {
        ELLSA ellsa = new ELLSA(null,  null);
        StudiedSystem studiedSystem = new Model_Manager(PARAMS_OLD.spaceSize, PARAMS_OLD.dimension, PARAMS_OLD.nbOfModels, PARAMS_OLD.normType, PARAMS_OLD.randomExploration, PARAMS_OLD.explorationIncrement,PARAMS_OLD.explorationWidht,PARAMS_OLD.limitedToSpaceZone, PARAMS_OLD.noiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File("resources/"+PARAMS_OLD.configFile);
        backupSystem.load(file);


        ellsa.allowGraphicalScheduler(false);
        ellsa.setRenderUpdate(false);


        ellsa.getEnvironment().setMappingErrorAllowed(PARAMS_OLD.validityRangesPrecision);
        ellsa.data.PARAM_modelErrorMargin = PARAMS_OLD.modelErrorMargin;
        ellsa.data.PARAM_bootstrapCycle = PARAMS_OLD.setbootstrapCycle;
        ellsa.data.PARAM_exogenousLearningWeight = PARAMS_OLD.exogenousLearningWeight;
        ellsa.data.PARAM_endogenousLearningWeight = PARAMS_OLD.endogenousLearningWeight;

        ellsa.data.PARAM_neighborhoodRadiusCoefficient = PARAMS_OLD.neighborhoodRadiusCoefficient;
        ellsa.data.PARAM_influenceRadiusCoefficient = PARAMS_OLD.influenceRadiusCoefficient;
        ellsa.data.PARAM_maxRangeRadiusCoefficient = PARAMS_OLD.maxRangeRadiusCoefficient;
        ellsa.data.PARAM_rangeSimilarityCoefficient = PARAMS_OLD.rangeSimilarityCoefficient;
        ellsa.data.PARAM_minimumRangeCoefficient = PARAMS_OLD.minimumRangeCoefficient;

        ellsa.data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = PARAMS_OLD.nbOfNeighborForVoidDetectionInSelfLearning;
        ellsa.data.PARAM_creationNeighborNumberForContexCreationWithouOracle = PARAMS_OLD.nbOfNeighborForContexCreationWithouOracle;

        ellsa.data.PARAM_perceptionsGenerationCoefficient = PARAMS_OLD.perceptionsGenerationCoefficient
        ;
        ellsa.data.PARAM_modelSimilarityThreshold = PARAMS_OLD.modelSimilarityThreshold;

        ellsa.data.PARAM_LEARNING_WEIGHT_ACCURACY = PARAMS_OLD.LEARNING_WEIGHT_ACCURACY;
        ellsa.data.PARAM_LEARNING_WEIGHT_PROXIMITY = PARAMS_OLD.LEARNING_WEIGHT_PROXIMITY;
        ellsa.data.PARAM_LEARNING_WEIGHT_EXPERIENCE = PARAMS_OLD.LEARNING_WEIGHT_EXPERIENCE;
        ellsa.data.PARAM_LEARNING_WEIGHT_GENERALIZATION = PARAMS_OLD.LEARNING_WEIGHT_GENERALIZATION;

        ellsa.data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = PARAMS_OLD.EXPLOITATION_WEIGHT_PROXIMITY;
        ellsa.data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = PARAMS_OLD.EXPLOITATION_WEIGHT_EXPERIENCE;
        ellsa.data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = PARAMS_OLD.EXPLOITATION_WEIGHT_GENERALIZATION;


        ellsa.data.PARAM_isActiveLearning = PARAMS_OLD.setActiveLearning;
        ellsa.data.PARAM_isSelfLearning = PARAMS_OLD.setSelfLearning;

        ellsa.data.PARAM_NCS_isConflictDetection = PARAMS_OLD.setConflictDetection;
        ellsa.data.PARAM_NCS_isConcurrenceDetection = PARAMS_OLD.setConcurrenceDetection;
        ellsa.data.PARAM_NCS_isVoidDetection = PARAMS_OLD.setIncompetenceDetection;
        ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS_OLD.setSubIncompetencedDetection;
        ellsa.data.PARAM_NCS_isConflictResolution = PARAMS_OLD.setConflictResolution;
        ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS_OLD.setConcurrenceResolution;
        ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS_OLD.setRangeAmbiguityDetection;
        ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS_OLD.setModelAmbiguityDetection;
        ellsa.data.PARAM_NCS_isFusionResolution = PARAMS_OLD.setCompleteRedundancyDetection;
        ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS_OLD.setPartialRedundancyDetection;

        ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS_OLD.setisCreationWithNeighbor;


        ellsa.data.PARAM_isLearnFromNeighbors = PARAMS_OLD.setCooperativeNeighborhoodLearning;
        ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS_OLD.nbOfNeighborForLearningFromNeighbors;
        ellsa.data.PARAM_isDream = PARAMS_OLD.setDream;
        ellsa.data.PARAM_DreamCycleLaunch = PARAMS_OLD.setDreamCycleLaunch;


        ellsa.data.PARAM_isAutonomousMode = PARAMS_OLD.setAutonomousMode;

        ellsa.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS_OLD.isAllContextSearchAllowedForLearning;
        ellsa.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS_OLD.isAllContextSearchAllowedForExploitation;

        ellsa.data.PARAM_probabilityOfRangeAmbiguity = PARAMS_OLD.probabilityOfRangeAmbiguity;



        ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS_OLD.traceLevel;



        ellsa.setSubPercepts(PARAMS_OLD.subPercepts);


        ArrayList<Double> allLearningCycleTimes = new ArrayList<>();
        ArrayList<Double> allExploitationCycleTimes = new ArrayList<>();

        for (int i = 0; i < PARAMS_OLD.nbLearningCycle; ++i) {
            double start = System.nanoTime();
//            System.out.println(start);
            ellsa.cycle();
            double end = System.nanoTime()- start;
//            System.out.println(end);
            allLearningCycleTimes.add(end/1000000);

            //System.out.println(ellsa.getCycle() + " " + ellsa.getContexts().size());

            if(ellsa.getCycle()%200 == 0){
                // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
                long heapMaxSize = (long)Runtime.getRuntime().maxMemory();
                //System.out.println("heapMaxSize\t\t" + heapMaxSize);

                // Get current size of heap in bytes
                long heapSize = (Runtime.getRuntime().totalMemory());
                //System.out.println("heapSize\t\t" + heapSize);

                // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
                long heapFreeSize = Runtime.getRuntime().freeMemory();;
                //System.out.println("heapFreeSize\t" + heapFreeSize + "\n");
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
        HashMap<SITUATION, Integer> situationsCounts = ellsa.data.situationsCounts;
        double[] executionTimes = ellsa.data.executionTimesSums;

        ArrayList<Double> allPredictionErrors = new ArrayList<>();

        if(PARAMS_OLD.setActiveExploitation){

            ellsa.data.PARAM_isExploitationActive = true;

            for (int i = 0; i < PARAMS_OLD.nbEndoExploitationCycle; ++i) {
                //studiedSystem.getErrorOnRequest(ellsa);
                ellsa.cycle();
            }

            ellsa.data.PARAM_isExploitationActive = false;

            for (int i = 0; i < PARAMS_OLD.nbExploitationCycle; ++i) {
                double start = System.nanoTime();
                allPredictionErrors.add(new Double(studiedSystem.getErrorOnRequest(ellsa)));
                allExploitationCycleTimes.add((System.nanoTime()- start)/1000000);

            }

        }else{
            for (int i = 0; i < PARAMS_OLD.nbExploitationCycle; ++i) {
                double start = System.nanoTime();
                allPredictionErrors.add(new Double(studiedSystem.getErrorOnRequest(ellsa)));
                allExploitationCycleTimes.add((System.nanoTime()- start)/1000000);

            }
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
        System.out.println(situationsCounts);
        System.out.println(predictionError*100 + " [+-" + predictionDispersion*100 + "]");
        System.out.println(ellsa.getContexts().size() + " Agents");

        System.out.println(ellsa.getContexts().get(0).getVolume() + " Vol");
        System.out.println(ellsa.getHeadAgent().getMinMaxVolume()+ " MinMaxVol");

        System.out.println(ellsa.data.minMaxPerceptsStatesAfterBoostrap);


        WRITER.setData(data, ellsa, mappingScores, requestCounts, situationsCounts, executionTimes, predictionError, predictionDispersion, averageLearningCycleTimeDouble, learningcycleTimeDispersionDouble, averageExploitationCycleTimeDouble, ExploitationcycleTimeDispersionDouble);

        ellsa = null;
        studiedSystem = null;

    }





}
