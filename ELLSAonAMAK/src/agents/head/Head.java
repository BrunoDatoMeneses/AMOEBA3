package agents.head;

import java.util.*;

import agents.EllsaAgent;
import agents.context.Context;
import agents.context.CustomComparator;
import agents.context.Experiment;
import agents.context.VOID;
import agents.context.localModel.LocalModelMillerRegression;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.World;
import ncs.NCS;
import utils.Pair;
import utils.TRACE_LEVEL;

/**
 * The Class Head.
 */
public class Head extends EllsaAgent {

	// MEMBERS ---------------------
	
	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;

	//HashMap<Percept, Double> currentSituation = new HashMap<Percept, Double>();

	public Criticalities criticalities;
	//public Criticalities endogenousCriticalities;

	public ArrayList<Context> activatedContexts = new ArrayList<Context>();
	public ArrayList<Context> activatedNeighborsContexts = new ArrayList<Context>();
	public ArrayList<Context> activatedSubNeighborsContexts = new ArrayList<Context>();
	
	public Double meanNeighborhoodVolume;
	public HashMap<Percept, Double> meanNeighborhoodRaduises;
	public HashMap<Percept, Double> meanNeighborhoodStartIncrements;
	public HashMap<Percept, Double> meanNeighborhoodEndIncrements;

	public HashMap<Percept, Double> minNeighborhoodRaduises;
	public Double minMeanNeighborhoodRaduises = null;
	public Double minMeanNeighborhoodStartIncrements = null;
	public Double minMeanNeighborhoodEndIncrements = null;
	
	public Double minNeighborhoodRadius = null;
	public Double minNeighborhoodStartIncrement = null;
	public Double minNeighborhoodEndIncrement = null;
	
	public EndogenousRequest lastEndogenousRequest = null;
	public EndogenousRequest currentEndogenousRequest = null;

	static final int NEIGH_VOID_CYCLE_START = 0;


