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

public class LaunchExampleXPWithXML_OLD {

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

        //On affecte les champs de PARAMS_OLD avec les valeur des attribut de chaque tag name du fichier xml

        //GENERAL SETTINGS
        for (int i = 0; i < nodeListGeneral.getLength(); i++) {
            Node node = nodeListGeneral.item(i);
            Element element = (Element) node;
            PARAMS_OLD.model = element.getAttribute("model");
            PARAMS_OLD.extension = element.getAttribute("extension");
            PARAMS_OLD.subPercepts = PARSER.convertStringToArraylist(element.getAttribute("subPercepts"));
            PARAMS_OLD.dimension = Integer.parseInt(element.getAttribute("dimension"));
            PARAMS_OLD.nbLearningCycle = Integer.parseInt(element.getAttribute("nbLearningCycle"));
            PARAMS_OLD.nbEndoExploitationCycle = Integer.parseInt(element.getAttribute("nbEndoExploitationCycle"));
            PARAMS_OLD.nbExploitationCycle = Integer.parseInt(element.getAttribute("nbExploitationCycle"));
            PARAMS_OLD.setActiveExploitation = Boolean.parseBoolean(element.getAttribute("setActiveExploitation"));
            PARAMS_OLD.nbEpisodes = Integer.parseInt(element.getAttribute("nbEpisodes"));
            PARAMS_OLD.transferCyclesRatio = Double.parseDouble(element.getAttribute("transferCyclesRatio"));
            PARAMS_OLD.spaceSize = Double.parseDouble(element.getAttribute("spaceSize"));
            PARAMS_OLD.validityRangesPrecision = Double.parseDouble(element.getAttribute("validityRangesPrecision"));
        }

        //LEARNING_WEIGHTS SETTINGS
        for (int i = 0; i < nodeListLearningWeights.getLength(); i++) {
            Node node = nodeListLearningWeights.item(i);
            Element element = (Element) node;
            PARAMS_OLD.LEARNING_WEIGHT_ACCURACY = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_ACCURACY"));
            PARAMS_OLD.LEARNING_WEIGHT_PROXIMITY = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_PROXIMITY"));
            PARAMS_OLD.LEARNING_WEIGHT_EXPERIENCE = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_EXPERIENCE"));
            PARAMS_OLD.LEARNING_WEIGHT_GENERALIZATION = Double.parseDouble(element.getAttribute("LEARNING_WEIGHT_GENERALIZATION"));
        }

        //STRATEGIES SETTINGS
        for (int i = 0; i < nodeListStrategies.getLength(); i++) {
            Node node = nodeListStrategies.item(i);
            Element element = (Element) node;
            PARAMS_OLD.setActiveLearning = Boolean.parseBoolean(element.getAttribute("setActiveLearning"));
            PARAMS_OLD.setSelfLearning = Boolean.parseBoolean(element.getAttribute("setSelfLearning"));
            PARAMS_OLD.setCooperativeNeighborhoodLearning = Boolean.parseBoolean(element.getAttribute("setCooperativeNeighborhoodLearning"));
        }

        //EXPLOITATION_WEIGHTS SETTINGS
        for (int i = 0; i < nodeListExploitationWeights.getLength(); i++) {
            Node node = nodeListExploitationWeights.item(i);
            Element element = (Element) node;
            PARAMS_OLD.EXPLOITATION_WEIGHT_PROXIMITY = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_PROXIMITY"));
            PARAMS_OLD.EXPLOITATION_WEIGHT_EXPERIENCE = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_EXPERIENCE"));
            PARAMS_OLD.EXPLOITATION_WEIGHT_GENERALIZATION = Double.parseDouble(element.getAttribute("EXPLOITATION_WEIGHT_GENERALIZATION"));
        }

        //NEIGHBORHOOD SETTINGS
        for (int i = 0; i < nodeListNeighborhood.getLength(); i++) {
            Node node = nodeListNeighborhood.item(i);
            Element element = (Element) node;
            PARAMS_OLD.neighborhoodRadiusCoefficient = Double.parseDouble(element.getAttribute("neighborhoodRadiusCoefficient"));
            PARAMS_OLD.influenceRadiusCoefficient = Double.parseDouble(element.getAttribute("influenceRadiusCoefficient"));
            PARAMS_OLD.maxRangeRadiusCoefficient = Double.parseDouble(element.getAttribute("maxRangeRadiusCoefficient"));
            PARAMS_OLD.rangeSimilarityCoefficient = Double.parseDouble(element.getAttribute("rangeSimilarityCoefficient"));
            PARAMS_OLD.minimumRangeCoefficient = Double.parseDouble(element.getAttribute("minimumRangeCoefficient"));
        }

