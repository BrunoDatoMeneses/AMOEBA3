package experiments.nDimensionsLaunchers;





import agents.head.REQUEST;
import agents.percept.Percept;
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

        PARAMS.setisCreationWithNeighbor = Boolean.parseBoolean(args[24]);




        PARAMS.model = args[25];
        PARAMS.setbootstrapCycle = Integer.parseInt(args[26]);
        PARAMS.extension = args[27];




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
        ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;
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

        /*System.out.println(mappingScores);
        System.out.println(requestCounts);
        System.out.println(predictionError*100 + " [+-" + predictionDispersion*100 + "]");
        System.out.println(ellsa.getContexts().size() + " Agents");*/

        WRITER.setData(data, ellsa, mappingScores, requestCounts, executionTimes, predictionError, predictionDispersion, averageLearningCycleTimeDouble, learningcycleTimeDispersionDouble, averageExploitationCycleTimeDouble, ExploitationcycleTimeDispersionDouble);


        ellsa = null;
        studiedSystem = null;

    }




}