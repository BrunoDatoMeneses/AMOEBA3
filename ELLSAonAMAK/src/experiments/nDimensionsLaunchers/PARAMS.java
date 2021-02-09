package experiments.nDimensionsLaunchers;

import utils.TRACE_LEVEL;

import java.util.ArrayList;

public class PARAMS {

    public static String model = "CosSin" + "_AvecDetectionMinLocal" + "_VoisinagePredictionx4";
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
    public static double mappingErrorAllowed = 0.05;
    //public static double mappingErrorAllowed = 0.06; // USUAL
    //public static double mappingErrorAllowed = 0.08; // OTHER
    //public static double mappingErrorAllowed = 0.1; // BIG 3D


    /* NEIGHBORHOOD */

    public static  int setNeighborhoodMultiplicator = 2;
    public static  double setExternalContextInfluenceRatio = 0.25;

    /* PREDICTION */
    //public static  double setRegressionPerformance = 1; //Multi
    //public static  double setRegressionPerformance = 0.05; //SinCos
    public static  double setRegressionPerformance = 1; // Goutte
    //public static  double setRegressionPerformance = 1; // Carré


    /* REGRESSION */
    public static  double oracleNoiseRange = 0.0;
    public static  double learningSpeed = 0.1;
    public static  int regressionPoints = (int)(1/learningSpeed);

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
    public static  boolean setFrontierRequest = true ; // ONLY ACTIVE LEARNING


    public static  boolean setisCreationWithNeighbor = true;

    public static  boolean setConflictResolution = setConflictDetection ;
    public static  boolean setConcurrenceResolution = setConcurrenceDetection ;
    public static  boolean setSubVoidDetection = false ;


    public static  boolean setDream = false ;
    public static  int setDreamCycleLaunch = 1500 ;

    public static  boolean setLearnFromNeighbors = false ;




    public static  int nbOfNeighborForLearningFromNeighbors = 1;
    public static  int nbOfNeighborForContexCreationWithouOracle = 5000;
    public static  int nbOfNeighborForVoidDetectionInSelfLearning = 5000;

    public static   boolean setAutonomousMode = true;


    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.ERROR;



    /* UI */
    public static boolean STOP_UI = true;
    public static int STOP_UI_cycle = 500;

    //TODO à supprimer
    public static  boolean setCoopLearningASUPPRIMER = false ; // WITHOUT ORACLE
}
