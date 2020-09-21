package experiments.roboticArm;

import utils.TRACE_LEVEL;

import java.util.ArrayList;
import java.util.Arrays;

public class PARAMS {

    public static String model = "ROBOT";

    public static int nbTrainingCycle = 2000;
    public static int nbRequestCycle = 200;
    public static int nbTest = 10;
    public static double spaceSize = 50.0	;
    //public static double mappingErrorAllowed = 0.06; // USUAL
    public static double mappingErrorAllowed = 0.03; // OTHER
    public static int neighborhoodMultiplicator = 16;

    /* PREDICTION */
    public static  double setRegressionPerformance = 1;

    public static  String configFile = "100jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 101;
    public static int nbJoints = 100;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19",
            "ptheta20","ptheta21", "ptheta22", "ptheta23", "ptheta24", "ptheta25",
            "ptheta26", "ptheta27", "ptheta28", "ptheta29",
            "ptheta30","ptheta31", "ptheta32", "ptheta33", "ptheta34", "ptheta35",
            "ptheta36", "ptheta37", "ptheta38", "ptheta39",
            "ptheta40","ptheta41", "ptheta42", "ptheta43", "ptheta44", "ptheta45",
            "ptheta46", "ptheta47", "ptheta48", "ptheta49",
            "ptheta50","ptheta51", "ptheta52", "ptheta53", "ptheta54", "ptheta55",
            "ptheta56", "ptheta57", "ptheta58", "ptheta59",
            "ptheta60","ptheta61", "ptheta62", "ptheta63", "ptheta64", "ptheta65",
            "ptheta66", "ptheta67", "ptheta68", "ptheta69",
            "ptheta70","ptheta71", "ptheta72", "ptheta73", "ptheta74", "ptheta75",
            "ptheta76", "ptheta77", "ptheta78", "ptheta79",
            "ptheta80","ptheta81", "ptheta82", "ptheta83", "ptheta84", "ptheta85",
            "ptheta86", "ptheta87", "ptheta88", "ptheta89",
            "ptheta90","ptheta91", "ptheta92", "ptheta93", "ptheta94", "ptheta95",
            "ptheta96", "ptheta97", "ptheta98", "ptheta99")  );


    /*public static  String configFile = "50jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 51;
    public static int nbJoints = 50;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19",
            "ptheta20","ptheta21", "ptheta22", "ptheta23", "ptheta24", "ptheta25",
            "ptheta26", "ptheta27", "ptheta28", "ptheta29",
            "ptheta30","ptheta31", "ptheta32", "ptheta33", "ptheta34", "ptheta35",
            "ptheta36", "ptheta37", "ptheta38", "ptheta39",
            "ptheta40","ptheta41", "ptheta42", "ptheta43", "ptheta44", "ptheta45",
            "ptheta46", "ptheta47", "ptheta48", "ptheta49")  );*/


    /*public static  String configFile = "30jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 31;
    public static int nbJoints = 30;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19",
            "ptheta20","ptheta21", "ptheta22", "ptheta23", "ptheta24", "ptheta25",
            "ptheta26", "ptheta27", "ptheta28", "ptheta29")  );*/

    /*public static  String configFile = "20jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 21;
    public static int nbJoints = 20;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3",
            "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15",
            "ptheta16", "ptheta17", "ptheta18", "ptheta19")  );*/

    /*public static  String configFile = "10jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 11;
    public static int nbJoints = 10;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4",
    "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9"));*/

    /*public static  String configFile = "6jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 7;
    public static int nbJoints = 6;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4",
            "ptheta5"));*/


    /*public static  String configFile = "3jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 4;
    public static int nbJoints = 3;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2"));*/

    /*public static  String configFile = "2jointsRobot3DimensionsLauncher.xml";
    public static  int dimension = 3;
    public static int nbJoints = 2;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));*/

    /*public static  String configFile = "2jointsRobot2DimensionsLauncher.xml";
    public static  int dimension = 2;
    public static int nbJoints = 2;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));*/

    /*public static  String configFile = "1jointRobot2DimensionsLauncher.xml";
    public static  int dimension = 2;
    public static int nbJoints = 1;
    public static  ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));*/

    /*public static  String configFile = "threeDimensionsLauncher.xml";
    public static  int dimension = 3;*/





    /* REGRESSION */
    public static  double oracleNoiseRange = 0.5;
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


    /* LEARNING */
    public static  boolean setActiveLearning = false	;
    public static  boolean setSelfLearning = true ;//!setActiveLearning;

    public static  boolean setAutonomousMode = true ;





    /*NCS*/
    public static  boolean setVoidDetection = false ; // OLD VOID

    public static  boolean setConflictDetection = true ;
    public static  boolean setConflictResolution = setConflictDetection ;
    public static  boolean setConcurrenceDetection = true ;
    public static  boolean setConcurrenceResolution = setConcurrenceDetection ;
    public static  boolean setVoidDetection2 = false ;
    public static  boolean setSubVoidDetection = false ;
    public static  boolean setFrontierRequest = false ; // ONLY FOR LINEAR MODELS
    public static  boolean setSelfModelRequest = true ;

    public static  boolean setCoopLearning = false ; // WITHOUT ORACLE

    public static  boolean setDream = false ;

    public static  boolean setLearnFromNeighbors = true ;

    public static  int nbOfNeighborForCoopLearning = 6;

    public static  int nbOfNeighborForLearningFromNeighbors = 1;
    public static  int nbOfNeighborForContexCreationWithouOracle = 100;
    public static  int nbOfNeighborForVoidDetectionInSelfLearning = 500;


    /*UI*/


    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.ERROR;

    public static double armBaseSize = 50.0;
}