	public Queue<EndogenousRequest> endogenousRequests = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
		   public int compare(EndogenousRequest r1, EndogenousRequest r2) {
			      return r2.getPriority().compareTo(r1.getPriority());
			   }
			});

	public Queue<EndogenousRequest> endogenousChildRequests = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
		   public int compare(EndogenousRequest r1, EndogenousRequest r2) {
			      return r2.getPriority().compareTo(r1.getPriority());
			   }
			});

	public Queue<EndogenousRequest> endogenousDreamRequests = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
		public int compare(EndogenousRequest r1, EndogenousRequest r2) {
			return r2.getPriority().compareTo(r1.getPriority());
		}
	});

	public Queue<EndogenousRequest> endogenousSubRequests = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
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

	public Head(ELLSA ellsa) {
		super(ellsa);
		

	}

	/**
	 * The core method of the head agent. Manage the whole behavior, and call method
	 * from context agents when needed.
	 */
	@Override
	public void onAct() {


		getAmas().data.executionTimes[6]=System.currentTimeMillis();
		onActInit();
		if(getAmas().data.isSubPercepts){
			playWithoutOracleAndAllPercets();
		}
		else if (getAmas().data.useOracle) {
			playWithOracle();
		} else {
			playWithoutOracle();
		}

		if(bestContext != null){
			bestContext.isBest = true;
			//bestContext.isInNeighborhood = true;
		}
		getAmas().data.executionTimes[6]=System.currentTimeMillis()- getAmas().data.executionTimes[6];

		getAmas().data.executionTimes[7]=System.currentTimeMillis();
		testIfrequest();
		updateStatisticalInformations(); // regarder dans le détail, possible que ce pas trop utile
		newContext = null;
		getAmas().data.executionTimes[7]=System.currentTimeMillis()- getAmas().data.executionTimes[7];

	}

	private void onActInit() {

		if(getAmas().getCycle()==getAmas().data.PARAM_bootstrapCycle+1){
			getAmas().data.minMaxPerceptsStatesAfterBoostrap = new HashMap<>();
			for(Percept pct : getAmas().getPercepts()){
				getAmas().data.minMaxPerceptsStatesAfterBoostrap.put(pct,new Pair<>(pct.getMin(),pct.getMax()));
			}
		}

		if(getAmas().data.useOracle){
			getAmas().data.neighborsCounts += activatedNeighborsContexts.size();
			if(activatedNeighborsContexts.size()>0){
				getAmas().data.lastNeihborsCount = activatedNeighborsContexts.size();
			}else{
				getAmas().data.lastNeihborsCount=0;
			}
		}

		if(getAmas().getCycle()%1000 == 0) {
			getEnvironment().trace(TRACE_LEVEL.ERROR, new ArrayList<String>(Arrays.asList("")));
		}
		meanNeighborhoodVolume = null;
		meanNeighborhoodRaduises = null;
		minNeighborhoodRaduises = null;
		meanNeighborhoodEndIncrements = null;
		meanNeighborhoodStartIncrements = null;

		minMeanNeighborhoodRaduises = Double.POSITIVE_INFINITY;
		minMeanNeighborhoodStartIncrements = Double.POSITIVE_INFINITY;
		minMeanNeighborhoodEndIncrements = Double.POSITIVE_INFINITY;

		minNeighborhoodRadius = Double.POSITIVE_INFINITY;
		minNeighborhoodStartIncrement = Double.POSITIVE_INFINITY;
		minNeighborhoodEndIncrement = Double.POSITIVE_INFINITY;

		getAmas().data.currentCriticalityPrediction = 0;
		getAmas().data.currentCriticalityMapping = 0;
		getAmas().data.currentCriticalityConfidence = 0;

		/*for (Percept pct : getAmas().getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}*/

		getAmas().data.nPropositionsReceived = activatedContexts.size();
		getAmas().data.newContextWasCreated = false;
		//setContextFromPropositionWasSelected(false);
		getAmas().data.oldOracleValue = getAmas().data.oracleValue;
		getAmas().data.oracleValue = getAmas().getPerceptions("oracle");
		setAverageRegressionPerformanceIndicator(); //TODO not working ? Seems to works after all

		/* The head memorize last used context agent */
		lastUsedContext = bestContext;
		bestContext = null;

		/* Neighbors */





		for(Context subCtxt : activatedSubNeighborsContexts){
			subCtxt.isInSubNeighborhood = true;
		}

		if(activatedNeighborsContexts.size()>0) {

			updateNeighborhoodMeanValues();
		}
	}

	private void updateNeighborhoodMeanValues() {
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", ""+activatedNeighborsContexts)));

		double neighborhoodVolumesSum = 0;
		HashMap<Percept,Double> neighborhoodRangesSums = new HashMap<Percept,Double>();
		HashMap<Percept,Double> neighborhoodStartIncrementSums = new HashMap<Percept,Double>();
		HashMap<Percept,Double> neighborhoodEndIncrementSums = new HashMap<Percept,Double>();


		for (Percept pct : getAmas().getPercepts()) {
			neighborhoodRangesSums.put(pct, 0.0);
			neighborhoodStartIncrementSums.put(pct, 0.0);
			neighborhoodEndIncrementSums.put(pct, 0.0);
		}

		for (Context ctxt : activatedNeighborsContexts) {

			ctxt.isInNeighborhood = true;
			ctxt.restructured = false;
			ctxt.modified = false;
			ctxt.fusionned = false;
			neighborhoodVolumesSum += ctxt.getVolume();
			/*if(activatedContexts.contains(ctxt)){
				ctxt.isActivated = true;
			}*/


			for (Percept pct : ctxt.getRanges().keySet()) {
				Double oldRadiusSum = neighborhoodRangesSums.get(pct);
				Double oldStartIncrSum = neighborhoodStartIncrementSums.get(pct);
				Double oldEndIncrSum = neighborhoodEndIncrementSums.get(pct);

				if( ctxt.getRanges().get(pct).getRadius() < minNeighborhoodRadius) {
					minNeighborhoodRadius = ctxt.getRanges().get(pct).getRadius();
				}

				if( ctxt.getRanges().get(pct).getStartIncrement() < minNeighborhoodStartIncrement) {
					minNeighborhoodStartIncrement = ctxt.getRanges().get(pct).getStartIncrement();
				}

				if( ctxt.getRanges().get(pct).getEndIncrement() < minNeighborhoodEndIncrement) {
					minNeighborhoodEndIncrement = ctxt.getRanges().get(pct).getEndIncrement();
				}




				neighborhoodRangesSums.put(pct, oldRadiusSum + ctxt.getRanges().get(pct).getRadius());
				neighborhoodStartIncrementSums.put(pct, oldStartIncrSum + ctxt.getRanges().get(pct).getStartIncrement());
				neighborhoodEndIncrementSums.put(pct, oldEndIncrSum + ctxt.getRanges().get(pct).getEndIncrement());
			}


		}

		meanNeighborhoodVolume = neighborhoodVolumesSum / activatedNeighborsContexts.size();

		meanNeighborhoodRaduises = new HashMap<Percept, Double>();
		meanNeighborhoodStartIncrements = new HashMap<Percept, Double>();
		meanNeighborhoodEndIncrements = new HashMap<Percept, Double>();
		minNeighborhoodRaduises = new HashMap<>();


		for (Percept pct : getAmas().getPercepts()) {


			double meanRadius = neighborhoodRangesSums.get(pct)/activatedNeighborsContexts.size();
			double meanStartIncrement = neighborhoodStartIncrementSums.get(pct)/activatedNeighborsContexts.size();
			double meanEndIncrement = neighborhoodEndIncrementSums.get(pct)/activatedNeighborsContexts.size();
			meanNeighborhoodRaduises.put(pct, meanRadius);
			meanNeighborhoodStartIncrements.put(pct, meanStartIncrement);
			meanNeighborhoodEndIncrements.put(pct, meanEndIncrement);

			if(meanRadius < minMeanNeighborhoodRaduises) {
				minMeanNeighborhoodRaduises = meanRadius;
			}
			if(meanStartIncrement < minMeanNeighborhoodStartIncrements) {
				minMeanNeighborhoodStartIncrements = meanStartIncrement;
			}
			if(meanEndIncrement < minMeanNeighborhoodEndIncrements) {
				minMeanNeighborhoodEndIncrements = meanEndIncrement;
			}


		}


		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "size", ""+activatedNeighborsContexts.size())));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "meanNeighborhoodVolume", ""+meanNeighborhoodVolume)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "meanNeighborhoodRaduises", ""+meanNeighborhoodRaduises)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "meanNeighborhoodStartIncrements", ""+meanNeighborhoodStartIncrements)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "meanNeighborhoodEndIncrements", ""+meanNeighborhoodEndIncrements)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "minMeanNeighborhoodRaduises", ""+minMeanNeighborhoodRaduises)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "minMeanNeighborhoodStartIncrements", ""+minMeanNeighborhoodStartIncrements)));
		getAmas().getEnvironment()
		.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("NEIGHBORDBOOD", "minMeanNeighborhoodEndIncrements", ""+minMeanNeighborhoodEndIncrements)));
	}

	public void testIfrequest() {

		boolean testIfSelfRequest = isSelfRequest();
		boolean testIfChildSelfRequest = isSelfChildRequest();
		boolean testIfDreamRequest = isDreamRequest();
		boolean testIfSubRequest = isSubRequest();
		/*if((testIfSelfRequest||testIfChildSelfRequest||testIfDreamRequest||testIfSubRequest) && getAmas().data.isSelfLearning){
			getAmas().data.selfLearning = true;
		}
		if((testIfSelfRequest||testIfChildSelfRequest||testIfDreamRequest||testIfSubRequest) && getAmas().data.isActiveLearning) {
			getAmas().data.activeLearning = true;
		}*/
		if((testIfSelfRequest||testIfChildSelfRequest||testIfDreamRequest) && getAmas().data.PARAM_isSelfLearning){
			getAmas().data.selfLearning = true;
		}
		if((testIfSelfRequest||testIfChildSelfRequest||testIfDreamRequest) && getAmas().data.PARAM_isActiveLearning) {
			getAmas().data.activeLearning = true;
		}
	}

	private void playWithOracle() {
		
		
		/*if(getAmas().isReinforcement()) {
			reinforcementWithOracle();
		}*/

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("\n\n")));

		if(getAmas().getCycle() % 50 == 0){
			if(lastEndogenousRequest != null){
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITH ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
			}else{
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITH ORACLE" + " " + getWaitingEndogenousRequests())));
			}
		}
		if(lastEndogenousRequest != null){
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITH ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
		}else{
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITH ORACLE" + " " + getWaitingEndogenousRequests())));
		}

		updateBestContextWithOracle();
		updateCriticalityWithOracle();

		/* If we have a bestcontext, send a selection message to it */
		if (bestContext != null) {
			bestContext.notifySelection();
			getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList(bestContext.getName(),
					"*********************************************************************************************************** BEST CONTEXT")));
		}

		updateNeighborContextLastPredictions();
		selfAnalysationOfContexts4();

		if(getAmas().getCycle()>getAmas().data.PARAM_bootstrapCycle){
			allNCSDetectionsWithOracle();
		}

		updatePerformanceIndicators();



		
		
	}

	private void updateCriticalityWithOracle() {
		/* Compute the criticity. Will be used by context agents. */
		getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction)/ Math.abs(getAmas().data.oracleValue);
		if(bestContext != null){
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(bestContext.getName(),
					"Best Context Cricicality",""+getAmas().data.criticity)));
		}

	}

	private void updateCriticalityWithoutOracle() {

		if(getAmas().studiedSystem != null){
			/* Compute the criticity. Will be used by context agents. */
			Double[] request = new Double[getAmas().getPercepts().size()];
			for(int i=0;i<getAmas().getPercepts().size();i++){
				request[i]=getAmas().getPercepts().get(i).getValue();
			}

			getAmas().data.oracleValue = ((F_N_Manager)(getAmas().studiedSystem)).model(request);
			getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction)/ Math.abs(getAmas().data.oracleValue);
			//System.out.println("A " + getAmas().getCycle() + "\t\t\t" + getAmas().data.oracleValue + "\t\t\t" + getAmas().data.prediction + "\t\t\t" + getAmas().data.criticity);
			if(bestContext != null){
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(bestContext.getName(),
						"Best Context Cricicality",""+getAmas().data.criticity)));
			}
		}



	}

	public StudiedSystem getStudiedSystem(){
		return getAmas().studiedSystem;
	}

	private void updateBestContextWithOracle() {
		if (activatedContexts.size() > 0) {
            getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithOracle","withActivatedContexts",activatedContexts.size());

			selectBestContextWithOracleWeighted(activatedContexts, getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PREDICTION,
					getAmas().data.PARAM_LEARNING_WEIGHT_CONFIDENCE,getAmas().data.PARAM_LEARNING_WEIGHT_VOLUME,getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PERCEPTIONS);

		} else if (activatedNeighborsContexts.size()>0){
            getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithOracle","withActivatedNeighborContexts",activatedNeighborsContexts.size());

            selectBestContextWithOracleWeighted(activatedNeighborsContexts, getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PREDICTION,
					getAmas().data.PARAM_LEARNING_WEIGHT_CONFIDENCE,getAmas().data.PARAM_LEARNING_WEIGHT_VOLUME,getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PERCEPTIONS);
		} else if (getAmas().getContexts().size()>0 && getAmas().data.PARAM_NCS_isAllContextSearchAllowedForLearning){
            getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithOracle","withAllContexts",getAmas().getContexts().size());

            selectBestContextWithOracleWeighted(getAmas().getContexts(), getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PREDICTION,
					getAmas().data.PARAM_LEARNING_WEIGHT_CONFIDENCE,getAmas().data.PARAM_LEARNING_WEIGHT_VOLUME,getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PERCEPTIONS);
        }

		if (bestContext != null) {
			//setContextFromPropositionWasSelected(true);
			getAmas().data.prediction = bestContext.getActionProposal();

		} else {
                setPredictionWithoutContextAgent();
		}

        /*if (activatedContexts.size() > 0) {
            //selectBestContextWithConfidenceAndVolume(); // using highest confidence and volume
            //selectBestContextWithDistanceToModelAndVolume(); // using closest distance and volume
            //selectBestContextWithDistanceToModel();
            //selectBestContextWithDistanceToModelAndConfidance();
            selectBestContextWithDistanceToModelConfidanceAndVolume();

        } else {
            bestContext = lastUsedContext;
        }

        if (bestContext != null) {
            setContextFromPropositionWasSelected(true);
            getAmas().data.prediction = bestContext.getActionProposal();

        } else { // happens only at the beginning
            setNearestContextAsBestContext();
        }*/
	}

	/*private void reinforcementWithOracle() {
		int nb=0;
		Double meanNeighborsLastPredictions = null;

		ArrayList<Context> usedNeighbors = new ArrayList<Context>();

		if(activatedNeighborsContexts.size()>0) {

			meanNeighborsLastPredictions = 0.0;
			for (Context ctxt : activatedNeighborsContexts) {



				if(ctxt.lastPrediction != null) {
					usedNeighbors.add(ctxt);
					meanNeighborsLastPredictions += ctxt.lastPrediction;
					nb++;
				}
			}
			if(nb>0) {
				meanNeighborsLastPredictions /= nb;
			}
			else {
				meanNeighborsLastPredictions = null;
			}
		}
		if(meanNeighborsLastPredictions != null) {
			getAmas().data.oracleValue = (getAmas().data.oracleValue + meanNeighborsLastPredictions)/2;
		}
	}*/

	private void updatePerformanceIndicators() {


		double volumeOfAllContexts=getVolumeOfAllContexts();
		criticalities.addCriticality("spatialCriticality",
				(getMinMaxVolume() - volumeOfAllContexts) / getMinMaxVolume());

		getAmas().data.spatialGeneralizationScore = volumeOfAllContexts / getAmas().getContexts().size();

		//double globalConfidence = 0;

		/*for (Context ctxt : getAmas().getContexts()) {
			globalConfidence += ctxt.getConfidence();
		}
		globalConfidence = globalConfidence / getAmas().getContexts().size();*/


//		if (activatedNeighborsContexts.size() > 1) {
//
//
//			double bestNeighborLastPrediction = Double.NEGATIVE_INFINITY;
//			Context bestNeighbor = null;
//
//			int i = 1;
//			for (Context ctxt : activatedNeighborsContexts) {
//
////				if(getAmas().isReinforcement()) {
////					System.out.println("####################### NEIGHBORS #############################");
////					System.out.println(ctxt.getName()  + " " + ctxt.lastPrediction);
////					if(ctxt.lastPrediction> bestNeighborLastPrediction) {
////
////
////						bestNeighborLastPrediction = ctxt.lastPrediction;
////						bestNeighbor = ctxt;
////					}
////				}
//
//
//
//
//				for (Context otherCtxt : activatedNeighborsContexts.subList(i, activatedNeighborsContexts.size())) {
//
//					// if(nearestLocalNeighbor(ctxt, otherCtxt)) {
//
//					Pair<Double, Percept> distanceAndPercept = ctxt.distance(otherCtxt);
//					// distanceAndPercept.getB());
//					if (distanceAndPercept.getA() < 0) {
//						criticalities.addCriticality("localOverlapMappingCriticality",
//								Math.abs(distanceAndPercept.getA()));
//					} else if (distanceAndPercept.getA() > 0 && distanceAndPercept.getB() != null) {
//						criticalities.addCriticality("localVoidMappingCriticality", distanceAndPercept.getA());
//					} else {
//						criticalities.addCriticality("localOpenVoidMappingCriticality", distanceAndPercept.getA());
//					}
//
//					// }
//
//				}
//				i++;
//
//
//
//
//			}
//
////			if(getAmas().isReinforcement()) {
////				System.out.println(bestNeighbor.getName() );
////				getAmas().data.higherNeighborLastPredictionPercepts = new HashMap<String, Double>();
////				for(Percept pct : getAmas().getPercepts()) {
////					getAmas().data.higherNeighborLastPredictionPercepts.put(pct.getName(),bestNeighbor.getRanges().get(pct).getCenter());
////				}
////				System.out.println(getAmas().data.higherNeighborLastPredictionPercepts );
////			}
//
//
//
//		}

		getAmas().data.mappingPerformance.setPerformanceIndicator(getEnvironment().getMappingErrorAllowed());// Math.pow(world.getMappingErrorAllowed(),
		// world.getScheduler().getPercepts().size());

		getAmas().data.evolutionCriticalityPrediction = (lembda * getAmas().data.evolutionCriticalityPrediction)
				+ ((1 - lembda) * getAmas().data.currentCriticalityPrediction);
		getAmas().data.evolutionCriticalityMapping = (lembda * getAmas().data.evolutionCriticalityMapping)
				+ ((1 - lembda) * getAmas().data.currentCriticalityMapping);
		getAmas().data.evolutionCriticalityConfidence = (lembda * getAmas().data.evolutionCriticalityConfidence)
				+ ((1 - lembda) * getAmas().data.currentCriticalityConfidence);



	}

	private void allNCSDetectionsWithOracle() {
		getAmas().data.executionTimes[8]=System.currentTimeMillis();

		NCSDetection_Uselessness();
		if(lastEndogenousRequest==null){
			NCSDetection_IncompetentHead();
		}
		else if(lastEndogenousRequest.getType()!= REQUEST.VOID  && lastEndogenousRequest.getType()!= REQUEST.SUBVOID){
			NCSDetection_IncompetentHead();
		}

		NCSDetection_ConcurrenceAndConlict(); /* If result is good, shrink redundant context (concurrence NCS) */
		NCSDetection_Create_New_Context(); /* Finally, head agent check the need for a new context agent */
		NCSDetection_Context_Overmapping();
		NCSDetection_ChildContext();

		if(getAmas().getCycle()>0){
			NCSDetection_PotentialRequest();
			//NCSDetection_PotentialSubRequest();
		}
		NCSDetection_Dream();

		getAmas().data.executionTimes[8]=System.currentTimeMillis()- getAmas().data.executionTimes[8];
	}


	private void allNCSDetectionsWithoutOracle() {
		getAmas().data.executionTimes[8]=System.currentTimeMillis();

		NCSDetection_Uselessness();
		/*if(lastEndogenousRequest==null){
			if(getAmas().data.isSelfLearning){
				NCSDetection_IncompetentHeadWitoutOracle();
			}

		}
		else if(lastEndogenousRequest.getType()!= REQUEST.VOID){
			NCSDetection_IncompetentHeadWitoutOracle();
		}*/

		//NCSDetection_LearnFromNeighbors();
		NCSDetection_ConcurrenceAndConclictWithoutOracle(); // If result is good, shrink redundant context (concurrence NCS)
		NCSDetection_Create_New_ContextWithoutOracle();  //Finally, head agent check the need for a new context agent
		NCSDetection_Context_OvermappingWithouOracle();
		NCSDetection_ChildContext();
		NCSDetection_Dream();
		if(lastEndogenousRequest!= null){
			if(getAmas().getCycle()>0 && lastEndogenousRequest.getType() == REQUEST.DREAM){
				NCSDetection_PotentialRequest();
			}
		}
		resetLastEndogenousRequest();

		getAmas().data.executionTimes[8]=System.currentTimeMillis()- getAmas().data.executionTimes[8];
	}

	/*private void NCSDetection_LearnFromNeighbors() {
		getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION LEARN FROM NEIGHBORS WITHOUT ORACLE")));

		if(bestContext.isChild() && lastEndogenousRequest!= null){
			if(lastEndogenousRequest.getType()== REQUEST.MODEL)
				bestContext.solveNCS_LearnFromNeighbors();
		}
	}*/

	/*private void NCSDetection_FitWithNeighbors() {

		if(getAmas().data.isCoopLearningWithoutOracle){


			getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION FIT WITH NEIGHBORS WITHOUT ORACLE")));

			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("NB NEIGHBORS", activatedNeighborsContexts.size()+"")));
			for (Context ctxt : activatedNeighborsContexts) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(ctxt.getName())));
			}
			if(activatedNeighborsContexts.size()>1 *//*&& bestContext.isChild()*//*){

				if(bestContext!=null){
					bestContext.solveNCS_FitWithNeighbors();
				}



			}


			*//*if(lastEndogenousRequest.getType() == REQUEST.SELF && activatedNeighborsContexts.size() > PARAMS.nbOfNeighborForCoopLearning){
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(bestContext.getName(), "ASKING HELP TO NEIGHBORS")));
				Experiment cooperativeExperiment = bestContext.getPerceptionsAsExperiment();
				double weightedPreditions = 0;
				double normalization = 0;
				for(Context ctxt : activatedNeighborsContexts){

					double distanceToPerceptions = ctxt.distanceBetweenCurrentPercetionsAndCenter();
					double prediction = ((LocalModelMillerRegression)ctxt.getLocalModel()).getProposition(cooperativeExperiment);
					weightedPreditions += prediction / distanceToPerceptions;
					normalization += (1/distanceToPerceptions);
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(ctxt.getName(),""+prediction, ""+distanceToPerceptions )));
				}
				cooperativeExperiment.setOracleProposition(weightedPreditions/normalization);
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(cooperativeExperiment+"" )));
				bestContext.getLocalModel().updateModel(cooperativeExperiment, getAmas().data.learningSpeed);

			}*//*


		}


	}*/






	public HashMap<String, Double> getMappingScoresAndPrint(){

		Pair<Double,Double> volumeOfOverlaps = getVolumeOfAllOverlaps();
		double minMaxVolume = getMinMaxVolume();
		double volumeOfAllContexts = getVolumeOfAllContexts()  - volumeOfOverlaps.getA() - volumeOfOverlaps.getB();
		double voidVolume = minMaxVolume - volumeOfAllContexts;
		double allVolumes = voidVolume + volumeOfAllContexts ;

		System.out.println("\nMINMAX VOL \t" + minMaxVolume + " \tNORM \t1.0"  );
		System.out.println("CTXT VOL \t" + volumeOfAllContexts + " \tNORM \t"  + volumeOfAllContexts/minMaxVolume);
		System.out.println("CONF VOL \t" + volumeOfOverlaps.getA() + " \tNORM \t"  + volumeOfOverlaps.getA()/minMaxVolume);
		System.out.println("CONC VOL \t" + volumeOfOverlaps.getB() + " \tNORM \t"  + volumeOfOverlaps.getB()/minMaxVolume);
		System.out.println("VOIDS VOL \t" + voidVolume + " \tNORM \t"  + voidVolume/minMaxVolume);
		System.out.println("TEST VOL \t" + allVolumes +  " \tNORM \t"  + allVolumes/minMaxVolume);



		HashMap<String, Double> scores = new HashMap<>();
		scores.put("CTXT",volumeOfAllContexts/minMaxVolume);
		scores.put("CONF",volumeOfOverlaps.getA()/minMaxVolume);
		scores.put("CONC",volumeOfOverlaps.getB()/minMaxVolume);
		scores.put("VOIDS",voidVolume/minMaxVolume);

		return scores;
	}

	public HashMap<String, Double> getMappingScores(){

		Pair<Double,Double> volumeOfOverlaps = getVolumeOfAllOverlaps();
		double minMaxVolume = getMinMaxVolume();
		double volumeOfAllContexts = getVolumeOfAllContexts()  - volumeOfOverlaps.getA() - volumeOfOverlaps.getB();
		double voidVolume = minMaxVolume - volumeOfAllContexts;
		double allVolumes = voidVolume + volumeOfAllContexts ;





		HashMap<String, Double> scores = new HashMap<>();
		scores.put("CTXT",volumeOfAllContexts/minMaxVolume);
		scores.put("CONF",volumeOfOverlaps.getA()/minMaxVolume);
		scores.put("CONC",volumeOfOverlaps.getB()/minMaxVolume);
		scores.put("VOIDS",voidVolume/minMaxVolume);

		return scores;
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

	public Pair<Double, Double> getVolumeOfAllOverlaps() {
		double allConflictsVolume = 0;
		double allConcurrencesVolume = 0;
		int i = 1;
		for (Context ctxt : getAmas().getContexts()) {
			for (Context otherCtxt : getAmas().getContexts().subList(i, getAmas().getContexts().size())) {
				if(otherCtxt != ctxt){

					//ctxt.isSameModel()

					if(ctxt.isSameModel(otherCtxt)){
						allConcurrencesVolume += ctxt.getOverlappingVolume(otherCtxt);
					}
					else{
						allConflictsVolume += ctxt.getOverlappingVolume(otherCtxt);
					}

				}
			}
			i++;

		}

		return new Pair<Double,Double>(allConflictsVolume, allConcurrencesVolume);
	}

	/*public double getSpatialCriticality() {
		return criticalities.getCriticality("spatialCriticality");
	}*/

	/**
	 * Play without oracle.
	 */
	private void playWithoutOracle() {
		getAmas().data.oracleValue = null;


		if(getAmas().getCycle() % 50 == 0){
			if(lastEndogenousRequest != null){
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
			}else{
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE" + " " + getWaitingEndogenousRequests())));
			}
		}

		if(lastEndogenousRequest != null){
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
		}else{
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE" + " " + getWaitingEndogenousRequests())));
		}

		updateBestContextAndPropositionWithoutOracle();

		updateCriticalityWithoutOracle();

		updateNeighborContextLastPredictions();

		if(getAmas().data.PARAM_isSelfLearning && getAmas().getCycle()>getAmas().data.PARAM_bootstrapCycle){
			allNCSDetectionsWithoutOracle();
		}



		/*if(getAmas().isReinforcement()) {
			reinforcementWithouOracle();
		}*/
		
		

	}

	private void updateActivatedContextLastPredictions() {
		for(Context activatedContext : activatedContexts){
			activatedContext.lastPrediction = activatedContext.getActionProposal();
		}
	}
	private void updateNeighborContextLastPredictions() {
		for(Context neighborContext : activatedNeighborsContexts){
			neighborContext.lastPrediction = neighborContext.getActionProposal();
		}
	}

	private void playWithoutOracleAndAllPercets() {
		getAmas().data.oracleValue = null;


		if(getAmas().getCycle() % 50 == 0){
			if(lastEndogenousRequest != null){
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE AND ALL PERCEPTS \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
			}else{
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE AND ALL PERCEPTS" + " " + getWaitingEndogenousRequests())));
			}
		}

		if(lastEndogenousRequest != null){
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE AND ALL PERCEPTS \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests())));
		}else{
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE AND ALL PERCEPTS" + " " + getWaitingEndogenousRequests())));
		}

		updateBestContextAndPropositionWithoutOracleFromPseudoActivatedContexts();

		//updateCriticalityWithoutOracle();

		/*if(getAmas().data.isSelfLearning){
			allNCSDetectionsWithoutOracle();
		}



		if(getAmas().isReinforcement()) {
			reinforcementWithouOracle();
		}*/



	}

	private int getWaitingEndogenousRequests() {
		return endogenousChildRequests.size()+endogenousDreamRequests.size()+endogenousRequests.size() + endogenousSubRequests.size();
	}

	/*private void reinforcementWithouOracle() {
		if (activatedNeighborsContexts.size() > 1) {

			double bestNeighborLastPrediction = Double.NEGATIVE_INFINITY;
			Context bestNeighbor = null;

			int i = 1;
			System.out.println("####################### NEIGHBORS ############################# " +  activatedNeighborsContexts.size());
			for (Context ctxt : activatedNeighborsContexts) {



				System.out.println(ctxt.getName()  + " " + ctxt.lastPrediction);
				if(ctxt.lastPrediction> bestNeighborLastPrediction) {


					bestNeighborLastPrediction = ctxt.lastPrediction;
					bestNeighbor = ctxt;
				}


			}


			System.out.println(bestNeighbor.getName() );
			getAmas().data.higherNeighborLastPredictionPercepts = new HashMap<String, Double>();
			for(Percept pct : getAmas().getPercepts()) {
				getAmas().data.higherNeighborLastPredictionPercepts.put(pct.getName(),bestNeighbor.getRanges().get(pct).getCenter());
			}
			System.out.println(getAmas().data.higherNeighborLastPredictionPercepts );

		}
	}*/

	private void updateBestContextAndPropositionWithoutOracle() {
		Double weightedPrediction = null;
		if (activatedContexts.size() > 0) {
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withActivatedContexts",activatedContexts.size());

			selectBestContextWithoutOracleWeighted(activatedContexts, getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);

		} else if (activatedNeighborsContexts.size()>0){
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withActivatedNeighborContexts",activatedNeighborsContexts.size());

			selectBestContextWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);

			/*weightedPrediction = getPredictionWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);*/
		} else if (getAmas().getContexts().size()>0 && getAmas().data.PARAM_NCS_isAllContextSearchAllowedForExploitation){
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withAllContexts",getAmas().getContexts().size());

			selectBestContextWithoutOracleWeighted(getAmas().getContexts(),  getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);

		}

		if (bestContext != null) {
			//setContextFromPropositionWasSelected(true);
			if(weightedPrediction!=null){
				getAmas().data.prediction = weightedPrediction.doubleValue();
			}else{
				getAmas().data.prediction = bestContext.getActionProposal();
			}


		} else {
			setPredictionWithoutContextAgent();
		}




	}

	/*private double getNeighborhoodWeightedPrediction() {
		double weightedPredictionSum=0;
		double normalisation=0;
		for(Context ctxt : activatedNeighborsContexts){
			double distance = ctxt.distanceBetweenCurrentPercetionsAndBorders();
			weightedPredictionSum += (1/distance)*ctxt.getLocalModel().getProposition();
			normalisation += (1/distance);

		}
		return weightedPredictionSum/normalisation;
	}*/

	private void updateBestContextAndPropositionWithoutOracleFromPseudoActivatedContexts() {
		//logger().debug("HEAD without oracle and all percepts", "Nombre de contextes activés: " + activatedContexts.size());
		getEnvironment().print(TRACE_LEVEL.DEBUG, "HEAD without oracle and all percepts", "Nombre de contextes activés: " + activatedContexts.size());


		getAmas().data.nonCondireredPerceptsSyntheticValues = new HashMap<>();

		//selectBestContextWithConfidenceAndVolume();
		//selectBestContextWithConfidenceOrDistance();
		selectBestSubContextWithDistance();

		if (bestContext != null) {
			//getAmas().data.noBestContext = false;
			getAmas().data.prediction = bestContext.getActionProposalWithSubPercepts();

			for(Percept unconsideredPct : getAmas().getUnconsideredPercepts()){
				getAmas().data.nonCondireredPerceptsSyntheticValues.put(unconsideredPct,bestContext.getRanges().get(unconsideredPct).getCenter());
			}

		} else {

			//getAmas().data.noBestContext = true;
			Context nearestContext = this.getNearestContextWithoutAllPercepts(activatedNeighborsContexts);

			if(nearestContext != null) {



				//getAmas().data.prediction = nearestContext.getActionProposalWithSubPercepts();
				bestContext = nearestContext;
				getAmas().data.prediction = bestContext.getActionProposalWithSubPercepts();

				for(Percept unconsideredPct : getAmas().getUnconsideredPercepts()){
					getAmas().data.nonCondireredPerceptsSyntheticValues.put(unconsideredPct,bestContext.getRanges().get(unconsideredPct).getCenter());
				}

				/*ArrayList<Context> activatedNeighborsContextsFilteredByPrediction = new ArrayList<>();

				for(Context neighborCtxt : activatedNeighborsContexts){
					if(Math.abs(nearestContext.lastPrediction - getAmas().data.prediction)< getPredictionNeighborhoodRange()){
						activatedNeighborsContextsFilteredByPrediction.add(neighborCtxt);
					}

				}

				if(activatedNeighborsContextsFilteredByPrediction.size()>1) {

					double weightedPredictionSum = 0;
					double normalisation = 0;
					for (Context ctxt : activatedNeighborsContextsFilteredByPrediction) {
						double distance = ctxt.distanceBetweenCurrentPercetionsAndBordersWithSubPercepts();
						weightedPredictionSum += (1 / distance) * ctxt.getActionProposalWithSubPercepts();
						normalisation += (1 / distance);


					}
					getAmas().data.prediction = weightedPredictionSum / normalisation;

					for(Percept unconsideredPct : getAmas().getUnconsideredPercepts()){
						double weightedPerceptSum = 0;
						double perceptNormalisationValue = 0;
						for (Context ctxt : activatedNeighborsContextsFilteredByPrediction) {
							double distance = ctxt.distanceBetweenCurrentPercetionsAndBordersWithSubPercepts();
							weightedPerceptSum += (1 / distance) * ctxt.getRanges().get(unconsideredPct).getCenter();
							perceptNormalisationValue += (1 / distance);
						}
						getAmas().data.nonCondireredPerceptsSyntheticValues.put(unconsideredPct,weightedPerceptSum/perceptNormalisationValue);
					}

				}else{
					getAmas().data.prediction = bestContext.getActionProposalWithSubPercepts();

					for(Percept unconsideredPct : getAmas().getUnconsideredPercepts()){
						getAmas().data.nonCondireredPerceptsSyntheticValues.put(unconsideredPct,bestContext.getRanges().get(unconsideredPct).getCenter());
					}
				}*/
			} else {
				//TODO THIS IS VERY INEFICIENT ! amoeba should not look globally, but right now there's no other strategy.
				// To limit performance impact, we limit our search on a random sample.
				// A better way would be to increase neighborhood.
				getEnvironment().print(TRACE_LEVEL.ERROR,"Play without oracle : no nearest context in neighbors, searching in a all Contexts");
				//PrintOnce.print("Play without oracle : no nearest context in neighbors, searching in a random sample. (only shown once)");
				//List<Context> searchList = RandomUtils.pickNRandomElements(getAmas().getContexts(), 100);
				nearestContext = this.getNearestContextWithoutAllPercepts(getAmas().getContexts());
				if(nearestContext != null) {
					getAmas().data.prediction = nearestContext.getActionProposalWithSubPercepts();
					bestContext = nearestContext;

					for(Percept unconsideredPct : getAmas().getUnconsideredPercepts()){
						getAmas().data.nonCondireredPerceptsSyntheticValues.put(unconsideredPct,bestContext.getRanges().get(unconsideredPct).getCenter());
					}

				} else {
					getEnvironment().print(TRACE_LEVEL.ERROR,"NO NEAREST CONTEXT"); // Sould not happend
                    setPredictionWithoutContextAgent();
				}
			}

		}



		if(bestContext != null) {
			//logger().debug("HEAD without oracle and all percepts", "Best context selected without oracle is : " + bestContext.getName());

			getEnvironment().print(TRACE_LEVEL.DEBUG, "HEAD without oracle and all percepts", "Best context selected without oracle is : " + bestContext.getName());

			// Config.print("With function : " +
			// bestContext.getFunction().getFormula(bestContext), 0);
			//logger().debug("HEAD without oracle and all percepts","BestContext : " + bestContext.toStringFull() + " " + bestContext.getConfidence());

			getEnvironment().print(TRACE_LEVEL.DEBUG, "HEAD without oracle and all percepts","BestContext : " + bestContext.toStringFull() + " " + bestContext.getConfidence());

			// functionSelected = bestContext.getFunction().getFormula(bestContext);

		}
		else {
			//logger().debug("HEAD without oracle", "no Best context selected ");
			getEnvironment().print(TRACE_LEVEL.DEBUG, "HEAD without oracle", "no Best context selected ");
		}

	}

	/*private void endogenousPlay() {

		getAmas().data.endogenousPredictionActivatedContextsOverlaps = null;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = null;

		getAmas().data.endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = null;
		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = null;

		getAmas().data.endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = null;

		getAmas().data.endogenousPredictionActivatedContextsSharedIncompetence = null;
		getAmas().data.endogenousPredictionNContextsByInfluence = null;

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

		ArrayList<Context> contextsNeighborsByInfluence = new ArrayList<>();
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
	}*/

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

		HashMap<Percept, ContextPair<Context, Context>> sharedIncompetenceContextPairs = getSharedIncompetenceContextPair();

		// displayContexts();

		for (Percept pcpt : getAmas().getPercepts()) {

			if (sharedIncompetenceContextPairs.get(pcpt) != null) {
				if (sharedIncompetenceContextPairs.get(pcpt).containTwoContexts()) {
					testSurroudingContext = true;
					break;
				}
			}

		}

		return testSurroudingContext;
	}

	private HashMap<Percept, ContextPair<Context, Context>> getSharedIncompetenceContextPair() {
		HashMap<Percept, ContextPair<Context, Context>> sharedIncompetenceContextPairs = new HashMap<>();
		
		for (Percept pct : getAmas().getPercepts()) {
			ContextPair<Context, Context> nearestContexts = computeNearestContextsByPercept(pct);
			if (nearestContexts.getL() != null && nearestContexts.getR() != null) {
				sharedIncompetenceContextPairs.put(pct, nearestContexts);
			}
		}
		
		return sharedIncompetenceContextPairs;
	}

	private ContextPair<Context, Context> computeNearestContextsByPercept(Percept pct) {
		ContextPair<Context, Context> nearestContexts = new ContextPair<>(null, null);
		boolean startNeighbor = false;
		boolean endNeighbor = false;

		ArrayList<Context> activatedContextInOtherPercepts = getAllActivatedContextsExeptForOnePercept(pct);
		if (activatedContextInOtherPercepts.size() > 0) {

			CustomComparator rangeStartComparator = new CustomComparator(pct, "start");
			Collections.sort(activatedContextInOtherPercepts, rangeStartComparator);

			for (Context ctxt : activatedContextInOtherPercepts) {
				if (ctxt.getRanges().get(pct).getRange("start") > pct.getValue() && !startNeighbor) {
					nearestContexts.setR(ctxt);
					startNeighbor = true;
				}
			}

			CustomComparator rangeEndComparator = new CustomComparator(pct, "end");
			Collections.sort(activatedContextInOtherPercepts, rangeEndComparator);
			Collections.reverse(activatedContextInOtherPercepts);

			for (Context ctxt : activatedContextInOtherPercepts) {
				if (ctxt.getRanges().get(pct).getRange("end") < pct.getValue() && !endNeighbor) {
					nearestContexts.setL(ctxt);
					endNeighbor = true;
				}
			}

		}
		return nearestContexts;
	}

	/**
	 * Return a list of contexts that have been activated by all percepts except a particular one.
	 * @param pct
	 * @return
	 */
	private ArrayList<Context> getAllActivatedContextsExeptForOnePercept(Percept pct) {
		ArrayList<Percept> percepts = new ArrayList<>(getAmas().getPercepts());
		percepts.remove(pct);
		ArrayList<Context> ret = new ArrayList<>(getAmas().getContexts());
		ret.removeIf(c -> {
			for(Percept p : percepts) {
				if(!p.activateContext(c)) return true;
			}
			return false;
		});
		return ret;
	}

	/*private void NCS_EndogenousCompetition() {

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

	}*/

	/*private void NCS_EndogenousSharedIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the
		// prediction

		;

		ContextPair<Context, Context> closestContexts = new ContextPair<Context, Context>(null, null);
		double smallestDistanceBetweenContexts = Double.POSITIVE_INFINITY;
		double currentDistance;

		HashMap<Percept, ContextPair<Context, Context>> sharedIncompetenceContextPairs = getSharedIncompetenceContextPair();
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

	}*/
	
	private void NCSDetection_ChildContext() {
		getAmas().data.executionTimes[14]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isSelfModelRequest){
			if(getAmas().data.PARAM_isActiveLearning) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- NCS DETECTION CHILD CONTEXT")));

				if(bestContext!=null) {
					if(bestContext.isChild() && getAmas().data.firstContext && getAmas().getCycle()>0 && !bestContext.isDying()) {
						bestContext.solveNCS_ChildContext();


					}else if(getAmas().data.firstContext && getAmas().getCycle()>1 && !bestContext.isDying()){
						if(getAmas().data.PARAM_isLearnFromNeighbors && getAmas().getHeadAgent().getActivatedNeighborsContexts().size()>getAmas().data.PARAM_nbOfNeighborForLearningFromNeighbors){
							bestContext.learnFromNeighbors();
						}

					}

				}
			}
			else if (getAmas().data.PARAM_isSelfLearning){
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- NCS DETECTION CHILD CONTEXT WITHOUT ORACLE")));

				if(bestContext!=null) {
					if(bestContext.isChild() && getAmas().data.firstContext && getAmas().getCycle()>1 && !bestContext.isDying()) {
						bestContext.solveNCS_ChildContextWithoutOracle();


					}else if(getAmas().data.firstContext && getAmas().getCycle()>1 && !bestContext.isDying()){
						if(getAmas().data.PARAM_isLearnFromNeighbors && getAmas().getHeadAgent().getActivatedNeighborsContexts().size()>getAmas().data.PARAM_nbOfNeighborForLearningFromNeighbors){
							bestContext.learnFromNeighbors();
						}

					}
				}

			}
		}




		getAmas().data.executionTimes[14]=System.currentTimeMillis()- getAmas().data.executionTimes[14];
	}

	/*private void NCSDetection_ChildContextWithoutOracle() {

		if(getAmas().data.PARAM_isSelfLearning) {
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION CHILD CONTEXT WITHOUT ORACLE")));

			if(bestContext!=null && activatedNeighborsContexts.size()>1) {
				if(!bestContext.getLocalModel().finishedFirstExperiments() && getAmas().data.firstContext && getAmas().getCycle()>0 && !bestContext.isDying()) {
					bestContext.solveNCS_ChildContext();


				}
			}
		}

	}*/
	
	
		
		
		
		
		
	
	
	

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

	private void NCSDetection_Create_New_Context() {
		getAmas().data.executionTimes[12]=System.currentTimeMillis();

		/* Finally, head agent check the need for a new context agent */

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION CREATE NEW CONTEXT")));

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("Activated contexts")));
		if(((World)getAmas().getEnvironment()).PARAM_minTraceLevel ==TRACE_LEVEL.DEBUG){
			for(Context ctxt : activatedContexts){
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(ctxt.getName())));
			}
		}

		boolean newContextCreated = false;

		if (activatedContexts.size() == 0) {
			
			getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(
					"*********************************************************************************************************** SOLVE NCS CREATE NEW CONTEXT")));


			Context goodContext = getGoodContextWithOracleWeighted(activatedNeighborsContexts,getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PREDICTION,
					getAmas().data.PARAM_LEARNING_WEIGHT_CONFIDENCE,getAmas().data.PARAM_LEARNING_WEIGHT_VOLUME,getAmas().data.PARAM_LEARNING_WEIGHT_DISTANCE_TO_PERCEPTIONS);

			Context context;
			if (goodContext != null && getAmas().data.PARAM_NCS_isCreationWithNeighbor) {
				getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList(goodContext.getName(),
						"************************************* NEAREST GOOD CONTEXT")));
				context = createNewContext(goodContext);
			} else {
				context = createNewContext();
			}

			/*Context context;
			Pair<Context, Double> nearestGoodContext = getbestContextInNeighborsWithDistanceToModel(activatedNeighborsContexts);
			if (nearestGoodContext.getA() != null && getAmas().data.PARAM_NCS_isCreationWithNeighbor) {
				getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList(nearestGoodContext.getA().getName(),
						"************************************* NEAREST GOOD CONTEXT")));
				context = createNewContext(nearestGoodContext.getA());
			} else {
				context = createNewContext();
			}*/

			bestContext = context;
			newContext = context;
			newContextCreated = true;
			
			newContext.lastPrediction = newContext.getActionProposal();
			activatedSubNeighborsContexts.add(newContext);
			activatedNeighborsContexts.add(newContext);
			activatedContexts.add(newContext);
			/*double maxCoef = 0.0;
			for(Double coef : newContext.getLocalModel().getCoef()) {
				if(Math.abs(coef)> maxCoef) {
					maxCoef = Math.abs(coef);
				}
			}*/


			
			//newContext.initEndoChildRequests();


			
		}

		if (!newContextCreated) {
			updateStatisticalInformations();
		}

		resetLastEndogenousRequest();

		getAmas().data.executionTimes[12]=System.currentTimeMillis()- getAmas().data.executionTimes[12];
	}

	private void NCSDetection_Create_New_ContextWithoutOracle() {
		getAmas().data.executionTimes[12]=System.currentTimeMillis();

		/* Finally, head agent check the need for a new context agent */

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION CREATE NEW CONTEXT")));


		boolean newContextCreated = false;
		if (activatedContexts.size() == 0 && activatedNeighborsContexts.size()>0 && getAmas().data.PARAM_isSelfLearning) {

			getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(
					"*********************************************************************************************************** SOLVE NCS CREATE NEW CONTEXT")));


			Context context = createNewContextWithoutOracle();

			if(context != null){

				bestContext = context;
				newContext = context;
				newContextCreated = true;
				//newContext.initEndoChildRequests();
				newContext.lastPrediction = newContext.getActionProposal();
				activatedSubNeighborsContexts.add(newContext);
				activatedNeighborsContexts.add(newContext);
				activatedContexts.add(newContext);
				double maxCoef = 0.0;
				for(Double coef : newContext.getLocalModel().getCoef()) {
					if(Math.abs(coef)> maxCoef) {
						maxCoef = Math.abs(coef);
					}
				}
			}


		}


		if (!newContextCreated) {
			updateStatisticalInformations();
		}

		getAmas().data.executionTimes[12]=System.currentTimeMillis()- getAmas().data.executionTimes[12];
	}

	private void NCSDetection_Context_Overmapping() {
		getAmas().data.executionTimes[13]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isFusionResolution || getAmas().data.PARAM_NCS_isRetrucstureResolution){
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION OVERMAPPING")));

			ArrayList<Context> activatedContextsCopy = new ArrayList<Context>();
			activatedContextsCopy.addAll(activatedContexts);

			for (Context ctxt : activatedContextsCopy) {
				if(!ctxt.isDying() && ctxt.getLocalModel().finishedFirstExperiments()) {
					ctxt.NCSDetection_OverMapping();
				}

			}
		}
		


		getAmas().data.executionTimes[13]=System.currentTimeMillis()- getAmas().data.executionTimes[13];
	}

	private void NCSDetection_Context_OvermappingWithouOracle() {
		getAmas().data.executionTimes[13]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isFusionResolution || getAmas().data.PARAM_NCS_isRetrucstureResolution){
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION OVERMAPPING WITHOU ORACLE")));

			ArrayList<Context> activatedContextsCopy = new ArrayList<Context>();
			activatedContextsCopy.addAll(activatedContexts);

			for (Context ctxt : activatedContextsCopy) {
				if(!ctxt.isDying() && ctxt.getLocalModel().finishedFirstExperiments()) {
					ctxt.NCSDetection_OverMappingWithouOracle();
				}

			}
		}



		getAmas().data.executionTimes[13]=System.currentTimeMillis()- getAmas().data.executionTimes[13];
	}

	private void NCSDetection_ConcurrenceAndConlict() {
		getAmas().data.executionTimes[11]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isConcurrenceResolution || getAmas().data.PARAM_NCS_isConflictResolution){
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION CONCURRENCE")));

			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("bestContext != null", "" + (bestContext != null))));
			if(bestContext != null) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("bestContext.getLocalModel().distance(bestContext.getCurrentExperiment()) < getAverageRegressionPerformanceIndicator()", "" + ((bestContext.lastDistanceToModel) < getPredicionPerformanceIndicator() ))));
			}

			if (bestContext != null && bestContext.lastDistanceToModel < getPredicionPerformanceIndicator() && bestContext.isInNeighborhood) {

				for (int i = 0; i<activatedContexts.size();i++) {

					boolean testSameModel = activatedContexts.get(i).isSameModel(bestContext);
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(activatedContexts.get(i).getName(),"activatedContexts.get(i) != bestContext", "" + ( activatedContexts.get(i) != bestContext))));
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(activatedContexts.get(i).getName(),"!activatedContexts.get(i).isDying()", "" + ( !activatedContexts.get(i).isDying()))));
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(activatedContexts.get(i).getName(),"", "" + testSameModel)));

					if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && testSameModel && getAmas().data.PARAM_NCS_isConcurrenceResolution) {
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-0.5);
					}else if(activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && !testSameModel && getAmas().data.PARAM_NCS_isConflictResolution){
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-4.0);
					}


				}
			}
		}



		getAmas().data.executionTimes[11]=System.currentTimeMillis()- getAmas().data.executionTimes[11];
	}

	private void NCSDetection_ConcurrenceAndConclictWithoutOracle() {
		getAmas().data.executionTimes[11]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isConcurrenceResolution || getAmas().data.PARAM_NCS_isConflictResolution){
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION CONCURRENCE WITHOUT ORACLE")));

			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("bestContext != null", "" + (bestContext != null))));



			/* If result is good, shrink redundant context (concurrence NCS) */
			if (bestContext != null && bestContext.isInNeighborhood) {

				for (int i = 0; i<activatedContexts.size();i++) {

					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("activatedContexts.get(i) != bestContext", "" + ( activatedContexts.get(i) != bestContext))));
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("!activatedContexts.get(i).isDying()", "" + ( !activatedContexts.get(i).isDying()))));

					boolean testSameModel = activatedContexts.get(i).isSameModelWithoutOracle(bestContext);
					if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && testSameModel && getAmas().data.PARAM_NCS_isConcurrenceResolution) {
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-0.5);
					}
					else if(activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && !testSameModel && getAmas().data.PARAM_NCS_isConflictResolution){
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-4.0);
					}
				}
			}
		}


		getAmas().data.executionTimes[11]=System.currentTimeMillis()- getAmas().data.executionTimes[11];
	}

	private void NCSDetection_IncompetentHead() {
		/*
		 * If there isn't any proposition or only bad propositions, the head is
		 * incompetent. It needs help from a context.
		 */
		getAmas().data.executionTimes[10]=System.currentTimeMillis();
		
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION INCOMPETENT HEAD")));

		if(activatedContexts.isEmpty()) {

			Context nearestGoodContext = getGoodContextWithOracleWeighted(activatedNeighborsContexts,1.0,0.0,0.0,1.0);

			if (nearestGoodContext != null) {
				nearestGoodContext.solveNCS_IncompetentHead();
			}
		}

		getAmas().data.executionTimes[10]=System.currentTimeMillis()- getAmas().data.executionTimes[10];
	}

	/*private void NCSDetection_IncompetentHeadWitoutOracle() {
		*//*
		 * If there isn't any proposition or only bad propositions, the head is
		 * incompetent. It needs help from a context.
		 *//*

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION INCOMPETENT HEAD WITHOUT ORACLE")));

		if(activatedContexts.isEmpty()) {

			Context c = bestContext;

			if (c != null) {
				c.solveNCS_IncompetentHead(this);
			}

			bestContext = c;


			*//* This allow to test for all contexts rather than the nearest *//*
			*//*
			 * for (Agent a : allContexts) { Context c = (Context) a; if
			 * (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c !=
			 * newContext && !c.isDying() && c != bestContext && !contexts.contains(c)) {
			 * c.growRanges(this);
			 *
			 * } }
			 *//*

		}
	}*/

	public void NCSDetection_Dream() {
		getAmas().data.executionTimes[16]=System.currentTimeMillis();


		//if(getAmas().getCycle() % (500 * getAmas().getPercepts().size()) ==0 && getAmas().data.isDream){
		if(getAmas().getCycle() == getAmas().data.PARAM_DreamCycleLaunch && getAmas().data.PARAM_isDream){


			getAmas().data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning = 5;
			getAmas().data.PARAM_nbOfNeighborForContexCreationWithouOracle = 5;
			//getEnvironment().PARAM_minTraceLevel = TRACE_LEVEL.DEBUG;

			getEnvironment().print(TRACE_LEVEL.ERROR, PARAMS.traceLevel, getAmas().data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning, getAmas().data.PARAM_nbOfNeighborForContexCreationWithouOracle);



			for(Context ctxt : getAmas().getContexts()){
				HashMap<Percept,Double> request = new HashMap<>();
				for(Percept pct : getAmas().getPercepts()){
					request.put(pct,ctxt.getRanges().get(pct).getRandom());
				}
				addDreamRequest(request,5,ctxt);

			}
			getAmas().data.STATE_DreamCompleted=0;
		}

		getAmas().data.executionTimes[16]=System.currentTimeMillis()- getAmas().data.executionTimes[16];
	}

	public void NCSDetection_PotentialRequest() {
		getAmas().data.executionTimes[15]=System.currentTimeMillis();

		if(getAmas().data.PARAM_NCS_isConflictDetection || getAmas().data.PARAM_NCS_isConcurrenceDetection
				|| getAmas().data.PARAM_NCS_isVoidDetection
				|| getAmas().data.PARAM_NCS_isFrontierRequest
		){

			if(getAmas().data.PARAM_isActiveLearning || getAmas().data.PARAM_isSelfLearning) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- NCS DETECTION POTENTIAL REQUESTS")));

				addPotentialConflictConcurrenceOrFrontierRequests();
				addPotentialVoidRequest();
			}
		}




		getAmas().data.executionTimes[15]=System.currentTimeMillis()- getAmas().data.executionTimes[15];
	}

	private void addPotentialVoidRequest() {
		boolean testVoid = false;
		if(getAmas().data.PARAM_isActiveLearning){
			testVoid = true;
		}else{
			if(lastEndogenousRequest!=null){
				testVoid = lastEndogenousRequest.getType() == REQUEST.DREAM;
			}
		}

		if((getAmas().getCycle()> NEIGH_VOID_CYCLE_START && endogenousRequests.size()==0 && getAmas().data.PARAM_NCS_isVoidDetection) && testVoid){
			HashMap<Percept, Pair<Double, Double>> neighborhoodBounds = new HashMap<>();
			for(Percept pct : getAmas().getPercepts()){
				neighborhoodBounds.put(pct, new Pair<>( pct.getValue()-(pct.getNeigborhoodRadius()), pct.getValue()+(pct.getNeigborhoodRadius())));
			}
			ArrayList<VOID> detectedVoids = getVoidsFromContextsAndZone(neighborhoodBounds, activatedNeighborsContexts);

			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DETECTED VOIDS", ""+detectedVoids.size())));

			for(VOID detectedVoid : detectedVoids){

				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("VOID", ""+detectedVoid)));
				HashMap<Percept, Double> request = new HashMap<>();
				boolean isInMinMax = true;
				boolean isNotTooSmall = true;
				for(Percept pct : getAmas().getPercepts()){
					double value = (detectedVoid.bounds.get(pct).getB() + detectedVoid.bounds.get(pct).getA())/2;
					double range = detectedVoid.bounds.get(pct).getB() - detectedVoid.bounds.get(pct).getA();
					request.put(pct, value);
					isInMinMax = isInMinMax && pct.isInMinMax(value);
					isNotTooSmall = isNotTooSmall && !pct.isTooSmall(range);

				/*if(pct.isTooBig(range)){
					detectedVoid.bounds.get(pct).setA(value - pct.getRadiusContextForCreation());
					detectedVoid.bounds.get(pct).setB(value + pct.getRadiusContextForCreation());
				}*/
				}
				if(isInMinMax && isNotTooSmall){
					if(getAmas().data.PARAM_isSelfLearning){
						if(activatedNeighborsContexts.size()>getAmas().data.PARAM_nbOfNeighborForVoidDetectionInSelfLearning){
							EndogenousRequest potentialRequest = new EndogenousRequest(request, detectedVoid.bounds, 5, new ArrayList<Context>(activatedNeighborsContexts), REQUEST.VOID);
							addEndogenousRequest(potentialRequest, endogenousRequests); //VOID
						}
					}else{
						EndogenousRequest potentialRequest = new EndogenousRequest(request, detectedVoid.bounds, 5, new ArrayList<Context>(activatedNeighborsContexts), REQUEST.VOID);
						addEndogenousRequest(potentialRequest, endogenousRequests); //VOID
					}

				}

			}
		}
	}

	private void addPotentialConflictConcurrenceOrFrontierRequests() {

		if(getAmas().data.PARAM_NCS_isConflictDetection || getAmas().data.PARAM_NCS_isConcurrenceDetection
				|| getAmas().data.PARAM_NCS_isFrontierRequest
		){
			if (activatedNeighborsContexts.size() > 1) {
				int i = 1;
				for (Context ctxt : activatedNeighborsContexts) {
					for (Context otherCtxt : activatedNeighborsContexts.subList(i, activatedNeighborsContexts.size())) {
						if(!this.isDying() && !ctxt.isDying() ) {
							ArrayList<EndogenousRequest> potentialRequests = ctxt.endogenousRequest(otherCtxt);
							if(potentialRequests.size()>0) {

								for(EndogenousRequest potentialRequest : potentialRequests){
									addEndogenousRequest(potentialRequest, endogenousRequests); //RGE, CONFL, CONC
								}

							}
						}
					}
					i++;
				}
			}
		}


	}

	public void NCSDetection_PotentialSubRequest() {

		if(getAmas().data.PARAM_isActiveLearning || getAmas().data.PARAM_isSelfLearning) {
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- NCS DETECTION POTENTIAL SUB REQUESTS")));


			if((getAmas().data.PARAM_NCS_isSubVoidDetection) && getAmas().getCycle()> 10 && endogenousRequests.size()==0 && endogenousSubRequests.size()==0) {
				HashMap<Percept, Pair<Double, Double>> neighborhoodBounds = new HashMap<>();
				for (Percept pct : getAmas().getSubPercepts()) {
					neighborhoodBounds.put(pct, new Pair<>(pct.getValue() - (pct.getNeigborhoodRadius() ), pct.getValue() + (pct.getNeigborhoodRadius() )));
				}
				ArrayList<VOID> detectedVoids = getSubVoidsFromContextsAndZone(neighborhoodBounds, activatedSubNeighborsContexts);

				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DETECTED VOIDS", "" + detectedVoids.size())));

				for (VOID detectedVoid : detectedVoids) {

					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("VOID", "" + detectedVoid)));
					HashMap<Percept, Double> request = new HashMap<>();
					boolean isInMinMax = true;
					boolean isNotTooSmall = true;

					double centerDistance = 0.0;
					for (Percept pct : getAmas().getSubPercepts()) {
						double value = (detectedVoid.bounds.get(pct).getB() + detectedVoid.bounds.get(pct).getA()) / 2;
						double range = detectedVoid.bounds.get(pct).getB() - detectedVoid.bounds.get(pct).getA();
						request.put(pct, value);
						centerDistance += Math.pow(value,2);
						isInMinMax = isInMinMax && pct.isInMinMax(value);
						isNotTooSmall = isNotTooSmall && !pct.isTooSmall(range);

						/*if(pct.isTooBig(range)){
							detectedVoid.bounds.get(pct).setA(value - pct.getRadiusContextForCreation());
							detectedVoid.bounds.get(pct).setB(value + pct.getRadiusContextForCreation());
						}*/
					}
					boolean isInOperationalSpace = 20 < Math.sqrt(centerDistance) && Math.sqrt(centerDistance)< 180 ;
					if (isNotTooSmall && isInOperationalSpace) {

						EndogenousRequest potentialRequest = new EndogenousRequest(request, detectedVoid.bounds, 5, new ArrayList<Context>(activatedSubNeighborsContexts), REQUEST.SUBVOID);
						addEndogenousRequest(potentialRequest, endogenousSubRequests);

					}
				}
			}
		}
	}
	

	private void selfAnalysationOfContexts4() {


		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- SELF ANALYSIS OF CTXT")));
		


		double minDistanceToOraclePrediction = Double.POSITIVE_INFINITY;

		for (Context activatedContext : activatedContexts) {

			getAmas().data.contextNotFinished = false;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("MODEL DISTANCE", activatedContext.getName(),
					"" + activatedContext.lastDistanceToModel)));

			if (activatedContext.isChild()) {
				
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.PARAM_learningSpeed);
				getAmas().data.contextNotFinished = true;
				
			}
			else if (activatedContext.lastDistanceToModel < getPredicionPerformanceIndicator()) {
			//else if (currentDistanceToOraclePrediction < regressionPerformance.getPerformanceIndicator()) {
				
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.PARAM_learningSpeed); //TODO update all contexts ?


			}

			if (activatedContext.lastDistanceToModel < minDistanceToOraclePrediction) {
				minDistanceToOraclePrediction = activatedContext.lastDistanceToModel;
				getAmas().data.lastMinDistanceToRegression = minDistanceToOraclePrediction;
			}

			if (!getAmas().data.contextNotFinished) {
				criticalities.addCriticality("distanceToRegression", activatedContext.lastDistanceToModel);
				
			}
			
			activatedContext.criticalities.addCriticality("distanceToRegression", activatedContext.lastDistanceToModel);
			//getEnvironment().trace(new ArrayList<String>(Arrays.asList("ADD CRITICALITY TO CTXT", ""+activatedContext.getName(), ""+criticalities.getLastValues().get("distanceToRegression").size())));

			activatedContext.lastPrediction = activatedContext.getActionProposal();
			
			/*double maxCoef = 0.0;
			for(Double coef : activatedContext.getLocalModel().getCoef()) {
				if(Math.abs(coef)> maxCoef) {
					maxCoef = Math.abs(coef);
				}
			}*/

			
		}

		
		
		for (int i = 0; i< activatedContexts.size() ; i++) {
			
			activatedContexts.get(i).criticalities.updateMeans();
			
			if (activatedContexts.get(i).criticalities.getCriticalityMean("distanceToRegression") != null) {
				
				activatedContexts.get(i).regressionPerformance.update(activatedContexts.get(i).criticalities.getCriticalityMean("distanceToRegression"));
				getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("UPDATE REGRESSION PERFORMANCE", activatedContexts.get(i).getName(), ""+activatedContexts.get(i).regressionPerformance.getPerformanceIndicator())));
			}
			
			
			activatedContexts.get(i).analyzeResults5(this);

		}



	}

	private void NCSDetection_Uselessness() {
		getAmas().data.executionTimes[9]=System.currentTimeMillis();

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("NCS DECTECTION USELESSNESS IN SELF ANALISIS")));
		for (Context ctxt : activatedNeighborsContexts) {

			if (!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}

		}

		getAmas().data.executionTimes[9]=System.currentTimeMillis()- getAmas().data.executionTimes[9];
	}

	/*private void setNearestContextAsBestContext() {
		Context nearestContext = this.getNearestContext(activatedNeighborsContexts);

		if (nearestContext != null) {
			getAmas().data.prediction = nearestContext.getActionProposal();
		} else {
            setPredictionWithoutContextAgent();
		}

		bestContext = nearestContext;
	}*/



    private void setPredictionWithoutContextAgent() {
		if (getAmas().getContexts().isEmpty()) {
			if (getAmas().getCycle() <= 1) {
				setPredictionToZero();
			}else{
				setPredictionToMean();
			}
		} else {
			useLastBestContext();
		}

	}

	private void setPredictionToZero(){
		getAmas().data.prediction = 0.0;
		getEnvironment().print(TRACE_LEVEL.INFORM,"Prediction set to zero");
	}

	private void useLastBestContext() {
		bestContext = lastUsedContext;
		getAmas().data.prediction = bestContext.getActionProposal();
		getEnvironment().print(TRACE_LEVEL.INFORM,"Use last Best Context",bestContext.getName());
	}

	private void setPredictionToMean() {
		getAmas().data.prediction = (getAmas().data.maxPrediction + getAmas().data.minPrediction) / 2;
		getEnvironment().print(TRACE_LEVEL.INFORM, "Prediction set to mean", getAmas().data.prediction, "Max prediction", getAmas().data.maxPrediction, "Min prediction", getAmas().data.minPrediction);
	}

	/**
	 * Gets the nearest good context.
	 *
	 * @param allContext the all context
	 * @return the nearest good context
	 */
	public Context getNearestGoodContext(ArrayList<Context> contextNeighbors) {

		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Context ctxt : contextNeighbors) {
			if (ctxt != newContext && !ctxt.isDying()) {
				if(ctxt.lastDistanceToModel < getPredicionPerformanceIndicator()){
					double externalDistanceToContext = getExternalDistanceToContext(ctxt);
					if (nearest == null || externalDistanceToContext < distanceToNearest) {

						nearest = ctxt;
						distanceToNearest = externalDistanceToContext;
					}
				}

			}
		}

		return nearest;

	}

	/*public Context getSmallestGoodContext(ArrayList<Context> neighbors) {
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

	}*/

	/*private Pair<Context, Double> getbestContextInNeighborsWithDistanceToModel(ArrayList<Context> contextNeighbors) {
		getAmas().data.executionTimes[17]=System.currentTimeMillis();


		double d = Double.MAX_VALUE;
		Context bestContextInNeighbors = null;
		
		Double averageDistanceToModels = getPredicionPerformanceIndicator();
		
		for (Context c : contextNeighbors) {
			
			double currentDistanceToOraclePrediction = c.getLocalModel()
					.distance(c.getCurrentExperiment());
			
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("MODEL DISTANCE FOR FATHER CTXT", c.getName(),
					"" + c.getLocalModel().distance(c.getCurrentExperiment()))));
			
			if (currentDistanceToOraclePrediction < averageDistanceToModels) {
				if(currentDistanceToOraclePrediction < d) {
					d = currentDistanceToOraclePrediction;
					bestContextInNeighbors = c;
				}
				

			}
			
		}
		getAmas().data.executionTimes[17]=System.currentTimeMillis()- getAmas().data.executionTimes[17];
		return new Pair<Context, Double>(bestContextInNeighbors, d);
	}*/


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

	private Context getNearestContextWithoutAllPercepts(List<Context> contextNeighboors) {
		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Context c : contextNeighboors) {
			if (c != newContext && !c.isDying()) {
				double externalDistanceToContext = getExternalDistanceToContextWithSubPercepts(c);
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
		ArrayList<Percept> percepts;
		percepts = getAmas().getPercepts();
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

	private double getExternalDistanceToContextWithSubPercepts(Context context) {
		double d = 0.0;
		ArrayList<Percept> percepts;
		percepts = getAmas().getSubPercepts();

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

	/**
	 * One of proposed context was good.
	 *
	 * @return true, if successful
	 */
	private boolean oneOfProposedContextWasGood() {
		boolean b = false;
		for (Context c : activatedContexts) {
			if (c.getLocalModel().distance(c.getCurrentExperiment()) < getPredicionPerformanceIndicator()) {
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
		getAmas().data.executionTimes[19]=System.currentTimeMillis();

		getAmas().data.executionTimes[24]=System.currentTimeMillis();
		getAmas().data.newContextWasCreated = true;
//		if (contexts.size() != 0) {
//			System.exit(0);
//		}
		getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		getAmas().data.executionTimes[24]=System.currentTimeMillis()- getAmas().data.executionTimes[24];
		getAmas().data.executionTimes[23]=System.currentTimeMillis();
		Context context;
		if (getAmas().data.firstContext) {
			//logger().debug("HEAD", "new context agent");
		} else {
			getAmas().data.firstContext = true;
		}
		getAmas().data.executionTimes[23]=System.currentTimeMillis()- getAmas().data.executionTimes[23];


		context = new Context(getAmas());


		getAmas().data.executionTimes[19]=System.currentTimeMillis()- getAmas().data.executionTimes[19];
		return context;
	}

	private Context createNewContext(Context bestNearestCtxt) {
		getAmas().data.executionTimes[18]=System.currentTimeMillis();

		getAmas().data.newContextWasCreated = true;
		getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		Context context;
		if (getAmas().data.firstContext) {

			//logger().debug("HEAD", "new context agent");
		} else {
			System.err.println("THERE SHOULD BE A FIRST CONTEXT");
			/*context = new Context(getAmas());
			getAmas().data.firstContext = true;*/
		}
		context = new Context(getAmas(), bestNearestCtxt);
		getAmas().data.executionTimes[18]=System.currentTimeMillis()- getAmas().data.executionTimes[18];

		return context;
	}

	private Context createNewContextWithoutOracle() {


		Context context = null;
		if (getAmas().data.firstContext) {

			getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
			//double endogenousPrediction = ((LocalModelMillerRegression)bestNearestCtxt.getLocalModel()).getProposition(bestNearestCtxt.getCurrentExperimentWithouOracle());
			Experiment currentExp = getAmas().getCurrentExperimentWithoutProposition();

			double endogenousPrediction;
			if(getAmas().getHeadAgent().getActivatedNeighborsContexts().size()>= getAmas().data.PARAM_nbOfNeighborForContexCreationWithouOracle){
				/*double weightedSumOfPredictions = 0;
				double normalisation = 0;
				for (Context ctxtNeighbor : getAmas().getHeadAgent().getActivatedNeighborsContexts()){


					double neighborDistance = ctxtNeighbor.distanceBetweenCurrentPercetionsAndBorders();
					weightedSumOfPredictions += ((LocalModelMillerRegression)ctxtNeighbor.getLocalModel()).getProposition(ctxtNeighbor.getCurrentExperimentWithouOracle())/neighborDistance;
					normalisation += 1/neighborDistance;


				}
				endogenousPrediction = weightedSumOfPredictions/normalisation;*/
				endogenousPrediction = getPredictionWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
						getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);

				currentExp.setProposition(endogenousPrediction);
				getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("CREATE CTXT WITHOUT ORACLE WITH NEIGHBORS", ""+this.getName())) );
				context = new Context(getAmas(), endogenousPrediction);
				getAmas().data.newContextWasCreated = true;
			}

			return context;
		}


		return null;
	}

	/**
	 * Update statistical informations.
	 */
	private void updateStatisticalInformations() {




		if(getAmas().data.oracleValue != null) {
			if(Math.abs(getAmas().data.oracleValue)>getAmas().data.maxPrediction) {
				getAmas().data.maxPrediction = Math.abs(getAmas().data.oracleValue);
				if(getAmas().multiUIWindow!=null){
					getAmas().multiUIWindow.guiData.maxPrediction=getAmas().data.maxPrediction;
				}

			}
			if(Math.abs(getAmas().data.oracleValue)<getAmas().data.minPrediction) {
				getAmas().data.minPrediction = Math.abs(getAmas().data.oracleValue);
				if(getAmas().multiUIWindow!=null){
					getAmas().multiUIWindow.guiData.minPrediction= getAmas().data.minPrediction;
				}

			}
			

			getAmas().data.normalizedCriticality = getAmas().data.criticity;// /getAmas().data.maxPrediction;
			criticalities.addCriticality("predictionCriticality", getAmas().data.normalizedCriticality);
			
			criticalities.updateMeans();

			/*if (severalActivatedContexts()) {

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

			}*/

			getAmas().data.predictionPerformance.update(criticalities.getCriticalityMean("predictionCriticality"));
			if (criticalities.getCriticalityMean("distanceToRegression") != null) {
				getAmas().data.regressionPerformance.update(criticalities.getCriticalityMean("distanceToRegression"));
			}
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

//	/**
//	 * Select best context.
//	 */
//	private void selectBestContextWithConfidenceAndVolume() {
//		if(activatedContexts != null && !activatedContexts.isEmpty()) {
//			Context bc;
//
//			bc = activatedContexts.get(0);
//			double currentConfidence = bc.getConfidence();
//
//			for (Context context : activatedContexts) {
//				double confidenceWithVolume = context.getConfidence()*context.getVolume();
//				if (confidenceWithVolume > currentConfidence) {
//					bc = context;
//					currentConfidence = confidenceWithVolume;
//				}
//			}
//			bestContext = bc;
//		} else {
//			bestContext = null;
//		}
//	}

	private void selectBestContextWithConfidence() {
		if(activatedContexts != null && !activatedContexts.isEmpty()) {
			Context bc;

			bc = activatedContexts.get(0);
			double currentConfidence = bc.getConfidence();

			for (Context context : activatedContexts) {
				double confidence = context.getConfidence();
				if (confidence > currentConfidence) {
					bc = context;
					currentConfidence = confidence;
				}
			}
			bestContext = bc;
		} else {
			bestContext = null;
		}
	}

	/*private void selectBestContextWithConfidenceOrDistance() {

		if(activatedContexts != null && !activatedContexts.isEmpty()) {
			Context bc;

			bc = activatedContexts.get(0);
			double currentConfidence = bc.getConfidence();
			boolean testIfSameConfidence = true;
			double testConfidence = activatedContexts.get(0).getConfidence();
			for (Context context : activatedContexts) {
				double confidence = context.getConfidence();
				if (confidence > currentConfidence) {
					bc = context;
					currentConfidence = confidence;
				}
				testIfSameConfidence = testIfSameConfidence && (context.getConfidence() == testConfidence);
			}
			bestContext = bc;
			if(testIfSameConfidence && activatedContexts.size()>1){
				bc = activatedContexts.get(0);
				double minDistance = bc.centerDistanceFromExperiment();
				bc.centerDistanceFromExperiment = minDistance;
				for (Context context : activatedContexts.subList(1,activatedContexts.size())) {
					context.centerDistanceFromExperiment = context.centerDistanceFromExperiment();
					if (context.centerDistanceFromExperiment < minDistance) {
						bc = context;
						minDistance = context.centerDistanceFromExperiment;
					}
				}
				bestContext = bc;
			}
		} else {
			bestContext = null;
		}


	}*/

	private void selectBestSubContextWithDistance() {

		if(activatedContexts != null && !activatedContexts.isEmpty()) {
			Context bc;

			bc = activatedContexts.get(0);
			double minDistance = bc.centerDistanceFromExperiment();
			bc.centerDistanceFromExperiment = minDistance;
			for (Context context : activatedContexts.subList(1,activatedContexts.size())) {
				context.centerDistanceFromExperiment = context.centerDistanceFromExperiment();
				if (context.centerDistanceFromExperiment < minDistance) {
					bc = context;
					minDistance = context.centerDistanceFromExperiment;
				}
			}
			bestContext = bc;

		} else {
			bestContext = null;
		}


	}

	/*private void selectBestContextWithDistance() {

		if(activatedContexts != null && !activatedContexts.isEmpty()) {

			if(activatedContexts.size()>1){
				HashMap<Context,Double> distancesFromPerception = new HashMap<>();
				for(Percept pct : getAmas().getSubPercepts()){
					for(Context ctxt : activatedContexts){
						if(distancesFromPerception.containsKey(ctxt)){
							double oldDistance = distancesFromPerception.get(ctxt);
							double distanceToAdd = Math.abs(ctxt.getRanges().get(pct).getCenter()-pct.getValue());
							distancesFromPerception.put(ctxt,oldDistance+ distanceToAdd);
						}else{
							distancesFromPerception.put(ctxt,Math.abs(ctxt.getRanges().get(pct).getCenter()-pct.getValue()));
						}
					}
				}

				Context bc = activatedContexts.get(0);
				double minDistance = distancesFromPerception.get(bc);
				for (Context context : activatedContexts.subList(1,activatedContexts.size())) {
					double distance = distancesFromPerception.get(context);
					if (distance < minDistance) {
						bc = context;
						minDistance = distance;
					}
				}
				bestContext = bc;
			}else{
				bestContext=activatedContexts.get(0);
			}
		}else{
			bestContext = null;
		}


	}*/
	
	

	/*private void selectBestContextWithDistanceToModelAndVolume() {

		Context bc;

		bc = activatedContexts.get(0);
		double distanceToModel = ((LocalModelMillerRegression) bc.getLocalModel()).distance(bc.getCurrentExperiment());
		double currentDistanceToModelWithVolume;

		for (Context context : activatedContexts) {

			currentDistanceToModelWithVolume = context.getLocalModel().distance(context.getCurrentExperiment())/context.getVolume();
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("DISTANCE / VOLUME ", context.getName(), ""+currentDistanceToModelWithVolume)));
			if (currentDistanceToModelWithVolume < distanceToModel) {
				bc = context;
				distanceToModel = currentDistanceToModelWithVolume;
			}
		}
		bestContext = bc;
	}*/

	/*private void selectBestContextWithDistanceToModel() {

		Context bc;

		bc = activatedContexts.get(0);
		double distanceToModel = ((LocalModelMillerRegression) bc.getLocalModel()).distance(bc.getCurrentExperiment());
		double currentDistanceToModel;

		for (Context context : activatedContexts) {

			currentDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), ""+currentDistanceToModel)));
			if (currentDistanceToModel < distanceToModel) {
				bc = context;
				distanceToModel = currentDistanceToModel;
			}
		}
		bestContext = bc;
	}*/

	/*private void selectBestContextWithDistanceToModelAndConfidance() {

		Context bc;

		bc = activatedContexts.get(0);
		double distanceToModel = ((LocalModelMillerRegression) bc.getLocalModel()).distance(bc.getCurrentExperiment());
		double inverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
		double criticality = ( 0.5 * distanceToModel ) + ( 0.5 * inverseConfidance ) ;

		double currentDistanceToModel;
		double currentInverseConfidance;
		double currentCricality;

		for (Context context : activatedContexts) {

			currentDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			currentInverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
			currentCricality = ( 0.5 * currentDistanceToModel ) + ( 0.5 * currentInverseConfidance ) ;
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "distance to model "+currentDistanceToModel, "confidence "+context.getConfidence(), "criticality "+currentCricality)));

			if (currentCricality < criticality) {
				bc = context;
				criticality = currentCricality;
			}
		}
		bestContext = bc;
	}*/


	private double getPredictionWithoutOracleWeighted(ArrayList<Context> contextsList, double confidenceWeigh, double volumeWeigh, double perceptionDistanceWeigh) {

		double weightedPredictionSum=0.0;
		double normalization=0.0;


		double currentInverseConfidance;
		double currentInverseVolume;
		double currentCricality;
		double currentPerceptionDistance;

		for (Context context : contextsList) {

			currentPerceptionDistance = getExternalDistanceToContext(context);
			currentInverseConfidance = 1/context.getNormalizedConfidenceWithParams(0.99,0.01);
			currentInverseVolume = 1/context.getVolume();
			currentCricality = (( confidenceWeigh * currentInverseConfidance )  + ( volumeWeigh * currentInverseVolume ) + ( perceptionDistanceWeigh * currentPerceptionDistance ))/(confidenceWeigh+volumeWeigh+ perceptionDistanceWeigh);
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "confidence "+context.getConfidence(), "normalized confidence "+(1/currentInverseConfidance), "volume "+1/currentInverseVolume, "perceptionDistance "+currentPerceptionDistance, "criticality "+currentCricality)));

			weightedPredictionSum += (1/currentCricality)*context.getLocalModel().getProposition();
			normalization += (1/currentCricality);
		}
		return weightedPredictionSum/normalization;
	}
	
	private void selectBestContextWithoutOracleWeighted(ArrayList<Context> contextsList, double confidenceWeigh, double volumeWeigh, double perceptionDistanceWeigh) {

		Context bc;

		bc = contextsList.get(0);

		double rangeDistance = getExternalDistanceToContext(bc);
		double inverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
		double inverseVolume = 1/bc.getVolume();
		double criticality = ( ( confidenceWeigh * inverseConfidance )  + ( volumeWeigh * inverseVolume ) + (perceptionDistanceWeigh*rangeDistance))/(confidenceWeigh+volumeWeigh+perceptionDistanceWeigh);



		double currentInverseConfidance;
		double currentInverseVolume;
		double currentCricality;
		double currentPerceptionDistance;

		for (Context context : contextsList) {

			currentPerceptionDistance = getExternalDistanceToContext(context);
			currentInverseConfidance = 1/context.getNormalizedConfidenceWithParams(0.99,0.01);
			currentInverseVolume = 1/context.getVolume();
			currentCricality = (( confidenceWeigh * currentInverseConfidance )  + ( volumeWeigh * currentInverseVolume ) + ( perceptionDistanceWeigh * currentPerceptionDistance ))/(confidenceWeigh+volumeWeigh+ perceptionDistanceWeigh);
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "confidence "+context.getConfidence(), "normalized confidence "+(1/currentInverseConfidance), "volume "+1/currentInverseVolume, "perceptionDistance "+currentPerceptionDistance, "criticality "+currentCricality)));
			//getEnvironment().print(TRACE_LEVEL.ERROR, "minConfidence", getAmas().data.minConfidence,"maxConfidence", getAmas().data.maxConfidence);
			if (currentCricality < criticality) {
				bc = context;
				criticality = currentCricality;
			}
		}
		bestContext = bc;
	}

	private void selectBestContextWithOracleWeighted(ArrayList<Context> contextsList, double distanceToModelWeigh, double confidenceWeigh, double volumeWeigh, double perceptionDistanceWeigh) {

		Context bc;

		bc = contextsList.get(0);
		double distanceToModel = (bc.getLocalModel()).distance(bc.getCurrentExperiment());
		double rangeDistance = getExternalDistanceToContext(bc);
		double inverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
		double inverseVolume = 1/bc.getVolume();
		double criticality = (( distanceToModelWeigh * distanceToModel ) + ( confidenceWeigh * inverseConfidance )  + ( volumeWeigh * inverseVolume ) + (perceptionDistanceWeigh*rangeDistance))/(distanceToModelWeigh+confidenceWeigh+volumeWeigh+perceptionDistanceWeigh);



		double currentInverseConfidance;
		double currentInverseVolume;
		double currentCricality;
		double currentPerceptionDistance;

		for (Context context : contextsList) {

			context.lastDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			currentPerceptionDistance = getExternalDistanceToContext(context);
			currentInverseConfidance = 1/context.getNormalizedConfidenceWithParams(0.99,0.01);
			currentInverseVolume = 1/context.getVolume();
			currentCricality = (( distanceToModelWeigh * context.lastDistanceToModel ) + ( confidenceWeigh * currentInverseConfidance )  + ( volumeWeigh * currentInverseVolume ) + ( perceptionDistanceWeigh * currentPerceptionDistance ))/(distanceToModelWeigh+confidenceWeigh+volumeWeigh+ perceptionDistanceWeigh);
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "distance to model "+context.lastDistanceToModel, "confidence "+context.getConfidence(), "normalized confidence "+(1/currentInverseConfidance), "volume "+1/currentInverseVolume, "perceptionDistance "+currentPerceptionDistance, "criticality "+currentCricality)));
			//getEnvironment().print(TRACE_LEVEL.ERROR, "minConfidence", getAmas().data.minConfidence,"maxConfidence", getAmas().data.maxConfidence);
			if (currentCricality < criticality) {
				bc = context;
				criticality = currentCricality;
			}
		}
		bestContext = bc;
	}

	private Context getGoodContextWithOracleWeighted(ArrayList<Context> contextsList, double distanceToModelWeigh, double confidenceWeigh, double volumeWeigh, double perceptionDistanceWeigh) {

		Context bc=null;

		double criticality = Double.POSITIVE_INFINITY;

		double currentInverseConfidance;
		double currentInverseVolume;
		double currentCricality;
		double currentPerceptionDistance;

		for (Context context : contextsList) {
			getEnvironment().print(TRACE_LEVEL.DEBUG,context.getName());
			context.lastDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			currentPerceptionDistance = getExternalDistanceToContext(context);
			currentInverseConfidance = 1/context.getNormalizedConfidenceWithParams(0.99,0.01);
			currentInverseVolume = 1/context.getVolume();
			currentCricality = (( distanceToModelWeigh * context.lastDistanceToModel ) + ( confidenceWeigh * currentInverseConfidance )  + ( volumeWeigh * currentInverseVolume ) + ( perceptionDistanceWeigh * currentPerceptionDistance ))/(distanceToModelWeigh+confidenceWeigh+volumeWeigh+ perceptionDistanceWeigh);
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "distance to model "+context.lastDistanceToModel, "confidence "+context.getConfidence(), "normalized confidence "+(1/currentInverseConfidance), "volume "+1/currentInverseVolume, "perceptionDistance "+currentPerceptionDistance, "criticality "+currentCricality)));
			//getEnvironment().print(TRACE_LEVEL.ERROR, "minConfidence", getAmas().data.minConfidence,"maxConfidence", getAmas().data.maxConfidence);
			if (currentCricality < criticality && context.lastDistanceToModel<getPredicionPerformanceIndicator()) {
				bc = context;
				criticality = currentCricality;
			}
		}
		return bc;
	}

	private void selectBestContextWithDistanceToModelConfidanceAndVolume(ArrayList<Context> contextsList) {

		Context bc;

		bc = contextsList.get(0);
		double distanceToModel = (bc.getLocalModel()).distance(bc.getCurrentExperiment());
		double inverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
		double inverseVolume = 1/bc.getVolume();
		double criticality = (( 0.5 * distanceToModel ) + ( 0.5 * inverseConfidance )  + ( 0.5 * inverseVolume ))/1.5;



		double currentInverseConfidance;
		double currentInverseVolume;
		double currentCricality;

		for (Context context : contextsList) {

			context.lastDistanceToModel = context.getLocalModel().distance(context.getCurrentExperiment());
			currentInverseConfidance = 1/bc.getNormalizedConfidenceWithParams(0.99,0.01);
			currentInverseVolume = 1/context.getVolume();
			currentCricality = (( 0.5 * context.lastDistanceToModel ) + ( 0.5 * currentInverseConfidance )  + ( 0.5 * currentInverseVolume ))/1.5;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DISTANCE  ", context.getName(), "distance to model "+context.lastDistanceToModel, "confidence "+context.getConfidence(), "normalized confidence "+(1/currentInverseConfidance), "volume "+1/currentInverseVolume, "criticality "+currentCricality)));
			//getEnvironment().print(TRACE_LEVEL.ERROR, "minConfidence", getAmas().data.minConfidence,"maxConfidence", getAmas().data.maxConfidence);
			if (currentCricality < criticality) {
				bc = context;
				criticality = currentCricality;
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

	
	public HashMap<String, Double> getHigherNeighborLastPredictionPercepts() {
		return getAmas().data.higherNeighborLastPredictionPercepts;
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
	public Double getOracleValue() {
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

	/*public double getAveragePredictionCriticityCopy() {
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
	}*/

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

	public ArrayList<Context> getActivatedNeighborsContexts() {
		return activatedNeighborsContexts;
	}
	
	public void setActivatedNeighborsContexts(ArrayList<Context> neighbors) {
		activatedNeighborsContexts = neighbors;
	}
	public void setActivatedSubNeighborsContexts(ArrayList<Context> subNeighbors) {
		activatedSubNeighborsContexts = subNeighbors;
	}




	public void clearAllUseableContextLists() {
		activatedContexts.clear();
		for (Context ctxt : activatedSubNeighborsContexts) {
			ctxt.isInSubNeighborhood = false;

		}
		for (Context ctxt : activatedNeighborsContexts) {
			ctxt.isInNeighborhood = false;
			ctxt.isActivated = false;

		}
		if(bestContext!=null){
			bestContext.isBest = false;
		}
		activatedNeighborsContexts.clear();
	}

	/*public Double getMaxRadiusForContextCreation(Percept pct) {
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

	}*/

	
	public Pair<Double, Double> getRadiusesForContextCreation(Percept pct) {
		return new Pair<Double, Double>(
				pct.getRadiusContextForCreation(),
				pct.getRadiusContextForCreation());
	}
	
	
	/*public Pair<Double, Double> getMaxRadiusesForContextCreation(Percept pct) {
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

	}*/

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

	public double getLastMinDistanceToRegression() {
		return getAmas().data.lastMinDistanceToRegression;
	}

	public double getDistanceToRegressionAllowed() {
		return getAmas().data.regressionPerformance.getPerformanceIndicator();
	}
	
	
	public boolean isActiveLearning() {
		return isSelfRequest() && getAmas().data.activeLearning;
	}
	
	public boolean isSelfLearning() {
		return isSelfRequest() && getAmas().data.selfLearning;
	}
	
	
	
	public HashMap<Percept, Double> getSelfRequest(){
		EndogenousRequest futureRequest = null;
		if(endogenousChildRequests.size()>0) {
			futureRequest = endogenousChildRequests.poll();
		}else if(endogenousRequests.size()>0) {

			futureRequest = pollRequest(endogenousRequests);
		/*}else if(endogenousSubRequests.size()>0) {
			futureRequest = pollRequest(endogenousSubRequests);*/
		}else if(endogenousDreamRequests.size()>0){
			futureRequest = endogenousDreamRequests.poll();
		}
		getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("FUTURE SELF LEARNING", ""+futureRequest)));


		lastEndogenousRequest = futureRequest;
		for(Context ctxt : futureRequest.getAskingContexts()) {
			ctxt.deleteWaitingRequest(futureRequest);
		}

		getAmas().data.requestCounts.put(futureRequest.getType(),getAmas().data.requestCounts.get(futureRequest.getType())+1);
		return futureRequest.getRequest();

	}
	
	public HashMap<Percept, Double> getActiveRequest(){
		EndogenousRequest futureRequest = null;
		if(endogenousChildRequests.size()>0) {
			futureRequest = endogenousChildRequests.poll();
		}else if(endogenousRequests.size()>0) {

			futureRequest = pollRequest(endogenousRequests);
		/*}else if(endogenousSubRequests.size()>0) {
			futureRequest = pollRequest(endogenousSubRequests);*/
		}else if(endogenousDreamRequests.size()>0){
		futureRequest = endogenousDreamRequests.poll();
		}
		getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("FUTURE ACTIVE LEARNING", ""+futureRequest)));

		lastEndogenousRequest = futureRequest;
		for(Context ctxt : futureRequest.getAskingContexts()) {
			ctxt.deleteWaitingRequest(futureRequest);
		}

		getAmas().data.requestCounts.put(futureRequest.getType(),getAmas().data.requestCounts.get(futureRequest.getType())+1);
		return futureRequest.getRequest();
	}

	public boolean requestIsEmpty(){
		return endogenousRequests.size()==0 && endogenousChildRequests.size() == 0;
	}

	private EndogenousRequest pollRequest(Queue<EndogenousRequest> endogenousRequests){

		if(endogenousRequests.element().getType() == REQUEST.VOID){
			ArrayList<EndogenousRequest> endogenousRequestList = new ArrayList<>(endogenousRequests);
			Random rdn = new Random();
			int i = rdn.nextInt(endogenousRequestList.size());
			endogenousRequests.remove(endogenousRequestList.get(i));
			return endogenousRequestList.get(i);
		}else{
			return endogenousRequests.poll();
		}

	}
	
	public EndogenousRequest getLastEndogenousRequest() {
		return lastEndogenousRequest;
	}
	
	public void resetLastEndogenousRequest() {
		currentEndogenousRequest = lastEndogenousRequest;
		lastEndogenousRequest = null;
	}


	
	public void deleteRequest(Context ctxt) {
		
	}
	
	public boolean isSelfChildRequest(){
		getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("ENDO CHILD REQUESTS", ""+endogenousChildRequests.size())));
		for(EndogenousRequest endoRequest : endogenousChildRequests) {
			getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("" + endoRequest)));
		}
		return endogenousChildRequests.size()>0;
	}

	public boolean isDreamRequest(){
		getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("ENDO DREAM REQUESTS", ""+endogenousDreamRequests.size())));

		if(getAmas().data.STATE_DreamCompleted==0 && endogenousDreamRequests.isEmpty()){
			getAmas().data.STATE_DreamCompleted=1;
		}

		return endogenousDreamRequests.size()>0;
	}

	public boolean isSubRequest(){
		getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("ENDO SUB REQUESTS", ""+endogenousSubRequests.size())));

		return endogenousSubRequests.size()>0;
	}
	
	public boolean isSelfRequest(){
		getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("ENDO REQUESTS", ""+endogenousRequests.size())));
		for(EndogenousRequest endoRequest : endogenousRequests) {
			getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList("" + endoRequest)));
		}
		return endogenousRequests.size()>0;
	}
	
	public void addChildRequest(HashMap<Percept, Double> request, int priority, Context ctxt){		
		
		//getAmas().data.activeLearning = true;
		addEndogenousRequest(new EndogenousRequest(request, null, priority,new ArrayList<Context>(Arrays.asList(ctxt)), REQUEST.MODEL), endogenousChildRequests);
	}

	public void addDreamRequest(HashMap<Percept, Double> request, int priority, Context ctxt){

		endogenousDreamRequests.add(new EndogenousRequest(request, null, priority,new ArrayList<Context>(Arrays.asList(ctxt)), REQUEST.DREAM));
	}
	
	public void addEndogenousRequest(EndogenousRequest request, Queue<EndogenousRequest> endogenousRequestsList) {
		
		boolean existingRequestTest = false;
		
		if(request.getAskingContexts().size()>1 || request.getType() == REQUEST.VOID || request.getType() == REQUEST.SUBVOID) {
			
			Iterator<EndogenousRequest> itr = endogenousRequestsList.iterator();
			while(!existingRequestTest && itr.hasNext()) {
				
				EndogenousRequest currentRequest = itr.next();

				if(currentRequest.getType() == REQUEST.CONFLICT || currentRequest.getType() == REQUEST.CONCURRENCE) {
					existingRequestTest = existingRequestTest || currentRequest.testIfContextsAlreadyAsked(request.getAskingContexts()); 
				}
				if(currentRequest.getType() == REQUEST.VOID) {
					existingRequestTest = existingRequestTest || currentRequest.requestInBounds(request.getRequest());
				}
				if(currentRequest.getType() == REQUEST.SUBVOID) {
					existingRequestTest = existingRequestTest || currentRequest.requestInBounds(request.getRequest());
				}
				
			}
			if(!existingRequestTest) {
				for(Context ctxt : request.getAskingContexts()) {
					ctxt.addWaitingRequest(request);
				}
				endogenousRequestsList.add(request);
				getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("NEW ADDED ENDO REQUEST", ""+request)));
			}
		}else {
			request.getAskingContexts().get(0).addWaitingRequest(request);
			endogenousRequestsList.add(request);
			getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("NEW ADDED ENDO REQUEST", ""+request)));
		}
		
		
		
	}
	
	/*public boolean isRealVoid(HashMap<Percept, Double> request) {
		boolean test;
		
		for(Context ctxt : activatedNeighborsContexts) {
			
			
			
			test = true;
			for(Percept pct : getAmas().getPercepts()) {
//				test = test && ctxt.getRanges().get(pct).contains2(request.get(pct));
				test = test && ctxt.getRanges().get(pct).contains(request.get(pct), pct.getMappingErrorAllowedMin());
			}
			
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("IS REAL VOID",ctxt.getName(), "-->", ""+!test)));
			
			if(test) {
				return false;
			}
		}
		return true;
		
		
	}*/
	
	public Double getPredicionPerformanceIndicator() {
		
		//return getAmas().data.averageRegressionPerformanceIndicator; //TODO solution ?
		return getAmas().data.PARAM_initRegressionPerformance;
		
	}
	
	public void setAverageRegressionPerformanceIndicator() {
		
		int numberOfRegressions = 0;
		if(activatedNeighborsContexts.size()>0) {
			double meanRegressionPerformanceIndicator = 0.0;
			for(Context ctxt : activatedNeighborsContexts) {
					meanRegressionPerformanceIndicator += ctxt.regressionPerformance.performanceIndicator;
					numberOfRegressions+=1;
			}
			assert numberOfRegressions != 0;
			getAmas().data.averageRegressionPerformanceIndicator =  (meanRegressionPerformanceIndicator/numberOfRegressions > getAmas().data.PARAM_initRegressionPerformance) ? meanRegressionPerformanceIndicator/numberOfRegressions :  getAmas().data.PARAM_initRegressionPerformance;
		}
		else{
			getAmas().data.averageRegressionPerformanceIndicator = getAmas().data.PARAM_initRegressionPerformance;
		}
		
	}
	

	public void proposition(Context c) {
		activatedContexts.add(c);
	}

	public void neigborhoodProposition(Context c) {
		activatedNeighborsContexts.add(c);
	}
	
	@Override
	protected void onInitialization() {
		super.onInitialization();

		criticalities = new Criticalities(getAmas().data.numberOfCriticityValuesForAverage);
		//endogenousCriticalities = new Criticalities(getAmas().data.numberOfCriticityValuesForAverageforVizualisation);
	}

	public ArrayList<VOID> getVoidsFromContextsAndZone(HashMap<Percept, Pair<Double, Double>> zoneBounds, ArrayList<Context> contexts) {
		ArrayList<VOID> currentVoids = new ArrayList<>();
		currentVoids.add(new VOID(zoneBounds));

		for(Context testedCtxt : contexts){

			ArrayList<VOID> newVoids = new ArrayList<>();
			for(VOID currentVoid : currentVoids){

				ArrayList<Percept> computedPerceptsInit = new ArrayList<>();
				ArrayList<VOID> voidsToAdd = testedCtxt.getVoidsFromZone(currentVoid.bounds, computedPerceptsInit);
				newVoids.addAll(voidsToAdd);

			}
			currentVoids = newVoids;

		}
		return currentVoids;
	}

	public ArrayList<VOID> getSubVoidsFromContextsAndZone(HashMap<Percept, Pair<Double, Double>> zoneBounds, ArrayList<Context> contexts) {
		ArrayList<VOID> currentVoids = new ArrayList<>();
		currentVoids.add(new VOID(zoneBounds));

		for(Context testedCtxt : contexts){

			ArrayList<VOID> newVoids = new ArrayList<>();
			for(VOID currentVoid : currentVoids){

				ArrayList<Percept> computedPerceptsInit = new ArrayList<>();
				ArrayList<VOID> voidsToAdd = testedCtxt.getSubVoidsFromZone(currentVoid.bounds, computedPerceptsInit);
				newVoids.addAll(voidsToAdd);

			}
			currentVoids = newVoids;

		}
		return currentVoids;
	}

	public double getMinMaxPredictionRange(){
		return getAmas().data.maxPrediction-getAmas().data.minPrediction;
	}

	public double getPredictionNeighborhoodRange(){
		return getMinMaxPredictionRange()*getEnvironment().getMappingErrorAllowed()*getAmas().data.PARAM_neighborhoodMultiplicator;
		//return getMinMaxPredictionRange()*0.25 ;
		//return getMinMaxPredictionRange()*0.25 ;
	}


    public static double normalizeZeroOne(double dispersion, double value) {
        return ( 1 / (1 + Math.exp(-value / dispersion)));
    }
}
