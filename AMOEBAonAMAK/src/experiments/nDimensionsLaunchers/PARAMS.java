package experiments.nDimensionsLaunchers;

public class PARAMS {

    public static final String configFile = "twoDimensionsLauncher.xml";
    public static final int dimension = 2;
    public static final int nbCycle = 1000;
    public static final int nbTest = 10;
    public static final double spaceSize = 50.0	;
    public static double mappingErrorAllowed = 0.07;

    /* REGRESSION */
    public static final double oracleNoiseRange = 0.5;
    public static final double learningSpeed = 0.01;
    public static final int regressionPoints = 100;

    /* XP */
    public static final int nbOfModels = 2	;
    public static final int normType = 2	;

    /* EXPLORATION */
    public static final boolean randomExploration = true;
    public static final boolean limitedToSpaceZone = true;
    public static final double explorationIncrement = 1.0	;
    public static final double explorationWidht = 0.5	;


    /* LEARNING */
    public static final boolean setActiveLearning = true	;
    public static final boolean setSelfLearning = false	;

    /* PREDICTION */
    public static final double setRegressionPerformance = 1.0;


    /*NCS*/
    public static final boolean setVoidDetection = false ;

    public static final boolean setConflictDetection = true ;
    public static final boolean setConflictResolution = setConflictDetection ;

    public static final boolean setConcurrenceDetection = true ;
    public static final boolean setConcurrenceResolution = setConcurrenceDetection ;

    public static final boolean setVoidDetection2 = true ;

    public static final boolean setFrontierRequest = false ;


}
