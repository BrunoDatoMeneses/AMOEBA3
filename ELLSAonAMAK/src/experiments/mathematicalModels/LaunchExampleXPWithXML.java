package experiments.mathematicalModels;

import agents.head.REQUEST;
import agents.head.SITUATION;
import fr.irit.smac.amak.Configuration;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.backup.BackupSystem;
import kernel.backup.IBackupSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import utils.CSVWriter;
import utils.PARSER;
import utils.Pair;
import utils.TRACE;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.OptionalDouble;

public class LaunchExampleXPWithXML {

    private static CSVWriter xpCSV;
    private static String SETTING_FILE_NAME;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        //On fournit le fichier de parametrage en argument sous la forme d'un fichier xml
        SETTING_FILE_NAME = "resources/" + args[0];
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document setting_file = db.parse(new File(SETTING_FILE_NAME));

        //On crée des NodeList pour chaque tag name du fichier xml qui nous permettra par la suite de récupérer les différents attributs
        NodeList nodeListGeneral = setting_file.getElementsByTagName("General");
        NodeList nodeListLearningWeights = setting_file.getElementsByTagName("LearningWeights");
        NodeList nodeListStrategies= setting_file.getElementsByTagName("Strategies");
        NodeList nodeListExploitationWeights = setting_file.getElementsByTagName("ExploitationWeights");
        NodeList nodeListNeighborhood = setting_file.getElementsByTagName("Neighborhood");
        NodeList nodeListPrediction = setting_file.getElementsByTagName("Prediction");
        NodeList nodeListRegression = setting_file.getElementsByTagName("Regression");
        NodeList nodeListXp = setting_file.getElementsByTagName("Xp");
        NodeList nodeListExploration = setting_file.getElementsByTagName("Exploration");
        NodeList nodeListNCS = setting_file.getElementsByTagName("NCS");
        NodeList nodeListUi = setting_file.getElementsByTagName("Ui");
        NodeList nodeListAmakConfiguration = setting_file.getElementsByTagName("AmakConfiguration");

        //On affecte les champs de PARAMS avec les valeur des attribut de chaque tag name du fichier xml

        //GENERAL SETTINGS
        for (int i = 0; i < nodeListGeneral.getLength(); i++) {
            Node node = nodeListGeneral.item(i);
            Element element = (Element) node;
            PARAMS.model = element.getAttribute("model");
            PARAMS.extension = element.getAttribute("extension");
            PARAMS.subPercepts = PARSER.convertStringToArraylist(element.getAttribute("subPercepts"));
            PARAMS.dimension = Integer.parseInt(element.getAttribute("dimension"));
            PARAMS.nbLearningCycle = Integer.parseInt(element.getAttribute("nbLearningCycle"));
            PARAMS.nbEndoExploitationCycle = Integer.parseInt(element.getAttribute("nbEndoExploitationCycle"));
            PARAMS.nbExploitationCycle = Integer.parseInt(element.getAttribute("nbExploitationCycle"));
            PARAMS.setActiveExploitation = Boolean.parseBoolean(element.getAttribute("setActiveExploitation"));
            PARAMS.nbEpisodes = Integer.parseInt(element.getAttribute("nbEpisodes"));
            PARAMS.transferCyclesRatio = Double.parseDouble(element.getAttribute("transferCyclesRatio"));
            PARAMS.spaceSize = Double.parseDouble(element.getAttribute("spaceSize"));
            PARAMS.validityRangesPrecision = Double.parseDouble(element.getAttribute("validityRangesPrecision"));
        }

        //LEARNING_WEIGHTS SETTINGS
        for (int i = 0; i < nodeListLearningWeights.getLength(); i++) {
            Node node = nodeListLearningWeights.item(i);
            Element element = (Element) node;
            PARAMS.LEARNING_WEIGHT_ACCURACY = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_ACCURACY"));
            PARAMS.LEARNING_WEIGHT_PROXIMITY = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_PROXIMITY"));
            PARAMS.LEARNING_WEIGHT_EXPERIENCE = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_EXPERIENCE"));
            PARAMS.LEARNING_WEIGHT_GENERALIZATION = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_GENERALIZATION"));
        }