        //PREDICTION SETTINGS
        for (int i = 0; i < nodeListPrediction.getLength(); i++) {
            Node node = nodeListPrediction.item(i);
            Element element = (Element) node;
            PARAMS_OLD.modelErrorMargin = Double.parseDouble(element.getAttribute("modelErrorMargin"));
        }

        //REGRESSION SETTINGS
        for (int i = 0; i < nodeListRegression.getLength(); i++) {
            Node node = nodeListRegression.item(i);
            Element element = (Element) node;
            PARAMS_OLD.noiseRange = Double.parseDouble(element.getAttribute("noiseRange"));
            PARAMS_OLD.exogenousLearningWeight = Double.parseDouble(element.getAttribute("exogenousLearningWeight"));
            PARAMS_OLD.endogenousLearningWeight = Double.parseDouble(element.getAttribute("endogenousLearningWeight"));
            PARAMS_OLD.perceptionsGenerationCoefficient = Double.parseDouble(element.getAttribute("perceptionsGenerationCoefficient"));
            PARAMS_OLD.modelSimilarityThreshold = Double.parseDouble(element.getAttribute("modelSimilarityThreshold"));
            PARAMS_OLD.regressionPoints = (int)(1/PARAMS_OLD.exogenousLearningWeight);
        }

        //XP SETTINGS
        for (int i = 0; i < nodeListXp.getLength(); i++) {
            Node node = nodeListXp.item(i);
            Element element = (Element) node;
            PARAMS_OLD.nbOfModels = Integer.parseInt(element.getAttribute("nbOfModels"));
            PARAMS_OLD.normType = Integer.parseInt(element.getAttribute("normType"));
        }

        //EXPLORATION SETTINGS
        for (int i = 0; i < nodeListExploration.getLength(); i++) {
            Node node = nodeListExploration.item(i);
            Element element = (Element) node;
            PARAMS_OLD.continousExploration = Boolean.parseBoolean(element.getAttribute("continousExploration"));
            PARAMS_OLD.randomExploration = !PARAMS_OLD.continousExploration;
            PARAMS_OLD.limitedToSpaceZone = Boolean.parseBoolean(element.getAttribute("limitedToSpaceZone"));
            PARAMS_OLD.explorationIncrement = Double.parseDouble(element.getAttribute("explorationIncrement"));
            PARAMS_OLD.explorationWidht = Double.parseDouble(element.getAttribute("explorationWidht"));
            PARAMS_OLD.setbootstrapCycle = Integer.parseInt(element.getAttribute("setbootstrapCycle"));
        }

        //NCS SETTINGS
        for (int i = 0; i < nodeListNCS.getLength(); i++) {
            Node node = nodeListNCS.item(i);
            Element element = (Element) node;
            PARAMS_OLD.setModelAmbiguityDetection = Boolean.parseBoolean(element.getAttribute("setModelAmbiguityDetection"));
            PARAMS_OLD.setConflictDetection = Boolean.parseBoolean(element.getAttribute("setConflictDetection"));
            PARAMS_OLD.setConcurrenceDetection = Boolean.parseBoolean(element.getAttribute("setConcurrenceDetection"));
            PARAMS_OLD.setIncompetenceDetection = Boolean.parseBoolean(element.getAttribute("setIncompetenceDetection"));
            PARAMS_OLD.setCompleteRedundancyDetection = Boolean.parseBoolean(element.getAttribute("setCompleteRedundancyDetection"));
            PARAMS_OLD.setPartialRedundancyDetection = Boolean.parseBoolean(element.getAttribute("setPartialRedundancyDetection"));
            PARAMS_OLD.setRangeAmbiguityDetection = Boolean.parseBoolean(element.getAttribute("setRangeAmbiguityDetection"));
            PARAMS_OLD.setisCreationWithNeighbor = Boolean.parseBoolean(element.getAttribute("setisCreationWithNeighbor"));
            PARAMS_OLD.isAllContextSearchAllowedForLearning = Boolean.parseBoolean(element.getAttribute("isAllContextSearchAllowedForLearning"));
            PARAMS_OLD.isAllContextSearchAllowedForExploitation = Boolean.parseBoolean(element.getAttribute("isAllContextSearchAllowedForExploitation"));
            PARAMS_OLD.setConflictResolution = PARAMS_OLD.setConflictDetection;
            PARAMS_OLD.setConcurrenceResolution = PARAMS_OLD.setConcurrenceDetection;
            PARAMS_OLD.setSubIncompetencedDetection = Boolean.parseBoolean(element.getAttribute("setSubIncompetencedDetection"));
            PARAMS_OLD.setDream = Boolean.parseBoolean(element.getAttribute("setDream"));
            PARAMS_OLD.setDreamCycleLaunch = Integer.parseInt(element.getAttribute("setDreamCycleLaunch"));
            PARAMS_OLD.nbOfNeighborForLearningFromNeighbors = Integer.parseInt(element.getAttribute("nbOfNeighborForLearningFromNeighbors"));
            PARAMS_OLD.nbOfNeighborForContexCreationWithouOracle = Integer.parseInt(element.getAttribute("nbOfNeighborForContexCreationWithouOracle"));
            PARAMS_OLD.nbOfNeighborForVoidDetectionInSelfLearning = Integer.parseInt(element.getAttribute("nbOfNeighborForVoidDetectionInSelfLearning"));
            PARAMS_OLD.probabilityOfRangeAmbiguity = Double.parseDouble(element.getAttribute("probabilityOfRangeAmbiguity"));
            PARAMS_OLD.setAutonomousMode = Boolean.parseBoolean(element.getAttribute("setAutonomousMode"));
            PARAMS_OLD.traceLevel = TRACE.convertFromString(element.getAttribute("traceLevel"));
        }

