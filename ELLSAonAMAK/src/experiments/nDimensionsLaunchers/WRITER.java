package experiments.nDimensionsLaunchers;

import agents.head.REQUEST;
import agents.percept.Percept;
import kernel.ELLSA;
import utils.CSVWriter;
import utils.Pair;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

public class WRITER {

    // TODO add mean confidence and max min


    public static Pair<ArrayList<List<String>>,HashMap<String, ArrayList<Double>>> getData() {

        HashMap<String, ArrayList<Double>> data = new HashMap<>();


        List<String> dataStringsVolumes = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol");

        List<String> dataStringsPrediction = Arrays.asList("predictionError", "predictionErrorDeviation");

        List<String> dataStringsEndoRequests = Arrays.asList("activeRequests","conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests","subvoidRequests", "modelRequests", "rdmRequests", "dreamRequests", "endogenousLearningSituations","fusionRequests","restructureRequests");

        //List<String> dataStringsNCS =

        List<String> dataStringsTimeExecution = Arrays.asList("learningCycleExecutionTime","exploitationCycleExecutionTime", "learningCycleExecutionTimeDeviation","exploitationCycleExecutionTimeDeviation",
                "perceptsTimeExecution", "contextsTimeExecution" , "headTimeExecution", "NCSTimeExecution"
                , "NCS_UselessnessTimeExecution", "NCS_IncompetendHeadTimeExecution", "NCS_ConcurrenceAndConflictTimeExecution", "NCS_Create_New_ContextTimeExecution", "NCS_OvermappingTimeExecution", "NCS_ChildContextTimeExecution", "NCS_PotentialRequestTimeExecution", "NCS_DreamPotentialRequestTimeExecution");

        List<String> dataStringsOther = Arrays.asList("localMinima","nbAgents","neighborsCounts",
                "minPerceptionsExperiencedAverage","maxPerceptionsExperiencedAverage","minPerceptionsExperiencedDeviation","maxPerceptionsExperiencedDeviation",
                "rangeExperienceAverage", "rangeExperienceDeviation");

        ArrayList<List<String>> dataStrings = new ArrayList<>(Arrays.asList(dataStringsVolumes, dataStringsEndoRequests, dataStringsTimeExecution, dataStringsOther, dataStringsPrediction ));

        for(List<String> dataString : dataStrings){
            for (String dataName : dataString){
                data.put(dataName, new ArrayList<>());
            }
        }


        return new Pair<>(dataStrings,data);
    }