        //STRATEGIES SETTINGS
        for (int i = 0; i < nodeListStrategies.getLength(); i++) {
            Node node = nodeListStrategies.item(i);
            Element element = (Element) node;
            PARAMS.setActiveLearning = Boolean.parseBoolean(element.getAttribute("setActiveLearning"));
            PARAMS.setSelfLearning = Boolean.parseBoolean(element.getAttribute("setSelfLearning"));
            PARAMS.setCooperativeNeighborhoodLearning = Boolean.parseBoolean(element.getAttribute("setCooperativeNeighborhoodLearning"));
        }

        //EXPLOITATION_WEIGHTS SETTINGS
        for (int i = 0; i < nodeListExploitationWeights.getLength(); i++) {
            Node node = nodeListExploitationWeights.item(i);
            Element element = (Element) node;
            PARAMS.EXPLOITATION_WEIGHT_PROXIMITY = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_PROXIMITY"));
            PARAMS.EXPLOITATION_WEIGHT_EXPERIENCE = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_EXPERIENCE"));
            PARAMS.EXPLOITATION_WEIGHT_GENERALIZATION = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_GENERALIZATION"));
        }

        //NEIGHBORHOOD SETTINGS
        for (int i = 0; i < nodeListNeighborhood.getLength(); i++) {
            Node node = nodeListNeighborhood.item(i);
            Element element = (Element) node;
            PARAMS.neighborhoodRadiusCoefficient = Double.parseDouble(element.getAttribute("neighborhoodRadiusCoefficient"));
            PARAMS.influenceRadiusCoefficient = Double.parseDouble(element.getAttribute("influenceRadiusCoefficient"));
            PARAMS.maxRangeRadiusCoefficient = Double.parseDouble(element.getAttribute("maxRangeRadiusCoefficient"));
            PARAMS.rangeSimilarityCoefficient = Double.parseDouble(element.getAttribute("rangeSimilarityCoefficient"));
            PARAMS.minimumRangeCoefficient = Double.parseDouble(element.getAttribute("minimumRangeCoefficient"));
        }

        //PREDICTION SETTINGS
        for (int i = 0; i < nodeListPrediction.getLength(); i++) {
            Node node = nodeListPrediction.item(i);
            Element element = (Element) node;
            PARAMS.modelErrorMargin = Double.parseDouble(element.getAttribute("modelErrorMargin"));
        }

        //REGRESSION SETTINGS
        for (int i = 0; i < nodeListRegression.getLength(); i++) {
            Node node = nodeListRegression.item(i);
            Element element = (Element) node;
            PARAMS.noiseRange = Double.parseDouble(element.getAttribute("noiseRange"));
            PARAMS.exogenousLearningWeight = Double.parseDouble(element.getAttribute("exogenousLearningWeight"));
            PARAMS.endogenousLearningWeight = Double.parseDouble(element.getAttribute("endogenousLearningWeight"));
            PARAMS.perceptionsGenerationCoefficient = Double.parseDouble(element.getAttribute("perceptionsGenerationCoefficient"));
            PARAMS.modelSimilarityThreshold = Double.parseDouble(element.getAttribute("modelSimilarityThreshold"));
            PARAMS.regressionPoints = (int)(1/PARAMS.exogenousLearningWeight);
        }

        //XP SETTINGS
        for (int i = 0; i < nodeListXp.getLength(); i++) {
            Node node = nodeListXp.item(i);
            Element element = (Element) node;
            PARAMS.nbOfModels = Integer.parseInt(element.getAttribute("nbOfModels"));
            PARAMS.normType = Integer.parseInt(element.getAttribute("normType"));
        }

        //EXPLORATION SETTINGS
        for (int i = 0; i < nodeListExploration.getLength(); i++) {
            Node node = nodeListExploration.item(i);
            Element element = (Element) node;
            PARAMS.continousExploration = Boolean.parseBoolean(element.getAttribute("continousExploration"));
            PARAMS.randomExploration = !PARAMS.continousExploration;
            PARAMS.limitedToSpaceZone = Boolean.parseBoolean(element.getAttribute("limitedToSpaceZone"));
            PARAMS.explorationIncrement = Double.parseDouble(element.getAttribute("explorationIncrement"));
            PARAMS.explorationWidht = Double.parseDouble(element.getAttribute("explorationWidht"));
            PARAMS.setbootstrapCycle = Integer.parseInt(element.getAttribute("setbootstrapCycle"));
        }

