package agents.head;

import java.util.*;

import agents.EllsaAgent;
import agents.context.Context;
import agents.context.Experiment;
import agents.context.VOID;
import agents.percept.INPUT;
import agents.percept.Percept;
import experiments.mathematicalModels.Model_Manager;
import kernel.ELLSA;
import kernel.StudiedSystem;
import kernel.World;
import ncs.NCS;
import utils.Pair;
import utils.RAND_REPEATABLE;
import utils.TRACE_LEVEL;

/**
 * The Class Head.
 */
public class Head extends EllsaAgent {

	// MEMBERS ---------------------
	
	private Context bestContext = null;
	private Context lastUsedContext = null;
	private Context newContext;



	public Criticalities criticalities;


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


		getAmas().data.executionTimes[6]=System.nanoTime();
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
		}
		getAmas().data.executionTimes[6]=System.nanoTime()- getAmas().data.executionTimes[6];

		getAmas().data.executionTimes[7]=System.nanoTime();
		testIfrequest();
		updateStatisticalInformations(); // to be looked in detail, may not be useful
		newContext = null;
		getAmas().data.executionTimes[7]=System.nanoTime()- getAmas().data.executionTimes[7];

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



		getAmas().data.nPropositionsReceived = activatedContexts.size();
		getAmas().data.newContextWasCreated = false;

		getAmas().data.oldOracleValue = getAmas().data.oracleValue;
		getAmas().data.oracleValue = getAmas().getPerceptions("oracle");
		setAverageRegressionPerformanceIndicator();

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

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("\n\n")));

		if(getAmas().getCycle() % 50 == 0){
			if(lastEndogenousRequest != null){
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITH ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests() + "\t\t\t\t\t" + getAmas().data.currentINPUT)));
			}else{
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITH ORACLE" + " " + getWaitingEndogenousRequests() + "\t\t\t\t\t" + getAmas().data.currentINPUT)));
			}
		}
		if(lastEndogenousRequest != null){
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITH ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests() + "\t\t\t\t\t" + getAmas().data.currentINPUT)));
		}else{
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITH ORACLE" + " " + getWaitingEndogenousRequests() + "\t\t\t\t\t" + getAmas().data.currentINPUT)));
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
		/* Compute the criticality. Will be used by context agents. */

        if(getAmas().studiedSystem!=null){
            Double[] request = new Double[getAmas().getPercepts().size()];
            for(int i=0;i<getAmas().getPercepts().size();i++){
                request[i]=getAmas().getPercepts().get(i).getValue();
            }
            double oracleValueWithoutNoise = ((Model_Manager)(getAmas().studiedSystem)).modelWithoutNoise(request);

            if(getAmas().data.maxPrediction != Double.NEGATIVE_INFINITY && getAmas().data.minPrediction!=Double.POSITIVE_INFINITY && getAmas().data.minPrediction!=getAmas().data.maxPrediction ){
				getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ (getAmas().data.maxPrediction-getAmas().data.minPrediction);
				//getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ Math.abs(oracleValueWithoutNoise);
			}else{
				getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ Math.abs(oracleValueWithoutNoise);
			}


        }else{

			if(getAmas().data.maxPrediction != Double.NEGATIVE_INFINITY && getAmas().data.minPrediction!=Double.POSITIVE_INFINITY && getAmas().data.minPrediction!=getAmas().data.maxPrediction ){
				getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction)/ (getAmas().data.maxPrediction-getAmas().data.minPrediction);
				//getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ Math.abs(oracleValueWithoutNoise);
			}else{
				getAmas().data.criticity = Math.abs(getAmas().data.oracleValue - getAmas().data.prediction)/ Math.abs(getAmas().data.oracleValue);
			}
        }

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

			double oracleValueWithoutNoise = ((Model_Manager)(getAmas().studiedSystem)).modelWithoutNoise(request);

			if(getAmas().data.maxPrediction != Double.NEGATIVE_INFINITY && getAmas().data.minPrediction!=Double.POSITIVE_INFINITY && getAmas().data.minPrediction!=getAmas().data.maxPrediction ){
				getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ (getAmas().data.maxPrediction-getAmas().data.minPrediction);
				//getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ Math.abs(oracleValueWithoutNoise);
			}else{
				getAmas().data.criticity = Math.abs(oracleValueWithoutNoise - getAmas().data.prediction)/ Math.abs(oracleValueWithoutNoise);
			}
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

			selectBestContextWithOracleWeighted(activatedContexts, getAmas().data.PARAM_LEARNING_WEIGHT_ACCURACY,
					getAmas().data.PARAM_LEARNING_WEIGHT_EXPERIENCE,getAmas().data.PARAM_LEARNING_WEIGHT_GENERALIZATION,getAmas().data.PARAM_LEARNING_WEIGHT_PROXIMITY);

		} else if (activatedNeighborsContexts.size()>0){
            getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithOracle","withActivatedNeighborContexts",activatedNeighborsContexts.size());

            selectBestContextWithOracleWeighted(activatedNeighborsContexts, getAmas().data.PARAM_LEARNING_WEIGHT_ACCURACY,
					getAmas().data.PARAM_LEARNING_WEIGHT_EXPERIENCE,getAmas().data.PARAM_LEARNING_WEIGHT_GENERALIZATION,getAmas().data.PARAM_LEARNING_WEIGHT_PROXIMITY);
		} else if (getAmas().getContexts().size()>0 && getAmas().data.PARAM_NCS_isAllContextSearchAllowedForLearning){
            getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithOracle","withAllContexts",getAmas().getContexts().size());

            selectBestContextWithOracleWeighted(getAmas().getContexts(), getAmas().data.PARAM_LEARNING_WEIGHT_ACCURACY,
					getAmas().data.PARAM_LEARNING_WEIGHT_EXPERIENCE,getAmas().data.PARAM_LEARNING_WEIGHT_GENERALIZATION,getAmas().data.PARAM_LEARNING_WEIGHT_PROXIMITY);
        }

		if (bestContext != null) {
			//setContextFromPropositionWasSelected(true);
			getAmas().data.prediction = bestContext.getActionProposal();

		} else {
                setPredictionWithoutContextAgent();
		}


	}



	private void updatePerformanceIndicators() {


		double volumeOfAllContexts=getVolumeOfAllContexts();
		criticalities.addCriticality("spatialCriticality",
				(getMinMaxVolume() - volumeOfAllContexts) / getMinMaxVolume());

		getAmas().data.spatialGeneralizationScore = volumeOfAllContexts / getAmas().getContexts().size();


		getAmas().data.mappingPerformance.setPerformanceIndicator(getEnvironment().getMappingErrorAllowed());

		getAmas().data.evolutionCriticalityPrediction = (lembda * getAmas().data.evolutionCriticalityPrediction)
				+ ((1 - lembda) * getAmas().data.currentCriticalityPrediction);
		getAmas().data.evolutionCriticalityMapping = (lembda * getAmas().data.evolutionCriticalityMapping)
				+ ((1 - lembda) * getAmas().data.currentCriticalityMapping);
		getAmas().data.evolutionCriticalityConfidence = (lembda * getAmas().data.evolutionCriticalityConfidence)
				+ ((1 - lembda) * getAmas().data.currentCriticalityConfidence);



	}

	private void allNCSDetectionsWithOracle() {
		getAmas().data.executionTimes[8]=System.nanoTime();

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

		getAmas().data.executionTimes[8]=System.nanoTime()- getAmas().data.executionTimes[8];
	}


	private void allNCSDetectionsWithoutOracle() {
		getAmas().data.executionTimes[8]=System.nanoTime();

		NCSDetection_Uselessness();

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
		if(getAmas().data.PARAM_isExploitationActive && getAmas().data.currentINPUT == INPUT.EXOGENOUS_EXPLOITATION){
			NCSDetection_PotentialRequest();
		}
		resetLastEndogenousRequest();

		getAmas().data.executionTimes[8]=System.nanoTime()- getAmas().data.executionTimes[8];
	}



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



	/**
	 * Play without oracle.
	 */
	private void playWithoutOracle() {
		getAmas().data.oracleValue = null;


		if(getAmas().getCycle() % 50 == 0){
			if(lastEndogenousRequest != null){
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests()+ "\t\t" + getAmas().data.currentINPUT)));
			}else{
				getEnvironment().trace(TRACE_LEVEL.SUBCYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
						+ "---------------------------------------- PLAY WITHOUT ORACLE" + " " + getWaitingEndogenousRequests()+ "\t\t" + getAmas().data.currentINPUT)));
			}
		}

		if(lastEndogenousRequest != null){
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE \t" + lastEndogenousRequest.getType() + " " + getWaitingEndogenousRequests()+ "\t\t" + getAmas().data.currentINPUT)));
		}else{
			getEnvironment().trace(TRACE_LEVEL.CYCLE, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
					+ "---------------------------------------- PLAY WITHOUT ORACLE" + " " + getWaitingEndogenousRequests()+ "\t\t" + getAmas().data.currentINPUT)));
		}

		updateBestContextAndPropositionWithoutOracle();

		updateCriticalityWithoutOracle();

		updateNeighborContextLastPredictions();

		if(getAmas().data.PARAM_isSelfLearning && getAmas().getCycle()>getAmas().data.PARAM_bootstrapCycle){
			allNCSDetectionsWithoutOracle();
		}


		

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
		}*/

	}

	private int getWaitingEndogenousRequests() {
		return endogenousChildRequests.size()+endogenousDreamRequests.size()+endogenousRequests.size() + endogenousSubRequests.size();
	}



	private void updateBestContextAndPropositionWithoutOracle() {
		Double weightedPrediction = null;
		if (activatedContexts.size() > 0) {
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withActivatedContexts",activatedContexts.size());

			selectBestContextWithoutOracleWeighted(activatedContexts, getAmas().data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION,getAmas().data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY);

		} else if (activatedNeighborsContexts.size()>0){
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withActivatedNeighborContexts",activatedNeighborsContexts.size());

			selectBestContextWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION,getAmas().data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY);

			/*weightedPrediction = getPredictionWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_CONFIDENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_VOLUME,getAmas().data.PARAM_EXPLOITATION_WEIGHT_DISTANCE_TO_PERCEPTIONS);*/
		} else if (getAmas().getContexts().size()>0 && getAmas().data.PARAM_NCS_isAllContextSearchAllowedForExploitation){
			getEnvironment().print(TRACE_LEVEL.INFORM,"----------- updateBestContextWithoutOracle","withAllContexts",getAmas().getContexts().size());

			selectBestContextWithoutOracleWeighted(getAmas().getContexts(),  getAmas().data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE,
					getAmas().data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION,getAmas().data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY);

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



	
	private void NCSDetection_ChildContext() {
		getAmas().data.executionTimes[14]=System.nanoTime();

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




		getAmas().data.executionTimes[14]=System.nanoTime()- getAmas().data.executionTimes[14];
	}



	private void NCSDetection_Create_New_Context() {
		getAmas().data.executionTimes[12]=System.nanoTime();

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


			Context goodContext = getGoodContextWithOracleWeighted(activatedNeighborsContexts,getAmas().data.PARAM_LEARNING_WEIGHT_ACCURACY,
					getAmas().data.PARAM_LEARNING_WEIGHT_EXPERIENCE,getAmas().data.PARAM_LEARNING_WEIGHT_GENERALIZATION,getAmas().data.PARAM_LEARNING_WEIGHT_PROXIMITY);

			getAmas().data.requestCounts.put(REQUEST.NCS_CREATION,getAmas().data.requestCounts.get(REQUEST.NCS_CREATION)+1);
			Context context;
			if (goodContext != null && getAmas().data.PARAM_NCS_isCreationWithNeighbor) {
				getEnvironment().trace(TRACE_LEVEL.STATE, new ArrayList<String>(Arrays.asList(goodContext.getName(),
						"************************************* NEAREST GOOD CONTEXT")));
				context = createNewContext(goodContext);
			} else {
				context = createNewContext();
			}


			bestContext = context;
			newContext = context;
			newContextCreated = true;
			
			newContext.lastPrediction = newContext.getActionProposal();
			activatedSubNeighborsContexts.add(newContext);
			activatedNeighborsContexts.add(newContext);
			activatedContexts.add(newContext);

			
		}

		if (!newContextCreated) {
			updateStatisticalInformations();
		}

		resetLastEndogenousRequest();

		getAmas().data.executionTimes[12]=System.nanoTime()- getAmas().data.executionTimes[12];
	}

	private void NCSDetection_Create_New_ContextWithoutOracle() {
		getAmas().data.executionTimes[12]=System.nanoTime();

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

		getAmas().data.executionTimes[12]=System.nanoTime()- getAmas().data.executionTimes[12];
	}

	private void NCSDetection_Context_Overmapping() {
		getAmas().data.executionTimes[13]=System.nanoTime();

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
		


		getAmas().data.executionTimes[13]=System.nanoTime()- getAmas().data.executionTimes[13];
	}

	private void NCSDetection_Context_OvermappingWithouOracle() {
		getAmas().data.executionTimes[13]=System.nanoTime();

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



		getAmas().data.executionTimes[13]=System.nanoTime()- getAmas().data.executionTimes[13];
	}

	private void NCSDetection_ConcurrenceAndConlict() {
		getAmas().data.executionTimes[11]=System.nanoTime();

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
						getAmas().data.requestCounts.put(REQUEST.NCS_CONCURRENCY,getAmas().data.requestCounts.get(REQUEST.NCS_CONCURRENCY)+1);
					}else if(activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && !testSameModel && getAmas().data.PARAM_NCS_isConflictResolution){
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-4.0);
						getAmas().data.requestCounts.put(REQUEST.NCS_CONFLICT,getAmas().data.requestCounts.get(REQUEST.NCS_CONFLICT)+1);
					}


				}
			}
		}



		getAmas().data.executionTimes[11]=System.nanoTime()- getAmas().data.executionTimes[11];
	}

	private void NCSDetection_ConcurrenceAndConclictWithoutOracle() {
		getAmas().data.executionTimes[11]=System.nanoTime();

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
						getAmas().data.requestCounts.put(REQUEST.NCS_CONCURRENCY,getAmas().data.requestCounts.get(REQUEST.NCS_CONCURRENCY)+1);
					}
					else if(activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && !testSameModel && getAmas().data.PARAM_NCS_isConflictResolution){
						activatedContexts.get(i).solveNCS_Overlap(bestContext);
						activatedContexts.get(i).setConfidenceVariation(-4.0);
						getAmas().data.requestCounts.put(REQUEST.NCS_CONFLICT,getAmas().data.requestCounts.get(REQUEST.NCS_CONFLICT)+1);
					}
				}
			}
		}


		getAmas().data.executionTimes[11]=System.nanoTime()- getAmas().data.executionTimes[11];
	}

	private void NCSDetection_IncompetentHead() {
		/*
		 * If there isn't any proposition or only bad propositions, the head is
		 * incompetent. It needs help from a context.
		 */
		getAmas().data.executionTimes[10]=System.nanoTime();
		
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- NCS DETECTION INCOMPETENT HEAD")));

		if(activatedContexts.isEmpty()) {
			getAmas().data.requestCounts.put(REQUEST.NCS_UNPRODUCTIVITY,getAmas().data.requestCounts.get(REQUEST.NCS_UNPRODUCTIVITY)+1);

			Context nearestGoodContext = getGoodContextWithOracleWeighted(activatedNeighborsContexts,1.0,0.0,0.0,1.0);

			if (nearestGoodContext != null) {
				nearestGoodContext.solveNCS_IncompetentHead();
			}
		}

		getAmas().data.executionTimes[10]=System.nanoTime()- getAmas().data.executionTimes[10];
	}


	public void NCSDetection_Dream() {
		getAmas().data.executionTimes[16]=System.nanoTime();


		//if(getAmas().getCycle() % (500 * getAmas().getPercepts().size()) ==0 && getAmas().data.isDream){
		if(getAmas().getCycle() == getAmas().data.PARAM_DreamCycleLaunch && getAmas().data.PARAM_isDream){


			/*getAmas().data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning = 5;
			getAmas().data.PARAM_creationNeighborNumberForContexCreationWithouOracle = 5;*/
			//getEnvironment().PARAM_minTraceLevel = TRACE_LEVEL.DEBUG;

			//getEnvironment().print(TRACE_LEVEL.ERROR, PARAMS.traceLevel, getAmas().data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning, getAmas().data.PARAM_creationNeighborNumberForContexCreationWithouOracle);



			for(Context ctxt : getAmas().getContexts()){
				HashMap<Percept,Double> request = new HashMap<>();
				for(Percept pct : getAmas().getPercepts()){
					request.put(pct,ctxt.getRanges().get(pct).getRandom());
				}
				addDreamRequest(request,5,ctxt);

			}
			getAmas().data.STATE_DreamCompleted=0;
		}

		getAmas().data.executionTimes[16]=System.nanoTime()- getAmas().data.executionTimes[16];
	}

	public void NCSDetection_PotentialRequest() {
		getAmas().data.executionTimes[15]=System.nanoTime();

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




		getAmas().data.executionTimes[15]=System.nanoTime()- getAmas().data.executionTimes[15];
	}

	private void addPotentialVoidRequest() {
		boolean testVoid = true;


		if((getAmas().getCycle()> NEIGH_VOID_CYCLE_START && endogenousRequests.size()==0 && getAmas().data.PARAM_NCS_isVoidDetection) && testVoid){
			HashMap<Percept, Pair<Double, Double>> neighborhoodBounds = new HashMap<>();
			for(Percept pct : getAmas().getPercepts()){
				neighborhoodBounds.put(pct, new Pair<>( pct.getValue()-(pct.getNeigborhoodRadius()), pct.getValue()+(pct.getNeigborhoodRadius())));
			}
			ArrayList<VOID> detectedVoids = getVoidsFromContextsAndZone(neighborhoodBounds, activatedNeighborsContexts);

			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("DETECTED VOIDS", ""+detectedVoids.size())));

			/*List<VOID> detectedVoidsToShuffle = new ArrayList<>( detectedVoids ) ;
			Collections.shuffle( detectedVoidsToShuffle ) ;*/
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


				}
				if(isInMinMax && isNotTooSmall){
					if(getAmas().data.PARAM_isSelfLearning){
						if(activatedNeighborsContexts.size()>getAmas().data.PARAM_creationNeighborNumberForVoidDetectionInSelfLearning){
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
				
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.PARAM_exogenousLearningWeight);
				getAmas().data.contextNotFinished = true;
				
			}
			else if (activatedContext.lastDistanceToModel < getPredicionPerformanceIndicator()) {
			//else if (currentDistanceToOraclePrediction < regressionPerformance.getPerformanceIndicator()) {
				
				activatedContext.getLocalModel().updateModel(activatedContext.getCurrentExperiment(), getAmas().data.PARAM_exogenousLearningWeight); //TODO update all contexts ?


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
		getAmas().data.executionTimes[9]=System.nanoTime();

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("NCS DECTECTION USELESSNESS IN SELF ANALISIS")));
		for (Context ctxt : activatedNeighborsContexts) {

			if (!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}

		}

		getAmas().data.executionTimes[9]=System.nanoTime()- getAmas().data.executionTimes[9];
	}





    private void setPredictionWithoutContextAgent() {
		if (getAmas().getContexts().isEmpty() || lastUsedContext==null) {
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
		getAmas().data.executionTimes[19]=System.nanoTime();

		getAmas().data.executionTimes[24]=System.nanoTime();
		getAmas().data.newContextWasCreated = true;
//		if (contexts.size() != 0) {
//			System.exit(0);
//		}
		getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		getAmas().data.executionTimes[24]=System.nanoTime()- getAmas().data.executionTimes[24];
		getAmas().data.executionTimes[23]=System.nanoTime();
		Context context;
		if (getAmas().data.firstContext) {
			//logger().debug("HEAD", "new context agent");
		} else {
			getAmas().data.firstContext = true;
		}
		getAmas().data.executionTimes[23]=System.nanoTime()- getAmas().data.executionTimes[23];


		context = new Context(getAmas());


		getAmas().data.executionTimes[19]=System.nanoTime()- getAmas().data.executionTimes[19];
		return context;
	}

	private Context createNewContext(Context bestNearestCtxt) {
		getAmas().data.executionTimes[18]=System.nanoTime();
		getAmas().data.requestCounts.put(REQUEST.CREATION_WITH_NEIGHBOR,getAmas().data.requestCounts.get(REQUEST.CREATION_WITH_NEIGHBOR)+1);

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
		getAmas().data.executionTimes[18]=System.nanoTime()- getAmas().data.executionTimes[18];

		return context;
	}

	private Context createNewContextWithoutOracle() {


		Context context = null;
		if (getAmas().data.firstContext) {

			getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
			Experiment currentExp = getAmas().getCurrentExperimentWithoutProposition();

			double endogenousPrediction;
			if(getAmas().getHeadAgent().getActivatedNeighborsContexts().size()>= getAmas().data.PARAM_creationNeighborNumberForContexCreationWithouOracle){
				getAmas().data.requestCounts.put(REQUEST.NCS_CREATION,getAmas().data.requestCounts.get(REQUEST.NCS_CREATION)+1);

				endogenousPrediction = getPredictionWithoutOracleWeighted(activatedNeighborsContexts,  getAmas().data.PARAM_EXPLOITATION_WEIGHT_EXPERIENCE,
						getAmas().data.PARAM_EXPLOITATION_WEIGHT_GENERALIZATION,getAmas().data.PARAM_EXPLOITATION_WEIGHT_PROXIMITY);

				currentExp.setProposition(endogenousPrediction);
				getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("CREATE CTXT WITHOUT ORACLE WITH NEIGHBORS", ""+this.getName())) );
				context = new Context(getAmas(), endogenousPrediction);
				getAmas().data.situationsCounts.put(SITUATION.ENDOGENOUS_LEARNING,getAmas().data.situationsCounts.get(SITUATION.ENDOGENOUS_LEARNING)+1);
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
			if(getAmas().data.oracleValue>getAmas().data.maxPrediction) {
				getAmas().data.maxPrediction = getAmas().data.oracleValue;
				if(getAmas().multiUIWindow!=null){
					getAmas().multiUIWindow.guiData.maxPrediction=getAmas().data.maxPrediction;
				}

			}
			if(getAmas().data.oracleValue<getAmas().data.minPrediction) {
				getAmas().data.minPrediction = getAmas().data.oracleValue;
				if(getAmas().multiUIWindow!=null){
					getAmas().multiUIWindow.guiData.minPrediction= getAmas().data.minPrediction;
				}

			}
			

			getAmas().data.normalizedCriticality = getAmas().data.criticity;// /getAmas().data.maxPrediction;
			criticalities.addCriticality("predictionCriticality", getAmas().data.normalizedCriticality);
			
			criticalities.updateMeans();



			getAmas().data.predictionPerformance.update(criticalities.getCriticalityMean("predictionCriticality"));
			if (criticalities.getCriticalityMean("distanceToRegression") != null) {
				getAmas().data.regressionPerformance.update(criticalities.getCriticalityMean("distanceToRegression"));
			}
		}
		

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



	
	public Pair<Double, Double> getRadiusesForContextCreation(Percept pct) {
		return new Pair<Double, Double>(
				pct.getRadiusContextForCreation(),
				pct.getRadiusContextForCreation());
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



	public double getLastMinDistanceToRegression() {
		return getAmas().data.lastMinDistanceToRegression;
	}

	public double getDistanceToRegressionAllowed() {
		//return getAmas().data.regressionPerformance.getPerformanceIndicator();
		return getPredicionPerformanceIndicator();
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
			int i = RAND_REPEATABLE.randomInt(endogenousRequestList.size());
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
	

	
	public Double getPredicionPerformanceIndicator() {
		
		//return getAmas().data.averageRegressionPerformanceIndicator; //TODO solution ?
		return getAmas().data.PARAM_modelErrorMargin;
		
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
			getAmas().data.averageRegressionPerformanceIndicator =  (meanRegressionPerformanceIndicator/numberOfRegressions > getAmas().data.PARAM_modelErrorMargin) ? meanRegressionPerformanceIndicator/numberOfRegressions :  getAmas().data.PARAM_modelErrorMargin;
		}
		else{
			getAmas().data.averageRegressionPerformanceIndicator = getAmas().data.PARAM_modelErrorMargin;
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
		return getMinMaxPredictionRange()*getEnvironment().getMappingErrorAllowed()*getAmas().data.PARAM_neighborhoodRadiusCoefficient;

	}


}
