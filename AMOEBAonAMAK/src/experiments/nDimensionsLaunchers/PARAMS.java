package experiments.nDimensionsLaunchers;

import utils.TRACE_LEVEL;

public class PARAMS {

    /*public static final String configFile = "twoDimensionsLauncher.xml";
    public static final int dimension = 2;*/

    public static final String configFile = "threeDimensionsLauncher.xml";
    public static final int dimension = 3;

    public static final int nbCycle = 1000;
    public static final int nbTest = 10;
    public static final double spaceSize = 50.0	;
    //public static double mappingErrorAllowed = 0.04; // USUAL
    public static double mappingErrorAllowed = 0.08; // OTHER

    /* PREDICTION */
    public static final double setRegressionPerformance = 1;

    /* REGRESSION */
    public static final double oracleNoiseRange = 0.5;
    public static final double learningSpeed = 0.1;
    public static final int regressionPoints = (int)(1/learningSpeed);

    /* XP */
    public static final int nbOfModels = 2	;
    public static final int normType = 2	;

    /* EXPLORATION */
    public static final boolean continousExploration = false;
    public static final boolean randomExploration = !continousExploration;
    public static final boolean limitedToSpaceZone = true;
    public static final double explorationIncrement = 2.0	;
    public static final double explorationWidht = 0.75	;


    /* LEARNING */
    public static final boolean setActiveLearning = true	;
    public static final boolean setSelfLearning = !setActiveLearning;




    /*NCS*/
    public static final boolean setVoidDetection = false ; // OLD VOID

    public static final boolean setConflictDetection = true ;
    public static final boolean setConflictResolution = setConflictDetection ;
    public static final boolean setConcurrenceDetection = true ;
    public static final boolean setConcurrenceResolution = setConcurrenceDetection ;
    public static final boolean setVoidDetection2 = true ;
    public static final boolean setFrontierRequest = true ; // ONLY FOR LINEAR MODELS
    public static final boolean setSelfModelRequest = true ;

    public static final boolean setCoopLearning = false ; // WITHOUT ORACLE

    public static final boolean setDream = true ;

    public static final boolean setLearnFromNeighbors = false ;

    public static final int nbOfNeighborForCoopLearning = 6;

    public static final int nbOfNeighborForLearningFromNeighbors = 1;
    public static final int nbOfNeighborForContexCreationWithouOracle = 3;
    public static final int nbOfNeighborForVoidDetectionInSelfLearning = 5;


    /*UI*/

    public static final boolean contextColorByCoef = true ;
    public static final int nbCycleRefresh3DView = 50;
    public static double maxPrediction = 500 ;
    public static double minPrediction = 0 ;
    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.ERROR;
}