        //NCS SETTINGS
        for (int i = 0; i < nodeListNCS.getLength(); i++) {
            Node node = nodeListNCS.item(i);
            Element element = (Element) node;
            PARAMS.setModelAmbiguityDetection = Boolean.parseBoolean(element.getAttribute("setModelAmbiguityDetection"));
            PARAMS.setConflictDetection = Boolean.parseBoolean(element.getAttribute("setConflictDetection"));
            PARAMS.setConcurrenceDetection = Boolean.parseBoolean(element.getAttribute("setConcurrenceDetection"));
            PARAMS.setIncompetenceDetection = Boolean.parseBoolean(element.getAttribute("setIncompetenceDetection"));
            PARAMS.setCompleteRedundancyDetection = Boolean.parseBoolean(element.getAttribute("setCompleteRedundancyDetection"));
            PARAMS.setPartialRedundancyDetection = Boolean.parseBoolean(element.getAttribute("setPartialRedundancyDetection"));
            PARAMS.setRangeAmbiguityDetection = Boolean.parseBoolean(element.getAttribute("setRangeAmbiguityDetection"));
            PARAMS.setisCreationWithNeighbor = Boolean.parseBoolean(element.getAttribute("setisCreationWithNeighbor"));
            PARAMS.isAllContextSearchAllowedForLearning = Boolean.parseBoolean(element.getAttribute("isAllContextSearchAllowedForLearning"));
            PARAMS.isAllContextSearchAllowedForExploitation = Boolean.parseBoolean(element.getAttribute("isAllContextSearchAllowedForExploitation"));
            PARAMS.setConflictResolution = PARAMS.setConflictDetection;
            PARAMS.setConcurrenceResolution = PARAMS.setConcurrenceDetection;
            PARAMS.setSubIncompetencedDetection = Boolean.parseBoolean(element.getAttribute("setSubIncompetencedDetection"));
            PARAMS.setDream = Boolean.parseBoolean(element.getAttribute("setDream"));
            PARAMS.setDreamCycleLaunch = Integer.parseInt(element.getAttribute("setDreamCycleLaunch"));
            PARAMS.nbOfNeighborForLearningFromNeighbors = Integer.parseInt(element.getAttribute("nbOfNeighborForLearningFromNeighbors"));
            PARAMS.nbOfNeighborForContexCreationWithouOracle = Integer.parseInt(element.getAttribute("nbOfNeighborForContexCreationWithouOracle"));
            PARAMS.nbOfNeighborForVoidDetectionInSelfLearning = Integer.parseInt(element.getAttribute("nbOfNeighborForVoidDetectionInSelfLearning"));
            PARAMS.probabilityOfRangeAmbiguity = Double.parseDouble(element.getAttribute("probabilityOfRangeAmbiguity"));
            PARAMS.setAutonomousMode = Boolean.parseBoolean(element.getAttribute("setAutonomousMode"));
            PARAMS.traceLevel = TRACE.convertFromString(element.getAttribute("traceLevel"));
        }

        //UI SETTINGS
        for (int i = 0; i < nodeListUi.getLength(); i++) {
            Node node = nodeListUi.item(i);
            Element element = (Element) node;
            PARAMS.STOP_UI = Boolean.parseBoolean(element.getAttribute("STOP_UI"));
            PARAMS.STOP_UI_cycle = PARAMS.nbLearningCycle;
        }

        //On affecte les champs de Configuration avec les valeur des attribut de chaque tag name du fichier xml
        //CONFIGURATION SETTINGS
        // Set AMAK configuration before creating an AMOEBA
        for (int i = 0; i < nodeListAmakConfiguration.getLength(); i++) {
            Node node = nodeListAmakConfiguration.item(i);
            Element element = (Element) node;
            Configuration.multiUI = Boolean.parseBoolean(element.getAttribute("multiUI"));
            Configuration.commandLineMode = Boolean.parseBoolean(element.getAttribute("commandLineMode"));
            Configuration.allowedSimultaneousAgentsExecution = Integer.parseInt(element.getAttribute("allowedSimultaneousAgentsExecution"));
            Configuration.waitForGUI = Boolean.parseBoolean(element.getAttribute("waitForGUI"));
            Configuration.plotMilliSecondsUpdate = Integer.parseInt(element.getAttribute("plotMilliSecondsUpdate"));
        }

