package experiments.roboticArm;



import agents.head.REQUEST;
import experiments.nDimensionsLaunchers.F_N_Manager;
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
            xpCSV = new CSVWriter(dateAndHour+"_Dim_"+ PARAMS.dimension
                    +"_LearningCycles_" + PARAMS.nbTrainingCycle
                    +"_ExplotationCycles_" + PARAMS.nbRequestCycle
                    +"_Episodes_" + PARAMS.nbTest
                    +"_Joints_" + PARAMS.nbJoints
                    +"_Dimensions_" + PARAMS.dimension
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
                ELLSA ellsaTheta0 = new ELLSA(null,  null);
                ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
                IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
                File file = new File("resources/"+PARAMS.configFile);
                backupSystem.load(file);

                //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

                ellsaTheta0.allowGraphicalScheduler(false);
                ellsaTheta0.setRenderUpdate(false);

                StudiedSystem studiedSystemTheta1 = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement,PARAMS.explorationWidht,PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
                ELLSA ellsaTheta1 = new ELLSA(null,  null);
                ellsaTheta1.setStudiedSystem(studiedSystemTheta1);
                IBackupSystem backupSystem1 = new BackupSystem(ellsaTheta1);
                File file1 = new File("resources/"+PARAMS.configFile);
                backupSystem1.load(file1);

                //amoeba.saver = new SaveHelperImpl(amoeba, amoebaUI);

                ellsaTheta1.allowGraphicalScheduler(false);
                ellsaTheta1.setRenderUpdate(false);

                ellsaTheta0.data.nameID = "ellsaTheta0";
                ellsaTheta0.data.learningSpeed = PARAMS.learningSpeed;
                ellsaTheta0.data.numberOfPointsForRegression = PARAMS.regressionPoints;
                ellsaTheta0.data.isActiveLearning = PARAMS.setActiveLearning;
                ellsaTheta0.data.isSelfLearning = PARAMS.setSelfLearning;
                ellsaTheta0.data.isAutonomousMode = PARAMS.setAutonomousMode;
                ellsaTheta0.data.isConflictDetection = PARAMS.setConflictDetection;
                ellsaTheta0.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
                ellsaTheta0.data.isVoidDetection2 = PARAMS.setVoidDetection2;
                ellsaTheta0.data.isConflictResolution = PARAMS.setConflictResolution;
                ellsaTheta0.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
                ellsaTheta0.data.isFrontierRequest = PARAMS.setFrontierRequest;
                ellsaTheta0.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
                ellsaTheta0.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

                ellsaTheta0.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
                ellsaTheta0.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
                ellsaTheta0.data.isDream = PARAMS.setDream;
                ellsaTheta0.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
                ellsaTheta0.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

                ellsaTheta0.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
                ellsaTheta0.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
                ellsaTheta0.getEnvironment().minLevel = TRACE_LEVEL.OFF;


                ellsaTheta1.data.nameID = "ellsaTheta1";
                ellsaTheta1.data.learningSpeed = PARAMS.learningSpeed;
                ellsaTheta1.data.numberOfPointsForRegression = PARAMS.regressionPoints;
                ellsaTheta1.data.isActiveLearning = PARAMS.setActiveLearning;
                ellsaTheta1.data.isSelfLearning = PARAMS.setSelfLearning;
                ellsaTheta1.data.isAutonomousMode = PARAMS.setAutonomousMode;
                ellsaTheta1.data.isConflictDetection = PARAMS.setConflictDetection;
                ellsaTheta1.data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
                ellsaTheta1.data.isVoidDetection2 = PARAMS.setVoidDetection2;
                ellsaTheta1.data.isConflictResolution = PARAMS.setConflictResolution;
                ellsaTheta1.data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
                ellsaTheta1.data.isFrontierRequest = PARAMS.setFrontierRequest;
                ellsaTheta1.data.isSelfModelRequest = PARAMS.setSelfModelRequest;
                ellsaTheta1.data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

                ellsaTheta1.data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
                ellsaTheta1.data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
                ellsaTheta1.data.isDream = PARAMS.setDream;
                ellsaTheta1.data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
                ellsaTheta1.data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

                ellsaTheta1.getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
                ellsaTheta1.data.initRegressionPerformance = PARAMS.setRegressionPerformance;
                ellsaTheta1.getEnvironment().minLevel = TRACE_LEVEL.OFF;

                ellsaTheta1.setSubPercepts(PARAMS.subPercepts);
                ellsaTheta0.setSubPercepts(PARAMS.subPercepts);

                int jointsNb = PARAMS.nbJoints;
                //AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
                //WorldExampleMultiUI env = new WorldExampleMultiUI(window);
                //VUIMulti vui = new VUIMulti("Robot");


            double distances[] = new double[jointsNb];
            double incLength = PARAMS.armBaseSize/jointsNb;

            for(int i = 0;i<jointsNb;i++){
                distances[i] = incLength;
            }

                ELLSA ellsas[] = new ELLSA[2];
                ellsas[0] = ellsaTheta0;
                ellsas[1] = ellsaTheta1;
                RobotController robotController = new RobotController(jointsNb);
                RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
                robotArmManager.maxError = PARAMS.armBaseSize*2;


                RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(null, null, null, robotController, robotArmManager, jointsNb);

                while(!robotArmManager.finished){
                    robot.cycleCommandLine();
                }

            System.err.println("\nERROR REQUESTS "+robotArmManager.errorRequests);

                //TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.finished);
                //TRACE.print(TRACE_LEVEL.ERROR, + " [ " + Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size()) + " ]      -    " + robotArmManager.goalErrors);
                double error = robotArmManager.averageError.getAsDouble();
                double dispersion = Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size());

                HashMap<String, Double> mappingScores = ellsaTheta0.getHeadAgent().getMappingScores();
                System.out.println(mappingScores);
                HashMap<REQUEST, Integer> requestCounts = ellsaTheta0.data.requestCounts;
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
                data.get("nbAgents").add((double) ellsaTheta0.getContexts().size());
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
