package experiments.roboticArm.launchers;



import agents.head.REQUEST;
import experiments.managers.F_N_Manager;
import experiments.roboticArm.simulation.RobotArmManager;
import experiments.roboticArm.simulation.RobotController;
import experiments.roboticArm.simulation.RobotWorlExampleMultiUI;
import multiagent.framework.Configuration;
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

public class RobotLaunchExampleOneExperimentationWithArgs {

    private static CSVWriter xpCSV;

	public static void main (String[] args)  {

	    TRACE.minLevel = TRACE_LEVEL.SUBCYCLE;

        PARAMS.nbJoints = Integer.parseInt(args[0]);
        PARAMS.dimension = PARAMS.nbJoints+1;
        PARAMS.nbTrainingCycle = Integer.parseInt(args[1]);
        PARAMS.nbRequestCycle = Integer.parseInt(args[2]);
        PARAMS.nbEpisodes = Integer.parseInt(args[3]);
        PARAMS.mappingErrorAllowed = ((double)Integer.parseInt(args[4]))/100;
        System.out.println(PARAMS.mappingErrorAllowed);
        PARAMS.neighborhoodMultiplicator = Integer.parseInt(args[5]);
        PARAMS.extendedArmLength = Integer.parseInt(args[6]);

        PARAMS.configFile = args[0]+"jointsRobot3DimensionsLauncher.xml";

        if(PARAMS.nbJoints==2){
            PARAMS.subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));
        }
        else{
            ArrayList<String> subPercepts = new ArrayList<>();
            for(int i=1;i<PARAMS.nbJoints;i++){
                subPercepts.add("ptheta"+i);
            }
            PARAMS.subPercepts = subPercepts;
        }

		start();
		
	
	}


	public static void start() {

            String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String date = new SimpleDateFormat("ddMMyyyy").format(new Date());
            xpCSV = new CSVWriter(dateAndHour+"_Dim_"+ PARAMS.dimension
                    +"_LearningCycles_" + PARAMS.nbTrainingCycle
                    +"_ExplotationCycles_" + PARAMS.nbRequestCycle
                    +"_Episodes_" + PARAMS.nbEpisodes
                    +"_Joints_" + PARAMS.nbJoints
                    +"_Dimensions_" + PARAMS.dimension
                    +"_" + PARAMS.model

            );

            writeParams(PARAMS.model);

        // Set AMAK configuration before creating an ELLSA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

            HashMap<String, ArrayList<Double>> data = new HashMap<>();

            List<String> dataStrings = Arrays.asList("endogenousLearningSituations", "prediction", "predictionDisp");

            for (String dataName : dataStrings){
                    data.put(dataName, new ArrayList<>());
            }

            double start = System.currentTimeMillis();
            for (int i = 0; i < PARAMS.nbEpisodes; ++i) {
                    System.out.println("Learning episode " +i + " ");
                    learningEpisode(data);
            }

            double total = (System.currentTimeMillis()- start)/1000;
            double mean = total/ PARAMS.nbEpisodes;
            System.out.println("[MEAN TIME] " + mean + " s");
            System.out.println("[TOTAL TIME] " + total + " s");


            xpCSV.write(new ArrayList<>(Arrays.asList("MEAN TIME", mean + " s","TOTAL TIME",total + " s" )));
            xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for (String dataName : dataStrings){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            if(averageScore.getAsDouble()<1){
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()*100+"" ,"DEVIATION","" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));
            }else{
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()+"" ,"DEVIATION","" + Math.sqrt(deviationScore/data.get(dataName).size()))));
            }

        }

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        //Create the formatter for round the values of scores
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
        xpCSV.write(new ArrayList<>(Arrays.asList("ROUNDED")));
        for (String dataName : dataStrings){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            if(averageScore.getAsDouble()<1){
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,df.format(averageScore.getAsDouble()*100)+"" ,"DEVIATION","" + df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())))));
            }


        }
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for (String dataName : dataStrings){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            if(averageScore.getAsDouble()>=1){
                xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,df.format(averageScore.getAsDouble())+"" ,"DEVIATION","" + df.format(Math.sqrt(deviationScore/data.get(dataName).size())))));
            }


        }

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        OptionalDouble averageScore = data.get("prediction").stream().mapToDouble(a->a).average();
        OptionalDouble averageScoreDisp = data.get("predictionDisp").stream().mapToDouble(a->a).average();
        Double deviationScore = data.get("prediction").stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
        Double deviationScoreDisp = data.get("predictionDisp").stream().mapToDouble(a->Math.pow((a-averageScoreDisp.getAsDouble()),2)).sum();


        xpCSV.write(new ArrayList<>(Arrays.asList("PREDICTION AVERAGE" , ""+averageScore.getAsDouble() ,"DEVIATION" ,""+Math.sqrt(deviationScore/data.get("prediction").size()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("DISPERSION AVERAGE" , ""+averageScoreDisp.getAsDouble() ,"DEVIATION" ,""+Math.sqrt(deviationScoreDisp/data.get("predictionDisp").size()))));


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



                double error = robotArmManager.averageError.getAsDouble();
                double dispersion = Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size());

                HashMap<String, Double> mappingScores = ellsaTheta.getHeadAgent().getMappingScores();
                HashMap<REQUEST, Integer> requestCounts = ellsaTheta.data.requestCounts;



                data.get("endogenousLearningSituations").add((double)requestCounts.get(REQUEST.NEIGHBOR));
                data.get("prediction").add(error);
                data.get("predictionDisp").add(dispersion);

	}


        private static void writeParams(String model) {
                xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Dim", PARAMS.dimension+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Model",model)));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learning cycles", PARAMS.nbTrainingCycle+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Exploitation cycles", PARAMS.nbRequestCycle+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Learning episodes", PARAMS.nbEpisodes +"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Space size", PARAMS.extendedArmLength*2+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Precision range", PARAMS.mappingErrorAllowed+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList("Neighborhood size", PARAMS.neighborhoodMultiplicator+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("Error margin", PARAMS.setRegressionPerformance+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

                xpCSV.write(new ArrayList<>(Arrays.asList("Learning speed", PARAMS.learningSpeed+"")));
                xpCSV.write(new ArrayList<>(Arrays.asList(" ")));


        }
	

}