        experimentation();
        System.out.print(" DONE");
        System.exit(1);
    }


    public static void experimentation() {


        xpCSV = new CSVWriter(""+System.currentTimeMillis());
        WRITER.writeParams(xpCSV);
        xpCSV.close();
        Pair<ArrayList<List<String>>, HashMap<String, ArrayList<Double>>> dataPair = WRITER.getData();
        ArrayList<List<String>> dataStrings = dataPair.getA();
        HashMap<String, ArrayList<Double>> data = dataPair.getB();

        double start = System.nanoTime();

        for (int i = 0; i < PARAMS.nbEpisodes; ++i) {
            //System.out.print(i + " ");
            learningEpisode(data);
        }
        //System.out.println(" ");
        double total = (System.nanoTime() - start) / 1000;
        double mean = total / PARAMS.nbEpisodes;
        System.out.println("[TIME MEAN] " + mean + " s");
        System.out.println("[TIME TOTAL] " + total + " s");

        WRITER.writeData(xpCSV, data, dataStrings, total, mean);

        data = null;
    }


    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {
        ELLSA ellsa = new ELLSA(null, null);
        StudiedSystem studiedSystem = new Model_Manager(PARAMS.spaceSize, PARAMS.dimension, PARAMS.nbOfModels, PARAMS.normType, PARAMS.randomExploration, PARAMS.explorationIncrement, PARAMS.explorationWidht, PARAMS.limitedToSpaceZone, PARAMS.noiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File(SETTING_FILE_NAME);
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

        ellsa.data.PARAM_perceptionsGenerationCoefficient = PARAMS.perceptionsGenerationCoefficient;
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
        ellsa.data.PARAM_NCS_isVoidDetection = PARAMS.setIncompetenceDetection;
        ellsa.data.PARAM_NCS_isSubVoidDetection = PARAMS.setSubIncompetencedDetection;
        ellsa.data.PARAM_NCS_isConflictResolution = PARAMS.setConflictResolution;
        ellsa.data.PARAM_NCS_isConcurrenceResolution = PARAMS.setConcurrenceResolution;
        ellsa.data.PARAM_NCS_isFrontierRequest = PARAMS.setRangeAmbiguityDetection;
        ellsa.data.PARAM_NCS_isSelfModelRequest = PARAMS.setModelAmbiguityDetection;
        ellsa.data.PARAM_NCS_isFusionResolution = PARAMS.setCompleteRedundancyDetection;
        ellsa.data.PARAM_NCS_isRetrucstureResolution = PARAMS.setPartialRedundancyDetection;

        ellsa.data.PARAM_NCS_isCreationWithNeighbor = PARAMS.setisCreationWithNeighbor;


        ellsa.data.PARAM_isLearnFromNeighbors = PARAMS.setCooperativeNeighborhoodLearning;
        ellsa.data.PARAM_nbOfNeighborForLearningFromNeighbors = PARAMS.nbOfNeighborForLearningFromNeighbors;
        ellsa.data.PARAM_isDream = PARAMS.setDream;
        ellsa.data.PARAM_DreamCycleLaunch = PARAMS.setDreamCycleLaunch;


        ellsa.data.PARAM_isAutonomousMode = PARAMS.setAutonomousMode;

        ellsa.data.PARAM_NCS_isAllContextSearchAllowedForLearning = PARAMS.isAllContextSearchAllowedForLearning;
        ellsa.data.PARAM_NCS_isAllContextSearchAllowedForExploitation = PARAMS.isAllContextSearchAllowedForExploitation;

        ellsa.data.PARAM_probabilityOfRangeAmbiguity = PARAMS.probabilityOfRangeAmbiguity;


        ellsa.getEnvironment().PARAM_minTraceLevel = PARAMS.traceLevel;


        ellsa.setSubPercepts(experiments.roboticArmDistributedControl.PARAMS.subPercepts);


        ArrayList<Double> allLearningCycleTimes = new ArrayList<>();
        ArrayList<Double> allExploitationCycleTimes = new ArrayList<>();

        for (int i = 0; i < PARAMS.nbLearningCycle; ++i) {
            double start = System.nanoTime();
            ellsa.cycle();
            allLearningCycleTimes.add((System.nanoTime() - start) / 1000000);

        }
		/*while(ellsa.getContexts().size()>5 || ellsa.getCycle()<50){
			ellsa.cycle();
		}
		System.out.println(ellsa.getCycle());*/

		/*while(ellsa.data.STATE_DreamCompleted!=1){
			ellsa.cycle();
		}*/

        HashMap<String, Double> mappingScores;
        HashMap<REQUEST, Integer> requestCounts;
        HashMap<SITUATION, Integer> situationsCounts;
        double[] executionTimes;

        ArrayList<Double> allPredictionErrors;

        if (PARAMS.setActiveExploitation) {

            ellsa.data.PARAM_isExploitationActive = true;

            for (int i = 0; i < PARAMS.nbEndoExploitationCycle; ++i) {
                //studiedSystem.getErrorOnRequest(ellsa);
                ellsa.cycle();
            }

            ellsa.data.PARAM_isExploitationActive = false;

            mappingScores = ellsa.getHeadAgent().getMappingScores();
            requestCounts = ellsa.data.requestCounts;
            situationsCounts = ellsa.data.situationsCounts;
            executionTimes = ellsa.data.executionTimesSums;
            allPredictionErrors = new ArrayList<>();

            for (int i = 0; i < PARAMS.nbExploitationCycle; ++i) {
                double start = System.nanoTime();
                allPredictionErrors.add(new Double(studiedSystem.getErrorOnRequest(ellsa)));
                allExploitationCycleTimes.add((System.nanoTime() - start) / 1000000);

            }

        } else {

            mappingScores = ellsa.getHeadAgent().getMappingScores();
            requestCounts = ellsa.data.requestCounts;
            situationsCounts = ellsa.data.situationsCounts;
            executionTimes = ellsa.data.executionTimesSums;
            allPredictionErrors = new ArrayList<>();

            for (int i = 0; i < PARAMS.nbExploitationCycle; ++i) {
                double start = System.nanoTime();
                allPredictionErrors.add(new Double(studiedSystem.getErrorOnRequest(ellsa)));
                allExploitationCycleTimes.add((System.nanoTime() - start) / 1000000);

            }
        }


        OptionalDouble averageError = allPredictionErrors.stream().mapToDouble(a -> a).average();
        Double errorDispersion = allPredictionErrors.stream().mapToDouble(a -> Math.pow((a - averageError.getAsDouble()), 2)).sum();
        double predictionError = averageError.getAsDouble();
        double predictionDispersion = Math.sqrt(errorDispersion / allPredictionErrors.size());

        OptionalDouble averageLearningCycleTime = allLearningCycleTimes.stream().mapToDouble(a -> a).average();
        Double learningcycleTimeDispersion = allLearningCycleTimes.stream().mapToDouble(a -> Math.pow((a - averageLearningCycleTime.getAsDouble()), 2)).sum();
        double averageLearningCycleTimeDouble = averageLearningCycleTime.getAsDouble();
        double learningcycleTimeDispersionDouble = Math.sqrt(learningcycleTimeDispersion / allLearningCycleTimes.size());

        OptionalDouble averageExploitationCycleTime = allExploitationCycleTimes.stream().mapToDouble(a -> a).average();
        Double ExploitationcycleTimeDispersion = allExploitationCycleTimes.stream().mapToDouble(a -> Math.pow((a - averageExploitationCycleTime.getAsDouble()), 2)).sum();
        double averageExploitationCycleTimeDouble = averageExploitationCycleTime.getAsDouble();
        double ExploitationcycleTimeDispersionDouble = Math.sqrt(ExploitationcycleTimeDispersion / allExploitationCycleTimes.size());

        /*System.out.println(mappingScores);
        System.out.println(requestCounts);
        System.out.println(predictionError*100 + " [+-" + predictionDispersion*100 + "]");
        System.out.println(ellsa.getContexts().size() + " Agents");*/

        WRITER.setData(data, ellsa, mappingScores, requestCounts, situationsCounts, executionTimes, predictionError, predictionDispersion, averageLearningCycleTimeDouble, learningcycleTimeDispersionDouble, averageExploitationCycleTimeDouble, ExploitationcycleTimeDispersionDouble);


        ellsa = null;
        studiedSystem = null;

    }


}