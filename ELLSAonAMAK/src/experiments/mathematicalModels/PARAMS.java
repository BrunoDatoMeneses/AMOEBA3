package experiments.mathematicalModels;

import utils.TRACE_LEVEL;

import java.util.ArrayList;

public class PARAMS {

    //    public static String model = "los";
//    public static String model = "squareDisc";
//    public static String model = "squareDiscLos";
//    public static String model = "multi";
//    public static String model = "disc";
//    public static String model = "square";
//    public static String model = "squareFixed";
//    public static String model = "triangle";
//    public static String model = "gaussian";
//    public static String model = "polynomial";
    public static String model = "gaussianCos2";
//    public static String model = "cosX";
//    public static String model = "cosSinX";
//    public static String model = "rosenbrock";
//    public static String model = "squareSplitTriangle";
//    public static String model = "squareSplitFixed";

    public static String extension;
    public static ArrayList subPercepts = new ArrayList<>();
    public static String configFile;
    public static int dimension;
    public static int nbLearningCycle;
    public static int nbEndoExploitationCycle;
    public static int nbExploitationCycle;
    public static boolean setActiveExploitation;
    public static int nbEpisodes;
    public static double transferCyclesRatio;//0.429;
    public static double spaceSize;
    public static double validityRangesPrecision;
    /* LEARNING */
    public static double LEARNING_WEIGHT_ACCURACY;
    public static double LEARNING_WEIGHT_PROXIMITY;
    public static double LEARNING_WEIGHT_EXPERIENCE;
    public static double LEARNING_WEIGHT_GENERALIZATION;

    public static boolean setActiveLearning;
    public static boolean setSelfLearning;
    public static boolean setCooperativeNeighborhoodLearning;
    /* EXPLOITATION */
    public static double EXPLOITATION_WEIGHT_PROXIMITY;
    public static double EXPLOITATION_WEIGHT_EXPERIENCE;
    public static double EXPLOITATION_WEIGHT_GENERALIZATION;
    /* NEIGHBORHOOD */
    public static double neighborhoodRadiusCoefficient;
    public static double influenceRadiusCoefficient;
    public static double maxRangeRadiusCoefficient;
    public static double rangeSimilarityCoefficient;
    public static double minimumRangeCoefficient;
    /* PREDICTION */
    public static double modelErrorMargin;
    /* REGRESSION */
    public static double noiseRange;
    public static double exogenousLearningWeight;
    public static double endogenousLearningWeight;
    public static double perceptionsGenerationCoefficient;
    public static double modelSimilarityThreshold;
    public static int regressionPoints;
    /* XP */
    public static int nbOfModels;
    public static int normType;
    /* EXPLORATION */
    public static boolean continousExploration;
    public static boolean randomExploration;
    public static boolean limitedToSpaceZone;
    public static double explorationIncrement;
    public static double explorationWidht;
    public static int setbootstrapCycle;

    /*NCS*/
    public static boolean setModelAmbiguityDetection;
    public static boolean setConflictDetection;
    public static boolean setConcurrenceDetection;
    public static boolean setIncompetenceDetection;
    public static boolean setCompleteRedundancyDetection;
    public static boolean setPartialRedundancyDetection;
    public static boolean setRangeAmbiguityDetection;
    public static boolean setisCreationWithNeighbor;
    public static boolean isAllContextSearchAllowedForLearning;
    public static boolean isAllContextSearchAllowedForExploitation;
    public static boolean setConflictResolution;
    public static boolean setConcurrenceResolution;
    public static boolean setSubIncompetencedDetection;
    public static boolean setDream;
    public static int setDreamCycleLaunch;
    public static int nbOfNeighborForLearningFromNeighbors;
    public static int nbOfNeighborForContexCreationWithouOracle;
    public static int nbOfNeighborForVoidDetectionInSelfLearning;
    public static double probabilityOfRangeAmbiguity;
    public static boolean setAutonomousMode;
    public static TRACE_LEVEL traceLevel;
    /* UI */
    public static boolean STOP_UI;
    public static int STOP_UI_cycle;
}