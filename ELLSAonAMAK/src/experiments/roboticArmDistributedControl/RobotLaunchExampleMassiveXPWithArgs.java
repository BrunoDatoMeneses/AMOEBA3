package experiments.roboticArmDistributedControl;





import agents.head.REQUEST;
import experiments.mathematicalModels.Model_Manager;
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

public class RobotLaunchExampleMassiveXPWithArgs {

    private static CSVWriter xpCSV;

    public static void main (String[] args)  {

        TRACE.minLevel = TRACE_LEVEL.OFF;
        PARAMS.armBaseSize = 50.0;

        PARAMS.nbJoints = Integer.parseInt(args[0]);
        PARAMS.nbLearningCycle = Integer.parseInt(args[1]);
        PARAMS.nbExploitationCycle = Integer.parseInt(args[2]);
        PARAMS.requestControlCycles = Integer.parseInt(args[3]);
        PARAMS.nbepisodes = Integer.parseInt(args[4]);
        PARAMS.validityRangesPrecision = ((double)Integer.parseInt(args[5]))/100;
        PARAMS.neighborhoodRadiusCoefficient = Integer.parseInt(args[6]);
        PARAMS.isOrientationGoal = Boolean.parseBoolean(args[7]);
        PARAMS.setLearnFromNeighbors = Boolean.parseBoolean(args[8]);


        PARAMS.dimension = PARAMS.nbJoints+1;

        System.out.print("neighS " + PARAMS.neighborhoodRadiusCoefficient + " ");
        System.out.print("rgn " + PARAMS.validityRangesPrecision + " ");
        System.out.print("eps " + PARAMS.nbepisodes + " ");
        System.out.print("lrn " + PARAMS.nbLearningCycle + " ");
        System.out.print("expl " + PARAMS.nbExploitationCycle + " ");
        System.out.print("ctrl " + PARAMS.requestControlCycles + " ");
        System.out.print("jts " + PARAMS.nbJoints + " ");
        System.out.print("ori " + PARAMS.isOrientationGoal + " ");
        System.out.print("endo " + PARAMS.setLearnFromNeighbors + " ");


        PARAMS.configFile = "resources/1jointRobotOrigin2DimensionsLauncher.xml";


        experimentation();

        System.out.print(" DONE");

        System.exit(1);
    }


