package experiments.roboticArm;



import agents.head.REQUEST;
import experiments.managers.F_N_Manager;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class RobotLaunchExampleXP {

    private static CSVWriter xpCSV;

	public static void main (String[] args)  {

	    TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;
		start();
		
	
	}


	public static void start() {

            String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
            xpCSV = new CSVWriter(date,dateAndHour+"_Dim_"+ PARAMS.dimension
                    +"_LearningCycles_" + PARAMS.nbTrainingCycle
                    +"_ExplotationCycles_" + PARAMS.nbRequestCycle
                    +"_Episodes_" + PARAMS.nbTest
                    +"_Joints_" + PARAMS.nbJoints
                    +"_Dimensions_" + PARAMS.dimension
                    +"_Notes_" + PARAMS.model

            );

            writeParams(PARAMS.model);

        // Set AMAK configuration before creating an ELLSA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

            HashMap<String, ArrayList<Double>> data = new HashMap<>();

            List<String> dataStrings = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol","nbAgents", "conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests", "modelRequests","neighborRequests","fusionRequests","restructureRequests", "prediction", "predictionDisp");

            for (String dataName : dataStrings){
                    data.put(dataName, new ArrayList<>());
            }

            double start = System.currentTimeMillis();
            for (int i = 0; i < PARAMS.nbTest; ++i) {
                    System.out.print(i + " ");
                    learningEpisode(data);
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
        OptionalDouble averageScoreDisp = data.get("predictionDisp").stream().mapToDouble(a->a).average();
        Double deviationScore = data.get("prediction").stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
        Double deviationScoreDisp = data.get("predictionDisp").stream().mapToDouble(a->Math.pow((a-averageScoreDisp.getAsDouble()),2)).sum();

        System.out.println("[PREDICTION AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get("prediction").size()) );
        System.out.println("[DISPERSION AVERAGE] " + averageScoreDisp.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScoreDisp/data.get("predictionDisp").size()) );

        xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION AVERAGE" , ""+averageScore.getAsDouble() ,"DEVIATION" ,""+Math.sqrt(deviationScore/data.get("prediction").size()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("DISPERSION AVERAGE" , ""+averageScoreDisp.getAsDouble() ,"DEVIATION" ,""+Math.sqrt(deviationScoreDisp/data.get("predictionDisp").size()))));

        System.out.println("[PREDICTION AVERAGE %] " + df.format(100*averageScore.getAsDouble()) + " - " + "[DEVIATION %] " +df.format(100*Math.sqrt(deviationScore/data.get("prediction").size())));
        System.out.println("[DISPERSION AVERAGE %] " + df.format(100*averageScoreDisp.getAsDouble()) + " - " + "[DEVIATION %] " +df.format(100*Math.sqrt(deviationScoreDisp/data.get("predictionDisp").size())));

        xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION AVERAGE %" , ""+df.format(100*averageScore.getAsDouble()) ,"DEVIATION %" ,""+df.format(100*Math.sqrt(deviationScore/data.get("prediction").size())))));
        xpCSV.write(new ArrayList<>(Arrays.asList("DISPERSION AVERAGE %" , ""+df.format(100*averageScoreDisp.getAsDouble()) ,"DEVIATION %" ,""+df.format(100*Math.sqrt(deviationScoreDisp/data.get("predictionDisp").size())))));

        xpCSV.close();

    }

        private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {

            StudiedSystem studiedSystemTheta0 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
            ELLSA ellsaTheta = new ELLSA(null,  null);
            ellsaTheta.setStudiedSystem(studiedSystemTheta0);
            IBackupSystem backupSystem = new BackupSystem(ellsaTheta);
            File file = new File("resources/"+PARAMS.configFile);
            backupSystem.load(file);

            ellsaTheta.allowGraphicalScheduler(false);
            ellsaTheta.setRenderUpdate(false);

            ellsaTheta.data.nameID = "ellsaTheta";
            ellsaTheta.data.learningSpeed = PARAMS.learningSpeed;
            ellsaTheta.data.numberOfPointsForRegression = PARAMS.regressionPoints;
            ellsaTheta.data.isActiveLearning = PARAMS.setActiveLearning;
            ellsaTheta.data.isSelfLearning = PARAMS.setSelfLearning;
            ellsaTheta.data.isAutonomousMode = PARAMS.setAutonomousMode;
            ellsaTheta.data.isConflictDetection = PARAMS.setConflictDetection;
            ellsaTheta.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
            ellsaTheta.data.isVoidDetection2 = PARAMS.setVoidDetection2;
            ellsaTheta.data.isConflictResolution = PARAMS.setConflictResolution;
            ellsaTheta.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
            ellsaTheta.data.isFrontierRequest = PARAMS.setFrontierRequest;
            ellsaTheta.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
            ellsaTheta.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

            ellsaTheta.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
            ellsaTheta.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
            ellsaTheta.data.isDream = PARAMS.setDream;
            ellsaTheta.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
            ellsaTheta.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

            ellsaTheta.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
            ellsaTheta.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
            ellsaTheta.getEnvironment().minLevel = TRACE_LEVEL.OFF;


            ellsaTheta.setSubPercepts(PARAMS.subPercepts);

            int jointsNb = PARAMS.nbJoints;

            double distances[] = new double[jointsNb];
            double incLength = PARAMS.extendedArmLength /jointsNb;

            for(int i = 0;i<jointsNb;i++){
                distances[i] = incLength;
            }

                ELLSA ellsas[] = new ELLSA[2];
                ellsas[0] = ellsaTheta;
                RobotController robotController = new RobotController(jointsNb);
                RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
                robotArmManager.maxError = PARAMS.extendedArmLength *2;

                RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(null, null, null, robotController, robotArmManager, jointsNb);

                while(!robotArmManager.finished){
                    robot.cycleCommandLine();
                }

            System.err.println("\nERROR REQUESTS "+robotArmManager.errorRequests);

                double error = robotArmManager.averageError.getAsDouble();
                double dispersion = Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size());

                HashMap<String, Double> mappingScores = ellsaTheta.getHeadAgent().getMappingScores();
                System.out.println(mappingScores);
                HashMap<REQUEST, Integer> requestCounts = ellsaTheta.data.requestCounts;
                System.out.println(requestCounts);
                System.out.println(error*100 + " [ " + dispersion*100 + " ]");

                data.get("mappingScore").add(mappingScores.get("CTXT"));
                data.get("imprecisionScore").add(mappingScores.get("CONF") + mappingScores.get("CONC") + mappingScores.get("VOIDS"));
                data.get("conflictVol").add(mappingScores.get("CONF"));
                data.get("concurrenceVol").add(mappingScores.get("CONC"));
                data.get("voidVol").add(mappingScores.get("VOIDS"));
                data.get("conflictRequests").add((double)requestCounts.get(REQUEST.CONFLICT));
                data.get("concurrenceRequests").add((double)requestCounts.get(REQUEST.CONCURRENCE));
                data.get("frontierRequests").add((double)requestCounts.get(REQUEST.FRONTIER));
                data.get("voidRequests").add((double)requestCounts.get(REQUEST.VOID));
                data.get("modelRequests").add((double)requestCounts.get(REQUEST.MODEL));
                data.get("neighborRequests").add((double)requestCounts.get(REQUEST.NEIGHBOR));
                data.get("fusionRequests").add((double)requestCounts.get(REQUEST.FUSION));
                data.get("restructureRequests").add((double)requestCounts.get(REQUEST.RESTRUCTURE));
                data.get("nbAgents").add((double) ellsaTheta.getContexts().size());
                data.get("prediction").add(error);
                data.get("predictionDisp").add(dispersion);

	}


        private static void writeParams(String model) {
                xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
                xpCSV.write(new ArrayList<>(Arrays.asList("SET")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Dim", PARAMS.dimension+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Model",model)));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learning cycles", PARAMS.nbTrainingCycle+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Testting cycles", PARAMS.nbRequestCycle+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learning episodes", PARAMS.nbTest+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Space size", PARAMS.spaceSize*4+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Mapping error", PARAMS.mappingErrorAllowed+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Active Learning", PARAMS.setActiveLearning+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Self Learning", PARAMS.setSelfLearning+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Init regression performance", PARAMS.setRegressionPerformance+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Noise", PARAMS.oracleNoiseRange+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learning speed", PARAMS.learningSpeed+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Regression points", PARAMS.regressionPoints+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("EXPLORATION")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Random Exploration", PARAMS.randomExploration+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Continous Exploration", PARAMS.continousExploration+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Limited To SpaceZone", PARAMS.limitedToSpaceZone+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Exploration Increment", PARAMS.explorationIncrement+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Exploration Widht", PARAMS.explorationWidht+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("NCS")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Conflicts", PARAMS.setConflictDetection+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Concurrences", PARAMS.setConcurrenceDetection+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Incompetences", PARAMS.setVoidDetection2+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Ambiguities", PARAMS.setFrontierRequest+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Model", PARAMS.setSelfModelRequest+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learn From Neighbors", PARAMS.setLearnFromNeighbors+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Dream", PARAMS.setDream+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("OTHER")));
                xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForLearningFromNeighbors", PARAMS.nbOfNeighborForLearningFromNeighbors+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForContexCreationWithouOracle", PARAMS.nbOfNeighborForContexCreationWithouOracle+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForVoidDetectionInSelfLearning", PARAMS.nbOfNeighborForVoidDetectionInSelfLearning+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
        }
	

}
