package experiments.roboticArm;

import utils.TRACE_LEVEL;

import java.util.ArrayList;
import java.util.Arrays;

public class PARAMS {

    public static String model = "ROBOT";

    public static final int nbTrainingCycle = 1000;
    public static final int nbRequestCycle = 200;
    public static final int nbTest = 1;
    public static final double spaceSize = 50.0	;
    //public static double mappingErrorAllowed = 0.06; // USUAL
    public static double mappingErrorAllowed = 0.03; // OTHER

    /* PREDICTION */
    public static final double setRegressionPerformance = 1;


    /*public static final String configFile = "30jointsRobot3DimensionsLauncher.xml";
    public static final int dimension = 31;
    public static int nbJoints = 30;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9",
            "ptheta10","ptheta11", "ptheta12", "ptheta13", "ptheta14", "ptheta15", "ptheta16", "ptheta17", "ptheta18", "ptheta19",
            "ptheta20","ptheta21", "ptheta22", "ptheta23", "ptheta24", "ptheta25", "ptheta26", "ptheta27", "ptheta28", "ptheta29")  );
*/

    public static final String configFile = "10jointsRobot3DimensionsLauncher.xml";
    public static final int dimension = 11;
    public static int nbJoints = 10;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2", "ptheta3", "ptheta4", "ptheta5", "ptheta6", "ptheta7", "ptheta8", "ptheta9"));


    /*public static final String configFile = "3jointsRobot3DimensionsLauncher.xml";
    public static final int dimension = 4;
    public static int nbJoints = 3;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta1", "ptheta2"));
*/
    /*public static final String configFile = "2jointsRobot3DimensionsLauncher.xml";
    public static final int dimension = 3;
    public static int nbJoints = 2;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));
*/
    /*public static final String configFile = "2jointsRobot2DimensionsLauncher.xml";
    public static final int dimension = 2;
    public static int nbJoints = 2;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));*/

    /*public static final String configFile = "1jointRobot2DimensionsLauncher.xml";
    public static final int dimension = 2;
    public static int nbJoints = 1;
    public static final ArrayList subPercepts = new ArrayList<>(Arrays.asList("ptheta0"));*/

    /*public static final String configFile = "threeDimensionsLauncher.xml";
    public static final int dimension = 3;*/





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
    public static final boolean setActiveLearning = false	;
    public static final boolean setSelfLearning = true ;//!setActiveLearning;

    public static final boolean setAutonomousMode = true ;





    /*NCS*/
    public static final boolean setVoidDetection = false ; // OLD VOID

    public static final boolean setConflictDetection = true ;
    public static final boolean setConflictResolution = setConflictDetection ;
    public static final boolean setConcurrenceDetection = true ;
    public static final boolean setConcurrenceResolution = setConcurrenceDetection ;
    public static final boolean setVoidDetection2 = false ;
    public static final boolean setSubVoidDetection = false ;
    public static final boolean setFrontierRequest = false ; // ONLY FOR LINEAR MODELS
    public static final boolean setSelfModelRequest = true ;

    public static final boolean setCoopLearning = false ; // WITHOUT ORACLE

    public static final boolean setDream = false ;

    public static final boolean setLearnFromNeighbors = true ;

    public static final int nbOfNeighborForCoopLearning = 6;

    public static final int nbOfNeighborForLearningFromNeighbors = 1;
    public static final int nbOfNeighborForContexCreationWithouOracle = 100;
    public static final int nbOfNeighborForVoidDetectionInSelfLearning = 500;


    /*UI*/


    public static TRACE_LEVEL traceLevel = TRACE_LEVEL.ERROR;

    public static double armBaseSize = 100.0;
}