    public static void experimentation() {

        xpCSV = new CSVWriter(PARAMS.model + "_Jts_" + PARAMS.nbJoints
                +"_Lrn_" + PARAMS.nbLearningCycle
                +"_Exp_" + PARAMS.nbExploitationCycle
                +"_Eps_" + PARAMS.nbepisodes
                +"_Ctrl_" + PARAMS.requestControlCycles
                +"_Pre_" + PARAMS.validityRangesPrecision
                +"_NghS_" + PARAMS.neighborhoodRadiusCoefficient
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

        HashMap<String, ArrayList<Double>> data = new HashMap<>();
        List<String> dataStringsVolumes = Arrays.asList("mappingScore", "imprecisionScore", "conflictVol", "concurrenceVol", "voidVol");
        List<String> dataStringsPrediction = Arrays.asList("goalXYError", "goalXYErrorDeviation", "goalThetaError", "goalThetaErrorDeviation");
        List<String> dataStringsOther = Arrays.asList("localMinima","nbAgents", "conflictRequests", "concurrenceRequests", "frontierRequests", "voidRequests", "modelRequests","endogenousLearningSituations","fusionRequests","restructureRequests","neighborsCounts","learningCycleExecutionTime","exploitationCycleExecutionTime", "learningCycleExecutionTimeDeviation","exploitationCycleExecutionTimeDeviation");


        for (String dataName : dataStringsVolumes){
            data.put(dataName, new ArrayList<>());
        }

        for (String dataName : dataStringsPrediction){
            data.put(dataName, new ArrayList<>());
        }

        for (String dataName : dataStringsOther){
            data.put(dataName, new ArrayList<>());
        }

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

            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",averageScore.getAsDouble()+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" ,"" + Math.sqrt(deviationScore/data.get(dataName).size()))));

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

            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Average",df.format(averageScore.getAsDouble())+"")));
            xpCSV.write(new ArrayList<>(Arrays.asList(dataName+"Deviation" , df.format(Math.sqrt(deviationScore/data.get(dataName).size())))));


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

        data = null;
    }

    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {
        ELLSA[] ellsas;
        StudiedSystem[] studiedSystems;
        ellsas = new ELLSA[PARAMS.nbJoints];
        studiedSystems = new StudiedSystem[PARAMS.nbJoints];

        for(int i=0;i<PARAMS.nbJoints;i++){


            studiedSystems[i] = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
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
            ellsas[i].getEnvironment().setMappingErrorAllowed(experiments.roboticArmCentralizedControl.PARAMS.validityRangesPrecision);
            ellsas[i].data.PARAM_modelErrorMargin = experiments.roboticArmCentralizedControl.PARAMS.modelErrorMargin;
            ellsas[i].data.PARAM_bootstrapCycle = experiments.roboticArmCentralizedControl.PARAMS.setbootstrapCycle;
            ellsas[i].data.PARAM_exogenousLearningWeight = experiments.roboticArmCentralizedControl.PARAMS.exogenousLearningWeight;
            ellsas[i].data.PARAM_endogenousLearningWeight = experiments.roboticArmCentralizedControl.PARAMS.endogenousLearningWeight;

            ellsas[i].data.PARAM_neighborhoodRadiusCoefficient = experiments.roboticArmCentralizedControl.PARAMS.neighborhoodRadiusCoefficient;
            ellsas[i].data.PARAM_influenceRadiusCoefficient = experiments.roboticArmCentralizedControl.PARAMS.influenceRadiusCoefficient;
            ellsas[i].data.PARAM_maxRangeRadiusCoefficient = experiments.roboticArmCentralizedControl.PARAMS.maxRangeRadiusCoefficient;
            ellsas[i].data.PARAM_rangeSimilarityCoefficient = experiments.roboticArmCentralizedControl.PARAMS.rangeSimilarityCoefficient;
            ellsas[i].data.PARAM_minimumRangeCoefficient = experiments.roboticArmCentralizedControl.PARAMS.minimumRangeCoefficient;

            ellsas[i].data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = experiments.roboticArmCentralizedControl.PARAMS.nbOfNeighborForVoidDetectionInSelfLearning;
            ellsas[i].data.PARAM_creationNeighborNumberForContexCreationWithouOracle = experiments.roboticArmCentralizedControl.PARAMS.nbOfNeighborForContexCreationWithouOracle;

            ellsas[i].data.PARAM_perceptionsGenerationCoefficient = experiments.roboticArmCentralizedControl.PARAMS.perceptionsGenerationCoefficient;
            ellsas[i].data.PARAM_modelSimilarityThreshold = experiments.roboticArmCentralizedControl.PARAMS.modelSimilarityThreshold;

            ellsas[i].data.PARAM_LEARNING_WEIGHT_ACCURACY = experiments.roboticArmCentralizedControl.PARAMS.LEARNING_WEIGHT_ACCURACY;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_PROXIMITY = experiments.roboticArmCentralizedControl.PARAMS.LEARNING_WEIGHT_PROXIMITY;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_EXPERIENCE = experiments.roboticArmCentralizedControl.PARAMS.LEARNING_WEIGHT_EXPERIENCE;
            ellsas[i].data.PARAM_LEARNING_WEIGHT_GENERALIZATION = experiments.roboticArmCentralizedControl.PARAMS.LEARNING_WEIGHT_GENERALIZATION;

            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY = experiments.roboticArmCentralizedControl.PARAMS.EXPLOITATION_WEIGHT_PROXIMITY;
            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = experiments.roboticArmCentralizedControl.PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE;
            ellsas[i].data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = experiments.roboticArmCentralizedControl.PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION;


            ellsas[i].data.PARAM_isActiveLearning = experiments.roboticArmCentralizedControl.PARAMS.setActiveLearning;
            ellsas[i].data.PARAM_isSelfLearning = experiments.roboticArmCentralizedControl.PARAMS.setSelfLearning;

            ellsas[i].data.PARAM_NCS_isConflictDetection = experiments.roboticArmCentralizedControl.PARAMS.setConflictDetection;
            ellsas[i].data.PARAM_NCS_isConcurrenceDetection = experiments.roboticArmCentralizedControl.PARAMS.setConcurrenceDetection;
            ellsas[i].data.PARAM_NCS_isVoidDetection = experiments.roboticArmCentralizedControl.PARAMS.setVoidDetection;
            ellsas[i].data.PARAM_NCS_isSubVoidDetection = experiments.roboticArmCentralizedControl.PARAMS.setSubVoidDetection;
            ellsas[i].data.PARAM_NCS_isConflictResolution = experiments.roboticArmCentralizedControl.PARAMS.setConflictResolution;
            ellsas[i].data.PARAM_NCS_isConcurrenceResolution = experiments.roboticArmCentralizedControl.PARAMS.setConcurrenceResolution;
            ellsas[i].data.PARAM_NCS_isFrontierRequest = experiments.roboticArmCentralizedControl.PARAMS.setFrontierRequest;
            ellsas[i].data.PARAM_NCS_isSelfModelRequest = experiments.roboticArmCentralizedControl.PARAMS.setSelfModelRequest;
            ellsas[i].data.PARAM_NCS_isFusionResolution = experiments.roboticArmCentralizedControl.PARAMS.setFusionResolution;
            ellsas[i].data.PARAM_NCS_isRetrucstureResolution = experiments.roboticArmCentralizedControl.PARAMS.setRestructureResolution;

            ellsas[i].data.PARAM_NCS_isCreationWithNeighbor = experiments.roboticArmCentralizedControl.PARAMS.setisCreationWithNeighbor;


            ellsas[i].data.PARAM_isLearnFromNeighbors = experiments.roboticArmCentralizedControl.PARAMS.setLearnFromNeighbors;
            ellsas[i].data.PARAM_nbOfNeighborForLearningFromNeighbors = experiments.roboticArmCentralizedControl.PARAMS.nbOfNeighborForLearningFromNeighbors;
            ellsas[i].data.PARAM_isDream = experiments.roboticArmCentralizedControl.PARAMS.setDream;
            ellsas[i].data.PARAM_DreamCycleLaunch = experiments.roboticArmCentralizedControl.PARAMS.setDreamCycleLaunch;


            ellsas[i].data.PARAM_isAutonomousMode = experiments.roboticArmCentralizedControl.PARAMS.setAutonomousMode;

            ellsas[i].data.PARAM_NCS_isAllContextSearchAllowedForLearning = experiments.roboticArmCentralizedControl.PARAMS.isAllContextSearchAllowedForLearning;
            ellsas[i].data.PARAM_NCS_isAllContextSearchAllowedForExploitation = experiments.roboticArmCentralizedControl.PARAMS.isAllContextSearchAllowedForExploitation;

            ellsas[i].data.PARAM_probabilityOfRangeAmbiguity = experiments.roboticArmCentralizedControl.PARAMS.probabilityOfRangeAmbiguity;



            ellsas[i].getEnvironment().PARAM_minTraceLevel = experiments.roboticArmCentralizedControl.PARAMS.traceLevel;



            ellsas[i].setSubPercepts(experiments.roboticArmCentralizedControl.PARAMS.subPercepts);
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

        double learningTime = robotArmManager.averageLearningTimes.getAsDouble();
        double learningTimeDispersion = Math.sqrt(robotArmManager.learningTimesDispersion /robotArmManager.learningTimes.size());

        double exploitationTime = robotArmManager.averageExploitationTimes.getAsDouble();
        double exploitationTimeDispersion = Math.sqrt(robotArmManager.exploitationTimesDispersion /robotArmManager.exploitationTimes.size());


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

        data.get("learningCycleExecutionTime").add(learningTime);
        data.get("exploitationCycleExecutionTime").add(exploitationTime);
        data.get("learningCycleExecutionTimeDeviation").add(learningTimeDispersion);
        data.get("exploitationCycleExecutionTimeDeviation").add(exploitationTimeDispersion);

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
        xpCSV.write(new ArrayList<>(Arrays.asList("precisionRange", PARAMS.validityRangesPrecision +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("neighborhoodSize", PARAMS.neighborhoodRadiusCoefficient +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isOrientationGoal", PARAMS.isOrientationGoal+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("armBaseSize", PARAMS.armBaseSize+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("requestControlCycles", PARAMS.requestControlCycles+"")));


        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("LEARNING")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isActiveLearning", PARAMS.setActiveLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("isSelfLearning", PARAMS.setSelfLearning+"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("goalXYError")));
        xpCSV.write(new ArrayList<>(Arrays.asList("errorMargin", PARAMS.modelErrorMargin +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList(" ")));

        xpCSV.write(new ArrayList<>(Arrays.asList("REGRESSION")));
        xpCSV.write(new ArrayList<>(Arrays.asList("noise", PARAMS.noiseRange +"")));
        xpCSV.write(new ArrayList<>(Arrays.asList("learningSpeed", PARAMS.exogenousLearningWeight +"")));
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
        xpCSV.write(new ArrayList<>(Arrays.asList("isIncompetenceNCS", PARAMS.setVoidDetection +"")));
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