        //UI SETTINGS
        for (int i = 0; i < nodeListUi.getLength(); i++) {
            Node node = nodeListUi.item(i);
            Element element = (Element) node;
            PARAMS_OLD.STOP_UI = Boolean.parseBoolean(element.getAttribute("STOP_UI"));
            PARAMS_OLD.STOP_UI_cycle = PARAMS_OLD.nbLearningCycle;
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
        //WRITER.writeParams(xpCSV);
        //xpCSV.close();
        Pair<ArrayList<List<String>>, HashMap<String, ArrayList<Double>>> dataPair = WRITER.getData();
        ArrayList<List<String>> dataStrings = dataPair.getA();
        HashMap<String, ArrayList<Double>> data = dataPair.getB();

        double start = System.nanoTime();

        for (int i = 0; i < PARAMS_OLD.nbEpisodes; ++i) {
            //System.out.print(i + " ");
            learningEpisode(data);
        }
        //System.out.println(" ");
        double total = (System.nanoTime() - start) / 1000;
        double mean = total / PARAMS_OLD.nbEpisodes;
        System.out.println("[TIME MEAN] " + mean + " s");
        System.out.println("[TIME TOTAL] " + total + " s");

        WRITER.writeData(xpCSV, data, dataStrings, total, mean);

        data = null;
    }


    private static void learningEpisode(HashMap<String, ArrayList<Double>> data) {
        ELLSA ellsa = new ELLSA(null, null);
        StudiedSystem studiedSystem = new Model_Manager(PARAMS_OLD.spaceSize, PARAMS_OLD.dimension, PARAMS_OLD.nbOfModels, PARAMS_OLD.normType, PARAMS_OLD.randomExploration, PARAMS_OLD.explorationIncrement, PARAMS_OLD.explorationWidht, PARAMS_OLD.limitedToSpaceZone, PARAMS_OLD.noiseRange);
        ellsa.setStudiedSystem(studiedSystem);
        IBackupSystem backupSystem = new BackupSystem(ellsa);
        File file = new File(SETTING_FILE_NAME);
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

        ellsa.data.PARAM_perceptionsGenerationCoefficient = PARAMS_OLD.perceptionsGenerationCoefficient;
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

        if (PARAMS_OLD.setActiveExploitation) {

            ellsa.data.PARAM_isExploitationActive = true;

            for (int i = 0; i < PARAMS_OLD.nbEndoExploitationCycle; ++i) {
                //studiedSystem.getErrorOnRequest(ellsa);
                ellsa.cycle();
            }

            ellsa.data.PARAM_isExploitationActive = false;

            mappingScores = ellsa.getHeadAgent().getMappingScores();
            requestCounts = ellsa.data.requestCounts;
            situationsCounts = ellsa.data.situationsCounts;
            executionTimes = ellsa.data.executionTimesSums;
            allPredictionErrors = new ArrayList<>();

            for (int i = 0; i < PARAMS_OLD.nbExploitationCycle; ++i) {
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

            for (int i = 0; i < PARAMS_OLD.nbExploitationCycle; ++i) {
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