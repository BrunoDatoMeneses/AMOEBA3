package agents.head;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

import agents.AmoebaAgent;
import agents.context.Context;
import agents.context.CustomComparator;
import agents.percept.Percept;
import kernel.AMOEBA;
import ncs.NCS;
import utils.Pair;
import utils.PickRandom;
import utils.PrintOnce;
import utils.Quadruplet;

/**
 * The Class Head.
 */
public class Head extends AmoebaAgent {

	// MEMBERS ---------------------
	
	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;
	private String functionSelected;

	HashMap<Percept, Double> currentSituation = new HashMap<Percept, Double>();

	public Criticalities criticalities;
	public Criticalities endogenousCriticalities;

	private ArrayList<Context> activatedContexts = new ArrayList<Context>();
	private ArrayList<Context> activatedContextsCopyForUpdates = new ArrayList<Context>();
	private ArrayList<Context> activatedNeighborsContexts = new ArrayList<Context>();

	private HashMap<Percept, ArrayList<Context>> partiallyActivatedContextInNeighbors = new HashMap<Percept, ArrayList<Context>>();
	private HashMap<Percept, ArrayList<Context>> partiallyActivatedContexts = new HashMap<Percept, ArrayList<Context>>();
	private HashMap<Percept, ArrayList<Context>> partialNeighborContexts = new HashMap<Percept, ArrayList<Context>>();

	private ArrayList<Context> contextsNeighborsByInfluence = new ArrayList<Context>();

	private HashMap<Percept, ContextPair<Context, Context>> requestSurroundings = new HashMap<Percept, ContextPair<Context, Context>>();
	private HashMap<Percept, ContextPair<Context, Context>> sharedIncompetenceContextPairs = new HashMap<Percept, ContextPair<Context, Context>>();

	private ArrayList<Context> contextsInCompetition = new ArrayList<Context>();

