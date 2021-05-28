package experiments.roboticDistributedArm;

import utils.TRACE_LEVEL;

import java.util.ArrayList;

public class PARAMS {

    public static String model = "robotArmDist";

    public static int nbJoints = 5;
    public static  int dimension = nbJoints +1;
    public static  String configFile = "";
    public static  ArrayList subPercepts = new ArrayList<>();

    public static int nbLearningCycle = 100;
    public static int nbExploitationCycle = 100;
    public static int nbepisodes = 1;
    public static  int requestControlCycles = 10;

    public static boolean isOrientationGoal = true;

    public static double spaceSize = 50.0	;
    public static double validityRangesPrecision = 0.04;

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

    public static int neighborhoodRadiusCoefficient = 2;
    public static  double influenceRadiusCoefficient = 0.5;
    public static double maxRangeRadiusCoefficient = 2.0;
    public static double rangeSimilarityCoefficient = 0.375;
    public static double minimumRangeCoefficient = 0.25;

    /* PREDICTION */
    public static  double modelErrorMargin = 1;
    public static double armBaseSize = 300.0;









    /* REGRESSION */
    public static  double noiseRange = 0.0;
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

    public static  int setbootstrapCycle = 10;

    /* LEARNING */
    public static  boolean setActiveLearning = false	;
    public static  boolean setSelfLearning = true;
    public static  boolean setLearnFromNeighbors = true ;

    public static  boolean setAutonomousMode = true ;





    /*NCS*/
//    public static  boolean setVoidDetection = false ; // OLD VOID

    public static  boolean setSelfModelRequest = true ;
    public static  boolean setConflictDetection = true ;
    public static  boolean setConflictResolution = setConflictDetection ;

    public static  boolean setConcurrenceDetection = true ;
    public static  boolean setConcurrenceResolution = setConcurrenceDetection ;

    public static  boolean setVoidDetection = false ;


    public static  boolean setSubVoidDetection = false ;
    public static  boolean setFrontierRequest = false ; // ONLY FOR LINEAR MODELS

    public static  boolean setisCreationWithNeighbor = true;

    public static boolean isAllContextSearchAllowedForLearning = true;
    public static boolean isAllContextSearchAllowedForExploitation = true;

    public static  boolean setDream = false ;
    public static  int setDreamCycleLaunch = 5000 ;


    public static  int nbOfNeighborForCoopLearning = 6;

    public static  int nbOfNeighborForLearningFromNeighbors = 1;
    public static  int nbOfNeighborForContexCreationWithouOracle = 7;
    public static  int nbOfNeighborForVoidDetectionInSelfLearning = 7;

    public static  boolean setFusionResolution = true ;
    public static  boolean setRestructureResolution = true ;

    public static double probabilityOfRangeAmbiguity = 0.1;


    /*UI*/


    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.ERROR;

    //TODO à supprimer
    public static  boolean setCoopLearningASUPPRIMER = false ; // WITHOUT ORACLE

}
