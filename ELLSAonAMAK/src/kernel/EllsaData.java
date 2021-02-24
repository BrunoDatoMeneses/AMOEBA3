package kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import agents.context.Context;
import agents.head.DynamicPerformance;
import agents.head.REQUEST;
import agents.head.SITUATION;
import agents.percept.INPUT;
import agents.percept.Percept;
import utils.Pair;

/**
 * A Plain Old Java Object for storing parameters for the Head. Keep it as simple as possible for ease of serialization.<br/>
 * Make sure that all member are : public, serializable, and with a default constructor (taking no parameters).
 * @author Hugo
 *
 */
public class EllsaData implements Serializable {
	private static final long serialVersionUID = 1L;



	//* DATA *//
	public String nameID = null;
	public INPUT currentINPUT = null;
	
	public int nPropositionsReceived;
	public int averagePredictionCriticityWeight = 0;
	public int numberOfCriticityValuesForAverage = 100;
	public int numberOfCriticityValuesForAverageforVizualisation = 300;

	public Double prediction;
	public HashMap<Percept, Double> nonCondireredPerceptsSyntheticValues;
	public HashMap<String, Double> higherNeighborLastPredictionPercepts = null;
	public Double endogenousPredictionActivatedContextsOverlaps = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = 0.0;
	public Double endogenousPredictionActivatedContextsSharedIncompetence = 0.0;
	public Double endogenousPredictionNContexts = 0.0;
	public Double endogenousPredictionNContextsByInfluence = 0.0;

	public Double oracleValue = null;
	public Double oldOracleValue;
	public double criticity = 0.0;
	public double lastMinDistanceToRegression;
	public double oldCriticity;

	public double spatialGeneralizationScore = 0;

	public boolean noCreation = true;
	public boolean useOracle = true;
	public boolean firstContext = false;
	public boolean newContextWasCreated = false;
	public boolean contextFromPropositionWasSelected = false;

	public boolean activeLearning = false;
	public boolean selfLearning = false;

	public Double maxConfidence = null;
	public Double minConfidence = null;
	
	public Double maxPrediction = Double.NEGATIVE_INFINITY;
	public Double minPrediction = Double.POSITIVE_INFINITY;

	public Double maxModelCoef = null;
	public Double minModelCoef = null;
	
	public double normalizedCriticality = 0.0;

	// Endogenous feedback
	public boolean noBestContext;

	public double evolutionCriticalityPrediction = 0.5;
	public double evolutionCriticalityMapping = 0.5;
	public double evolutionCriticalityConfidence = 0.5;

	public int currentCriticalityPrediction = 0;
	public int currentCriticalityMapping = 0;
	public int currentCriticalityConfidence = 0;


	public boolean contextNotFinished = false;


	
	public DynamicPerformance predictionPerformance;
	public DynamicPerformance regressionPerformance;
	public DynamicPerformance mappingPerformance;
	
	public double[] executionTimes = new  double[30];
	public double[] executionTimesSums = new double[30];
	

	
	public double averageRegressionPerformanceIndicator;










	public boolean isSubPercepts = false;


	public HashMap<REQUEST,Integer> requestCounts = new HashMap<>();
	public HashMap<SITUATION,Integer> situationsCounts = new HashMap<>();

	public int countLocalMinina = 0;



	public int neighborsCounts = 0;
	public int lastNeihborsCount = 0;

	public HashMap<Percept, Pair<Double,Double>> minMaxPerceptsStatesAfterBoostrap;


	//* PARAMETERS *//



	/* USER PARAMETERS */

	public double PARAM_validityRangesPrecision = 0.04;
	public double PARAM_modelErrorMargin = 1.0;
	public int PARAM_bootstrapCycle = 10;
	public double PARAM_exogenousLearningWeight = 0.25;
	public double PARAM_endogenousLearningWeight = 0.25;


	public boolean PARAM_isActiveLearning = false;
	public boolean PARAM_isSelfLearning = false;
	public boolean PARAM_isAutonomousMode = false;

	/* DESIGNER PARAMETERS */

	// Neighborhood
	public double PARAM_neighborhoodRadiusCoefficient = 2;
	public double PARAM_influenceRadiusCoefficient = 0.5;
	public double PARAM_maxRangeRadiusCoefficient = 2.0;
	public double PARAM_rangeSimilarityCoefficient = 0.375;
	public double PARAM_minimumRangeCoefficient = 0.25;

	public int PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = 10000;
	public int PARAM_creationNeighborNumberForContexCreationWithouOracle = 10000;

	public double PARAM_perceptionsGenerationCoefficient = 0.1;

	public double PARAM_modelSimilarityThreshold = 0.001;

	public double PARAM_LEARNING_WEIGHT_ACCURACY = 1.0;
	public double PARAM_LEARNING_WEIGHT_PROXIMITY = 0.0;
	public double PARAM_LEARNING_WEIGHT_EXPERIENCE = 1.0;
	public double PARAM_LEARNING_WEIGHT_GENERALIZATION = 1.0;

	public double PARAM_EXPLOITATION_WEIGHT_PROXIMITY = 1.0;
	public double PARAM_EXPLOITATION_WEIGHT_EXPERIENCE = 1.0;
	public double PARAM_EXPLOITATION_WEIGHT_GENERALIZATION = 1.0;

	// Local Models

	public int PARAM_quantileForGenerationOfArtificialPerceptions = 3;


	// AVT

	public double PARAM_AVT_acceleration = 2;
	public double PARAM_AVT_deceleration = 1. / 3.0;
	public double PARAM_AVT_percentAtStart = 0.2;


	// NCS
	public boolean PARAM_NCS_isCreationWithNeighbor = false;
	public boolean PARAM_NCS_isAllContextSearchAllowedForLearning = true;
	public boolean PARAM_NCS_isAllContextSearchAllowedForExploitation = true;

	public boolean PARAM_NCS_isConflictDetection = false;
	public boolean PARAM_NCS_isConcurrenceDetection = false;
	public boolean PARAM_NCS_isVoidDetection = false;
	public boolean PARAM_NCS_isSubVoidDetection = false;
	public boolean PARAM_NCS_isFrontierRequest = false;
	public boolean PARAM_NCS_isSelfModelRequest = false;
	public boolean PARAM_NCS_isConflictResolution = false;
	public boolean PARAM_NCS_isConcurrenceResolution = false;
	public boolean PARAM_NCS_isFusionResolution = false;
	public boolean PARAM_NCS_isRetrucstureResolution = false;


	public boolean PARAM_isLearnFromNeighbors = false;
	public boolean PARAM_isDream = false;

	public boolean PARAM_isExploitationActive = false;

	public int PARAM_DreamCycleLaunch = 1500;
	public int STATE_DreamCompleted = -1; // -1 : before process / 0 : processing / 1 processed

	public int PARAM_nbOfNeighborForLearningFromNeighbors = 10000;


	public double PARAM_probabilityOfRangeAmbiguity = 0.1;


	// TODO A SUPPRIMER

	public int PARAM_numberOfPointsForRegression_ASUPPRIMER = 50;
	public boolean isVoidDetection_old = false;
	public boolean isCoopLearningWithoutOracle_ASUPPRIMER = false;

	public boolean STOP_UI = false;
	public int STOP_UI_cycle = 500;
}