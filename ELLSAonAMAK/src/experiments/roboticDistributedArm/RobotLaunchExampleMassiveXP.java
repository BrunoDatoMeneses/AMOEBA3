package experiments.roboticDistributedArm;





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
import java.util.*;

public class RobotLaunchExampleMassiveXP {

    private static CSVWriter xpCSV;
    private static HashMap<String, ArrayList<Double>> data;

    private static ELLSA[] ellsas;
    private static StudiedSystem[] studiedSystems;

    private static List<String> dataStringsVolumes = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol");
    private static List<String> dataStringsPrediction = Arrays.asList("goalXYError", "goalXYErrorDeviation", "goalThetaError", "goalThetaErrorDeviation");
    private static List<String> dataStringsOther = Arrays.asList("localMinima","nbAgents", "conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests", "modelRequests","endogenousLearningSituations","fusionRequests","restructureRequests","neighborsCounts");


    public static void main (String[] args)  {



        TRACE.minLevel = TRACE_LEVEL.OFF;
        PARAMS.nbepisodes = 1;
        PARAMS.armBaseSize = 50.0;


        /*ArrayList<Integer> neighborhoodMultiplicators = new ArrayList<>(Arrays.asList(2));
        ArrayList<Integer> trainingCycles = new ArrayList<>(Arrays.asList(200));
        ArrayList<Integer> requestCycles = new ArrayList<>(Arrays.asList(50));
        ArrayList<Double> mappingErrors = new ArrayList<>(Arrays.asList(0.02,0.04,0.06));
        ArrayList<Integer> jointsNb  = new ArrayList<>(Arrays.asList(2,3,6,10,20,30,50,100));
        ArrayList<Integer> episodes = new ArrayList<>(Arrays.asList(15));
        ArrayList<Integer> controlCycles = new ArrayList<>(Arrays.asList(1,5,10,20));
        ArrayList<Boolean> orientationGoal = new ArrayList<>(Arrays.asList(true, false));
        ArrayList<Boolean> isLearnFromNeighbors = new ArrayList<>(Arrays.asList(true, false));*/

        ArrayList<Integer> neighborhoodMultiplicators = new ArrayList<>(Arrays.asList(2));
        ArrayList<Integer> trainingCycles = new ArrayList<>(Arrays.asList(200));
        ArrayList<Integer> requestCycles = new ArrayList<>(Arrays.asList(50));
        ArrayList<Double> mappingErrors = new ArrayList<>(Arrays.asList(0.04));
        ArrayList<Integer> jointsNb  = new ArrayList<>(Arrays.asList(5));
        ArrayList<Integer> episodes = new ArrayList<>(Arrays.asList(5));
        ArrayList<Integer> controlCycles = new ArrayList<>(Arrays.asList(10));
        ArrayList<Boolean> orientationGoal = new ArrayList<>(Arrays.asList(false));
        ArrayList<Boolean> isLearnFromNeighbors = new ArrayList<>(Arrays.asList(true));


        for(Integer neighborhoodMultiplicator : neighborhoodMultiplicators) {
            for (Double mappingError : mappingErrors) {
                for (Integer trainingCycle : trainingCycles) {
                    for (Integer requestCycle : requestCycles) {
                        for (Integer eps : episodes) {
                            for (Integer control : controlCycles) {
                                for (Integer jointNb : jointsNb){
                                    for (Boolean goal : orientationGoal){
                                        for (Boolean endo : isLearnFromNeighbors){
                                            PARAMS.mappingErrorAllowed = mappingError;
                                            PARAMS.neighborhoodMultiplicator = neighborhoodMultiplicator;
                                            PARAMS.nbExploitationCycle = requestCycle;
                                            PARAMS.nbLearningCycle = trainingCycle;
                                            PARAMS.nbJoints = jointNb;
                                            PARAMS.dimension = jointNb + 1;
                                            PARAMS.nbepisodes = eps;
                                            PARAMS.isOrientationGoal = goal;
                                            PARAMS.requestControlCycles = control;
                                            PARAMS.setLearnFromNeighbors = endo;

                                            PARAMS.configFile = "resources/1jointRobotOrigin2DimensionsLauncher.xml";

                                            System.out.print("neighborhoodMultiplicator " + PARAMS.neighborhoodMultiplicator + " ");
                                            System.out.print("mappingError " + PARAMS.mappingErrorAllowed + " ");
                                            System.out.print("episodes " + PARAMS.nbepisodes + " ");
                                            System.out.print("learningCycles " + PARAMS.nbLearningCycle + " ");
                                            System.out.print("exploitationCycles " + PARAMS.nbExploitationCycle + " ");
                                            System.out.print("controlCycles " + PARAMS.requestControlCycles + " ");
                                            System.out.print("jointNb " + PARAMS.nbJoints + " ");
                                            System.out.print("isOrientationGoal " + PARAMS.isOrientationGoal + " ");
                                            System.out.print("isLearnFromNeighbors " + PARAMS.setLearnFromNeighbors + " ");

                                            start();

                                            System.out.println(" DONE");
                                        }

                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        System.out.println("THE END");
        System.exit(1);
    }


    public static void start() {

        data = new HashMap<>();

        for (String dataName : dataStringsVolumes){
            data.put(dataName, new ArrayList<>());
        }

        for (String dataName : dataStringsPrediction){
            data.put(dataName, new ArrayList<>());
        }

        for (String dataName : dataStringsOther){
            data.put(dataName, new ArrayList<>());
        }

        xpCSV = new CSVWriter(PARAMS.model + "_Jts_" + PARAMS.nbJoints
                +"_Lrn_" + PARAMS.nbLearningCycle
                +"_Exp_" + PARAMS.nbExploitationCycle
                +"_Eps_" + PARAMS.nbepisodes
                +"_Ctrl_" + PARAMS.requestControlCycles
                +"_Pre_" + PARAMS.mappingErrorAllowed
                +"_NghS_" + PARAMS.neighborhoodMultiplicator
                +"_Orie_" + PARAMS.isOrientationGoal
                +"_Endo_" + PARAMS.setLearnFromNeighbors




                //+ dateAndHour

        );

        writeParams(PARAMS.model);

        // Set AMAK configuration before creating an AMOEBA
        Configuration.multiUI=true;
        Configuration.commandLineMode = true;
        Configuration.allowedSimultaneousAgentsExecution = 1;
        Configuration.waitForGUI = false;
        Configuration.plotMilliSecondsUpdate = 20000;




        double start = System.currentTimeMillis();
        for (int i = 0; i < PARAMS.nbepisodes; ++i) {
            //System.out.print(i + " ");
            learningEpisode(data);
        }
        //System.out.println(" ");
        double total = (System.currentTimeMillis()- start)/1000;
        double mean = total/ PARAMS.nbepisodes;
        //System.out.println("[TIME MEAN] " + mean + " s");
        //System.out.println("[TIME TOTAL] " + total + " s");


        xpCSV.write(new ArrayList<>(Arrays.asList("meanTime", ""+mean)));
        xpCSV.write(new ArrayList<>(Arrays.asList("totalTime",""+total )));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for (String dataName : dataStringsVolumes){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",averageScore.getAsDouble()*100+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" ,"" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));



        }

        for (String dataName : dataStringsPrediction){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //.println(dataName +" [AVERAGE] " + averageScore.getAsDouble()*100 + " - " + "[DEVIATION] " +100*Math.sqrt(deviationScore/data.get(dataName).size()));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",averageScore.getAsDouble()*100+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" ,"" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));


        }

        for (String dataName : dataStringsOther){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();

            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",averageScore.getAsDouble()*100+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" ,"" + 100*Math.sqrt(deviationScore/data.get(dataName).size()))));

        }

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        //Create the formatter for round the values of scores
        Locale currentLocale = Locale.getDefault();
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(currentLocale);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("##.##", otherSymbols);
        //System.out.println("ROUNDED");
        xpCSV.write(new ArrayList<>(Arrays.asList("ROUNDED")));
        for (String dataName : dataStringsVolumes){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();
            //System.out.println(dataName +" [AVERAGE] " + df.format(averageScore.getAsDouble()*100) + " - " + "[DEVIATION] " +df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",df.format(averageScore.getAsDouble()*100)+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" , df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())))));


        }
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        for (String dataName : dataStringsOther){
            OptionalDouble averageScore = data.get(dataName).stream().mapToDouble(a->a).average();
            Double deviationScore = data.get(dataName).stream().mapToDouble(a->Math.pow((a-averageScore.getAsDouble()),2)).sum();

            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",df.format(averageScore.getAsDouble()*100)+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" , df.format(100*Math.sqrt(deviationScore/data.get(dataName).size())))));


        }

        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        OptionalDouble averageXYScore = data.get("goalXYError").stream().mapToDouble(a->a).average();
        OptionalDouble averageXYScoreDeviation = data.get("goalXYErrorDeviation").stream().mapToDouble(a->a).average();
        Double deviationXYScore = data.get("goalXYError").stream().mapToDouble(a->Math.pow((a-averageXYScore.getAsDouble()),2)).sum();
        Double deviationXYScoreDisp = data.get("goalXYErrorDeviation").stream().mapToDouble(a->Math.pow((a-averageXYScoreDeviation.getAsDouble()),2)).sum();

        /*System.out.println("[GOAL XY ERROR AVERAGE          ] " + averageXYScore.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationXYScore/data.get("goalXYError").size()) );
        System.out.println("[GOAL XY ERROR DEVIATION AVERAGE] " + averageXYScoreDeviation.getAsDouble() + " - " + "[DEVIATION] " +Math.sqrt(deviationXYScoreDisp/data.get("goalXYErrorDeviation").size()) );
*/
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorAverage" , ""+averageXYScore.getAsDouble())));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorDeviationAverage" , ""+averageXYScoreDeviation.getAsDouble())));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorEpisodeDeviationAverage" , ""+Math.sqrt(deviationXYScore/data.get("goalXYError").size()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorDeviationEpisodeDeviationAverage" ,""+Math.sqrt(deviationXYScoreDisp/data.get("goalXYErrorDeviation").size()))));


        OptionalDouble averageThetaScore = data.get("goalThetaError").stream().mapToDouble(a->a).average();
        OptionalDouble averageThetaScoreDeviation = data.get("goalThetaErrorDeviation").stream().mapToDouble(a->a).average();
        Double deviationThetaScore = data.get("goalThetaError").stream().mapToDouble(a->Math.pow((a-averageThetaScore.getAsDouble()),2)).sum();
        Double deviationThetaScoreDisp = data.get("goalThetaErrorDeviation").stream().mapToDouble(a->Math.pow((a-averageThetaScoreDeviation.getAsDouble()),2)).sum();
        
        

        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorAverage" , ""+averageThetaScore.getAsDouble())));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorDeviationAverage" , ""+averageThetaScoreDeviation.getAsDouble())));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorEpisodeDeviationAverage" , ""+Math.sqrt(deviationThetaScore/data.get("goalThetaError").size()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorDeviationEpisodeDeviationAverage" ,""+Math.sqrt(deviationThetaScoreDisp/data.get("goalThetaErrorDeviation").size()))));


        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorAveragePercentage" , ""+df.format(100*averageXYScore.getAsDouble()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorDeviationAveragePercentage" , ""+df.format(100*averageXYScoreDeviation.getAsDouble()) )));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorEpisodeDeviationAveragePercentage" , ""+df.format(100*Math.sqrt(deviationXYScore/data.get("goalXYError").size())))));
        xpCSV.write(new ArrayList<>(Arrays.asList("xyErrorDeviationEpisodeDeviationAveragePercentage" ,""+df.format(100*Math.sqrt(deviationXYScoreDisp/data.get("goalXYErrorDeviation").size())))));

        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorAveragePercentage" , ""+df.format(100*averageThetaScore.getAsDouble()))));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorDeviationAveragePercentage" , ""+df.format(100*averageThetaScoreDeviation.getAsDouble()) )));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorEpisodeDeviationAveragePercentage" , ""+df.format(100*Math.sqrt(deviationThetaScore/data.get("goalThetaError").size())))));
        xpCSV.write(new ArrayList<>(Arrays.asList("thetaErrorDeviationEpisodeDeviationAveragePercentage" ,""+df.format(100*Math.sqrt(deviationThetaScoreDisp/data.get("goalThetaErrorDeviation").size())))));


        xpCSV.close();


    }

    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {

        ellsas = new ELLSA[PARAMS.nbJoints];
        studiedSystems = new StudiedSystem[PARAMS.nbJoints];

        for(int i=0;i<PARAMS.nbJoints;i++){


            studiedSystems[i] = new F_N_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.oracleNoiseRange);
            ellsas[i] = new ELLSA(null,  null);
            ellsas[i].setStudiedSystem(studiedSystems[i]);
            IBackupSystem backupSystem = new BackupSystem(ellsas[i]);
            File file;
            file = new File("resources/1jointRobotOrigin2DimensionsLauncher.xml");

            backupSystem.load(file);

            //ellsaTheta0.saver = new SaveHelperImpl(ellsaTheta0, amoebaUITheta0);

            ellsas[i].allowGraphicalScheduler(true);
            ellsas[i].setRenderUpdate(false);
            ellsas[i].data.nameID = "ellsaTheta"+i;
            ellsas[i].data.learningSpeed = PARAMS.learningSpeed;
            ellsas[i].data.numberOfPointsForRegression = PARAMS.regressionPoints;
            ellsas[i].data.isActiveLearning = PARAMS.setActiveLearning;
            ellsas[i].data.isSelfLearning = PARAMS.setSelfLearning;
            ellsas[i].data.isAutonomousMode = PARAMS.setAutonomousMode;
            ellsas[i].data.isConflictDetection = PARAMS.setConflictDetection;
            ellsas[i].data.isConcurrenceDetection = PARAMS.setConcurrenceDetection;
            ellsas[i].data.isVoidDetection2 = PARAMS.setVoidDetection2;
            ellsas[i].data.isSubVoidDetection = PARAMS.setSubVoidDetection;
            ellsas[i].data.isConflictResolution = PARAMS.setConflictResolution;
            ellsas[i].data.isConcurrenceResolution = PARAMS.setConcurrenceResolution;
            ellsas[i].data.isFrontierRequest = PARAMS.setFrontierRequest;
            ellsas[i].data.isSelfModelRequest = PARAMS.setSelfModelRequest;
            ellsas[i].data.isCoopLearningWithoutOracle = PARAMS.setCoopLearning;

            ellsas[i].data.isLearnFromNeighbors = PARAMS.setLearnFromNeighbors;
            ellsas[i].data.nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
            ellsas[i].data.isDream = PARAMS.setDream;
            ellsas[i].data.nbOfNeighborForVoidDetectionInSelfLearning = PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
            ellsas[i].data.nbOfNeighborForContexCreationWithouOracle = PARAMS.nbOfNeighborForContexCreationWithouOracle;

            ellsas[i].getEnvironment().setMappingErrorAllowed(PARAMS.mappingErrorAllowed);
            ellsas[i].data.initRegressionPerformance = PARAMS.setRegressionPerformance;
            ellsas[i].getEnvironment().minLevel = TRACE_LEVEL.OFF;
            ellsas[i].setSubPercepts(PARAMS.subPercepts);
        }


        //AmasMultiUIWindow window = new AmasMultiUIWindow("Robot Arm");
        //WorldExampleMultiUI env = new WorldExampleMultiUI(window);
        //VUIMulti vui = new VUIMulti("Robot");


        double distances[] = new double[PARAMS.nbJoints];
        double incLength = PARAMS.armBaseSize/PARAMS.nbJoints;

        double sum = 0.0;
        for(int i = 0;i<PARAMS.nbJoints;i++){
            distances[i] = incLength-(i*(incLength/(PARAMS.nbJoints*2)));
            sum += distances[i];
        }


        RobotController robotController = new RobotController(PARAMS.nbJoints);
        RobotArmManager robotArmManager = new RobotArmManager(PARAMS.nbJoints, distances, ellsas, robotController, PARAMS.nbLearningCycle, PARAMS.nbExploitationCycle);
        robotArmManager.maxError = sum*2;

        robotArmManager.propagationControlWaves = PARAMS.requestControlCycles;
        robotArmManager.isOrientationGoal = PARAMS.isOrientationGoal;


        RobotWorldExampleMultiUI robot = new RobotWorldExampleMultiUI(null, null, null, robotController, robotArmManager, PARAMS.nbJoints);

        while(!robotArmManager.finished){
            robot.cycleCommandLine();
        }



        //TRACE.print(TRACE_LEVEL.ERROR,robotArmManager.finished);
        //TRACE.print(TRACE_LEVEL.ERROR, + " [ " + Math.sqrt(robotArmManager.errorDispersion/robotArmManager.allGoalErrors.size()) + " ]      -    " + robotArmManager.goalErrors);
        double XYerror = robotArmManager.averageXYError.getAsDouble();
        double XYdispersion = Math.sqrt(robotArmManager.XYErrorDispersion /robotArmManager.allXYGoalErrors.size());

        double Thetaerror = robotArmManager.averageThetaError.getAsDouble();
        double Thetadispersion = Math.sqrt(robotArmManager.ThetaErrorDispersion /robotArmManager.allThetaGoalErrors.size());

        HashMap<String, Double> mappingScores = ellsas[0].getHeadAgent().getMappingScores();

        HashMap<REQUEST, Integer> requestCounts = ellsas[0].data.requestCounts;


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
        data.get("endogenousLearningSituations").add((double)requestCounts.get(REQUEST.NEIGHBOR));
        data.get("fusionRequests").add((double)requestCounts.get(REQUEST.FUSION));
        data.get("restructureRequests").add((double)requestCounts.get(REQUEST.RESTRUCTURE));
        data.get("nbAgents").add((double) ellsas[0].getContexts().size());
        data.get("localMinima").add((double) ellsas[0].data.countLocalMinina);
        data.get("goalXYError").add(XYerror);
        data.get("goalXYErrorDeviation").add(XYdispersion);
        data.get("goalThetaError").add(Thetaerror);
        data.get("goalThetaErrorDeviation").add(Thetadispersion);
        data.get("neighborsCounts").add((double)ellsas[0].data.neighborsCounts/ellsas[0].getCycle());



        ellsas = null;
        studiedSystems = null;
        robotController = null;
        robotArmManager = null;
        robot = null;
    }


    private static void writeParams(String model) {
        xpCSV.write(new ArrayList<>(Arrays.asList("PARAMS")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
        xpCSV.write(new ArrayList<>(Arrays.asList("SET")));
        xpCSV.write(new ArrayList<>(Arrays.asList("joints", PARAMS.nbJoints+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("dimension", PARAMS.dimension+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("model",model)));
        xpCSV.write(new ArrayList<>(Arrays.asList("learningCycles", PARAMS.nbLearningCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("exploitatingCycles", PARAMS.nbExploitationCycle +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("episodes", PARAMS.nbepisodes +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("spaceSize", PARAMS.spaceSize*4+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("precisionRange", PARAMS.mappingErrorAllowed+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("neighborhoodSize", PARAMS.neighborhoodMultiplicator+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isOrientationGoal", PARAMS.isOrientationGoal+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("armBaseSize", PARAMS.armBaseSize+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("requestControlCycles", PARAMS.requestControlCycles+"")));


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
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("NCS")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isConflictNCS", PARAMS.setConflictDetection+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isConcurenceNCS", PARAMS.setConcurrenceDetection+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isIncompetenceNCS", PARAMS.setVoidDetection2+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isAmbiguityNCS", PARAMS.setFrontierRequest+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isModelNCS", PARAMS.setSelfModelRequest+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isLearnFromNeighbors", PARAMS.setLearnFromNeighbors+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isDream", PARAMS.setDream+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("OTHER")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForLearningFromNeighbors", PARAMS.nbOfNeighborForLearningFromNeighbors+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForContexCreationWithouOracle", PARAMS.nbOfNeighborForContexCreationWithouOracle+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("nbOfNeighborForVoidDetectionInSelfLearning", PARAMS.nbOfNeighborForVoidDetectionInSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));
    }


}
