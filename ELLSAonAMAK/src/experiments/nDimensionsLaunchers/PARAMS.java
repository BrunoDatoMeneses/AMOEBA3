package experiments.nDimensionsLaunchers;

import utils.TRACE_LEVEL;

import java.util.ArrayList;

public class PARAMS {

//    public static String model = "multi";
//    public static String model = "disc";
//    public static String model = "square";
    public static String model = "squareFixed";
//    public static String model = "triangle";
//    public static String model = "gaussian";
//    public static String model = "polynomial";
//    public static String model = "gaussianCos2";
//    public static String model = "cosX";
//    public static String model = "cosSinX";
//    public static String model = "rosenbrock";
//    public static String model = "squareSplitTriangle";
//    public static String model = "squareSplitFixed";





    public static String extension = "";

    public static ArrayList subPercepts = new ArrayList<>();

    public static  String configFile = "twoDimensionsLauncher.xml";
    public static  int dimension = 2;

    /*public static  String configFile = "tenDimensionsLauncher.xml";
    public static  int dimension = 10;*/

    /*public static  String configFile = "threeDimensionsLauncher.xml";
    public static  int dimension = 3;*/

    /*public static  String configFile = "fourDimensionsLauncher.xml";
    public static  int dimension = 4;*/

    public static  int nbLearningCycle = 500;
    public static  int nbExploitationCycle = 250;
    public static  int nbEpisodes = 1;

    public static  double spaceSize = 50.0	;
    public static double validityRangesPrecision = 0.05;

    /* LEARNING */

    public static double LEARNING_WEIGHT_ACCURACY = 1.0;
    public static double LEARNING_WEIGHT_PROXIMITY = 0.0;
    public static double LEARNING_WEIGHT_EXPERIENCE = 1.0;
    public static double LEARNING_WEIGHT_GENERALIZATION = 1.0;

    /* EXPLOITATION */

    public static double EXPLOITATION_WEIGHT_PROXIMITY = 1.0;
    public static double EXPLOITATION_WEIGHT_EXPERIENCE = 1.0;
    public static double EXPLOITATION_WEIGHT_GENERALIZATION = 1.0;


    /* NEIGHBORHOOD */

    public static  double neighborhoodRadiusCoefficient = 2;
    public static  double influenceRadiusCoefficient = 0.5;
    //public static double neighborhoodRadiusCoefficient = 2;
    //public static double influenceRadiusCoefficient = 0.5;
    public static double maxRangeRadiusCoefficient = 2.0;
    public static double rangeSimilarityCoefficient = 0.375;
    public static double minimumRangeCoefficient = 0.25;

    /* PREDICTION */
    //public static  double modelErrorMargin = 1; //Multi
    //public static  double modelErrorMargin = 0.05; //SinCos
    public static  double modelErrorMargin = 1; // Goutte
    //public static  double modelErrorMargin = 1; // Carré


    /* REGRESSION */
    public static  double oracleNoiseRange = 0.0;
    public static  double exogenousLearningWeight = 0.1;
    public static  double endogenousLearningWeight = 0.1;

    public static double perceptionsGenerationCoefficient = 0.1;

    public static double modelSimilarityThreshold = 0.001;


    public static  int regressionPoints = (int)(1/ exogenousLearningWeight);

    /* XP */
    public static  int nbOfModels = 2	;
    public static  int normType = 2	;

    /* EXPLORATION */
    public static  boolean continousExploration = false;
    public static  boolean randomExploration = !continousExploration;
    public static  boolean limitedToSpaceZone = true;
    public static  double explorationIncrement = 2.0	;
    public static  double explorationWidht = 0.75	;

    public static  int setbootstrapCycle = 15;


    /* LEARNING */
    public static  boolean setActiveLearning = true	;
    public static  boolean setSelfLearning = false;

/*    public static  boolean setActiveLearning = false	;
    public static  boolean setSelfLearning = true;*/




    /*NCS*/

    public static  boolean setSelfModelRequest = true ;
    public static  boolean setConflictDetection = true ;
    public static  boolean setConcurrenceDetection = true ;
    public static  boolean setVoidDetection = true ;
    public static  boolean setFusionResolution = true ;
    public static  boolean setRestructureResolution = true ;
    public static  boolean setFrontierRequest = false ; // ONLY ACTIVE LEARNING


    public static  boolean setisCreationWithNeighbor = true;

    public static boolean isAllContextSearchAllowedForLearning = true;
    public static boolean isAllContextSearchAllowedForExploitation = true;

    public static  boolean setConflictResolution = setConflictDetection ;
    public static  boolean setConcurrenceResolution = setConcurrenceDetection ;
    public static  boolean setSubVoidDetection = false ;


    public static  boolean setDream = false ;
    public static  int setDreamCycleLaunch = 1500 ;

    public static  boolean setLearnFromNeighbors = false ;


    public static  int nbOfNeighborForLearningFromNeighbors = 1;
    public static  int nbOfNeighborForContexCreationWithouOracle = 5000;
    public static  int nbOfNeighborForVoidDetectionInSelfLearning = 5000;

    public static double probabilityOfRangeAmbiguity = 0.1;

    public static   boolean setAutonomousMode = true;


    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.OFF;



    /* UI */
    public static boolean STOP_UI = true;
    public static int STOP_UI_cycle = 500;

    //TODO à supprimer
    public static  boolean setCoopLearningASUPPRIMER = false ; // WITHOUT ORACLE
}