    public static void writeData(CSVWriter xpCSV,HashMap<String, ArrayList<Double>> data, ArrayList<List<String>> dataStrings, double total, double mean) {
        writeParams(xpCSV);

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

    private static void writeParams(CSVWriter xpCSV) {
        xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
        xpCSV.write(new ArrayList<>(Arrays.asList("SET")));
        xpCSV.write(new ArrayList<>(Arrays.asList("dimension", PARAMS.dimension+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("model",PARAMS.model)));
        xpCSV.write(new ArrayList<>(Arrays.asList("learningCycles", PARAMS.nbLearningCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("exploitatingCycles", PARAMS.nbExploitationCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("episodes", PARAMS.nbEpisodes +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("spaceSize", PARAMS.spaceSize*4+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("validityRangesPrecision", PARAMS.validityRangesPrecision +"")));


        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isActiveLearning", PARAMS.setActiveLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isSelfLearning", PARAMS.setSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING_WEIGHT_ACCURACY", PARAMS.LEARNING_WEIGHT_ACCURACY+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING_WEIGHT_PROXIMITY", PARAMS.LEARNING_WEIGHT_PROXIMITY+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING_WEIGHT_EXPERIENCE", PARAMS.LEARNING_WEIGHT_EXPERIENCE+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING_WEIGHT_GENERALIZATION", PARAMS.LEARNING_WEIGHT_GENERALIZATION+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("EXPLOITATION_WEIGHT_PROXIMITY", PARAMS.EXPLOITATION_WEIGHT_PROXIMITY+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("EXPLOITATION_WEIGHT_EXPERIENCE", PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("EXPLOITATION_WEIGHT_GENERALIZATION", PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("goalXYError")));
        xpCSV.write(new ArrayList<>(Arrays.asList("errorMargin", PARAMS.modelErrorMargin +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
        xpCSV.write(new ArrayList<>(Arrays.asList("noise", PARAMS.oracleNoiseRange+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("exogenousLearningWeight", PARAMS.exogenousLearningWeight +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("endogenousLearningWeight", PARAMS.endogenousLearningWeight +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("perceptionsGenerationCoefficient", PARAMS.perceptionsGenerationCoefficient+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("regressionPoints", PARAMS.regressionPoints+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("modelSimilarityThreshold", PARAMS.modelSimilarityThreshold+"")));


        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("EXPLORATION")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isRandomExploration", PARAMS.randomExploration+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isContinuousExploration", PARAMS.continousExploration+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isLimitedToSpaceZone", PARAMS.limitedToSpaceZone+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("explorationIncrement", PARAMS.explorationIncrement+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("explorationWidth", PARAMS.explorationWidht+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("bootstrapCycle", PARAMS.setbootstrapCycle+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("RANGES")));
        xpCSV.write(new ArrayList<>(Arrays.asList("neighborhoodRadiusCoefficient", PARAMS.neighborhoodRadiusCoefficient+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("influenceRadiusCoefficient", PARAMS.influenceRadiusCoefficient+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("maxRangeRadiusCoefficient", PARAMS.maxRangeRadiusCoefficient+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("rangeSimilarityCoefficient", PARAMS.rangeSimilarityCoefficient+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("minimumRangeCoefficient", PARAMS.minimumRangeCoefficient+"")));
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
        xpCSV.write(new ArrayList<>(Arrays.asList("isFusionResolution", PARAMS.setFusionResolution+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isRetructureResolution", PARAMS.setRestructureResolution+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("NCS PARAMS")));

        xpCSV.write(new ArrayList<>(Arrays.asList("dreamLaunch", PARAMS.setDreamCycleLaunch+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForLearningFromNeighbors", PARAMS.nbOfNeighborForLearningFromNeighbors+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForContexCreationWithouOracle", PARAMS.nbOfNeighborForContexCreationWithouOracle+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForVoidDetectionInSelfLearning", PARAMS.nbOfNeighborForVoidDetectionInSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isCreationFromNeighbor", PARAMS.setisCreationWithNeighbor+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isAllContextSearchAllowedForLearning", PARAMS.isAllContextSearchAllowedForLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isAllContextSearchAllowedForExploitation", PARAMS.isAllContextSearchAllowedForExploitation+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
    }

    public static void setData(HashMap<String, ArrayList<Double>> data, ELLSA ellsa, HashMap<String, Double> mappingScores, HashMap<REQUEST, Integer> requestCounts, double[] executionTimes, double predictionError, double predictionDispersion, double averageLearningCycleTimeDouble, double learningcycleTimeDispersionDouble, double averageExploitationCycleTimeDouble, double exploitationcycleTimeDispersionDouble) {
        // Volumes
        data.get("mappingScore").add(mappingScores.get("CTXT"));
        data.get("imprecisionScore").add(mappingScores.get("CONF") + mappingScores.get("CONC") + mappingScores.get("VOIDS"));
        data.get("conflictVol").add(mappingScores.get("CONF"));
        data.get("concurrenceVol").add(mappingScores.get("CONC"));
        data.get("voidVol").add(mappingScores.get("VOIDS"));

        // Predictions
        data.get("predictionError").add(predictionError);
        data.get("predictionErrorDeviation").add(predictionDispersion);

        int activeRequests = 0;
        for (Map.Entry<REQUEST, Integer> entry : requestCounts.entrySet()) {
            if(entry.getKey()!=REQUEST.RDM){
                activeRequests +=entry.getValue();
            }
        }


        // Endo Requests
        data.get("activeRequests").add((double)activeRequests);
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
        data.get("exploitationCycleExecutionTimeDeviation").add(exploitationcycleTimeDispersionDouble);

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

        ArrayList<Double> allPerceptMin = new ArrayList<>();
        ArrayList<Double> allPerceptMax = new ArrayList<>();
        ArrayList<Double> allPerceptRangeExp = new ArrayList<>();
        for(Percept pct : ellsa.getPercepts()){
            double min = ellsa.data.minMaxPerceptsStatesAfterBoostrap.get(pct).getA();
            double max = ellsa.data.minMaxPerceptsStatesAfterBoostrap.get(pct).getB();
            allPerceptMin.add(new Double(min));
            allPerceptMax.add(new Double(max));
            allPerceptRangeExp.add(new Double(Math.abs(max-min)));
        }

        OptionalDouble averageMin = allPerceptMin.stream().mapToDouble(a->a).average();
        Double minDispersion = allPerceptMin.stream().mapToDouble(a->Math.pow((a- averageMin.getAsDouble()),2)).sum();
        double minAverageValue = averageMin.getAsDouble();
        double minDispersionValue = Math.sqrt(minDispersion /allPerceptMin.size());

        OptionalDouble averageMax = allPerceptMax.stream().mapToDouble(a->a).average();
        Double MaxDispersion = allPerceptMax.stream().mapToDouble(a->Math.pow((a- averageMax.getAsDouble()),2)).sum();
        double MaxAverageValue = averageMax.getAsDouble();
        double MaxDispersionValue = Math.sqrt(MaxDispersion /allPerceptMax.size());

        OptionalDouble averageRange = allPerceptRangeExp.stream().mapToDouble(a->a).average();
        Double rangDispersion = allPerceptRangeExp.stream().mapToDouble(a->Math.pow((a- averageRange.getAsDouble()),2)).sum();
        double rangeAverageValue = averageRange.getAsDouble();
        double rangeDispersionValue = Math.sqrt(rangDispersion /allPerceptRangeExp.size());

        data.get("minPerceptionsExperiencedAverage").add(minAverageValue);
        data.get("maxPerceptionsExperiencedAverage").add(MaxAverageValue);
        data.get("minPerceptionsExperiencedDeviation").add(minDispersionValue);
        data.get("maxPerceptionsExperiencedDeviation").add(MaxDispersionValue);
        data.get("rangeExperienceAverage").add(rangeAverageValue);
        data.get("rangeExperienceDeviation").add(rangeDispersionValue);

    }

}