	Queue<EndogenousRequest> endogenousRequests = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
		   public int compare(EndogenousRequest r1, EndogenousRequest r2) {
			      return r2.getPriority().compareTo(r1.getPriority());
			   }
			});
	
	static double lembda = 0.99;
	// -----------------------------
	
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError,
			double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution) {

		getAmas().data.predictionPerformance = new DynamicPerformance(nSuccessBeforeDiminution, nConflictBeforeAugmentation,
				errorAllowed, augmentationFactorError, diminutionFactorError, minErrorAllowed);

		getAmas().data.regressionPerformance = new DynamicPerformance(100, 100, 200, 0.5, 0.5, 1);

		getAmas().data.mappingPerformance = new DynamicPerformance(100000, 1000000, getEnvironment().getMappingErrorAllowed(), 1.1,
				0.9, 1);
	}

	public Head(AMOEBA amoeba) {
		super(amoeba);
		
		for(int i =0 ; i< 20;i++) {
			getAmas().data.executionTimesSums[i]=0.0;
		}
		

		
		

	}
	
	

	public void addPartiallyActivatedContext(Percept nonValidPercept, Context validContextExecptOnTheNonValidPercept) {
		partiallyActivatedContexts.get(nonValidPercept).add(validContextExecptOnTheNonValidPercept);

	}

	public void addPartiallyActivatedContextInNeighbors(Percept nonValidPercept,
			Context validContextExecptOnTheNonValidPercept) {
		partiallyActivatedContextInNeighbors.get(nonValidPercept).add(validContextExecptOnTheNonValidPercept);

	}

	public void addPartialRequestNeighborContext(Percept nonValidPercept, Context validContextNeighborExecptOnTheNonValidPercept) {
		partialNeighborContexts.get(nonValidPercept).add(validContextNeighborExecptOnTheNonValidPercept);

	}

	/**
	 * The core method of the head agent. Manage the whole behavior, and call method
	 * from context agents when needed.
	 */
	@Override
	public void onAct() {
		
		getAmas().data.currentCriticalityPrediction = 0;
		getAmas().data.currentCriticalityMapping = 0;
		getAmas().data.currentCriticalityConfidence = 0;

		for (Percept pct : getAmas().getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}

		getAmas().data.nPropositionsReceived = activatedContexts.size();
		getAmas().data.newContextWasCreated = false;
		setContextFromPropositionWasSelected(false);
		getAmas().data.oldOracleValue = getAmas().data.oracleValue;
		getAmas().data.oracleValue = getAmas().getPerceptions("oracle");

		/* The head memorize last used context agent */
		lastUsedContext = bestContext;
		bestContext = null;

		if (getAmas().data.useOracle) {
			playWithOracle();
		} else {
			playWithoutOracle();
		}

		updateStatisticalInformations(); /// regarder dans le détail, possible que ce pas trop utile

		newContext = null;

	}

	private void playWithOracle() {

		getAmas().data.executionTimes[0]=System.currentTimeMillis();
		if (activatedContexts.size() > 0) {
			selectBestContext(); // using highest confidence
			// selectBestContextWithDistanceToModel();
		} else {
			bestContext = lastUsedContext;
		}

		if (bestContext != null) {
			setContextFromPropositionWasSelected(true);
			getAmas().data.prediction = bestContext.getActionProposal();

		} else { // happens only at the beginning
			setNearestContextAsBestContext();
		}

		/* Compute the criticity. Will be used by context agents. */
		getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction);
		

		/* If we have a bestcontext, send a selection message to it */
		if (bestContext != null) {
			bestContext.notifySelection();
			getEnvironment().trace(new ArrayList<String>(Arrays.asList(bestContext.getName(),
					"*********************************************************************************************************** BEST CONTEXT")));
		}

		getAmas().data.executionTimes[0]=System.currentTimeMillis()- getAmas().data.executionTimes[0];

		getAmas().data.executionTimes[1]=System.currentTimeMillis();
		// endogenousPlay();
		getAmas().data.executionTimes[1]=System.currentTimeMillis()- getAmas().data.executionTimes[1];

		getAmas().data.executionTimes[2]=System.currentTimeMillis();
		selfAnalysationOfContexts4();
		getAmas().data.executionTimes[2]=System.currentTimeMillis()- getAmas().data.executionTimes[2];
		

		getAmas().data.executionTimes[3]=System.currentTimeMillis();
		NCSDetection_IncompetentHead(); /*
		 * If there isn't any proposition or only bad propositions, the head is
		 * incompetent. It needs help from a context.
		 */
		getAmas().data.executionTimes[3]=System.currentTimeMillis()- getAmas().data.executionTimes[3];
		

		getAmas().data.executionTimes[4]=System.currentTimeMillis();
		NCSDetection_Concurrence(); /* If result is good, shrink redundant context (concurrence NCS) */
		getAmas().data.executionTimes[4]=System.currentTimeMillis()- getAmas().data.executionTimes[4];

		getAmas().data.executionTimes[5]=System.currentTimeMillis();
		NCSDetection_Create_New_Context(); /* Finally, head agent check the need for a new context agent */
		getAmas().data.executionTimes[5]=System.currentTimeMillis()- getAmas().data.executionTimes[5];

		getAmas().data.executionTimes[6]=System.currentTimeMillis();
		NCSDetection_Context_Overmapping();
		getAmas().data.executionTimes[6]=System.currentTimeMillis()- getAmas().data.executionTimes[6];


		getAmas().data.executionTimes[7]=System.currentTimeMillis();
		
		NCSDetection_ChildContext();
		
		NCSDetection_PotentialRequest();
		
		criticalities.addCriticality("spatialCriticality",
				(getMinMaxVolume() - getVolumeOfAllContexts()) / getMinMaxVolume());

		getAmas().data.spatialGeneralizationScore = getVolumeOfAllContexts() / getAmas().getContexts().size();

		double globalConfidence = 0;

		for (Context ctxt : getAmas().getContexts()) {
			globalConfidence += ctxt.getConfidence();
		}
		globalConfidence = globalConfidence / getAmas().getContexts().size();

		if (activatedNeighborsContexts.size() > 1) {



			int i = 1;
			for (Context ctxt : activatedNeighborsContexts) {

				for (Context otherCtxt : activatedNeighborsContexts.subList(i, activatedNeighborsContexts.size())) {

					// if(nearestLocalNeighbor(ctxt, otherCtxt)) {

					Pair<Double, Percept> distanceAndPercept = ctxt.distance(otherCtxt);
					// distanceAndPercept.getB());
					if (distanceAndPercept.getA() < 0) {
						criticalities.addCriticality("localOverlapMappingCriticality",
								Math.abs(distanceAndPercept.getA()));
					} else if (distanceAndPercept.getA() > 0 && distanceAndPercept.getB() != null) {
						criticalities.addCriticality("localVoidMappingCriticality", distanceAndPercept.getA());
					} else {
						criticalities.addCriticality("localOpenVoidMappingCriticality", distanceAndPercept.getA());
					}

					// }

				}
				i++;

			}

		}
		
		getAmas().data.mappingPerformance.setPerformanceIndicator(getEnvironment().getMappingErrorAllowed());// Math.pow(world.getMappingErrorAllowed(),
		// world.getScheduler().getPercepts().size());

		getAmas().data.evolutionCriticalityPrediction = (lembda * getAmas().data.evolutionCriticalityPrediction)
				+ ((1 - lembda) * getAmas().data.currentCriticalityPrediction);
		getAmas().data.evolutionCriticalityMapping = (lembda * getAmas().data.evolutionCriticalityMapping)
				+ ((1 - lembda) * getAmas().data.currentCriticalityMapping);
		getAmas().data.evolutionCriticalityConfidence = (lembda * getAmas().data.evolutionCriticalityConfidence)
				+ ((1 - lembda) * getAmas().data.currentCriticalityConfidence);

		
		getAmas().data.executionTimes[7]=System.currentTimeMillis()- getAmas().data.executionTimes[7];

		
		for(int i = 0 ; i<20;i++) {
			getAmas().data.executionTimesSums[i] += getAmas().data.executionTimes[i];
		}			

	}

	public double getSpatialGeneralizationScore() {
		return getAmas().data.spatialGeneralizationScore;
	}

	private boolean nearestLocalNeighbor(Context ctxt1, Context ctxt2) {

		boolean nearestLocalNeighborTest = false;

		for (Percept pct : getAmas().getPercepts()) {

			if (partiallyActivatedContextInNeighbors.get(pct).contains(ctxt1)
					&& partiallyActivatedContextInNeighbors.get(pct).contains(ctxt2)) {
				nearestLocalNeighborTest = nearestLocalNeighborTest
						|| (Math.abs(partiallyActivatedContextInNeighbors.get(pct).indexOf(ctxt1)
								- partiallyActivatedContextInNeighbors.get(pct).indexOf(ctxt2)) == 1);
			}

		}

		return nearestLocalNeighborTest;
	}

	public double getMinMaxVolume() {
		double minMaxVolume = 1;
		for (Percept pct : getAmas().getPercepts()) {
			minMaxVolume *= pct.getMinMaxDistance();
		}
		return (minMaxVolume == 0.0) ? 1 : minMaxVolume;
	}

	public double getVolumeOfAllContexts() {
		double allContextsVolume = 0;
		for (Context ctxt : getAmas().getContexts()) {
			allContextsVolume += ctxt.getVolume();
		}
		return allContextsVolume;
	}

	public double getSpatialCriticality() {
		return criticalities.getCriticality("spatialCriticality");
	}

	/**
	 * Play without oracle.
	 */
	private void playWithoutOracle() {

		logger().debug("HEAD without oracle", "Nombre de contextes activés: " + activatedContexts.size());

		selectBestContext();
		if (bestContext != null) {
			getAmas().data.noBestContext = false;
			getAmas().data.prediction = bestContext.getActionProposal();
		} else {

			getAmas().data.noBestContext = true;
			ArrayList<Context> allContexts = getAmas().getContexts();
			Context nearestContext = this.getNearestContext(activatedNeighborsContexts);
			if(nearestContext != null) {
				getAmas().data.prediction = nearestContext.getActionProposal();
				bestContext = nearestContext;
			} else {
				//TODO THIS IS VERY INEFICIENT ! amoeba should not look globally, but right now there's no other strategy
				// to limit performance impact, we limit our search on a random sample.
				// a better way would be to increase neighborhood.
				PrintOnce.print("Play without oracle : no nearest context in neighbors, searching in a random sample. (only shown once)");
				List<Context> searchList = PickRandom.pickNRandomElements(getAmas().getContexts(), 100);
				nearestContext = this.getNearestContext(searchList);
				if(nearestContext != null) {
					getAmas().data.prediction = nearestContext.getActionProposal();
				} else {
					getAmas().data.prediction = 0.0;
				}
			}
		}
		if(bestContext != null) {
			logger().debug("HEAD without oracle", "Best context selected without oracle is : " + bestContext.getName());
			// Config.print("With function : " +
			// bestContext.getFunction().getFormula(bestContext), 0);
			logger().debug("HEAD without oracle",
					"BestContext : " + bestContext.toStringFull() + " " + bestContext.getConfidence());
			// functionSelected = bestContext.getFunction().getFormula(bestContext);
			
		}
		else {
			logger().debug("HEAD without oracle", "no Best context selected ");
		}
		
		getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction);

		endogenousPlay();
	}

	private void endogenousPlay() {

		getAmas().data.endogenousPredictionActivatedContextsOverlaps = null;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = null;

		getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = null;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = null;

		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = null;

		getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = null;
		getAmas().data.endogenousPredictionNContextsByInfluence = null;

		for (Percept pcpt : getAmas().getPercepts()) {
			requestSurroundings.get(pcpt).clear();
			sharedIncompetenceContextPairs.get(pcpt).clear();
		}
		contextsInCompetition.clear();

		if (uniqueActivatedContext()) {
			getAmas().data.endogenousPredictionActivatedContextsOverlaps = activatedContexts.get(0).getActionProposal();
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = activatedContexts.get(0)
					.getActionProposal();
			getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = activatedContexts.get(0).getActionProposal();
			getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = activatedContexts.get(0)
					.getActionProposal();
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = activatedContexts.get(0)
					.getActionProposal();
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = activatedContexts.get(0)
					.getActionProposal();
			getAmas().saver.newManualSave("Unique Context");
		} else if (severalActivatedContexts()) {
			NCS_EndogenousCompetition();
		} else {
			if (surroundingContexts()) {
				NCS_EndogenousSharedIncompetence();
			} else if (activatedContexts.size() > 0) {
				getAmas().saver.newManualSave("Other activated");
			} else if (activatedContexts.size() == 0) {
				getAmas().saver.newManualSave("Other non activated");
			}
//			else if(noActivatedContext()) {
//				endogenousPrediction = -2000.0;
//				NCS_EndogenousIncompetence();
//			}	
//			else {
//				endogenousPrediction = -3000.0;
//			}
//			else {
//				endogenousPrediction = prediction;
//			}
		}

		// Endogenous prediction N contexts //

		Double endogenousSumTerm = 0.0;
		Double endogenousNormalizationTerm = 0.0;

		for (Context ctxt : activatedNeighborsContexts) {
			endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation) * ctxt.getActionProposal();
			endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
		}
		getAmas().data.endogenousPredictionNContexts = endogenousSumTerm / endogenousNormalizationTerm;

		// Endogenous prediction N contexts by influence //

		getAmas().data.maxConfidence = Double.NEGATIVE_INFINITY;
		getAmas().data.minConfidence = Double.POSITIVE_INFINITY;

		for (Context ctxt : getAmas().getContexts()) {

			if (ctxt.getConfidence() > getAmas().data.maxConfidence) {
				getAmas().data.maxConfidence = ctxt.getConfidence();
			}
			if (ctxt.getConfidence() < getAmas().data.minConfidence) {
				getAmas().data.minConfidence = ctxt.getConfidence();
			}

			if (ctxt.getInfluenceWithConfidence(currentSituation) > 0.5) {
				contextsNeighborsByInfluence.add(ctxt);

			}
		}

		endogenousSumTerm = 0.0;
		endogenousNormalizationTerm = 0.0;
		if (contextsNeighborsByInfluence.size() > 0) {
			for (Context ctxt : contextsNeighborsByInfluence) {
				endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation) * ctxt.getActionProposal();
				endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
			}

			getAmas().data.endogenousPredictionNContextsByInfluence = endogenousSumTerm / endogenousNormalizationTerm;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsOverlaps == null) {
			getAmas().data.endogenousPredictionActivatedContextsOverlaps = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence == null) {
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence == null) {
			getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence == null) {
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume == null) {
			getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence == null) {
			getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = getAmas().data.prediction;
		}

		if (getAmas().data.endogenousPredictionNContextsByInfluence == null) {
			getAmas().data.endogenousPredictionNContextsByInfluence = getAmas().data.prediction;
		}
	}

	private boolean noActivatedContext() {
		// Test if only one context is activated
		return activatedContexts.size() == 0;
	}

	public boolean uniqueActivatedContext() {
		// Test if only one context is activated
		return activatedContexts.size() == 1;
	}

	public boolean severalActivatedContexts() {
		// Test if several context are activated
		return activatedContexts.size() > 1;
	}

	public boolean surroundingContexts() {
		// Test if there are surrounding contexts
		boolean testSurroudingContext = false;

		for (Percept pct : getAmas().getPercepts()) {

			computeNearestContextsByPercept(pct);

		}

		// displayContexts();

		for (Percept pcpt : getAmas().getPercepts()) {

			requestSurroundings.get(pcpt).print(pcpt);
			if (sharedIncompetenceContextPairs.get(pcpt) != null) {
				if (sharedIncompetenceContextPairs.get(pcpt).containTwoContexts()) {
					testSurroudingContext = true;
				}
			}

		}

		return testSurroudingContext;
	}

	private void diplayListRanges(ArrayList<Context> list, Percept prct, String range) {

		System.out.print(range + " ranges list" + "  ");
		for (Context ctxt : list) {
			System.out.print(ctxt.getRanges().get(prct).getRange(range) + "  ");
		}
		System.out.println(" ");

	}

	private void computeNearestContextsByPercept(Percept pct) {
		ContextPair<Context, Context> nearestContexts = new ContextPair(null, null);
		boolean startNeighbor = false;
		boolean endNeighbor = false;

		ArrayList<Context> activatedContextInOtherPercepts = getAllActivatedContextsExeptForOnePercept(pct);

		if (activatedContextInOtherPercepts.size() > 0) {



			CustomComparator rangeStartComparator = new CustomComparator(pct, "start");
			Collections.sort(activatedContextInOtherPercepts, rangeStartComparator);
			// diplayListRanges(activatedContextInOtherPercepts, pct, "start");

			for (Context ctxt : activatedContextInOtherPercepts) {
				if (ctxt.getRanges().get(pct).getRange("start") > pct.getValue() && !startNeighbor) {
					nearestContexts.setR(ctxt);
					startNeighbor = true;
				}

			}

			CustomComparator rangeEndComparator = new CustomComparator(pct, "end");
			Collections.sort(activatedContextInOtherPercepts, rangeEndComparator);
			Collections.reverse(activatedContextInOtherPercepts);
			// diplayListRanges(activatedContextInOtherPercepts, pct, "end");

			for (Context ctxt : activatedContextInOtherPercepts) {
				if (ctxt.getRanges().get(pct).getRange("end") < pct.getValue() && !endNeighbor) {
					nearestContexts.setL(ctxt);
					endNeighbor = true;
				}
			}

			// nearestContexts.print(pct);
			requestSurroundings.put(pct, nearestContexts);

			if (nearestContexts.getL() != null && nearestContexts.getR() != null) {
				sharedIncompetenceContextPairs.put(pct, nearestContexts);
			}
		} else {

		}

	}

	private ArrayList<Context> getAllActivatedContextsExeptForOnePercept(Percept onePercept) {

		return partiallyActivatedContexts.get(onePercept);
	}

	private ArrayList<Context> getAllActivatedNeighborContextsExeptForOnePercept(Percept onePercept) {

		return partialNeighborContexts.get(onePercept);
	}

	public void displayPartiallyActivatedContexts() {
		//////// System.out.println("PARTIALLY ACTIVATED CONTEXTS");
		for (Percept pct : partiallyActivatedContexts.keySet()) {
			//// System.out.print(pct.getName() + " : ");
			if (partiallyActivatedContexts.get(pct).size() > 0)
				for (Context ctxt : partiallyActivatedContexts.get(pct)) {
					//// System.out.print(ctxt.getName() + " ; ");
				}
			//////// System.out.println(" ");
		}
	}

	public void displayContexts() {
		//////// System.out.println("CONTEXTS");
		for (Context ctxt : getAmas().getContexts()) {
			//// System.out.print(ctxt.getName() + " ; ");
		}
		//////// System.out.println(" ");
	}

	private Percept getDifferentPercept(Percept p) {
		for (Percept pct : partiallyActivatedContexts.keySet()) {
			if (p != pct) {
				return pct;
			}
		}
		return null;
	}

	private boolean contextActivateInOtherPerceptsThan(Context ctxt, Percept p1, Percept p2) {
		boolean test = true;
		for (Percept prct : partiallyActivatedContexts.keySet()) {
			if (prct != p1 && prct != p2) {
				test = test & partiallyActivatedContexts.get(prct).contains(ctxt);
			}
		}
		return test;
	}

	private void NCS_EndogenousCompetition() {

		// Creation of twin contexts to give the endogenous prediction

		// 2 CTXT
//		if(activatedContexts.get(0).getInfluence(currentSituation)>activatedContexts.get(1).getInfluence(currentSituation)) {
//			highestConfidenceContext = activatedContexts.get(0);
//			secondHighestConfidenceContext = activatedContexts.get(1);
//		}
//		else {
//			highestConfidenceContext = activatedContexts.get(1);
//			secondHighestConfidenceContext = activatedContexts.get(0);
//		}
//		
//		
//		for(int i=2; i<activatedContexts.size();i++) {
//			if(activatedContexts.get(i).getInfluence(currentSituation)>highestConfidenceContext.getInfluence(currentSituation)) {		
//				secondHighestConfidenceContext = highestConfidenceContext;
//				highestConfidenceContext = activatedContexts.get(i);
//			}
//			else if(activatedContexts.get(i).getInfluence(currentSituation)>secondHighestConfidenceContext.getInfluence(currentSituation)) {
//				secondHighestConfidenceContext = activatedContexts.get(i);
//			}
//		}
//		
//		contextsInCompetition.add(highestConfidenceContext);
//		contextsInCompetition.add(secondHighestConfidenceContext);
//		
//		double highestConfidenceContextInfluence = highestConfidenceContext.getInfluence(currentSituation);
//		double secondHighestConfidenceContextInfluence = secondHighestConfidenceContext.getInfluence(currentSituation);
//		
//		
//		endogenousPrediction2Contexts = (highestConfidenceContextInfluence*highestConfidenceContext.getActionProposal() + secondHighestConfidenceContextInfluence*secondHighestConfidenceContext.getActionProposal()) / (highestConfidenceContextInfluence + secondHighestConfidenceContextInfluence);
//		
//		ArrayList<Context> concernContexts = new ArrayList<Context>();
//		concernContexts.add(highestConfidenceContext);
//		concernContexts.add(secondHighestConfidenceContext);

		// N CTXT
		Double endogenousSumTerm = 0.0;
		Double endogenousNormalizationTerm = 0.0;

		Double endogenousSumTerm2 = 0.0;
		Double endogenousNormalizationTerm2 = 0.0;

		Double endogenousSumTerm3 = 0.0;
		Double endogenousNormalizationTerm3 = 0.0;

		Double endogenousSumTerm4 = 0.0;
		Double endogenousNormalizationTerm4 = 0.0;

		Double endogenousSumTerm5 = 0.0;
		Double endogenousNormalizationTerm5 = 0.0;

		ArrayList<Context> concernContexts = new ArrayList<Context>();
		for (Context ctxt : activatedContexts) {
			endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation) * ctxt.getActionProposal();
			endogenousSumTerm2 += ctxt.getWorstInfluenceWithConfidence(currentSituation) * ctxt.getActionProposal();
			// endogenousSumTerm3 +=
			// ctxt.getInfluence(currentSituation)*ctxt.getActionProposal();
			endogenousSumTerm3 += ctxt.getInfluenceWithConfidenceAndVolume(currentSituation) * ctxt.getActionProposal();
			endogenousSumTerm4 += ctxt.getWorstInfluence(currentSituation) * ctxt.getActionProposal();
			endogenousSumTerm5 += ctxt.getWorstInfluenceWithVolume(currentSituation) * ctxt.getActionProposal();
			// endogenousSumTerm5 +=
			// ctxt.getWorstInfluenceWithWorstRange(currentSituation)*ctxt.getActionProposal();

			endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
			endogenousNormalizationTerm2 += ctxt.getWorstInfluenceWithConfidence(currentSituation);
			// endogenousNormalizationTerm3 += ctxt.getInfluence(currentSituation);
			endogenousNormalizationTerm3 += ctxt.getInfluenceWithConfidenceAndVolume(currentSituation);
			endogenousNormalizationTerm4 += ctxt.getWorstInfluence(currentSituation);
			endogenousNormalizationTerm5 += ctxt.getWorstInfluenceWithVolume(currentSituation);
			// endogenousNormalizationTerm5 +=
			// ctxt.getWorstInfluenceWithWorstRange(currentSituation);

			concernContexts.add(ctxt);
		}
		getAmas().data.endogenousPredictionActivatedContextsOverlaps = endogenousSumTerm / endogenousNormalizationTerm;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = endogenousSumTerm2
				/ endogenousNormalizationTerm2;

		getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = endogenousSumTerm3
				/ endogenousNormalizationTerm3;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = endogenousSumTerm4
				/ endogenousNormalizationTerm4;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = endogenousSumTerm5
				/ endogenousNormalizationTerm5;

		getAmas().saver.newManualSave("Competition");

	}

	private void NCS_EndogenousSharedIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the
		// prediction

		;

		ContextPair<Context, Context> closestContexts = new ContextPair<Context, Context>(null, null);
		double smallestDistanceBetweenContexts = Double.POSITIVE_INFINITY;
		double currentDistance;

		for (Percept pct : sharedIncompetenceContextPairs.keySet()) {
			if (sharedIncompetenceContextPairs.get(pct) != null) {
				if (sharedIncompetenceContextPairs.get(pct).containTwoContexts()) {
					currentDistance = sharedIncompetenceContextPairs.get(pct).rangeToRangeDistance(pct);
					if (currentDistance < smallestDistanceBetweenContexts) {
						closestContexts = sharedIncompetenceContextPairs.get(pct);
						smallestDistanceBetweenContexts = currentDistance;
					}
				}
			}
		}

		double contextInfluenceL = closestContexts.getL().getInfluenceWithConfidence(currentSituation);
		double contextInfluenceR = closestContexts.getR().getInfluenceWithConfidence(currentSituation);



		if (compareClosestContextPairModels(closestContexts) < 10) {
			getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = (contextInfluenceL
					* closestContexts.getL().getActionProposal()
					+ contextInfluenceR * closestContexts.getR().getActionProposal())
					/ (contextInfluenceL + contextInfluenceR);
		} else {
			getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = getAmas().data.prediction;
		}

