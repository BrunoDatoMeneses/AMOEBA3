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

public class RobotLaunchExampleAllExperimentations {

    private static CSVWriter xpCSV;


    private static  String configFile30joints = "30jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts30joints = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19",
            "ptheta20","ptheta21", "ptheta22", "ptheta23", "ptheta24", "ptheta25",
            "ptheta26", "ptheta27", "ptheta28", "ptheta29")  );

    public static  String configFile20joints = "20jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts20joints = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19")  );

    public static  String configFile10joints = "10jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts10joints = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4",
            "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9"));

    public static  String configFile6joints = "6jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts6joints = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4",
            "ptheta5"));

    public static  String configFile3joints = "3jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts3joints = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2"));

    public static  String configFile2joints = "2jointsRobot3DimensionsLauncher.xml";
    public static  ArrayList subPercepts2joints = new ArrayList<>(Arrays.asList("ptheta0"));

	public static void main (String[] args)  {

	    TRACE.minLevel = TRACE_LEVEL.OFF;
        PARAMS.nbEpisodes = 15;
        PARAMS.extendedArmLength = 50.0;

        ArrayList<Integer> neighborhoodMultiplicators = new ArrayList<>(Arrays.asList(0,2,4,6,8,10,12,14,16));
	    ArrayList<Integer> trainingCycles = new ArrayList<>(Arrays.asList(1000));
        ArrayList<Integer> requestCycles = new ArrayList<>(Arrays.asList(200));
        ArrayList<Double> mappingErrors = new ArrayList<>(Arrays.asList(0.01,0.03));
        ArrayList<Integer> jointsNb  = new ArrayList<>(Arrays.asList(2,3,6,10,20,30));



        for(Integer neighborhoodMultiplicator : neighborhoodMultiplicators) {
            for (Double mappingError : mappingErrors) {
                for (Integer requestCycle : requestCycles) {
                    for (Integer trainingCycle : trainingCycles) {
                        for (Integer jointNb : jointsNb) {
                            System.out.print("neighborhoodSize " + neighborhoodMultiplicator + " ");
                            System.out.print("precisionRango " + mappingError + " ");
                            System.out.print("exploitationCycles " + requestCycle + " ");
                            System.out.print("learningCycles " + trainingCycle + " ");
                            System.out.print("jointNb " + jointNb + " ");

                            PARAMS.mappingErrorAllowed = mappingError;
                            PARAMS.neighborhoodMultiplicator = neighborhoodMultiplicator;
                            PARAMS.nbRequestCycle = requestCycle;
                            PARAMS.nbTrainingCycle = trainingCycle;
                            PARAMS.nbJoints = jointNb;
                            PARAMS.dimension = jointNb + 1;

                            if (jointNb == 30) {
                                PARAMS.configFile = configFile30joints;
                                PARAMS.subPercepts = subPercepts30joints;
                            } else if (jointNb == 20) {
                                PARAMS.configFile = configFile20joints;
                                PARAMS.subPercepts = subPercepts20joints;
                            } else if (jointNb == 10) {
                                PARAMS.configFile = configFile10joints;
                                PARAMS.subPercepts = subPercepts10joints;
                            } else if (jointNb == 6) {
                                PARAMS.configFile = configFile6joints;
                                PARAMS.subPercepts = subPercepts6joints;
                            } else if (jointNb == 3) {
                                PARAMS.configFile = configFile3joints;
                                PARAMS.subPercepts = subPercepts3joints;
                            } else if (jointNb == 2) {
                                PARAMS.configFile = configFile2joints;
                                PARAMS.subPercepts = subPercepts2joints;
                            }

                            start();

                            System.out.println(" ");
                        }
                    }
                }
            }
        }
	}


	public static void start() {

            String dateAndHour = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());

            xpCSV = new CSVWriter(dateAndHour+"_Dim_"+ PARAMS.dimension
                    +"_LearningCycles_" + PARAMS.nbTrainingCycle
                    +"_ExplotationCycles_" + PARAMS.nbRequestCycle
                    +"_Episodes_" + PARAMS.nbEpisodes
                    +"_Joints_" + PARAMS.nbJoints
                    +"_Dimensions_" + PARAMS.dimension
                    +"_" + PARAMS.model

            );

            writeParams(PARAMS.model);

        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;

        HashMap<String, ArrayList<Double>> data = new HashMap<>();
        List<String> dataStringsPrediction = Arrays.asList("prediction", "predictionDisp");
        List<String> dataStringsOther = Arrays.asList("endogenousLearningSituations", "prediction");




        for (String dataName : dataStringsPrediction){
            data.put(dataName, new ArrayList<>());
        }

        for (String dataName : dataStringsOther){
            data.put(dataName, new ArrayList<>());
        }

            double start = System.currentTimeMillis();

            for (int i = 0; i < PARAMS.nbEpisodes; ++i) {

                    learningEpisode(data);
            }

            double total = (System.currentTimeMillis()- start)/1000;
            double mean = total/ PARAMS.nbEpisodes;



            xpCSV.write(new ArrayList<>(Arrays.asList("MEAN TIME", mean + " s","TOTAL TIME",total + " s" )));
            xpCSV.write(new ArrayList<>(Arrays.asList(" ")));



        for (String dataName : dataStringsPrediction){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()*100+"" ,"DEVIATION","" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));



        }

        for (String dataName : dataStringsOther){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //System.out.println(dataName +" [AVERAGE] " + averageScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationScore/data.get(dataName).size()));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,averageScore.getAsDouble()+"" ,"DEVIATION","" + Math.sqrt(deviationScore/data.get(dataName).size()))));



        }

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        //Create the formatter for round the values of scores
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
        //System.out.println("ROUNDED");
        xpCSV.write(new ArrayList<>(Arrays.asList("ROUNDED")));

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for (String dataName : dataStringsOther){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();

            //System.out.println(dataName +" [AVERAGE] " + Math.round(averageScore.getAsDouble()) + " - " + "[DEVIATION] " +Math.round(Math.sqrt(deviationScore/data.get(dataName).size())));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName ,"AVERAGE" ,df.format(averageScore.getAsDouble())+"" ,"DEVIATION","" + df.format(Math.sqrt(deviationScore/data.get(dataName).size())))));



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
                ELLSA ellsaTheta0 = new ELLSA(null,  null);
                ellsaTheta0.setStudiedSystem(studiedSystemTheta0);
                IBackupSystem backupSystem = new BackupSystem(ellsaTheta0);
                File file = new File("resources/"+PARAMS.configFile);
                backupSystem.load(file);



                ellsaTheta0.allowGraphicalScheduler(false);
                ellsaTheta0.setRenderUpdate(false);





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
                ellsaTheta0.data.neighborhoodMultiplicator = PARAMS.neighborhoodMultiplicator;


                ellsaTheta0.setSubPercepts(PARAMS.subPercepts);

                int jointsNb = PARAMS.nbJoints;



            double distances[] = new double[jointsNb];
            double incLength = PARAMS.extendedArmLength /jointsNb;

            for(int i = 0;i<jointsNb;i++){
                distances[i] = incLength;
            }

                ELLSA ellsas[] = new ELLSA[2];
                ellsas[0] = ellsaTheta0;
                RobotController robotController = new RobotController(jointsNb);
                RobotArmManager robotArmManager = new RobotArmManager(jointsNb, distances, ellsas, robotController, PARAMS.nbTrainingCycle, PARAMS.nbRequestCycle);
                robotArmManager.maxError = PARAMS.extendedArmLength *2;


                RobotWorlExampleMultiUI robot = new RobotWorlExampleMultiUI(null, null, null, robotController, robotArmManager, jointsNb);

                while(!robotArmManager.finished){
                    robot.cycleCommandLine();
                }

                double error = robotArmManager.averageError.getAsDouble();
                double dispersion = Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size());

                HashMap<String, Double> mappingScores = ellsaTheta0.getHeadAgent().getMappingScores();

                HashMap<REQUEST, Integer> requestCounts = ellsaTheta0.data.requestCounts;



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
