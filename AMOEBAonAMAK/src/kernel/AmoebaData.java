package kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import agents.context.Context;
import agents.head.DynamicPerformance;
import agents.head.REQUEST;
import agents.percept.Percept;
/**
 * A Plain Old Java Object for storing parameters for the Head. Keep it as simple as possible for ease of serialization.<br/>
 * Make sure that all member are : public, serializable, and with a default constructor (taking no parameters).
 * @author Hugo
 *
 */
public class AmoebaData implements Serializable {
	private static final long serialVersionUID = 1L;

	public String nameID = null;
	
	public int nPropositionsReceived;
	public int averagePredictionCriticityWeight = 0;
	public int numberOfCriticityValuesForAverage = 100;
	public int numberOfCriticityValuesForAverageforVizualisation = 300;

	public Double prediction;
	public HashMap<String, Double> higherNeighborLastPredictionPercepts = null;
	public Double endogenousPredictionActivatedContextsOverlaps = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = 0.0;
	public Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = 0.0;
	public Double endogenousPredictionActivatedContextsSharedIncompetence = 0.0;
	public Double endogenousPredictionNContexts = 0.0;
	public Double endogenousPredictionNContextsByInfluence = 0.0;

	public Double oracleValue = null; //TODO à changer !!!!!!!!!!
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
	
	public boolean isActiveLearning;
	public boolean isSelfLearning;
	
	public boolean activeLearning = false;
	public boolean selfLearning = false;
	
	public HashMap<String, Double> selfRequest;

	public Double maxConfidence;
	public Double minConfidence;
	
	public Double maxPrediction = Double.NEGATIVE_INFINITY;
	public Double minPrediction = Double.POSITIVE_INFINITY;
	
	public double normalizedCriticality = 0.0;

	// Endogenous feedback
	public boolean noBestContext;

	public double evolutionCriticalityPrediction = 0.5;
	public double evolutionCriticalityMapping = 0.5;
	public double evolutionCriticalityConfidence = 0.5;

	public int currentCriticalityPrediction = 0;
	public int currentCriticalityMapping = 0;
	public int currentCriticalityConfidence = 0;

	public double learningSpeed = 0.25;
	public int numberOfPointsForRegression = 50;

	public boolean contextNotFinished = false;

	public boolean isAutonomousMode = false;
	
	public DynamicPerformance predictionPerformance;
	public DynamicPerformance regressionPerformance;
	public DynamicPerformance mappingPerformance;
	
	public double[] executionTimes = new  double[20];
	public double[] executionTimesSums = new double[20];
	
	public double initRegressionPerformance = 1.0;
	
	public double averageRegressionPerformanceIndicator;

	public boolean isConflictResolution = false;
	public boolean isConcurrenceResolution = false;

	public boolean isVoidDetection = false;
	public boolean isConflictDetection = false;
	public boolean isConcurrenceDetection = false;
	public boolean isVoidDetection2 = false;
	public boolean isFrontierRequest = false;
	public boolean isSelfModelRequest = false;
	public boolean isCoopLearningWithoutOracle = false;

	public boolean isLearnFromNeighbors = false;
	public boolean isDream = false;


	public int nbOfNeighborForLearningFromNeighbors = 10000;
	public int nbOfNeighborForVoidDetectionInSelfLearning = 10000;
	public int nbOfNeighborForContexCreationWithouOracle = 10000;


	public boolean isSubPercepts = false;


	public HashMap<REQUEST,Integer> requestCounts = new HashMap<>();



}