//		double prediction = closestContexts.actionProposal(1.0);
//		if(prediction == Double.NEGATIVE_INFINITY) {
//			endogenousPrediction = - 1750.0;
//		}
//		else {
//			endogenousPrediction = prediction;
//		}

		ArrayList<Context> concernContexts = new ArrayList<Context>();
		concernContexts.add(closestContexts.getL());
		concernContexts.add(closestContexts.getR());
		getAmas().saver.newManualSave("SharedIncompetence");

	}
	
	private void NCSDetection_ChildContext() {
		
		if(bestContext!=null) {
			if(!bestContext.getLocalModel().finishedFirstExperiments() && getAmas().data.firstContext && getAmas().getCycle()>0 && !bestContext.isDying()) {
				bestContext.solveNCS_ChildContext();
				
				
			}
		}
		
		
		
	}
	
	

	private Double compareClosestContextPairModels(ContextPair<Context, Context> closestContexts) {
		Double difference = 0.0;

		if (closestContexts.getL().getLocalModel().getCoef().length == closestContexts.getR().getLocalModel()
				.getCoef().length) {
			Double[] coefL = closestContexts.getL().getLocalModel().getCoef();
			Double[] coefR = closestContexts.getR().getLocalModel().getCoef();
			for (int i = 0; i < closestContexts.getL().getLocalModel().getCoef().length; i++) {
				difference += Math.abs(coefL[i] - coefR[i]);
			}
		}

		if (difference == 0.0) {
			return Double.POSITIVE_INFINITY;
		} else {
			return difference;
		}

	}

	private void NCS_EndogenousIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the
		// prediction
	}

	private void NCSDetection_Create_New_Context() {
		/* Finally, head agent check the need for a new context agent */

		
		
		
		boolean newContextCreated = false;
		getAmas().data.executionTimes[9]=System.currentTimeMillis();
		if (activatedContexts.size() == 0) {
			
			getEnvironment().trace(new ArrayList<String>(Arrays.asList(
					"*********************************************************************************************************** SOLVE NCS CREATE NEW CONTEXT")));
			
			getAmas().data.executionTimes[8]=System.currentTimeMillis();		
			Pair<Context, Double> nearestGoodContext = getbestContextInNeighborsWithDistanceToModel(activatedNeighborsContexts);
			getAmas().data.executionTimes[8]=System.currentTimeMillis()- getAmas().data.executionTimes[8];

			
			
			Context context;
			if (nearestGoodContext.getA() != null) {
				getEnvironment().trace(new ArrayList<String>(Arrays.asList(nearestGoodContext.getA().getName(),
						"************************************* NEAREST GOOD CONTEXT")));
				context = createNewContext(nearestGoodContext.getA());
			} else {
				context = createNewContext();
			}
			// context = createNewContext();

			bestContext = context;
			newContext = context;
			newContextCreated = true;
			
			
		}
		getAmas().data.executionTimes[9]=System.currentTimeMillis()- getAmas().data.executionTimes[9];


		

		
		
		getAmas().data.executionTimes[10]=System.currentTimeMillis();
		if (!newContextCreated) {
			updateStatisticalInformations();
		}
		getAmas().data.executionTimes[10]=System.currentTimeMillis()- getAmas().data.executionTimes[10];

		
	}

	private void NCSDetection_Context_Overmapping() {

		ArrayList<Context> activatedContextsCopy = new ArrayList<Context>();
		activatedContextsCopy.addAll(activatedContexts);

		for (Context ctxt : activatedContextsCopy) {
			if(!ctxt.isDying() && ctxt.getLocalModel().finishedFirstExperiments()) {
				ctxt.NCSDetection_OverMapping();
			}

		}
	}

	private void NCSDetection_Concurrence() {
		/* If result is good, shrink redundant context (concurrence NCS) */
		if (bestContext != null && getAmas().data.criticity <= getAmas().data.predictionPerformance.getPerformanceIndicator()) {

			for (int i = 0; i < activatedContexts.size(); i++) {

				if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && this
						.getCriticity(activatedContexts.get(i)) <= getAmas().data.predictionPerformance.getPerformanceIndicator()) {

					activatedContexts.get(i).solveNCS_Concurrence(this);
				}
			}
		}
	}

	private void NCSDetection_IncompetentHead() {
		/*
		 * If there isn't any proposition or only bad propositions, the head is
		 * incompetent. It needs help from a context.
		 */
		if (activatedContexts.isEmpty()
				|| (getAmas().data.criticity > getAmas().data.predictionPerformance.getPerformanceIndicator() && !oneOfProposedContextWasGood())) {

			Context c = getNearestGoodContext(activatedNeighborsContexts);
			// Context c = getSmallestGoodContext(activatedNeighborsContexts);

			if (c != null)
				c.solveNCS_IncompetentHead(this);
			bestContext = c;

			/* This allow to test for all contexts rather than the nearest */
			/*
			 * for (Agent a : allContexts) { Context c = (Context) a; if
			 * (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c !=
			 * newContext && !c.isDying() && c != bestContext && !contexts.contains(c)) {
			 * c.growRanges(this);
			 * 
			 * } }
			 */

		}
	}
	
	private void NCSDetection_PotentialRequest() {
		
		

		
		if (activatedNeighborsContexts.size() > 1) {

			int i = 1;
			for (Context ctxt : activatedNeighborsContexts) {

				for (Context otherCtxt : activatedNeighborsContexts.subList(i, activatedNeighborsContexts.size())) {

					
					if(!this.isDying() && !ctxt.isDying()) {
						EndogenousRequest potentialRequest = ctxt.endogenousRequest(otherCtxt);
						
						if(potentialRequest != null) {
							addEndogenousRequest(potentialRequest);
							getEnvironment().trace(new ArrayList<String>(Arrays.asList("NEW ENDO REQUEST", ""+potentialRequest)));
						}
					}
					

				}
				i++;
			}
		}
		
		getEnvironment().trace(new ArrayList<String>(Arrays.asList("ENDO REQUESTS", ""+endogenousRequests.size())));
//		for(EndogenousRequest endoRequest : endogenousRequests) {
//			
//			System.out.println(endoRequest);
//			
//		}
		
	}

	private void selfAnalysationOfContexts2() {

		if (activatedContexts.size() > 1) {
			selfAnalysationOfContextOnSeveralActivatedContexts();

		} else if (activatedContexts.size() == 1) {

//			double distanceToOracleForActivatedContext = activatedContexts.get(0).getLocalModel().distance(activatedContexts.get(0).getCurrentExperiment());
//			//System.out.println(distanceToOracleForActivatedContext + " ******************************************************************DISTANCE TO MODEL : " );
//			if(activatedNeighborsContexts.size()>1) {
//				
//				Context closestContextToOracle = null;
//				double minDistanceToOraclePredictionInNeighbors = Double.POSITIVE_INFINITY;
//				double currentDistanceToOraclePrediction = 0.0; 
//				for(Context contextNeighbor : activatedNeighborsContexts) {
//					if(contextNeighbor != activatedContexts.get(0)) {
//						currentDistanceToOraclePrediction = contextNeighbor.getLocalModel().distance(contextNeighbor.getCurrentExperiment());
//						if(currentDistanceToOraclePrediction<minDistanceToOraclePredictionInNeighbors) {
//							minDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
//							closestContextToOracle = contextNeighbor;
//						} 
//					}
//				}
//				
//				if(minDistanceToOraclePredictionInNeighbors>distanceToOracleForActivatedContext) {
//					//System.out.println("OLD COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
//					activatedContexts.get(0).getLocalModel().updateModel(activatedContexts.get(0).getCurrentExperiment(),learningSpeed,numberOfPointsForRegression);
//					//System.out.println("NEW COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
//					
//				}else {
//					closestContextToOracle.solveNCS_IncompetentHead(this);
//					//activatedContexts.get(0).setLocalModel(new LocalModelMillerRegression(world, activatedContexts.get(0), closestContextToOracle.getLocalModel().getCoef(),closestContextToOracle.getLocalModel().getFirstExperiments()));
//					//LocalModelAgent remplacementModel = new LocalModelMillerRegression(world, activatedContexts.get(0), closestContextToOracle.getLocalModel().getCoef().clone());
//					//activatedContexts.get(0).setLocalModel(remplacementModel);
//					
//				}
//
//				
//			}else {
//				//System.out.println("OLD COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
//				activatedContexts.get(0).getLocalModel().updateModel(activatedContexts.get(0).getCurrentExperiment(),learningSpeed,numberOfPointsForRegression);
//				//System.out.println("NEW COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
//				
//				
//			}

			selfAnalysationOfContextOnUniqueActivatedContext();

		}

		for (Context ctxt : activatedNeighborsContexts) {

			if (!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}

		}
	}

	private void selfAnalysationOfContexts3() {

		if (activatedContexts.size() > 1) {

			selfAnalysationOfContextOnSeveralActivatedContexts();

		} else if (activatedContexts.size() == 1) {

			selfAnalysationOfContextOnUniqueActivatedContext();

		}

		for (Context ctxt : activatedNeighborsContexts) {

			if (!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}

		}
	}

	private void selfAnalysationOfContexts4() {

		double currentDistanceToOraclePrediction;
		Context closestContextToOracle = null;
		double minDistanceToOraclePrediction = Double.POSITIVE_INFINITY;

		for (Context activatedContext : activatedContexts) {
			currentDistanceToOraclePrediction = activatedContext.getLocalModel()
					.distance(activatedContext.getCurrentExperiment());
			getAmas().data.distanceToRegression = currentDistanceToOraclePrediction;

			getAmas().data.contextNotFinished = false;
			getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE", activatedContext.getName(),
					"" + activatedContext.getLocalModel().distance(activatedContext.getCurrentExperiment()))));
			if (!activatedContext.getLocalModel().finishedFirstExperiments()) {
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.learningSpeed,
						getAmas().data.numberOfPointsForRegression);
				getAmas().data.contextNotFinished = true;
			}

			else if (currentDistanceToOraclePrediction < getAverageRegressionPerformanceIndicator()) {
			//else if (currentDistanceToOraclePrediction < regressionPerformance.getPerformanceIndicator()) {
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.learningSpeed,
						getAmas().data.numberOfPointsForRegression);

			}

			if (currentDistanceToOraclePrediction < minDistanceToOraclePrediction) {
				minDistanceToOraclePrediction = currentDistanceToOraclePrediction;
				closestContextToOracle = activatedContext;
			}

			if (!getAmas().data.contextNotFinished) {
				criticalities.addCriticality("distanceToRegression", currentDistanceToOraclePrediction);
				
			}
			
			activatedContext.criticalities.addCriticality("distanceToRegression", currentDistanceToOraclePrediction);
			//getEnvironment().trace(new ArrayList<String>(Arrays.asList("ADD CRITICALITY TO CTXT", ""+activatedContext.getName(), ""+criticalities.getLastValues().get("distanceToRegression").size())));

		}

		activatedContextsCopyForUpdates = new ArrayList<Context>(activatedContexts);
		for (Context activatedContext : activatedContexts) {
			
			activatedContext.criticalities.updateMeans();
			
			if (activatedContext.criticalities.getCriticalityMean("distanceToRegression") != null) {
				activatedContext.regressionPerformance.update(activatedContext.criticalities.getCriticalityMean("distanceToRegression"));
				getEnvironment().trace(new ArrayList<String>(Arrays.asList("UPDATE REGRESSION PERFORMANCE", activatedContext.getName(), ""+activatedContext.regressionPerformance.getPerformanceIndicator())));
			}
			
			
			activatedContext.analyzeResults4(this, closestContextToOracle);

		}
		activatedContexts = activatedContextsCopyForUpdates;

		for (Context ctxt : activatedNeighborsContexts) {

			if (!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}

		}
	}

	private void selfAnalysationOfContextOnUniqueActivatedContext() {

		if (activatedNeighborsContexts.size() > 0) {
			double distanceToOracleForActivatedContext = activatedContexts.get(0).getLocalModel()
					.distance(activatedContexts.get(0).getCurrentExperiment());
			Quadruplet<Context, Double, Context, Double> closestAndFarestContextsToPredictionWithDistance = closestAndFarestContextsToPrediction();

			double distanceToMin = Math
					.abs(distanceToOracleForActivatedContext - closestAndFarestContextsToPredictionWithDistance.getB());
			double distanceToMax = Math
					.abs(distanceToOracleForActivatedContext - closestAndFarestContextsToPredictionWithDistance.getD());

			if (distanceToMin < distanceToMax) {
				getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE",
						activatedContexts.get(0).getName(), "" + distanceToOracleForActivatedContext)));
				activatedContexts.get(0).getLocalModel().updateModel(activatedContexts.get(0).getCurrentExperiment(),
						getAmas().data.learningSpeed, getAmas().data.numberOfPointsForRegression);

			}
		} else {
			getEnvironment().trace(new ArrayList<String>(
					Arrays.asList("MODEL DISTANCE", activatedContexts.get(0).getName(), "" + activatedContexts.get(0)
							.getLocalModel().distance(activatedContexts.get(0).getCurrentExperiment()))));
			activatedContexts.get(0).getLocalModel().updateModel(activatedContexts.get(0).getCurrentExperiment(),
					getAmas().data.learningSpeed, getAmas().data.numberOfPointsForRegression);
		}

		// world.trace(new ArrayList<String>(Arrays.asList("MODEL
		// DISTANCE",activatedContexts.get(0).getName(),
		// ""+activatedContexts.get(0).getLocalModel().distance(activatedContexts.get(0).getCurrentExperiment()))));
		// activatedContexts.get(0).getLocalModel().updateModel(activatedContexts.get(0).getCurrentExperiment(),learningSpeed,numberOfPointsForRegression);
		activatedContexts.get(0).analyzeResults3(this, activatedContexts.get(0));
	}

	private Pair<Context, Double> maxModelDistanceToOraclePredictionInNeighbors() {
		Context farestContextToOracle = null;
		double maxDistanceToOraclePredictionInNeighbors = Double.NEGATIVE_INFINITY;
		double currentDistanceToOraclePrediction = 0.0;
		for (Context contextNeighbor : activatedNeighborsContexts) {
			if (contextNeighbor != activatedContexts.get(0)) {
				currentDistanceToOraclePrediction = contextNeighbor.getLocalModel()
						.distance(contextNeighbor.getCurrentExperiment());
				getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE", contextNeighbor.getName(),
						"" + currentDistanceToOraclePrediction)));
				if (currentDistanceToOraclePrediction > maxDistanceToOraclePredictionInNeighbors) {
					maxDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
					farestContextToOracle = contextNeighbor;
				}
			}
		}
		return new Pair<Context, Double>(farestContextToOracle, maxDistanceToOraclePredictionInNeighbors);
	}

	private Pair<Context, Double> minModelDistanceToOraclePredictionInNeighbors() {
		Context closestContextToOracle = null;
		double minDistanceToOraclePredictionInNeighbors = Double.POSITIVE_INFINITY;
		double currentDistanceToOraclePrediction = 0.0;
		for (Context contextNeighbor : activatedNeighborsContexts) {
			if (contextNeighbor != activatedContexts.get(0)) {
				currentDistanceToOraclePrediction = contextNeighbor.getLocalModel()
						.distance(contextNeighbor.getCurrentExperiment());
				getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE", contextNeighbor.getName(),
						"" + currentDistanceToOraclePrediction)));
				if (currentDistanceToOraclePrediction < minDistanceToOraclePredictionInNeighbors) {
					minDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
					closestContextToOracle = contextNeighbor;
				}
			}
		}
		return new Pair<Context, Double>(closestContextToOracle, minDistanceToOraclePredictionInNeighbors);
	}

	private Quadruplet<Context, Double, Context, Double> closestAndFarestContextsToPrediction() {
		Context farestContextToOracle = null;
		double maxDistanceToOraclePredictionInNeighbors = Double.NEGATIVE_INFINITY;
		Context closestContextToOracle = null;
		double minDistanceToOraclePredictionInNeighbors = Double.POSITIVE_INFINITY;

		double currentDistanceToOraclePrediction = 0.0;
		for (Context contextNeighbor : activatedNeighborsContexts) {
			if (contextNeighbor != activatedContexts.get(0)) {
				currentDistanceToOraclePrediction = contextNeighbor.getLocalModel()
						.distance(contextNeighbor.getCurrentExperiment());
				getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE", contextNeighbor.getName(),
						"" + currentDistanceToOraclePrediction)));
				if (currentDistanceToOraclePrediction > maxDistanceToOraclePredictionInNeighbors) {
					maxDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
					farestContextToOracle = contextNeighbor;
				}
				if (currentDistanceToOraclePrediction < minDistanceToOraclePredictionInNeighbors) {
					minDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
					closestContextToOracle = contextNeighbor;
				}
			}
		}
		return new Quadruplet<Context, Double, Context, Double>(closestContextToOracle,
				minDistanceToOraclePredictionInNeighbors, farestContextToOracle,
				maxDistanceToOraclePredictionInNeighbors);
	}

	private void selfAnalysationOfContextOnSeveralActivatedContexts() {

		Context closestContextToOracle = null;
		double minDistanceToOraclePrediction = Double.POSITIVE_INFINITY;
		double currentDistanceToOraclePrediction = 0.0;

		for (Context activatedContext : activatedContexts) {
			currentDistanceToOraclePrediction = activatedContext.getLocalModel()
					.distance(activatedContext.getCurrentExperiment());

			getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE", activatedContext.getName(),
					"" + activatedContext.getLocalModel().distance(activatedContext.getCurrentExperiment()))));
			if (currentDistanceToOraclePrediction < minDistanceToOraclePrediction) {
				minDistanceToOraclePrediction = currentDistanceToOraclePrediction;
				closestContextToOracle = activatedContext;
			}

		}

		closestContextToOracle.getLocalModel().updateModel(closestContextToOracle.getCurrentExperiment(), getAmas().data.learningSpeed,
				getAmas().data.numberOfPointsForRegression);

		activatedContextsCopyForUpdates = new ArrayList<Context>(activatedContexts);
		for (Context activatedContext : activatedContexts) {
			activatedContext.analyzeResults3(this, closestContextToOracle);

		}
		activatedContexts = activatedContextsCopyForUpdates;
	}

	private void setNearestContextAsBestContext() {
		Context nearestContext = this.getNearestContext(activatedNeighborsContexts);

		if (nearestContext != null) {
			getAmas().data.prediction = nearestContext.getActionProposal();
		} else {
			getAmas().data.prediction = 0.0;
		}

		bestContext = nearestContext;
	}

	private void setNearestGoodContextAsBestContext() {
		Context nearestContext = this.getNearestGoodContext(activatedNeighborsContexts);

		if (nearestContext != null) {
			getAmas().data.prediction = nearestContext.getActionProposal();
		} else {
			getAmas().data.prediction = 0.0;
		}

		bestContext = nearestContext;
	}

	/**
	 * Endogenous feedback.
	 */
	private void endogenousFeedback() {
		// bestContext.growRanges(this);
	}

	/**
	 * Gets the nearest good context.
	 *
	 * @param allContext the all context
	 * @return the nearest good context
	 */
	public Context getNearestGoodContext(ArrayList<Context> allContext) {
		Context nearest = null;
		for (Context c : allContext) {
			if (Math.abs((c.getActionProposal() - getAmas().data.oracleValue)) <= getAmas().data.predictionPerformance.getPerformanceIndicator()
					&& c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < getExternalDistanceToContext(nearest)) {
					nearest = c;
				}
			}
		}

		return nearest;

	}

	public Context getSmallestGoodContext(ArrayList<Context> neighbors) {
		Context smallest = null;
		double minVolume = Double.POSITIVE_INFINITY;
		double currentVolume;
		for (Context c : neighbors) {
			currentVolume = c.getVolume();
			if (Math.abs((c.getActionProposal() - getAmas().data.oracleValue)) <= getAmas().data.predictionPerformance.getPerformanceIndicator()
					&& c != newContext && !c.isDying()) {
				if (smallest == null || currentVolume < minVolume) {
					smallest = c;
				}
			}
		}

		return smallest;

	}

	/**
	 * Gets the distance to nearest good context.
	 *
	 * @param allContext the all context
	 * @return the distance to nearest good context
	 */
	private Pair<Context, Double> getNearestGoodContextWithDistance(ArrayList<Context> contextNeighbors) {
		double d = Double.MAX_VALUE;
		Context nearestGoodContext = null;
		for (Context c : contextNeighbors) {
			if (Math.abs((c.getActionProposal() - getAmas().data.oracleValue)) <= getAmas().data.predictionPerformance.getPerformanceIndicator()
					&& c != newContext && !c.isDying()) {
				if (getExternalDistanceToContext(c) < d) {
					d = getExternalDistanceToContext(c);
					nearestGoodContext = c;
				}
			}
		}
		return new Pair<Context, Double>(nearestGoodContext, d);

	}
	
	private Pair<Context, Double> getbestContextInNeighborsWithDistanceToModel(ArrayList<Context> contextNeighbors) {
		double d = Double.MAX_VALUE;
		Context bestContextInNeighbors = null;
		
		for (Context c : contextNeighbors) {
			
			double currentDistanceToOraclePrediction = c.getLocalModel()
					.distance(c.getCurrentExperiment());
			
			getEnvironment().trace(new ArrayList<String>(Arrays.asList("MODEL DISTANCE FOR FATHER CTXT", c.getName(),
					"" + c.getLocalModel().distance(c.getCurrentExperiment()))));
			
			if (currentDistanceToOraclePrediction < getAmas().data.regressionPerformance.getPerformanceIndicator()) {
				if(currentDistanceToOraclePrediction < d) {
					d = currentDistanceToOraclePrediction;
					bestContextInNeighbors = c;
				}
				

			}
			
		}
		return new Pair<Context, Double>(bestContextInNeighbors, d);

	}

	/**
	 * Gets the nearest context.
	 *
	 * @param allContext the all context
	 * @return the nearest context
	 */
	private Context getNearestContext(List<Context> contextNeighboors) {
		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Context c : contextNeighboors) {
			if (c != newContext && !c.isDying()) {
				double externalDistanceToContext = getExternalDistanceToContext(c);
				if (nearest == null || externalDistanceToContext < distanceToNearest) {
					nearest = c;
					distanceToNearest = externalDistanceToContext;
				}
			}
		}

		return nearest;
	}

	/**
	 * Gets the external distance to context.
	 *
	 * @param context the context
	 * @return the external distance to context
	 */
	private double getExternalDistanceToContext(Context context) {
		double d = 0.0;
		ArrayList<Percept> percepts = getAmas().getPercepts();
		for (Percept p : percepts) {
			if (p.isEnum()) {
				if (!(context.getRanges().get(p).getStart() == p.getValue())) {
					d += Double.MAX_VALUE;
				}
			} else {
				double min = context.getRanges().get(p).getStart();
				double max = context.getRanges().get(p).getEnd();

				if (min > p.getValue() || max < p.getValue()) {
					d += Math.min(Math.abs(p.getValue() - min), Math.abs(p.getValue() - max));
				}
			}

		}

		return d;
	}

	private double getDistanceToContext(Context context) {
		double totalDistance = 0.0;
		for (Percept p : getAmas().getPercepts()) {
			double distance = context.distance(p, p.getValue());
			if (distance > 0) {
				totalDistance += distance;
			}

		}

		return totalDistance;
	}

	/**
	 * One of proposed context was good.
	 *
	 * @return true, if successful
	 */
	private boolean oneOfProposedContextWasGood() {
		boolean b = false;
		for (Context c : activatedContexts) {
			if (getAmas().data.oracleValue - c.getActionProposal() < getAmas().data.predictionPerformance.getPerformanceIndicator()) {
				b = true;
			}
		}

		return b;

	}

	/**
	 * Creates the new context.
	 *
	 * @return the context
	 */
	private Context createNewContext() {
		getAmas().data.newContextWasCreated = true;
//		if (contexts.size() != 0) {
//			System.exit(0);
//		}
		getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		Context context;
		if (getAmas().data.firstContext) {
			context = new Context(getAmas());
			logger().debug("HEAD", "new context agent");
		} else {
			context = new Context(getAmas());
			getAmas().data.firstContext = true;
		}

		return context;
	}

	private Context createNewContext(Context bestNearestCtxt) {

		getAmas().data.newContextWasCreated = true;
		getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		Context context;
		if (getAmas().data.firstContext) {
			context = new Context(getAmas(), bestNearestCtxt);
			logger().debug("HEAD", "new context agent");
		} else {
			context = new Context(getAmas());
			getAmas().data.firstContext = true;
		}

		return context;
	}

	/**
	 * Update statistical informations.
	 */
	private void updateStatisticalInformations() {

		
		if(Math.abs(getAmas().data.oracleValue)>getAmas().data.maxPrediction) {
			getAmas().data.maxPrediction = Math.abs(getAmas().data.oracleValue);
		}
		

		getAmas().data.normalizedCriticality = getAmas().data.criticity/getAmas().data.maxPrediction;
		criticalities.addCriticality("predictionCriticality", getAmas().data.normalizedCriticality);
		
		criticalities.updateMeans();

		if (severalActivatedContexts()) {

			endogenousCriticalities.addCriticality("predictionCriticality", getAmas().data.criticity);
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapspredictionCriticality",
					Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlaps));
			endogenousCriticalities.addCriticality(
					"endogenousPredictionActivatedContextsOverlapsWorstDimInfluencepredictionCriticality",
					Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence));
			endogenousCriticalities.addCriticality(
					"endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidencepredictionCriticality",
					Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence));
			endogenousCriticalities.addCriticality(
					"endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidencepredictionCriticality",
					Math.abs(getAmas().data.oracleValue
							- getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence));
			endogenousCriticalities.addCriticality(
					"endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumepredictionCriticality",
					Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume));
			endogenousCriticalities.addCriticality(
					"endogenousPredictionActivatedContextsSharedIncompetencepredictionCriticality",
					Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence));

			endogenousCriticalities.updateMeans();

		}

		getAmas().data.predictionPerformance.update(criticalities.getCriticalityMean("predictionCriticality"));
		if (criticalities.getCriticalityMean("distanceToRegression") != null) {
			getAmas().data.regressionPerformance.update(criticalities.getCriticalityMean("distanceToRegression"));
		}

		// getAmas().data.mappingPerformance.update(?);
	}

	/**
	 * Gets the contexts.
	 *
	 * @return the contexts
	 */
	public ArrayList<Context> getActivatedContexts() {
		return activatedContexts;
	}

	/**
	 * Sets the contexts.
	 *
	 * @param contexts the new contexts
	 */
	public void setActivatesContexts(ArrayList<Context> contexts) {
		this.activatedContexts = contexts;
	}

	/**
	 * Select best context.
	 */
	private void selectBestContext() {
		if(activatedContexts != null && !activatedContexts.isEmpty()) {
			Context bc;
	
			bc = activatedContexts.get(0);
			double currentConfidence = bc.getConfidence();

			for (Context context : activatedContexts) {
				if (context.getConfidence() > currentConfidence) {
					bc = context;
					currentConfidence = bc.getConfidence();
				}
			}
			bestContext = bc;
		} else {
			bestContext = null;
		}
	}

	private void selectBestContextWithDistanceToModel() {

		Context bc;

		bc = activatedContexts.get(0);
		double distanceToModel = bc.getLocalModel().distance(bc.getCurrentExperiment());
		double currentDistanceToModel;

		for (Context context : activatedContexts) {

			currentDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			if (currentDistanceToModel < distanceToModel) {
				bc = context;
				distanceToModel = currentDistanceToModel;
			}
		}
		bestContext = bc;
	}

	/**
	 * Gets the best context.
	 *
	 * @return the best context
	 */
	public Context getBestContext() {
		return bestContext;
	}

	/**
	 * Sets the best context.
	 *
	 * @param bestContext the new best context
	 */
	public void setBestContext(Context bestContext) {
		this.bestContext = bestContext;
	}

	/**
	 * Gets the criticity.
	 *
	 * @return the criticity
	 */
	public double getCriticity() {
		return getAmas().data.criticity;
	}
	
	public double getNormalizedCriticicality() {
		return getAmas().data.normalizedCriticality;
	}

	/**
	 * Gets the no best context.
	 *
	 * @return the no best context
	 */
	public boolean getNoBestContext() {
		return getAmas().data.noBestContext;
	}

	/**
	 * Gets the criticity.
	 *
	 * @param context the context
	 * @return the criticity
	 */
	public double getCriticity(Context context) {
		return Math.abs(getAmas().data.oracleValue - context.getActionProposal());
	}

	/**
	 * Sets the criticity.
	 *
	 * @param criticity the new criticity
	 */
	public void setCriticity(double criticity) {
		this.getAmas().data.criticity = criticity;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public double getAction() {
		return getAmas().data.prediction;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(double action) {
		this.getAmas().data.prediction = action;
	}

	/**
	 * Gets the last used context.
	 *
	 * @return the last used context
	 */
	public Context getLastUsedContext() {
		return lastUsedContext;
	}

	/**
	 * Sets the last used context.
	 *
	 * @param lastUsedContext the new last used context
	 */
	public void setLastUsedContext(Context lastUsedContext) {
		this.lastUsedContext = lastUsedContext;
	}

	/**
	 * Checks if is no creation.
	 *
	 * @return true, if is no creation
	 */
	public boolean isNoCreation() {
		return getAmas().data.noCreation;
	}

	/**
	 * Sets the no creation.
	 *
	 * @param noCreation the new no creation
	 */
	public void setNoCreation(boolean noCreation) {
		this.getAmas().data.noCreation = noCreation;
	}

	/**
	 * Gets the oracle.
	 *
	 * @return the oracle
	 */
//	public BlackBoxAgent getOracle() {
//		return oracle;
//	}

	/**
	 * Sets the oracle.
	 *
	 * @param oracle the new oracle
	 */
//	public void setOracle(BlackBoxAgent oracle) {
//		this.oracle = oracle;
//	}

	/**
	 * Gets the oracle value.
	 *
	 * @return the oracle value
	 */
	public double getOracleValue() {
		return getAmas().data.oracleValue;
	}

	/**
	 * Sets the oracle value.
	 *
	 * @param oracleValue the new oracle value
	 */
	public void setOracleValue(double oracleValue) {
		this.getAmas().data.oracleValue = oracleValue;
	}

	/**
	 * Gets the old oracle value.
	 *
	 * @return the old oracle value
	 */
	public double getOldOracleValue() {
		return getAmas().data.oldOracleValue;
	}

	/**
	 * Sets the old oracle value.
	 *
	 * @param oldOracleValue the new old oracle value
	 */
	public void setOldOracleValue(double oldOracleValue) {
		this.getAmas().data.oldOracleValue = oldOracleValue;
	}

	/**
	 * Gets the old criticity.
	 *
	 * @return the old criticity
	 */
	public double getOldCriticity() {
		return getAmas().data.oldCriticity;
	}

	/**
	 * Sets the old criticity.
	 *
	 * @param oldCriticity the new old criticity
	 */
	public void setOldCriticity(double oldCriticity) {
		this.getAmas().data.oldCriticity = oldCriticity;
	}

	/**
	 * Gets the error allowed.
	 *
	 * @return the error allowed
	 */
	public double getErrorAllowed() {
		return getAmas().data.predictionPerformance.getPerformanceIndicator();
	}

	/**
	 * Sets the error allowed.
	 *
	 * @param errorAllowed the new error allowed
	 */
	public void setErrorAllowed(double errorAllowed) {
		getAmas().data.predictionPerformance.setPerformanceIndicator(errorAllowed);
	}

	/**
	 * Gets the average prediction criticity.
	 *
	 * @return the average prediction criticity
	 */
	public double getAveragePredictionCriticity() {
		Double mean = criticalities.getCriticalityMean("predictionCriticality");
		if(mean == null) {
			return 0.0;
		}
		else {
			return mean;
		}
		
		
	}

	public double getAveragePredictionCriticityCopy() {
		return endogenousCriticalities.getCriticalityMean("predictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsOverlaps() {
		return endogenousCriticalities
				.getCriticalityMean("endogenousPredictionActivatedContextsOverlapspredictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluence() {
		return endogenousCriticalities.getCriticalityMean(
				"endogenousPredictionActivatedContextsOverlapsWorstDimInfluencepredictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsInfluenceWithoutConfidence() {
		return endogenousCriticalities.getCriticalityMean(
				"endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidencepredictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() {
		return endogenousCriticalities.getCriticalityMean(
				"endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidencepredictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithVolume() {
		return endogenousCriticalities.getCriticalityMean(
				"endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumepredictionCriticality");
	}

	public double getAveragePredictionCriticityEndoActivatedContextsSharedIncompetence() {
		return endogenousCriticalities
				.getCriticalityMean("endogenousPredictionActivatedContextsSharedIncompetencepredictionCriticality");
	}

	/**
	 * Sets the average prediction criticity.
	 *
	 * @param averagePredictionCriticity the new average prediction criticity
	 */
//	public void setAveragePredictionCriticity(double averagePredictionCriticity) {
//		this.averagePredictionCriticity = averagePredictionCriticity;
//	}

	/**
	 * Gets the average prediction criticity weight.
	 *
	 * @return the average prediction criticity weight
	 */
	public int getAveragePredictionCriticityWeight() {
		return getAmas().data.averagePredictionCriticityWeight;
	}

	/**
	 * Sets the average prediction criticity weight.
	 *
	 * @param averagePredictionCriticityWeight the new average prediction criticity
	 *                                         weight
	 */
	public void setAveragePredictionCriticityWeight(int averagePredictionCriticityWeight) {
		this.getAmas().data.averagePredictionCriticityWeight = averagePredictionCriticityWeight;
	}

	/**
	 * Gets the new context.
	 *
	 * @return the new context
	 */
	public Context getNewContext() {
		return newContext;
	}

	/**
	 * Sets the new context.
	 *
	 * @param newContext the new new context
	 */
	public void setNewContext(Context newContext) {
		this.newContext = newContext;
	}

	/**
	 * Change oracle connection.
	 */
	public void changeOracleConnection() {
		getAmas().data.useOracle = !getAmas().data.useOracle;
	}

	/**
	 * Gets the function selected.
	 *
	 * @return the function selected
	 */
	public String getFunctionSelected() {
		return functionSelected;
	}

	/**
	 * Sets the function selected.
	 *
	 * @param functionSelected the new function selected
	 */
	public void setFunctionSelected(String functionSelected) {
		this.functionSelected = functionSelected;
	}

	/**
	 * Gets the n propositions received.
	 *
	 * @return the n propositions received
	 */
	public int getnPropositionsReceived() {
		return getAmas().data.nPropositionsReceived;
	}

	/**
	 * Checks if is new context was created.
	 *
	 * @return true, if is new context was created
	 */
	public boolean isNewContextWasCreated() {
		return getAmas().data.newContextWasCreated;
	}

	/**
	 * Checks if is context from proposition was selected.
	 *
	 * @return true, if is context from proposition was selected
	 */
	public boolean isContextFromPropositionWasSelected() {
		return getAmas().data.contextFromPropositionWasSelected;
	}

	/**
	 * Sets the context from proposition was selected.
	 *
	 * @param contextFromPropositionWasSelected the new context from proposition was
	 *                                          selected
	 */
	public void setContextFromPropositionWasSelected(boolean contextFromPropositionWasSelected) {
		this.getAmas().data.contextFromPropositionWasSelected = contextFromPropositionWasSelected;
	}

	/**
	 * Gets the prediction.
	 *
	 * @return the prediction
	 */
	public double getPrediction() {
		return getAmas().data.prediction;
	}

	public Double getEndogenousPredictionActivatedContextsOverlaps() {
		return getAmas().data.endogenousPredictionActivatedContextsOverlaps;
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluence() {
		return getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence;
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence() {
		return getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence;
	}

	public Double getendogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() {
		return getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence;
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume() {
		return getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume;
	}

	public Double getEndogenousPredictionActivatedContextsSharedIncompetence() {
		return getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence;
	}

	public Double getEndogenousPredictionNContextsByInfluence() {
		return getAmas().data.endogenousPredictionNContextsByInfluence;
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlaps);
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence);
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidenceCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence);
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidenceCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence);
	}

	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumeCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume);
	}

	public Double getEndogenousPredictionActivatedContextsSharedIncompetenceCriticity() {
		return Math.abs(getAmas().data.oracleValue - getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence);
	}

	/**
	 * Sets the prediction.
	 *
	 * @param prediction the new prediction
	 */
	public void setPrediction(double prediction) {
		this.getAmas().data.prediction = prediction;
	}

	public ArrayList<Context> getPartiallyActivatedContexts(Percept pct) {
		return partiallyActivatedContexts.get(pct);
	}

	public ArrayList<Context> getPartiallyActivatedNeighborContexts(Percept pct) {
		return partialNeighborContexts.get(pct);
	}

	public HashMap<Percept, ContextPair<Context, Context>> getRequestSurroundings() {
		return requestSurroundings;
	}

	public boolean requestSurroundingContains(Context ctxt) {

		for (Percept pct : requestSurroundings.keySet()) {

			if (requestSurroundings.get(pct).contains(ctxt)) {
				return true;
			}
		}
		return false;

	}

	public ArrayList<Context> getContextsInCompetition() {
		return contextsInCompetition;
	}

	public Head clone() throws CloneNotSupportedException {
		return (Head) super.clone();
	}

	public void addRequestNeighbor(Context ctxt) {
		if (!activatedNeighborsContexts.contains(ctxt)) {

			activatedNeighborsContexts.add(ctxt);
		}
	}

	public void addActivatedContext(Context ctxt) {
		if (!activatedContexts.contains(ctxt)) {
			activatedContexts.add(ctxt);


			for (Context cxt : activatedContexts) {

			}
		}
	}

	public void addActivatedContextCopy(Context ctxt) {
		if (!activatedContextsCopyForUpdates.contains(ctxt)) {
			activatedContextsCopyForUpdates.add(ctxt);


			for (Context cxt : activatedContextsCopyForUpdates) {

			}
		}
	}

	public void removeActivatedContext(Context ctxt) {
		if (activatedContexts.contains(ctxt)) {
			activatedContexts.remove(ctxt);
		}
	}

	public void removeActivatedContextCopy(Context ctxt) {
		if (activatedContextsCopyForUpdates.contains(ctxt)) {
			activatedContextsCopyForUpdates.remove(ctxt);
		}
	}

	public void removeRequestNeighbor(Context ctxt) {
		if (activatedNeighborsContexts.contains(ctxt)) {

			activatedNeighborsContexts.remove(ctxt);
		}
	}

	public ArrayList<Context> getActivatedNeighborsContexts() {
		return activatedNeighborsContexts;
	}
	
	public void setActivatedNeighborsContexts(ArrayList<Context> neighbors) {
		activatedNeighborsContexts = neighbors;
	}


	public ArrayList<Context> getContextNeighborsByInfluence() {
		return contextsNeighborsByInfluence;
	}

	public void displayActivatedNeighborsContexts() {
		for (Context ctxt : activatedNeighborsContexts) {

		}
	}

	public void clearActivatedNeighborsContexts() {
		activatedNeighborsContexts.clear();
	}

	public void clearContextdNeighborsByInfluence() {
		contextsNeighborsByInfluence.clear();
	}

	public void clearAllUseableContextLists() {
		activatedContexts.clear();
		activatedNeighborsContexts.clear();
		contextsNeighborsByInfluence.clear();
		for (Percept pct : getAmas().getPercepts()) {
			partiallyActivatedContexts.get(pct).clear();
			partiallyActivatedContextInNeighbors.get(pct).clear();
			partialNeighborContexts.get(pct).clear();
		}
	}

	public Double getMaxRadiusForContextCreation(Percept pct) {
		double maxRadius = pct.getRadiusContextForCreation();
		double currentRadius;

		// for(Context ctxt:partialNeighborContexts.get(pct)) {
		for (Context ctxt : activatedNeighborsContexts) {

			currentRadius = ctxt.getRanges().get(pct).distance(pct.getValue());

			if (currentRadius < maxRadius && currentRadius > 0) {
				maxRadius = currentRadius;
			}
		}

		return maxRadius;

	}

	public Pair<Double, Double> getMaxRadiusesForContextCreation(Percept pct) {
//		Pair<Double, Double> maxRadiuses = new Pair<Double, Double>(
//				Math.min(pct.getRadiusContextForCreation(), Math.abs(pct.getMin() - pct.getValue())),
//				Math.min(pct.getRadiusContextForCreation(), Math.abs(pct.getMax() - pct.getValue())));
		
		Pair<Double, Double> maxRadiuses = new Pair<Double, Double>(
				pct.getRadiusContextForCreation(),
				pct.getRadiusContextForCreation());
		
		//return maxRadiuses;

		// Pair<Double,Double> maxRadiuses = new
		// Pair<Double,Double>(pct.getRadiusContextForCreation(),pct.getRadiusContextForCreation());
		// Pair<Double,Double> maxRadiuses = new
		// Pair<Double,Double>(Math.abs(pct.getMin()-
		// pct.getValue()),Math.abs(pct.getMax()-pct.getValue()));

		double currentStartRadius;
		double currentEndRadius;

		// for(Context ctxt:partialNeighborContexts.get(pct)) {
		for (Context ctxt : activatedNeighborsContexts) {

			if (ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				currentEndRadius = ctxt.getRanges().get(pct).distance(pct.getValue());
				if (currentEndRadius < maxRadiuses.getB() && currentEndRadius > 0) {
					maxRadiuses.setB(currentEndRadius);
				}
			}

			if (ctxt.getRanges().get(pct).centerDistance(pct.getValue()) > 0) {
				// Start radius
				currentStartRadius = ctxt.getRanges().get(pct).distance(pct.getValue());
				if (currentStartRadius < maxRadiuses.getA() && currentStartRadius > 0) {
					maxRadiuses.setA(currentStartRadius);
				}
			}

		}

		return maxRadiuses;

	}

	public double getAverageSpatialCriticality() {
		Double mean = criticalities.getCriticalityMean("spatialCriticality");
		if(mean == null) {
			return 0.0;
		}else {
			return mean;
		}
	}

	public void setBadCurrentCriticalityPrediction() {
		getAmas().data.currentCriticalityPrediction = 1;
	}

	public void setBadCurrentCriticalityConfidence() {
		getAmas().data.currentCriticalityConfidence = 1;
	}

	public void setBadCurrentCriticalityMapping() {
		getAmas().data.currentCriticalityMapping = 1;
	}

	public double fact(double n) {

		if (n == 0) {
			return 1;
		} else {
			return n * fact(n - 1);
		}
	}

	public double combinationsWithoutRepetitions(double n) {
		if (n == 2)
			return 1;
		else
			return fact(n) / (2 * (fact(n) - 2));
	}

	public double getMappingErrorAllowed() {
		return getAmas().data.mappingPerformance.getPerformanceIndicator();
	}

	public double getDistanceToRegression() {
		return getAmas().data.distanceToRegression;
	}

	public double getDistanceToRegressionAllowed() {
		return getAmas().data.regressionPerformance.getPerformanceIndicator();
	}
	
	
	public boolean isActiveLearning() {
		return isSelfRequest();
	}
	
	
	
	public HashMap<String, Double> getSelfRequest(){
		getEnvironment().trace(new ArrayList<String>(Arrays.asList("FUTURE ACTIVE LEARNING", ""+endogenousRequests.element())));
		return endogenousRequests.poll().getRequest();
	}
	
	public void deleteRequest(Context ctxt) {
		
	}
	
	public boolean isSelfRequest(){
		return endogenousRequests.size()>0;
	}
	
	public void addSelfRequest(HashMap<String, Double> request, int priority, Context ctxt){		
		
		addEndogenousRequest(new EndogenousRequest(request, priority,new ArrayList<Context>(Arrays.asList(ctxt))));
	}
	
	public void addEndogenousRequest(EndogenousRequest request) {
		
		boolean existingRequestTest = false;
		
		if(request.getAskingContexts().size()>1) {
			
			Iterator<EndogenousRequest> itr = endogenousRequests.iterator();
			while(!existingRequestTest && itr.hasNext()) {
				
				EndogenousRequest currentRequest = itr.next();
				
				if(currentRequest.getAskingContexts().size()>1) {
						
					existingRequestTest = existingRequestTest || currentRequest.testIfContextsAlreadyAsked(request.getAskingContexts()); 
				}
				
			}
			if(!existingRequestTest) {
				for(Context ctxt : request.getAskingContexts()) {
					ctxt.addWaitingRequest(request);
				}
				endogenousRequests.add(request);
			}
		}else {
			endogenousRequests.add(request);
		}
		
		
		
	}
	
	public void updatePartiallyActivatedNeighbors() {
		
		for(Percept pct: getAmas().getPercepts()) {
			
			pct.computeContextNeighborsValidity(activatedNeighborsContexts);
			
		}
		
		for(Context ctxt : activatedNeighborsContexts) {
			
			ctxt.computeContextNeighborsValidity();
			
		}
	}
	
	public boolean isVoid(HashMap<String, Double> request) {
		boolean test;
		
		for(Context ctxt : activatedNeighborsContexts) {
			
			test = true;
			for(Percept pct : getAmas().getPercepts()) {
				test = test && ctxt.getRanges().get(pct).contains2(request.get(pct.getName()));
			}
			if(test) {
				return false;
			}
		}
		return true;
		
		
	}
	
	public double getAverageRegressionPerformanceIndicator() {
		
		
		if(activatedNeighborsContexts.size()>1) {
			double meanRegressionPerformanceIndicator = 0.0;
			
			for(Context ctxt : activatedNeighborsContexts) {
				

				if(ctxt.regressionPerformance != null) {
					meanRegressionPerformanceIndicator += ctxt.regressionPerformance.performanceIndicator;
				}
				
				
			}
			
			return meanRegressionPerformanceIndicator/activatedNeighborsContexts.size();
		}
		else {
			return 200;
		}
		
	}
	

	// -----------------
	// AMOEBAonAMAK ---
	// -----------------

	public void proposition(Context c) {

		activatedContexts.add(c);
	}
	
	@Override
	protected void onInitialization() {
		super.onInitialization();
		getAmas().data.maxConfidence = Double.NEGATIVE_INFINITY;
		getAmas().data.minConfidence = Double.POSITIVE_INFINITY;

		for (Percept pct : getAmas().getPercepts()) {
			addPercept(pct);
		}

		// getAmas().data.mappingPerformance.setPerformanceIndicator(world.getMappingErrorAllowed());//
		// Math.pow(world.getMappingErrorAllowed(),
		// world.getScheduler().getPercepts().size());

		criticalities = new Criticalities(getAmas().data.numberOfCriticityValuesForAverage);
		endogenousCriticalities = new Criticalities(getAmas().data.numberOfCriticityValuesForAverageforVizualisation);
	}

	public void addPercept(Percept pct) {
		partiallyActivatedContextInNeighbors.put(pct, new ArrayList<Context>());
		partiallyActivatedContexts.put(pct, new ArrayList<Context>());
		partialNeighborContexts.put(pct, new ArrayList<Context>());
		requestSurroundings.put(pct, new ContextPair<Context, Context>(null, null));
		sharedIncompetenceContextPairs.put(pct, new ContextPair<Context, Context>(null, null));
	}
}
