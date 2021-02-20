package agents.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import agents.EllsaAgent;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelMillerRegression;
import agents.head.Criticalities;
import agents.head.DynamicPerformance;
import agents.head.EndogenousRequest;
import agents.head.Head;
import agents.head.REQUEST;
import agents.percept.Percept;
import gui.ContextRendererFX;
import gui.RenderStrategy;
import kernel.ELLSA;
import ncs.NCS;
import utils.Pair;
import utils.RAND_NUM;
import utils.TRACE_LEVEL;





/**
 * The core agent of AMOEBA.
 * 
 */
public class Context extends EllsaAgent {
	// STATIC ---
	public static Class<? extends RenderStrategy> defaultRenderStrategy = ContextRendererFX.class;
	// ----------

	private HashMap<Percept, Range> ranges;
	private LocalModel localModel;
	private double confidence = 0.0;

	/**
	 * The number of time the context was activated (present in validContext). Used
	 * for visualization.
	 */
	private int activations = 0;
	private int nSelection = 0;
	private int tickCreation;

	private Double action;

	private Double actionProposition = null;
	public Double lastPrediction = null;
	//public Double smoothedPrediction = null;

	//private boolean valid;

	private ArrayList<EndogenousRequest> waitingRequests = new ArrayList<EndogenousRequest>();
	
	public DynamicPerformance regressionPerformance;
	public Criticalities criticalities ;
	public double lastDistanceToModel = -1.0;
	public double lastAverageRegressionPerformanceIndicator = -1.0;
	
	public static final double  augmentationFactorError = 0.5;
	public static final double  diminutionFactorError = 0.66;
	public static final double  minError = 1;
	public static final int successesBeforeDiminution = 5;
	public static final int errorsBeforeAugmentation = 5;

	public boolean modified = false;
	public boolean fusionned = false;
	public boolean restructured = false;
	public boolean isInNeighborhood = false;
	public boolean isInSubNeighborhood = false;
	public boolean isActivated = false;
	public boolean isBest = false;

	public double centerDistanceFromExperiment;
	public double externalDistanceFromExperiment;
	
	static final int VOID_CYCLE_START = 0;
	static final int OVERLAP_CYCLE_START = 0;

	public Queue<HashMap<Percept, Double>> childRequests = new ArrayDeque<>();

	private int[] childContextCounter = null;

	int lastFrontierRequestTick = 0;
	
	public Context(ELLSA ellsa) {
		super(ellsa);


		getAmas().data.executionTimes[21]=System.currentTimeMillis();
		if(getAmas().getHeadAgent().lastEndogenousRequest!=null){
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request",getAmas().getHeadAgent().lastEndogenousRequest);
		}else{
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request","null");
		}
		getAmas().data.executionTimes[21]=System.currentTimeMillis()- getAmas().data.executionTimes[21];

		buildContext();
		setConfidence(0.0);

		getAmas().data.executionTimes[22]=System.currentTimeMillis();
		criticalities = new Criticalities(5);
		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getPredicionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("CTXT CREATION", this.getName())));
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
		getAmas().data.executionTimes[22]=System.currentTimeMillis()- getAmas().data.executionTimes[22];
	}

	// FOR TEST ONLY
	public Context(ELLSA ellsa, HashMap<Percept, Range> manualRanges) {
		super(ellsa);
		buildContext(manualRanges);
		criticalities = new Criticalities(5);

		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getPredicionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("CTXT CREATION", this.getName())));
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
	}

	public Context(ELLSA ellsa, Context bestNearestContext) {
		super(ellsa);
		if(getAmas().getHeadAgent().lastEndogenousRequest!=null){
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request",getAmas().getHeadAgent().lastEndogenousRequest);
		}else{
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request","null");
		}
		buildContext(bestNearestContext);
		//setConfidence(bestNearestContext.confidence/2);
		setConfidence(0.5);
		//this.confidence = bestNearestContext.confidence;
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER", this.getName())));
		criticalities = new Criticalities(5);
		
		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getPredicionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
	}

	public Context(ELLSA ellsa, double endogenousPrediction) {
		super(ellsa);
		if(getAmas().getHeadAgent().lastEndogenousRequest!=null){
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request",getAmas().getHeadAgent().lastEndogenousRequest);
		}else{
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request","null");
		}
		buildContextWithoutOracle(endogenousPrediction);
		setConfidence(0.01);
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER", this.getName())));
		criticalities = new Criticalities(5);

		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getPredicionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
	}

	/*public Context(ELLSA ellsa, Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {
		super(ellsa);
		if(getAmas().getHeadAgent().lastEndogenousRequest!=null){
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request",getAmas().getHeadAgent().lastEndogenousRequest);
		}else{
			getEnvironment().print(TRACE_LEVEL.DEBUG,"Last endogenous request","null");
		}
		buildContext(fatherContext, contextDimensions);
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER AND DIM", this.getName())));
	}*/

	private void buildContextCommon() {
		this.tickCreation = getAmas().getCycle();
		action = getAmas().getHeadAgent().getOracleValue();
	}

	/**
	 * Builds the context.
	 */
	private void buildContext() {
		getAmas().data.executionTimes[20]=System.currentTimeMillis();

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();



		for (Percept p : var) {
			Range r = null;

			//Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(p);
			//TODO use neihbors sizes to define radiuses for creation !!!!!!!!!!!
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getRadiusesForContextCreation(p);

			
			if(getAmas().data.PARAM_isActiveLearning) {


				if(getAmas().getHeadAgent().lastEndogenousRequest != null) {
					if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.VOID) {
						r = initRangeFromVOID(p);
					}else if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.SUBVOID){
						r = initRangeFromSUBVOID(p,radiuses);
					}
				}
				if(r==null && getAmas().getHeadAgent().activatedNeighborsContexts.size()>0 ) {
					r = initRangeFromNeighbors(p);
				}
			}

			if(r==null ){
				r = initRange(p, radiuses);
			}
			

			// r = new Range(this, v.getValue() - radius, v.getValue() + radius, 0, true,
			// true, v, world);
			ranges.put(p, r);
			ranges.get(p).setValue(p.getValue());

			firstPoint.addDimension(p, p.getValue());

			p.addContextProjection(this);
		}



		//expand();

		localModel = getAmas().buildLocalModel(this);
		firstPoint.setProposition(getAmas().getHeadAgent().getOracleValue());
		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"NEW EXP",
		// firstPoint.toString())));

		localModel.updateModel(this.getCurrentExperiment(), getAmas().data.PARAM_exogenousLearningWeight);
		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));

		getAmas().data.executionTimes[20]=System.currentTimeMillis()- getAmas().data.executionTimes[20];
	}

	private Range initRange(Percept p, Pair<Double, Double> radiuses) {
		Range r;
		r = new Range(this, p.getValue() - radiuses.getA(), p.getValue() + radiuses.getB(), 0, true, true, p);
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by init", this.getName(), p.getName(), radiuses.getA().toString())));
		return r;
	}

	private Range initRangeFromNeighbors(Percept p) {
		Range r;
		//double radiusCreation = getAmas().getHeadAgent().minNeighborhoodRadius;
		double radiusCreation = getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p);
		//double radiusCreation = getAmas().getHeadAgent().minMeanNeighborhoodRaduises;
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by mean", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
		r = new Range(this, p.getValue() - radiusCreation, p.getValue() + radiusCreation, 0, true, true, p, getAmas().getHeadAgent().minMeanNeighborhoodStartIncrements, getAmas().getHeadAgent().minMeanNeighborhoodEndIncrements);
		return r;
	}

	private Range initRangeFromVOID(Percept p) {
		Range r;
		double startRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getA();
		double endRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getB();

		getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by VOID", this.getName(), p.getName())));
		//getAmas().getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by VOID", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
		r = new Range(this, startRange, endRange, 0, true, true, p); //TODO start increment from neighbors
		return r;
	}

	private Range initRangeFromSUBVOID(Percept p, Pair<Double, Double> radiuses) {
		Range r;
		double startRange ;
		double endRange;
		if(getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p)!=null){
			startRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getA();
			endRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getB();
		}else{
			startRange = p.getValue() - radiuses.getA();
			endRange = p.getValue() + radiuses.getB();
		}


		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by SUBVOID", this.getName(), p.getName())));
		r = new Range(this, startRange, endRange, 0, true, true, p); //TODO start increment from neighbors
		return r;
	}

	// FOR TEST ONLY
	private void buildContext(HashMap<Percept, Range> manualRanges) {

		//buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r = new Range(this, manualRanges.get(p).getStart(), manualRanges.get(p).getEnd(), 0, true, true, p);
				getAmas().getEnvironment()
						.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by init", this.getName(), p.getName())));


			ranges.put(p, r);
			ranges.get(p).setValue((manualRanges.get(p).getEnd() +  manualRanges.get(p).getStart())/2);

			firstPoint.addDimension(p, ranges.get(p).getValue());

			p.addContextProjection(this);

		}




		localModel = getAmas().buildLocalModel(this);
		/*firstPoint.setOracleProposition(getAmas().getHeadAgent().getOracleValue());


		localModel.updateModel(this.getCurrentExperiment(), getAmas().data.learningSpeed);*/
		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));


	}

//	private void buildContext(Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {
//
//		buildContextCommon();
//
//		ArrayList<Percept> var = getAmas().getPercepts();
//		for (Percept pct : var) {
//			Range r;
//			double center = contextDimensions.get(pct).getA();
//			double length = contextDimensions.get(pct).getB();
//			r = new Range(this, center - length / 2, center + length / 2, 0, true, true, pct);
//
//			ranges.put(pct, r);
//			ranges.get(pct).setValue(center);
//
//			pct.addContextProjection(this);
//		}
//
//		// expand();
//
//		this.confidence = fatherContext.confidence;
//
//		this.localModel = getAmas().buildLocalModel(this);
//		// this.formulaLocalModel = ((LocalModelMillerRegression)
//		// bestNearestContext.localModel).getFormula(bestNearestContext);
//		Double[] coef = fatherContext.localModel.getCoef();
//		this.localModel.setCoef(coef);
//		this.actionProposition = fatherContext.localModel.getProposition();
//
//		getAmas().addAlteredContext(this);
//		this.setName(String.valueOf(this.hashCode()));
//
//		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
//	}


	private void buildContextWithoutOracle(double endogenousPredicion) {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r = null;
			//Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(v);
			//TODO use neihbors sizes to define radiuses for creation !??? pas sûr ? 12/03/2020
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getRadiusesForContextCreation(p);


			if((getAmas().data.PARAM_isActiveLearning ||  getAmas().data.PARAM_isSelfLearning)) {



				if(getAmas().getHeadAgent().lastEndogenousRequest != null) {
					if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.VOID) {
						r = initRangeFromVOID(p);
					}else if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.SUBVOID){
						r = initRangeFromSUBVOID(p,radiuses);
					}
				}
				if(r==null && getAmas().getHeadAgent().activatedNeighborsContexts.size()>0) {
					r = initRangeFromNeighbors(p);
				}
			}
			if(r==null) {
				r = initRange(p, radiuses);
			}


			ranges.put(p, r);
			ranges.get(p).setValue(p.getValue());

			firstPoint.addDimension(p, p.getValue());

			p.addContextProjection(this);;
		}



		localModel = getAmas().buildLocalModel(this);
		firstPoint.setProposition(endogenousPredicion);
		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"NEW EXP",
		// firstPoint.toString())));

		localModel.updateModel(firstPoint, getAmas().data.PARAM_exogenousLearningWeight);
		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

	}

	private void buildContext(Context bestNearestContext) {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r = null;
			//Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(v);
			//TODO use neihbors sizes to define radiuses for creation !??? pas sûr ? 12/03/2020
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getRadiusesForContextCreation(p);
			

			if( (getAmas().data.PARAM_isActiveLearning ||  getAmas().data.PARAM_isSelfLearning)) {
				
				
				
				if(getAmas().getHeadAgent().lastEndogenousRequest != null) {
					if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.VOID) {
						r = initRangeFromVOID(p);
					}else if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.SUBVOID){
					r = initRangeFromSUBVOID(p,radiuses);
				}
				}
				if(r==null && getAmas().getHeadAgent().activatedNeighborsContexts.size()>0) {
					r = initRangeFromNeighbors(p);
				}
			}
			if(r==null) {
				r = initRange(p, radiuses);
			}


			ranges.put(p, r);
			ranges.get(p).setValue(p.getValue());

			firstPoint.addDimension(p, p.getValue());

			p.addContextProjection(this);;
		}

		//expand();

		//this.confidence = bestNearestContext.confidence;
		this.localModel = getAmas().buildLocalModel(this);
		// this.formulaLocalModel = ((LocalModelMillerRegression)
		// bestNearestContext.localModel).getFormula(bestNearestContext);
		Double[] coef = bestNearestContext.localModel.getCoef();
		this.localModel.setCoef(coef);
		this.actionProposition = bestNearestContext.localModel.getProposition();
		
		localModel.setFirstExperiments(new ArrayList<Experiment>(bestNearestContext.getLocalModel().getFirstExperiments()));

		Experiment currentExperiment = this.getCurrentExperiment();
		if(currentExperiment != null){
			localModel.updateModel(currentExperiment, getAmas().data.PARAM_exogenousLearningWeight);
		}


		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

	}


	private void nestedLoopOperation(int[] counters, int[] length, int level) {
		if(level == counters.length) addEndoChildRequest(counters);
		else {
			for (counters[level] = 0; counters[level] < length[level]; counters[level]++) {
				nestedLoopOperation(counters, length, level + 1);
			}
		}
	}

	private void addEndoChildRequest(int[] counters) {

		HashMap<Percept, Double> endoRequest = new HashMap<>();

		for (int level = 0; level < counters.length; level++) {
			Percept pct = getAmas().getPercepts().get(level);
			if (counters[level] == 0){
				endoRequest.put(pct, this.ranges.get(pct).getStart() + pct.getMappingErrorAllowedMin());
			}else{
				endoRequest.put(pct, this.ranges.get(pct).getEnd() - pct.getMappingErrorAllowedMin());
			}
		}
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<>(Arrays.asList(""+ endoRequest)));
		childRequests.add(endoRequest);

	}

	public void initEndoChildRequests(){

		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),"INIT ENDO CHILD REQUESTS")));
		int depth = getAmas().getPercepts().size();
		int[] length = new int[depth];
		int[] counters = new int[depth];
		Arrays.fill(counters,0);
		Arrays.fill(length,2);

		nestedLoopOperation(counters, length, 0);

	}

	/*public ArrayList<Context> getContextsOnAPerceptDirectionFromContextsNeighbors(ArrayList<Context> contextNeighbors,
			Percept pctDirection) {
		ArrayList<Context> contexts = new ArrayList<Context>();

		boolean test = true;
		for (Context ctxtNeigbor : contextNeighbors) {
			for (Percept pct : ranges.keySet()) {
				if (pct != pctDirection) {
					test = test && (this.ranges.get(pct).distance(ctxtNeigbor.getRanges().get(pct)) < 0);
				}
			}
			if (test) {
				contexts.add(ctxtNeigbor);
			}
		}
		return contexts;
	}*/

	public ArrayList<Context> getContextsOnAPerceptDirectionFromContextsNeighbors(ArrayList<Context> contextNeighbors,
			Percept pctDirection, SpatialContext expandingContext) {
		ArrayList<Context> contexts = new ArrayList<Context>();

		boolean test = true;
		for (Context ctxtNeigbor : contextNeighbors) {
			for (Percept pct : ranges.keySet()) {
				if (pct != pctDirection) {

					test = test && (expandingContext.distance(pct, ctxtNeigbor.getRanges().get(pct)) < -0.0001);
				}
			}
			if (test) {
				contexts.add(ctxtNeigbor);
			}

			test = true;
		}
		return contexts;
	}

	/*public void expand() {
		ArrayList<Context> neighborsOnOneDirection;
		HashMap<Percept, SpatialContext> alternativeContexts = new HashMap<Percept, SpatialContext>();
		double maxVolume = this.getVolume();
		double currentVolume;
		SpatialContext maxVolumeSpatialContext = null;

		for (Percept fixedPct : ranges.keySet()) {

			alternativeContexts.put(fixedPct, new SpatialContext(this));

			for (Percept pctDirectionForExpanding : ranges.keySet()) {

				if (pctDirectionForExpanding != fixedPct) {

					neighborsOnOneDirection = getContextsOnAPerceptDirectionFromContextsNeighbors(
							getAmas().getHeadAgent().getActivatedNeighborsContexts(), pctDirectionForExpanding,
							alternativeContexts.get(fixedPct));

					Pair<Double, Double> expandingRadiuses = getMaxExpansionsForContextExpansionAfterCreation(
							neighborsOnOneDirection, pctDirectionForExpanding);
					alternativeContexts.get(fixedPct).expandEnd(pctDirectionForExpanding, expandingRadiuses.getB());
					alternativeContexts.get(fixedPct).expandStart(pctDirectionForExpanding, expandingRadiuses.getA());
				}
			}

			currentVolume = alternativeContexts.get(fixedPct).getVolume();
			if (currentVolume > maxVolume) {
				maxVolume = currentVolume;
				maxVolumeSpatialContext = alternativeContexts.get(fixedPct);
			}

		}
		if (maxVolumeSpatialContext != null) {
			matchSpatialContextRanges(maxVolumeSpatialContext);
		}
	}*/

	public void matchSpatialContextRanges(SpatialContext biggerContextForCreation) {
		for (Percept pct : ranges.keySet()) {
			ranges.get(pct).setStart(biggerContextForCreation.getStart(pct));
			ranges.get(pct).setEnd(biggerContextForCreation.getEnd(pct));
		}
	}

	/*public Pair<Double, Double> getMaxExpansionsForContextExpansionAfterCreation(
			ArrayList<Context> contextNeighborsInOneDirection, Percept pct) {
		
		
		double startRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getStart());
		double endRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getEnd());
//		Pair<Double, Double> maxExpansions = new Pair<Double, Double>(
//				Math.min(pct.getRadiusContextForCreation() - startRadiusFromCreation,
//						Math.abs(pct.getMin() - ranges.get(pct).getStart())),
//				Math.min(pct.getRadiusContextForCreation() - endRadiusFromCreation,
//						Math.abs(pct.getMax() - ranges.get(pct).getEnd())));
		
		Pair<Double, Double> maxExpansions = new Pair<Double, Double>(
				pct.getRadiusContextForCreation() - startRadiusFromCreation,
				pct.getRadiusContextForCreation() - endRadiusFromCreation);

		double currentStartExpansion;
		double currentEndExpansion;

		// for(Context ctxt:partialNeighborContexts.get(pct)) {
		for (Context ctxt : contextNeighborsInOneDirection) {

			if (ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				currentEndExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));

				if (currentEndExpansion < maxExpansions.getB() && currentEndExpansion >= -0.00001) {
					if (Math.abs(currentEndExpansion) < 0.0001) {
						currentEndExpansion = 0.0;
					}
					maxExpansions.setB(currentEndExpansion);
				}
			}

			if (ctxt.getRanges().get(pct).centerDistance(pct.getValue()) > 0) {
				// Start radius
				currentStartExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));

				if (currentStartExpansion < maxExpansions.getA() && currentStartExpansion >= -0.00001) {
					if (Math.abs(currentStartExpansion) < 0.0001) {
						currentEndExpansion = 0.0;
					}
					maxExpansions.setA(currentStartExpansion);
				}
			}
		}

		return maxExpansions;
	}*/

	// --------------------------------NCS
	// Resolutions-----------------------------------------

	/**
	 * Solve NC S incompetent head.
	 *
	 * @param head the head
	 */
	public void solveNCS_IncompetentHead() {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS INCOMPETENT HEAD")));

		getEnvironment().raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges();
		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
	}

	/**
	 * Solve NC S concurrence.
	 *
	 * @param head the head
	 */
	public void solveNCS_Overlap(Context bestContext) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS OVERLAP")));

		getEnvironment().raiseNCS(NCS.CONTEXT_OVERLAP);
		this.shrinkRangesToJoinBorders(bestContext);

		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();

		//confidence-=0.5;

		modified = true;
	}

	/**
	 * Solve NC S uselessness.
	 *
	 * @param head the head
	 */
	public void solveNCS_Uselessness() {
		if (!isDying()) {
			getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
					"*********************************************************************************************************** SOLVE NCS USELESSNESS")));
			getEnvironment().raiseNCS(NCS.CONTEXT_USELESSNESS);
			this.destroy();
			getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
		}

	}

	/*public void analyzeResults3(Head head, Context closestContextToOracle) {
		if (head.getCriticity(this) < head.getErrorAllowed()) {
			confidence++;
		} else {
			if (this != closestContextToOracle) {
				this.solveNCS_BadPrediction(head);
			}
		}

//		if (head.getCriticity(this) > head.getErrorAllowed()) {
//			
//			
//			Context betterContext = null;//head.getBetterContext(this,head.getActivatedNeighborsContexts(), getErrorOnAllExperiments());
//			
//			if(betterContext != null) {
//				System.out.println(this.getName() + "<---" + betterContext.getName());
//				this.setModelFromBetterContext(betterContext);				
//				world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
//			}
//			else {
//				System.out.println("OLD COEFS " + localModel.coefsToString());
//				LocalModelAgent newBetterModel = tryNewExperiment();
//				
//				if(tryNewExperiment2()) {
//					System.out.println("NEW COEFS " + localModel.coefsToString());
//					world.getScheduler().getHeadAgent().setBadCurrentCriticalityPrediction();
//				}
//				else {
//					solveNCS_Conflict(head);
//					this.world.getScheduler().addAlteredContext(this);
//				}
//			}
//			
//		}else {
//			System.out.println("OLD COEFS " + localModel.coefsToString());
//			localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(),0.5);
//			System.out.println("NEW COEFS " + localModel.coefsToString());
//			confidence++;
//			
//		}

//		mappingCriticality = 0.0;
//		if(head.getActivatedNeighborsContexts().size()>0) {
//			for(Context ctxt : head.getActivatedNeighborsContexts()) {
//				mappingCriticality += this.distance(ctxt);
//			}
//			mappingCriticality /= head.getActivatedNeighborsContexts().size();
//		}

		// NCSDetection_OverMapping();

	}*/


	public boolean isSameModel(Context ctxt) {
		/*return this.getLocalModel().distance(this.getCurrentExperiment()) < getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator() &&
				ctxt.getLocalModel().distance(ctxt.getCurrentExperiment()) < getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator() &&*/
		double distanceBetweenModels =  this.getLocalModel().getModelDifference(ctxt.getLocalModel());
		double modelSimilarityThreshold = getAmas().data.PARAM_modelSimilarityThreshold;
		getEnvironment().print(TRACE_LEVEL.DEBUG,getName(),ctxt.getName(),"modelSimilarityDistance",distanceBetweenModels,"SimilatityThreshold",modelSimilarityThreshold);
        return distanceBetweenModels<modelSimilarityThreshold;
		//return  this.getLocalModel().getModelDifference(ctxt.getLocalModel())<(getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator());
	}

	public boolean isSameModelWithoutOracle(Context ctxt) {
		return isSameModel(ctxt);
		//return  this.getLocalModel().getModelDifference(ctxt.getLocalModel())<(getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator());
	}

	/*public void analyzeResults4(Head head) {
		
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- ANALYSE RESULTS " + this.getName())));
		
		lastDistanceToModel = getLocalModel().distance(this.getCurrentExperiment());
		lastAverageRegressionPerformanceIndicator = head.getAverageRegressionPerformanceIndicator();
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "distance to model",""+lastDistanceToModel, "regression performance", "" + lastAverageRegressionPerformanceIndicator)));
		if(head.getBestContext() == this){
			confidence++;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "CONFIDENCE ++")));
		}
		else if(isSameModel(head.getBestContext())) {
			confidence++;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "CONFIDENCE ++")));
		} else {
			if ( getAmas().data.contextFromPropositionWasSelected ){
				solveNCS_Overlap(head.getBestContext());
			}else{
				solveNCS_BadPrediction(head);
			}

		}
	}*/

	public void analyzeResults5(Head head) {

		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- ANALYSE RESULTS " + this.getName())));

		lastAverageRegressionPerformanceIndicator = head.getPredicionPerformanceIndicator();
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "distance to model",""+lastDistanceToModel, "regression performance", "" + lastAverageRegressionPerformanceIndicator)));

		if(lastDistanceToModel < lastAverageRegressionPerformanceIndicator){
			setConfidenceVariation(1);
			//confidence++;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "CONFIDENCE ++")));
			/*if ( this !=  head.getBestContext()) {
				solveNCS_Overlap(head.getBestContext());
			}*/
		} else if(!isChild()){
				solveNCS_BadPrediction(head);
			}
	}



//	public double distance(Context ctxt) {
//		double totalDistance = 1.0;
//		double currentDistance = 0.0;
//		int overlaps = 0;
//		int voids = 0;
//		
//		for(Percept pct : world.getScheduler().getPercepts()) {
//			currentDistance = this.distance(ctxt, pct);
//			
//			overlaps += ((currentDistance < 0) ? 1 : 0);
//			voids += ((currentDistance > 0) ? 1 : 0);
//			
//			totalDistance *= currentDistance;
//		}
//		
//		if(overlaps == world.getScheduler().getPercepts().size()) {
//			return - Math.abs(totalDistance);
//		}
//		else if((voids == 1) && (overlaps == world.getScheduler().getPercepts().size()-1)) {
//			
//		}
//		else {
//			return 0;
//		}
//	}

	public Pair<Double, Percept> distance(Context ctxt) {
		double minDistance = Double.POSITIVE_INFINITY;
		double maxDistance = Double.NEGATIVE_INFINITY;
		double currentDistance = 0.0;

		int overlapCounts = 0;
		Percept voidPercept = null;
		double voidDistance = 0.0;
		
		ArrayList<Percept> percepts = getAmas().getPercepts();
		for (Percept pct : percepts) {
			currentDistance = this.distance(ctxt, pct);
			overlapCounts = (currentDistance < 0) ? overlapCounts + 1 : overlapCounts;

			if (currentDistance > 0) {
				voidPercept = pct;
				voidDistance = currentDistance / pct.getMinMaxDistance();
			}

			currentDistance = Math.abs(currentDistance);

			minDistance = Math.min(minDistance, currentDistance / pct.getMinMaxDistance());
			maxDistance = Math.max(maxDistance, currentDistance / pct.getMinMaxDistance());

		}

		if (overlapCounts == percepts.size()) {
			return new Pair<Double, Percept>(-minDistance, null);
		} else if (overlapCounts == (percepts.size() - 1)) {
			return new Pair<Double, Percept>(voidDistance, voidPercept);
		} else {
			return new Pair<Double, Percept>(maxDistance, null);
		}

	}
	
	public double distanceBetweenCurrentPercetionsAndCenter(){
		double distance = 0;
		for(Percept pct : getAmas().getPercepts()){

			distance += Math.pow(ranges.get(pct).getCenter() - pct.getValue() ,2);

		}

		return Math.sqrt(distance);
	}

	public double distanceBetweenCurrentPercetionsAndBorders(){
		double distance = 0;
		for(Percept pct : getAmas().getPercepts()){

			distance += Math.min(Math.pow(ranges.get(pct).getStart() - pct.getValue() ,2),Math.pow(ranges.get(pct).getEnd() - pct.getValue() ,2));

		}

		return Math.sqrt(distance);
	}

	public double distanceBetweenCurrentPercetionsAndBordersWithSubPercepts(){
		double distance = 0;
		for(Percept pct : getAmas().getSubPercepts()){

			distance += Math.min(Math.pow(ranges.get(pct).getStart() - pct.getValue() ,2),Math.pow(ranges.get(pct).getEnd() - pct.getValue() ,2));

		}

		return Math.sqrt(distance);
	}

	public boolean currentPerceptionsFarEnoughOfCenter(){
		boolean test = true;
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList("currentPerceptionsFarEnoughOfCenter")));
		for(Percept pct : getAmas().getPercepts()){
			double distance = Math.abs(ranges.get(pct).getCenter() - pct.getValue());
			test = test || (distance > pct.getMinDistanceFromCenterForNewRegressionPoint());
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<>(Arrays.asList(pct.getName(), "distance " + distance, "threshold " + pct.getMappingErrorAllowedMin(), "test " + test)));

		}

		return test;
	}
	
	public HashMap<Percept, Double> boundsToRequest(HashMap<Percept, Pair<Double, Double>> bounds) {
		HashMap<Percept, Double> request = new HashMap<Percept, Double>();
		
		for(Percept pct : bounds.keySet()) {
			
			if(bounds.get(pct) != null) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("ENDO REQUESTS BOUNDS", pct.getName(),""+ bounds.get(pct).getA(),""+ bounds.get(pct).getB(), ""+((bounds.get(pct).getB() + bounds.get(pct).getA())/2)) ));
				request.put(pct, (bounds.get(pct).getB() + bounds.get(pct).getA())/2);
			}else {
				getEnvironment().trace(TRACE_LEVEL.ERROR, new ArrayList<String>(Arrays.asList("ENDO REQUESTS ERROR missing percept bounds")));
			}
			
			
		}
		
		return request;
	}

	public boolean isOverlaping(Context otherCtxt){
		boolean test = true;
		int indicePercept = 0;
		while(test && indicePercept<getAmas().getPercepts().size()){
			Percept currentPct = getAmas().getPercepts().get(indicePercept);
			test = test && this.distance(otherCtxt, currentPct) < -currentPct.getMappingErrorAllowedMin();
			indicePercept++;
		}

		return test;
	}

	public ArrayList<EndogenousRequest> endogenousRequest(Context ctxt) {
		
		HashMap<Percept, Double> voidDistances = new HashMap<Percept, Double>();
		HashMap<Percept, Double> overlapDistances = new HashMap<Percept, Double>();
		HashMap<Percept, Pair<Double, Double>> bounds = new HashMap<Percept, Pair<Double, Double>>();

		ArrayList<EndogenousRequest> potentialRequests = new ArrayList<>();
		
		double currentDistance = 0.0;

		//double currentDistanceToOraclePrediction = this.getLocalModel().distance(this.getCurrentExperiment());
		//double otherContextDistanceToOraclePrediction = ctxt.getLocalModel().distance(ctxt.getCurrentExperiment());
		//Double averageDistanceToOraclePrediction = getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator();
		//Double distanceDifference = Math.abs(currentDistanceToOraclePrediction-otherContextDistanceToOraclePrediction);
		//boolean differentModel=distanceDifference>averageDistanceToOraclePrediction;

		boolean differentModel=!isSameModel(ctxt);
		boolean discontinuity= Math.abs(this.lastPrediction - ctxt.lastPrediction)> getAmas().getHeadAgent().getPredictionNeighborhoodRange();

		int overlapCounts = 0;
		for (Percept pct : getAmas().getPercepts()) {

			currentDistance = this.distance(ctxt, pct);

			getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("FRONTIER TEST",pct.getName(), ""+this,""+ctxt, "distance", ""+currentDistance , "ErrorAllowedMin", ""+pct.getMappingErrorAllowedMin(), "differentModel", ""+differentModel)) );


			if(currentDistance<-pct.getMappingErrorAllowedMin() && getAmas().getCycle()>OVERLAP_CYCLE_START) {
				overlapCounts = addOverlapCount(ctxt, overlapDistances, bounds, currentDistance, overlapCounts, pct);
			}
			else if (currentDistance > pct.getMappingErrorAllowedMin() && getAmas().getCycle()>VOID_CYCLE_START) {
				addVoids(ctxt, voidDistances, bounds, currentDistance, pct);
			}

			//else if ( Math.abs(currentDistance)< pct.getMappingErrorAllowedMin() && differentModel && discontinuity && (RAND_NUM.oneChanceIn((int)(1/getAmas().data.PARAM_probabilityOfRangeAmbiguity)) ) && getAmas().data.PARAM_NCS_isFrontierRequest){
			else if ( Math.abs(currentDistance)< pct.getMappingErrorAllowedMin() && differentModel && discontinuity && (Math.random()<getAmas().data.PARAM_probabilityOfRangeAmbiguity ) && getAmas().data.PARAM_NCS_isFrontierRequest){
				HashMap<Percept, Pair<Double, Double>> frontierBounds = getFrontierBounds(ctxt, pct);

				if (frontierBounds.size()==getAmas().getPercepts().size()){
					addFrontierRequests(ctxt, potentialRequests, pct, frontierBounds);
					getEnvironment().print(TRACE_LEVEL.DEBUG, this.lastPrediction, ctxt.lastPrediction,getAmas().getHeadAgent().getPredictionNeighborhoodRange());
				}
			}
		}

		addPotentialConcurrencesAndConflictsRequests(ctxt, bounds, potentialRequests, differentModel, overlapCounts);

		/*else if(overlapCounts == getAmas().getPercepts().size()-1 && voidDistances.size() == 1 && getAmas().getCycle() > VOID_CYCLE_START) {
			
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("VOID", ""+this,""+ctxt)) );
			
			updateBoundsWithNeighborhood(bounds);
			
			HashMap<Percept, Double> request = boundsToRequest(bounds);

			
			if(request != null) {
				
				if(getAmas().getHeadAgent().isRealVoid(request) && getAmas().data.isVoidDetection) {
					potentialRequests.add( new EndogenousRequest(request, bounds, 5, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.VOID));
				}		
			}
		}*/


		return potentialRequests;
	}

	private void addVoids(Context ctxt, HashMap<Percept, Double> voidDistances, HashMap<Percept, Pair<Double, Double>> bounds, double currentDistance, Percept pct) {
		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("VOID",pct.getName(), ""+this,""+ctxt, "distance", ""+currentDistance)) );
		voidDistances.put(pct, currentDistance);
		bounds.put(pct, this.voidBounds(ctxt, pct));
	}

	private void addFrontierRequests(Context ctxt, ArrayList<EndogenousRequest> potentialRequests, Percept pct, HashMap<Percept, Pair<Double, Double>> frontierBounds) {
		HashMap<Percept, Double> frontierRequestLeft = boundsToRequest(frontierBounds);
		HashMap<Percept, Double> frontierRequestRight = boundsToRequest(frontierBounds);

		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("REQUEST ", ""+frontierRequestLeft, ""+frontierRequestRight)) );
		frontierRequestLeft.put(pct, frontierBounds.get(pct).getA()+ (pct.getMappingErrorAllowedMin()/2));
		frontierRequestRight.put(pct, frontierBounds.get(pct).getB()- (pct.getMappingErrorAllowedMin()/2));
		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("REQUEST ", ""+frontierRequestLeft, ""+frontierRequestRight)) );


		if(getAmas().getHeadAgent().requestIsEmpty() && getAmas().data.PARAM_NCS_isFrontierRequest){

			potentialRequests.add( new EndogenousRequest(frontierRequestLeft, frontierBounds, 3, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.FRONTIER));
			potentialRequests.add( new EndogenousRequest(frontierRequestRight, frontierBounds, 3, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.FRONTIER));
		}
	}

	private HashMap<Percept, Pair<Double, Double>> getFrontierBounds(Context ctxt, Percept pct) {
		HashMap<Percept, Pair<Double, Double>> frontierBounds = new HashMap<>();

		double leftBound ;
		double rightBound ;
		if(this.getRanges().get(pct).getCenter() < ctxt.getRanges().get(pct).getCenter() ){
			leftBound = this.getRanges().get(pct).getEnd() - pct.getMappingErrorAllowedMin();
			rightBound = ctxt.getRanges().get(pct).getStart() + pct.getMappingErrorAllowedMin();
		}
		else{
			leftBound = ctxt.getRanges().get(pct).getEnd() - pct.getMappingErrorAllowedMin();
			rightBound = this.getRanges().get(pct).getStart() + pct.getMappingErrorAllowedMin();
		}
		frontierBounds.put(pct, new Pair<>(leftBound, rightBound));
		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("FIRST BOUNDS", pct.getName(), ""+frontierBounds.get(pct))) );
		for(Percept otherPercept : getAmas().getPercepts()){
			if(otherPercept!=pct){
				double frontierOverlapDistance = this.distance(ctxt, otherPercept);
				if(frontierOverlapDistance < - otherPercept.getMappingErrorAllowedMin()){
					frontierBounds.put(otherPercept, this.overlapBounds(ctxt, otherPercept));
					getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("OTHERS BOUNDS", otherPercept.getName(), ""+frontierBounds.get(otherPercept))) );
				}


			}
		}
		return frontierBounds;
	}

	private int addOverlapCount(Context ctxt, HashMap<Percept, Double> overlapDistances, HashMap<Percept, Pair<Double, Double>> bounds, double currentDistance, int overlapCounts, Percept pct) {
		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("OVERLAP",pct.getName(), ""+this,""+ctxt)) );
		overlapCounts+=1;
		overlapDistances.put(pct, Math.abs(currentDistance));
		bounds.put(pct, this.overlapBounds(ctxt, pct));
		return overlapCounts;
	}

	private void addPotentialConcurrencesAndConflictsRequests(Context ctxt, HashMap<Percept, Pair<Double, Double>> bounds, ArrayList<EndogenousRequest> potentialRequests, boolean differentModel, int overlapCounts) {
		if (overlapCounts == getAmas().getPercepts().size() && getAmas().getCycle() > OVERLAP_CYCLE_START ) {

			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(getAmas().getPercepts().size() + "OVERLAPS", ""+this,""+ctxt)) );

			HashMap<Percept, Double> request = boundsToRequest(bounds);
			if(request != null) {
				//getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(this.getName(),"currentDistanceToOraclePrediction",""+ currentDistanceToOraclePrediction,"otherContextDistanceToOraclePrediction",""+ otherContextDistanceToOraclePrediction, "distanceDifference", ""+distanceDifference)));

				if(!differentModel && getAmas().data.PARAM_NCS_isConcurrenceDetection) {
					potentialRequests.add( new EndogenousRequest(request, bounds, 6, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.CONCURRENCE));
				}
				else if(differentModel &&  getAmas().data.PARAM_NCS_isConflictDetection){
					potentialRequests.add( new EndogenousRequest(request, bounds, 7, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.CONFLICT));
				}
			}
		}
	}


	private void updateBoundsWithNeighborhood(HashMap<Percept, Pair<Double, Double>> voidBounds) {

		
		
		for (HashMap.Entry<Percept,  Pair<Double, Double>> voidBound : voidBounds.entrySet()) {
			
			double neighborhoodRadius = voidBound.getKey().getNeigborhoodRadius();
			
			if(voidBound.getValue().getA()<voidBound.getKey().getValue()-neighborhoodRadius) {
				voidBound.getValue().setA(voidBound.getKey().getValue()-neighborhoodRadius);
			}
			if(voidBound.getKey().getValue()+neighborhoodRadius < voidBound.getValue().getB()) {
				voidBound.getValue().setB(voidBound.getKey().getValue()+neighborhoodRadius);
			}
			
		    
		    
		}
		

		
	}





	public ArrayList<VOID> getVoidsFromZone(HashMap<Percept, Pair<Double, Double>> zoneBounds, ArrayList<Percept> computedPercepts) {

		ArrayList<VOID> voidsToReturn = new ArrayList<>();

		if(computedPercepts.size() == getAmas().getPercepts().size()){
			return voidsToReturn;
		}else{
			Percept pct = selectOnePerceptNotComputed(computedPercepts);
			HashMap<String,ArrayList<Pair<Double,Double>>> voidsAndFilleds = get1DVoidsAndFilled(pct, zoneBounds);


			for(Pair<Double,Double> void1D : voidsAndFilleds.get("1D_Voids")){

				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<>( Arrays.asList("NEW VOID")));
				HashMap<Percept, Pair<Double, Double>> voidToAdd = new HashMap<>();
				voidToAdd.put(pct, void1D);
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(pct.getName(),""+void1D)));

				//ArrayList<HashMap<Percept, Pair<Double, Double>>> additionnalVoidsToAdd = new ArrayList<>();

				for(Percept otherPercept : getAllOtherPercepts(pct)){

					Pair<Double, Double> perceptZoneBounds = zoneBounds.get(otherPercept);
					double perceptZoneBoundsLength =  perceptZoneBounds.getB() -  perceptZoneBounds.getA(); //TODO smaller voids ?

					addOtherPerceptZoneBounds(voidToAdd, otherPercept, perceptZoneBounds, perceptZoneBoundsLength);

					/*if(additionnalVoidsToAdd.size()>0){

						for(HashMap<Percept, Pair<Double, Double>> additionnalVoid : new ArrayList<>(additionnalVoidsToAdd)){
							addOtherPerceptZoneBounds(additionnalVoid, otherPercept, perceptZoneBounds, perceptZoneBoundsLength);
						}
					}*/

					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(otherPercept.getName(),""+zoneBounds.get(otherPercept))));
				}

				voidsToReturn.add(new VOID(voidToAdd));
				/*if(additionnalVoidsToAdd.size()>0){
					for(HashMap<Percept, Pair<Double, Double>> additionnalVoid : additionnalVoidsToAdd){
						voidsToReturn.add(new VOID(additionnalVoid));
					}
				}*/
			}



			if(voidsAndFilleds.keySet().contains("1D_Filleds")){
				HashMap<Percept, Pair<Double, Double>> filledZoneToTest = new HashMap<>();
				filledZoneToTest.put(pct, voidsAndFilleds.get("1D_Filleds").get(0));
				for(Percept otherPercept : getAllOtherPercepts(pct)){
					filledZoneToTest.put(otherPercept, zoneBounds.get(otherPercept));
				}

				ArrayList<Percept> newComputedPercepts = new ArrayList<>(computedPercepts);
				newComputedPercepts.add(pct);

				voidsToReturn.addAll(getVoidsFromZone(filledZoneToTest, newComputedPercepts));
			}



			return voidsToReturn;
		}




	}


    public ArrayList<VOID> getSubVoidsFromZone(HashMap<Percept, Pair<Double, Double>> zoneBounds, ArrayList<Percept> computedPercepts) {

        ArrayList<VOID> voidsToReturn = new ArrayList<>();

        if(computedPercepts.size() == getAmas().getSubPercepts().size()){
            return voidsToReturn;
        }else{
            Percept pct = selectOneSubPerceptNotComputed(computedPercepts);
            HashMap<String,ArrayList<Pair<Double,Double>>> voidsAndFilleds = get1DVoidsAndFilled(pct, zoneBounds);


            for(Pair<Double,Double> void1D : voidsAndFilleds.get("1D_Voids")){

                getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<>( Arrays.asList("NEW VOID")));
                HashMap<Percept, Pair<Double, Double>> voidToAdd = new HashMap<>();
                voidToAdd.put(pct, void1D);
                getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(pct.getName(),""+void1D)));

                //ArrayList<HashMap<Percept, Pair<Double, Double>>> additionnalVoidsToAdd = new ArrayList<>();

                for(Percept otherPercept : getAllOtherSubPercepts(pct)){

                    Pair<Double, Double> perceptZoneBounds = zoneBounds.get(otherPercept);
                    double perceptZoneBoundsLength =  perceptZoneBounds.getB() -  perceptZoneBounds.getA(); //TODO smaller voids ?

                    addOtherPerceptZoneBounds(voidToAdd, otherPercept, perceptZoneBounds, perceptZoneBoundsLength);

                    /*if(additionnalVoidsToAdd.size()>0){

                        for(HashMap<Percept, Pair<Double, Double>> additionnalVoid : new ArrayList<>(additionnalVoidsToAdd)){
                            addOtherPerceptZoneBounds(additionnalVoid, otherPercept, perceptZoneBounds, perceptZoneBoundsLength);
                        }
                    }*/

                    getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(otherPercept.getName(),""+zoneBounds.get(otherPercept))));
                }

                voidsToReturn.add(new VOID(voidToAdd));
                /*if(additionnalVoidsToAdd.size()>0){
                    for(HashMap<Percept, Pair<Double, Double>> additionnalVoid : additionnalVoidsToAdd){
                        voidsToReturn.add(new VOID(additionnalVoid));
                    }
                }*/
            }



            if(voidsAndFilleds.keySet().contains("1D_Filleds")){
                HashMap<Percept, Pair<Double, Double>> filledZoneToTest = new HashMap<>();
                filledZoneToTest.put(pct, voidsAndFilleds.get("1D_Filleds").get(0));
                for(Percept otherPercept : getAllOtherPercepts(pct)){
                    filledZoneToTest.put(otherPercept, zoneBounds.get(otherPercept));
                }

                ArrayList<Percept> newComputedPercepts = new ArrayList<>(computedPercepts);
                newComputedPercepts.add(pct);

                voidsToReturn.addAll(getSubVoidsFromZone(filledZoneToTest, newComputedPercepts));
            }



            return voidsToReturn;
        }




    }


	private void addOtherPerceptZoneBounds(HashMap<Percept, Pair<Double, Double>> voidToAdd, Percept otherPercept, Pair<Double, Double> perceptZoneBounds, double perceptZoneBoundsLength) {
		/*if(perceptZoneBoundsLength> otherPercept.getRadiusContextForCreation()*2){
			HashMap<Percept, Pair<Double, Double>> aditionnalVoidToAdd = new HashMap<>();

			for (Map.Entry<Percept, Pair<Double, Double>> entry : voidToAdd.entrySet()) {
				aditionnalVoidToAdd.put(entry.getKey(), entry.getValue());
			}
			double middleBound = (perceptZoneBounds.getA() + perceptZoneBounds.getB())  / 2 ;
			Pair<Double, Double> perceptZoneBounds1 = new Pair<>(perceptZoneBounds.getA(), middleBound);
			Pair<Double, Double> perceptZoneBounds2 = new Pair<>(middleBound, perceptZoneBounds.getB());

			aditionnalVoidToAdd.put(otherPercept, perceptZoneBounds1);
			voidToAdd.put(otherPercept, perceptZoneBounds2);

			additionnalVoidsToAdd.add(aditionnalVoidToAdd);
		}else{
			voidToAdd.put(otherPercept, perceptZoneBounds);
		}*/
		//TODO ??

		voidToAdd.put(otherPercept, perceptZoneBounds);
	}


	private Percept selectOnePerceptNotComputed(ArrayList<Percept> computedPercepts) {
		if(computedPercepts.size()==0){
			return getAmas().getPercepts().get(0);
		}else{
			for(Percept pct : getAmas().getPercepts()){
				if(!computedPercepts.contains(pct)){
					return pct;
				}
			}
		}

		return null;
	}

	private Percept selectOneSubPerceptNotComputed(ArrayList<Percept> computedPercepts) {
		if(computedPercepts.size()==0){
			return getAmas().getSubPercepts().get(0);
		}else{
			for(Percept pct : getAmas().getSubPercepts()){
				if(!computedPercepts.contains(pct)){
					return pct;
				}
			}
		}

		return null;
	}

	private ArrayList<Percept> getAllOtherPercepts(Percept pct) {
		ArrayList<Percept> percepts = new ArrayList<>();

		for(Percept otherPct : getAmas().getPercepts()){
			if(otherPct != pct){
				percepts.add(otherPct);
			}
		}

		return percepts;
	}

	private ArrayList<Percept> getAllOtherSubPercepts(Percept pct) {
		ArrayList<Percept> percepts = new ArrayList<>();

		for(Percept otherPct : getAmas().getSubPercepts()){
			if(otherPct != pct){
				percepts.add(otherPct);
			}
		}

		return percepts;
	}



	//TODO
	public HashMap<String,ArrayList<Pair<Double,Double>>> get1DVoidsAndFilled(Percept pct, HashMap<Percept, Pair<Double, Double>> zoneBounds){
		HashMap<String,ArrayList<Pair<Double,Double>>> voidsAndFilleds = new HashMap<>();
		voidsAndFilleds.put("1D_Voids", new ArrayList<>());


		double zoneStart = zoneBounds.get(pct).getA();
		double zoneEnd = zoneBounds.get(pct).getB();
		double ctxtStart = this.getRanges().get(pct).getStart();
		double ctxtEnd = this.getRanges().get(pct).getEnd();


		if (ctxtStart <= zoneStart && zoneEnd <= ctxtEnd) {
			voidsAndFilleds.put("1D_Filleds", new ArrayList<>());
			voidsAndFilleds.get("1D_Filleds").add(new Pair<>(zoneStart, zoneEnd));

		} else if (zoneStart < ctxtStart && ctxtEnd < zoneEnd) {
			voidsAndFilleds.put("1D_Filleds", new ArrayList<>());
			voidsAndFilleds.get("1D_Voids").add(new Pair<>(zoneStart, ctxtStart));
			voidsAndFilleds.get("1D_Filleds").add(new Pair<>(ctxtStart, ctxtEnd));
			voidsAndFilleds.get("1D_Voids").add(new Pair<>(ctxtEnd, zoneEnd));

		} else if (ctxtStart <= zoneStart && zoneStart < ctxtEnd &&  ctxtEnd < zoneEnd){
			voidsAndFilleds.put("1D_Filleds", new ArrayList<>());
			voidsAndFilleds.get("1D_Filleds").add(new Pair<>(zoneStart, ctxtEnd));
			voidsAndFilleds.get("1D_Voids").add(new Pair<>(ctxtEnd, zoneEnd));

		} else if (zoneStart < ctxtStart && ctxtStart < zoneEnd && zoneEnd <= ctxtEnd){
			voidsAndFilleds.put("1D_Filleds", new ArrayList<>());
			voidsAndFilleds.get("1D_Voids").add(new Pair<>(zoneStart, ctxtStart));
			voidsAndFilleds.get("1D_Filleds").add(new Pair<>(ctxtStart, zoneEnd));

		}else{
			voidsAndFilleds.get("1D_Voids").add(new Pair<>(zoneStart, zoneEnd));
		}


		return voidsAndFilleds;
	}
	

	public double distanceAsVolume(Context ctxt) {
		double totalDistanceAsVolume = 1.0;

		for (Percept pct : getAmas().getPercepts()) {
			double currentDistance = this.distanceForVolume(ctxt, pct);
			totalDistanceAsVolume *= currentDistance;


		}

		return Math.abs(totalDistanceAsVolume);
	}

	public double externalDistance(Context otherContext){

		double distance = 0;

		for (Percept pct : getAmas().getPercepts()){
			double pctDistance = this.distance(otherContext,pct);
			if(pctDistance > 0){
				distance += Math.pow(pctDistance,2);
			}


		}

		return Math.sqrt(distance);
	}

	public double centerDistance(Context otherContext){

		double distance = 0;

		for (Percept pct : getAmas().getPercepts()){
			double pctDistance = centerDistance(otherContext, pct);
			distance += Math.pow(pctDistance,2);



		}

		return Math.sqrt(distance);
	}

	private double centerDistance(Context otherContext, Percept pct) {
		return this.getRanges().get(pct).centerDistance(otherContext.getRanges().get(pct));
	}


	public boolean isNearby(Context otherCtxt){
		boolean test = true;
		int indicePercept = 0;
		while(test && indicePercept<getAmas().getPercepts().size()){

			Percept currentPct = getAmas().getPercepts().get(indicePercept);
			double pctDistance = this.distance(otherCtxt,currentPct);
			test = test && Math.abs(pctDistance) < currentPct.getMappingErrorAllowedMin() ;
			indicePercept++;
		}

		return test;
	}



	public double maxDistance(Context ctxt) {
		double maxDistance = Double.NEGATIVE_INFINITY;

		for (Percept pct : getAmas().getPercepts()) {
			double currentDistance = this.distanceForMaxOrMin(ctxt, pct) / pct.getMinMaxDistance();
			if (currentDistance > maxDistance) {
				maxDistance = currentDistance;
			}


		}

		return maxDistance;
	}

	public double minDistance(Context ctxt) {
		double minDistance = Double.POSITIVE_INFINITY;

		for (Percept pct : getAmas().getPercepts()) {
			minDistance = Math.min(minDistance, this.distanceForMaxOrMin(ctxt, pct) / pct.getMinMaxDistance());

		}
		return minDistance;
	}

	/*public void NCSDetection_BetterNeighbor() {
		Context closestContextToOracle = this;
		double minDistanceToOraclePrediction = getLocalModel().distance(this.getCurrentExperiment());
		double currentDistanceToOraclePrediction = 0.0;

		for (Context ctxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {

			if (ctxt != this) {
				currentDistanceToOraclePrediction = ctxt.getLocalModel().distance(this.getCurrentExperiment());
				if (currentDistanceToOraclePrediction < minDistanceToOraclePrediction) {
					minDistanceToOraclePrediction = currentDistanceToOraclePrediction;
					closestContextToOracle = ctxt;
				}
			}

		}

		if (closestContextToOracle != this) {
			solveNCS_BetterNeighbor(closestContextToOracle);
		}

	}*/

	public void solveNCS_BetterNeighbor(Context betterContext) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(), betterContext.getName(),
				"*********************************************************************************************************** SOLVE NCS BETTER NEIGHBOR")));
		localModel = new LocalModelMillerRegression(this, betterContext.getLocalModel().getCoef(),
				betterContext.getLocalModel().getFirstExperiments());
	}

	public void NCSDetection_OverMapping() {




		for(Context ctxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {


			if(ctxt != this && !ctxt.isDying()) {

				boolean sameModel = isSameModel(ctxt);
				getEnvironment().print(TRACE_LEVEL.DEBUG, this.getName(),ctxt.getName(),"isSameModel",sameModel);
				if(sameModel) {




					for(Percept pct : ranges.keySet()) {

						boolean fusionTest = true;
						int sameRanges = 0;
						int sameBorders = 0;
						Percept sameBorderPercept = null;
						String range = "";

						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),pct.getName(), ""+Math.abs(this.distance(ctxt, pct)), "DISTANCE", "" + getEnvironment().getMappingErrorAllowed())));
						if(Math.abs(this.distance(ctxt, pct)) < pct.getMappingErrorAllowedOverMapping()){

							for(Percept otherPct : ranges.keySet()) {

								if(otherPct != pct) {

									double lengthDifference = Math.abs(ranges.get(otherPct).getLenght() - ctxt.getRanges().get(otherPct).getLenght());
									double centerDifference = Math.abs(ranges.get(otherPct).getCenter() - ctxt.getRanges().get(otherPct).getCenter());

									double startDifference = Math.abs(ranges.get(otherPct).getStart() - ctxt.getRanges().get(otherPct).getStart());
									double endDifference = Math.abs(ranges.get(otherPct).getEnd() - ctxt.getRanges().get(otherPct).getEnd());

									getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),otherPct.getName(), ""+lengthDifference,""+centerDifference, "LENGTH & CENTER DIFF", ""  + getEnvironment().getMappingErrorAllowed())));
									fusionTest = fusionTest && (lengthDifference < otherPct.getMappingErrorAllowedOverMapping()) && (centerDifference< otherPct.getMappingErrorAllowedOverMapping());

									sameRanges += (startDifference < otherPct.getMappingErrorAllowedOverMapping()) && (endDifference< otherPct.getMappingErrorAllowedOverMapping()) ? 1 : 0;

									if((startDifference < otherPct.getMappingErrorAllowedOverMapping()) && !(endDifference< otherPct.getMappingErrorAllowedOverMapping())  ||
											!(startDifference < otherPct.getMappingErrorAllowedOverMapping()) && (endDifference< otherPct.getMappingErrorAllowedOverMapping())){
										sameBorders +=1;
										sameBorderPercept = otherPct;

										if(startDifference < otherPct.getMappingErrorAllowedOverMapping()){
											range = "Start";
										}
										if(endDifference < otherPct.getMappingErrorAllowedOverMapping()){
											range = "End";
										}
									}


								}
							}

							if(fusionTest && getAmas().data.PARAM_NCS_isFusionResolution) {
								solveNCS_OverMapping(ctxt, pct);
							}
							else if(sameRanges == (getAmas().getPercepts().size()-2) && sameBorders == 1 && !this.restructured && !ctxt.restructured && !this.modified && !ctxt.modified && getAmas().data.PARAM_NCS_isRetrucstureResolution){



								solveNCS_Restructure(ctxt, sameBorderPercept, range, pct);
							}

						}
					}

				}

			}
		}

	}

	public void NCSDetection_OverMappingWithouOracle() {




		for(Context ctxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {


			if(ctxt != this && !ctxt.isDying()) {


				//if(distanceDifference<averageDistanceToOraclePrediction) {
				//if(distanceDifference<getAmas().data.initRegressionPerformance) { //TODO améliorer ?
				if(isSameModelWithoutOracle(ctxt)) {




					for(Percept pct : ranges.keySet()) {

						boolean fusionTest = true;
						int sameRanges = 0;
						int sameBorders = 0;
						Percept sameBorderPercept = null;
						String range = "";

						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),pct.getName(), ""+Math.abs(this.distance(ctxt, pct)), "DISTANCE", "" + getEnvironment().getMappingErrorAllowed())));
						if(Math.abs(this.distance(ctxt, pct)) < pct.getMappingErrorAllowedOverMapping()){

							for(Percept otherPct : ranges.keySet()) {

								if(otherPct != pct) {

									double lengthDifference = Math.abs(ranges.get(otherPct).getLenght() - ctxt.getRanges().get(otherPct).getLenght());
									double centerDifference = Math.abs(ranges.get(otherPct).getCenter() - ctxt.getRanges().get(otherPct).getCenter());

									double startDifference = Math.abs(ranges.get(otherPct).getStart() - ctxt.getRanges().get(otherPct).getStart());
									double endDifference = Math.abs(ranges.get(otherPct).getEnd() - ctxt.getRanges().get(otherPct).getEnd());

									getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),otherPct.getName(), ""+lengthDifference,""+centerDifference, "LENGTH & CENTER DIFF", ""  + getEnvironment().getMappingErrorAllowed())));
									fusionTest = fusionTest && (lengthDifference < otherPct.getMappingErrorAllowedOverMapping()) && (centerDifference< otherPct.getMappingErrorAllowedOverMapping());

									sameRanges += (startDifference < otherPct.getMappingErrorAllowedOverMapping()) && (endDifference< otherPct.getMappingErrorAllowedOverMapping()) ? 1 : 0;

									if((startDifference < otherPct.getMappingErrorAllowedOverMapping()) && !(endDifference< otherPct.getMappingErrorAllowedOverMapping())  ||
											!(startDifference < otherPct.getMappingErrorAllowedOverMapping()) && (endDifference< otherPct.getMappingErrorAllowedOverMapping())){
										sameBorders +=1;
										sameBorderPercept = otherPct;

										if(startDifference < otherPct.getMappingErrorAllowedOverMapping()){
											range = "Start";
										}
										if(endDifference < otherPct.getMappingErrorAllowedOverMapping()){
											range = "End";
										}
									}


								}
							}

							if(fusionTest) {
								solveNCS_OverMapping(ctxt, pct);
							}
							else if(sameRanges == (getAmas().getPercepts().size()-2) && sameBorders == 1 && !this.restructured && !ctxt.restructured && !this.modified && !ctxt.modified){
								solveNCS_Restructure(ctxt, sameBorderPercept, range, pct);
							}

						}
					}

				}

			}
		}

	}



	private void solveNCS_OverMapping(Context fusionContext, Percept frontierPercept) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS OVERMAPPING", this.getName(), fusionContext.getName())));
		getEnvironment().raiseNCS(NCS.CONTEXT_OVERMAPPING);


		for(Percept pct : getAmas().getPercepts()) {

			this.getRanges().get(pct).setEnd(Math.max(this.getRanges().get(pct).getEnd(), fusionContext.getRanges().get(pct).getEnd()));
			this.getRanges().get(pct).setStart(Math.min(this.getRanges().get(pct).getStart(), fusionContext.getRanges().get(pct).getStart()));

			/*if(pct ==  frontierPercept){
				this.getRanges().get(pct).setEnd(Math.max(this.getRanges().get(pct).getEnd(), fusionContext.getRanges().get(pct).getEnd()));
				this.getRanges().get(pct).setStart(Math.min(this.getRanges().get(pct).getStart(), fusionContext.getRanges().get(pct).getStart()));
			}else{
				this.getRanges().get(pct).setEnd(Math.min(this.getRanges().get(pct).getEnd(), fusionContext.getRanges().get(pct).getEnd()));
				this.getRanges().get(pct).setStart(Math.max(this.getRanges().get(pct).getStart(), fusionContext.getRanges().get(pct).getStart()));
			}*/

		}
		
		//confidence = this.getConfidence() + fusionContext.getConfidence(); // TODO too much ?
		//setConfidence( Math.max(this.getConfidence(),fusionContext.getConfidence()));
		setConfidence( this.getConfidence()+fusionContext.getConfidence());

		regressionPerformance.setPerformanceIndicator(Math.max(this.regressionPerformance.getPerformanceIndicator(), fusionContext.regressionPerformance.getPerformanceIndicator()));
		
		
		fusionContext.destroy();
		fusionned =  true;
		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();


		//getAmas().data.requestCounts.put(REQUEST.SELF,getAmas().data.requestCounts.get(REQUEST.SELF)+1);

		getAmas().data.requestCounts.put(REQUEST.FUSION,getAmas().data.requestCounts.get(REQUEST.FUSION)+1);


	}

	private void solveNCS_Restructure(Context otherContext, Percept sameBorderPercept, String range, Percept frontierPercept) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS RESTRUCTURE", this.getName(), otherContext.getName())));
		getEnvironment().raiseNCS(NCS.CONTEXT_RESTRUCTURE);

		if(this.getRanges().get(sameBorderPercept).getLenght() > otherContext.getRanges().get(sameBorderPercept).getLenght()){


			double shrinkingContextCurrentVolume = this.getVolume();
			double growingContextFutureVolume = otherContext.getVolumeAfterRestructuration(frontierPercept,otherContext.getRanges().get(frontierPercept).getLenght() +this.getRanges().get(frontierPercept).getLenght());

			if(growingContextFutureVolume>shrinkingContextCurrentVolume){
				if(range == "Start"){
					this.getRanges().get(sameBorderPercept).setStart(otherContext.getRanges().get(sameBorderPercept).getEnd());
				}
				if(range == "End"){
					this.getRanges().get(sameBorderPercept).setEnd(otherContext.getRanges().get(sameBorderPercept).getStart());
				}

				if(this.getRanges().get(frontierPercept).getCenter() < otherContext.getRanges().get(frontierPercept).getCenter()){
					otherContext.getRanges().get(frontierPercept).setStart(this.getRanges().get(frontierPercept).getStart());
				}else{
					otherContext.getRanges().get(frontierPercept).setEnd(this.getRanges().get(frontierPercept).getEnd());
				}
				restructured =  true;
				otherContext.restructured = true;
				//getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
				double newConfidenceRatioForShrinkingContext = this.getVolume()/shrinkingContextCurrentVolume;
				double newconfidenceForShrinkingContext = this.getConfidence()* newConfidenceRatioForShrinkingContext;
				otherContext.setConfidence(otherContext.getConfidence() + (this.getConfidence()*(1-newConfidenceRatioForShrinkingContext)));
				//otherContext.confidence = otherContext.getConfidence() + (this.getConfidence()*(1-newConfidenceRatioForShrinkingContext)); // TODO too much ?
				this.setConfidence(newconfidenceForShrinkingContext);
				//this.confidence = newconfidenceForShrinkingContext;
				getAmas().data.requestCounts.put(REQUEST.RESTRUCTURE,getAmas().data.requestCounts.get(REQUEST.RESTRUCTURE)+1);
				//getAmas().data.requestCounts.put(REQUEST.SELF,getAmas().data.requestCounts.get(REQUEST.SELF)+1);

			}


		}else{

			double shrinkingContextCurrentVolume = otherContext.getVolume();
			double growingContextFutureVolume = this.getVolumeAfterRestructuration(frontierPercept, otherContext.getRanges().get(frontierPercept).getLenght()+this.getRanges().get(frontierPercept).getLenght());
			if(growingContextFutureVolume>shrinkingContextCurrentVolume){
				if(range == "Start"){
					otherContext.getRanges().get(sameBorderPercept).setStart(this.getRanges().get(sameBorderPercept).getEnd());
				}
				if(range == "End"){
					otherContext.getRanges().get(sameBorderPercept).setEnd(this.getRanges().get(sameBorderPercept).getStart());
				}

				if(this.getRanges().get(frontierPercept).getCenter() < otherContext.getRanges().get(frontierPercept).getCenter()){
					this.getRanges().get(frontierPercept).setEnd(otherContext.getRanges().get(frontierPercept).getEnd());
				}else{
					this.getRanges().get(frontierPercept).setStart(otherContext.getRanges().get(frontierPercept).getStart());
				}
				restructured =  true;
				otherContext.restructured = true;
				//getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
				double newConfidenceRatioForShrinkingContext = otherContext.getVolume()/shrinkingContextCurrentVolume;
				double newconfidenceForShrinkingContext = otherContext.getConfidence()* newConfidenceRatioForShrinkingContext;
				this.setConfidence(this.getConfidence() + (otherContext.getConfidence()*(1-newConfidenceRatioForShrinkingContext)));
				//this.confidence = this.getConfidence() + (otherContext.getConfidence()*(1-newConfidenceRatioForShrinkingContext)); // TODO too much ?
				otherContext.setConfidence(newconfidenceForShrinkingContext);
				//otherContext.confidence = newconfidenceForShrinkingContext;
				getAmas().data.requestCounts.put(REQUEST.RESTRUCTURE,getAmas().data.requestCounts.get(REQUEST.RESTRUCTURE)+1);
				//getAmas().data.requestCounts.put(REQUEST.SELF,getAmas().data.requestCounts.get(REQUEST.SELF)+1);
			}


		}






	}
	
	public void solveNCS_ChildContext() {
		HashMap<Percept, Double> request = new HashMap<Percept, Double>();
		//if(getAmas().data.isActiveLearning){
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS CHILD WITH ORACLE", this.getName())));

		request = getRandomRequestInRanges();

		getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW ENDO REQUEST","10", ""+request, ""+this.getName())));
		getAmas().getHeadAgent().addChildRequest(request, 10,this);

		
	}

	public void solveNCS_ChildContextWithoutOracle() {

		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS CHILD WITHOUT ORACLE", this.getName())));



		while (isChild()){
			Experiment endoExp = new Experiment(this);

			endoExp = getRandomExperimentInRanges();
			/*for(Percept pct : getAmas().getPercepts()) {
				endoExp.addDimension(pct, getRandomValueInRangeNextToStartAndEnd(pct));
			}*/
			/*while(isToCloseToFirstExperimements(endoExp,((LocalModelMillerRegression)localModel).getFirstExperiments())){
				endoExp = new Experiment(this);
				for(Percept pct : getAmas().getPercepts()) {
					endoExp.addDimension(pct, getRandomRangeBorder(pct));
				}
				System.out.println("......................................................................................................CA ARRIVE !!!!!!!!!!!!!");
			}*/

			endoExp.setProposition(((LocalModelMillerRegression)this.getLocalModel()).getProposition(endoExp));
			getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList(this.getName(),"NEW ENDO EXP FROM ITSELF WITHOUT NEIGHBORS", ""+endoExp)));
			getLocalModel().updateModel(endoExp, getAmas().data.PARAM_exogenousLearningWeight);
			getAmas().data.requestCounts.put(REQUEST.MODEL,getAmas().data.requestCounts.get(REQUEST.MODEL)+1);
			getAmas().data.requestCounts.put(REQUEST.ENDOGENOUS,getAmas().data.requestCounts.get(REQUEST.ENDOGENOUS)+1);
			//getAmas().data.requestCounts.put(REQUEST.SELF,getAmas().data.requestCounts.get(REQUEST.SELF)+1);


		}

		if(getAmas().getHeadAgent().getActivatedNeighborsContexts().size()>getAmas().data.PARAM_nbOfNeighborForLearningFromNeighbors){


			/*while (getLocalModel().getFirstExperiments().size()< (getAmas().getPercepts().size() + 3 - getAmas().getHeadAgent().getActivatedNeighborsContexts().size())){
				Experiment endoExp = new Experiment(this);
				for(Percept pct : getAmas().getPercepts()) {
					endoExp.addDimension(pct, getRandomValueInRange(pct));
				}
				endoExp.setOracleProposition(((LocalModelMillerRegression)this.getLocalModel()).getProposition(endoExp));
				getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList(this.getName(),"NEW ENDO EXP FROM ITSELF BEFORE NEIGHBORS", ""+endoExp)));
				getLocalModel().updateModel(endoExp, getAmas().data.learningSpeed);

			}*/

			if(getAmas().data.PARAM_isLearnFromNeighbors){
				learnFromNeighbors();
			}



		}







		/*getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW ENDO REQUEST","10", ""+request, ""+this.getName())));
		getAmas().getHeadAgent().addChildRequest(request, 10,this);*/

	}

	public boolean isToCloseToFirstExperimements(Experiment newExperiment, ArrayList<Experiment> firstExperiements){
		for(Experiment firstExp : firstExperiements){
			if(newExperiment.distance(firstExp)<(getMinRangeLenght()/2)){
				return true;
			}
		}
		return false;
	}

	public boolean isToCloseToFirstExperimements(HashMap<Percept, Double> request, ArrayList<Experiment> firstExperiements){
		for(Experiment firstExp : firstExperiements){
			if(firstExp.distance(request)<(getMinRangeLenght()/2)){
				return true;
			}
		}
		return false;
	}

	public double getMinRangeLenght(){
		Double minRangeLength = Double.POSITIVE_INFINITY;
		for(Range rng : ranges.values()){

			if(rng.getLenght()<minRangeLength){
				minRangeLength = rng.getLenght();
			}
		}
		return minRangeLength;

	}

	public void learnFromNeighbors() {
		//System.err.println("LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		HashMap<Percept, Pair<Double, Double>> neighborhoodBounds = new HashMap<>();
		for(Percept pct : getAmas().getPercepts()){
			neighborhoodBounds.put(pct, new Pair<>( pct.getValue()-(pct.getNeigborhoodRadius()), pct.getValue()+(pct.getNeigborhoodRadius())));
		}

		ArrayList<Experiment> endoExperiments = new ArrayList<>();

		for(Context ctxtNeighbor : getAmas().getHeadAgent().getActivatedNeighborsContexts()){


			Experiment endoExp = new Experiment(this);
			//Experiment symetricalEndoExp = new Experiment(this);
			if(ctxtNeighbor != this) {
				Experiment centerExperiment = this.getCenterExperiment();
				for (Percept pct : getAmas().getPercepts()) {
					double start = Math.max(neighborhoodBounds.get(pct).getA(), ctxtNeighbor.getRanges().get(pct).getStart());
					double length = Math.min(neighborhoodBounds.get(pct).getB(), ctxtNeighbor.getRanges().get(pct).getEnd()) - start;
					double value;
					if(length<0){
						//System.err.println("INLUENCE");
						double externalInfluence = this.getEnvironment().getContextInfluenceExternalRadius(this, pct);
						double influenceStart = Math.max(neighborhoodBounds.get(pct).getA(), ctxtNeighbor.getRanges().get(pct).getStart() - externalInfluence);
						double influenceLength = Math.min(neighborhoodBounds.get(pct).getB(), ctxtNeighbor.getRanges().get(pct).getEnd() + externalInfluence) - influenceStart;
						value = getRandomValueInRange(influenceStart, influenceLength);
					}else{
						value = getRandomValueInRange(start, length);
					}

					endoExp.addDimension(pct, value);

					double rangeCenter = this.getRanges().get(pct).getCenter();
					//symetricalEndoExp.addDimension(pct, rangeCenter-Math.abs(rangeCenter-value));
					//endoExp.addDimension(pct, ctxtNeighbor.getRanges().get(pct).getCenter());
				}




				double neighborPrediction = ((LocalModelMillerRegression) ctxtNeighbor.getLocalModel()).getProposition(endoExp);
				endoExp.setProposition(neighborPrediction);
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "EXP", "" + endoExp)));

				double centerProposition = ((LocalModelMillerRegression) getLocalModel()).getProposition(centerExperiment);
				//symetricalEndoExp.setProposition(centerProposition- Math.abs(centerProposition - neighborPrediction));
				//getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "SYM EXP", "" + symetricalEndoExp)));

				if(Math.abs(lastPrediction-neighborPrediction)<getAmas().getHeadAgent().getPredictionNeighborhoodRange()){
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "NEW ENDO EXP FROM", ctxtNeighbor.getName(), "" + endoExp)));
					endoExperiments.add(endoExp);
					//endoExperiments.add(symetricalEndoExp);
				}
				//getLocalModel().updateModel(endoExp, getAmas().data.learningSpeed);
				//getAmas().data.requestCounts.put(REQUEST.NEIGHBOR,getAmas().data.requestCounts.get(REQUEST.NEIGHBOR)+1);
			}
		}

		boolean isLocalMinimum = false;
		if(endoExperiments.size()>0){
			if(endoExperiments.size()>1) {
				if(isLocalMinimum(endoExperiments)) {
					isLocalMinimum = true;
					getAmas().data.countLocalMinina ++;
					getEnvironment().print(TRACE_LEVEL.DEBUG,"LOCAL MINIMA COUNTS",getAmas().data.countLocalMinina);
				}
			}
			if(!isLocalMinimum){
				((LocalModelMillerRegression)getLocalModel()).updateModel(endoExperiments, getAmas().data.PARAM_endogenousLearningWeight);
				getAmas().data.requestCounts.put(REQUEST.NEIGHBOR,getAmas().data.requestCounts.get(REQUEST.NEIGHBOR)+endoExperiments.size());
				getAmas().data.requestCounts.put(REQUEST.ENDOGENOUS,getAmas().data.requestCounts.get(REQUEST.ENDOGENOUS)+endoExperiments.size());
				setConfidenceVariation(endoExperiments.size()*0.01);
				//confidence+=endoExperiments.size()*0.01;
			}

		}

		/*if(endoExperiments.size()>=getAmas().data.nbOfNeighborForLearningFromNeighbors){
			if(endoExperiments.size()>1){
				if(testIfNeighborsPredictionsAreAllSuperiorOrInferiorToContextCenterPrediction(endoExperiments)){ //TODO ???
				//if(testIfNeighborsPredictionsMeanAreInContextPredictionRanges(endoExperiments)){


					((LocalModelMillerRegression)getLocalModel()).updateModel(endoExperiments, getAmas().data.learningSpeed);
					getAmas().data.requestCounts.put(REQUEST.NEIGHBOR,getAmas().data.requestCounts.get(REQUEST.NEIGHBOR)+endoExperiments.size());

				}
			}else{
				if(endoExperiments.size()>0){
					((LocalModelMillerRegression)getLocalModel()).updateModel(endoExperiments, getAmas().data.learningSpeed);
					getAmas().data.requestCounts.put(REQUEST.NEIGHBOR,getAmas().data.requestCounts.get(REQUEST.NEIGHBOR)+endoExperiments.size());
				}
			}
		}*/
	}

	private boolean isLocalMinimum(ArrayList<Experiment> endoExperiments){
		Experiment centerExp = getCenterExperimentWithProposition();
        boolean distributionTest = false;

		if(areAllPropositionSuperiorOrInferiorToCenterPrediction(endoExperiments, centerExp.getProposition())){
            distributionTest = true;
            for(Percept pct: getAmas().getPercepts()){
                boolean testLeft = false;
                boolean testRight = false;
                int i = 0;
                while(i<endoExperiments.size() && !(testLeft && testRight)){
					testLeft = testLeft || (endoExperiments.get(i).getValuesAsHashMap().get(pct)< centerExp.getValuesAsHashMap().get(pct));
					testRight = testRight || (centerExp.getValuesAsHashMap().get(pct) < endoExperiments.get(i).getValuesAsHashMap().get(pct));
					i++;
				}
                distributionTest = distributionTest && testLeft && testRight;
            }
		}
        if(distributionTest){
			getEnvironment().print(TRACE_LEVEL.DEBUG, "LOCAL MINIMUM");
            getEnvironment().print(TRACE_LEVEL.DEBUG, centerExp);
            getEnvironment().print(TRACE_LEVEL.DEBUG, endoExperiments);
        }

        return distributionTest;
    }


	private boolean isLocalMinimum2(ArrayList<Experiment> endoExperiments){
		Experiment centerExp = getCenterExperimentWithProposition();
		boolean distributionTest = false;

		if(areAllPropositionSuperiorOrInferiorToCenterPrediction(endoExperiments, centerExp.getProposition())){
			distributionTest = true;
			for(Percept pct: getAmas().getPercepts()){
				boolean testLeft = false;
				boolean testRight = false;
				int i = 0;
				while(i<endoExperiments.size() && !(testLeft && testRight)){
					testLeft = testLeft || (endoExperiments.get(i).getValuesAsHashMap().get(pct)< centerExp.getValuesAsHashMap().get(pct));
					testRight = testRight || (centerExp.getValuesAsHashMap().get(pct) < endoExperiments.get(i).getValuesAsHashMap().get(pct));
					i++;
				}
				distributionTest = distributionTest && testLeft && testRight;
			}
		}
		if(distributionTest){
			getEnvironment().print(TRACE_LEVEL.DEBUG, "LOCAL MINIMUM");
			getEnvironment().print(TRACE_LEVEL.DEBUG, centerExp);
			getEnvironment().print(TRACE_LEVEL.DEBUG, endoExperiments);
		}

		return distributionTest;
	}


    private boolean areAllPropositionSuperiorOrInferiorToCenterPrediction(ArrayList<Experiment> endoExperiments, double centerprediction) {
        boolean initTest = centerprediction < endoExperiments.get(0).getProposition();

        boolean test1 = initTest;
        boolean test2 = initTest;
        for(Experiment endoExp : endoExperiments.subList(1,endoExperiments.size())){
            boolean localTest = centerprediction< endoExp.getProposition();
            test1 = test1 && localTest;
            test2 = test2 || localTest;

        }

        return  !(initTest!=test1  || initTest!=test2);
    }

    public double getCenterProposition(){
		Experiment centerExp = new Experiment(this);
		for (Percept pct : getAmas().getPercepts()) {
			centerExp.addDimension(pct, getRanges().get(pct).getCenter());
		}
		return ((LocalModelMillerRegression) this.getLocalModel()).getProposition(centerExp);
	}

	public Experiment getCenterExperimentWithProposition(){
		Experiment centerExp = new Experiment(this);
		for (Percept pct : getAmas().getPercepts()) {
			centerExp.addDimension(pct, getRanges().get(pct).getCenter());
		}
		centerExp.setProposition(getPropositionFromExperiment(centerExp));
		return centerExp;
	}

	public double getPropositionFromExperiment(Experiment exp){
		return ((LocalModelMillerRegression) this.getLocalModel()).getProposition(exp);
	}

    public Experiment getCenterExperiment(){
        Experiment centerExp = new Experiment(this);
        for (Percept pct : getAmas().getPercepts()) {
            centerExp.addDimension(pct, getRanges().get(pct).getCenter());
        }
        return centerExp;
    }


	private boolean testIfNeighborsPredictionsMeanAreInContextPredictionRanges(ArrayList<Experiment> endoExperiments){
		Experiment centerExp = new Experiment(this);
		for (Percept pct : getAmas().getPercepts()) {
			centerExp.addDimension(pct, getRanges().get(pct).getCenter());
		}
		double centerprediction = ((LocalModelMillerRegression) this.getLocalModel()).getProposition(centerExp);

		double meanPrediction = 0.0;
		for(Experiment endoExp : endoExperiments){
			meanPrediction += endoExp.getProposition();
		}
		meanPrediction /= endoExperiments.size();

		return  Math.abs(meanPrediction - centerprediction) < (getAmas().getHeadAgent().getPredictionNeighborhoodRange()/2);
	}

	public void solveNCS_LearnFromNeighbors(){

		Experiment currentExp = getCurrentExperimentWithouOracle();
		getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("CHILD EXPERIMENT",""+currentExp)));
		if(getAmas().getHeadAgent().getActivatedNeighborsContexts().size()> getAmas().data.PARAM_nbOfNeighborForLearningFromNeighbors){

			ArrayList<Context> neighborsToKeep = new ArrayList<>();
			for (Context ctxtNeighbor : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {
				if (ctxtNeighbor != this) {

					boolean test = true;
					for(Percept pct : getAmas().getPercepts()){
						if(!this.getRanges().get(pct).contains2(ctxtNeighbor.getRanges().get(pct).getCenter())){

							test = test && perceptIsContainedBetweentContextsCenter(ctxtNeighbor, pct);
						}
					}
					if(test){
						neighborsToKeep.add(ctxtNeighbor);
					}
				}
			}

			if(neighborsToKeep.size()>0){
				neighborsToKeep.add(this);
				getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("KEPT NEIGHBORS", ""+neighborsToKeep.size())) );
				double weightedSumOfPredictions = 0;
				double normalisation = 0;
				for (Context ctxtNeighbor : neighborsToKeep){
					getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("KEPT NEIGHBOR", ""+ctxtNeighbor.getName())) );
					double neighborDistance = ctxtNeighbor.distanceBetweenCurrentPercetionsAndBorders();
					weightedSumOfPredictions += ((LocalModelMillerRegression)ctxtNeighbor.getLocalModel()).getProposition(currentExp)/neighborDistance;
					normalisation += 1/neighborDistance;

				}

				currentExp.setProposition(weightedSumOfPredictions/normalisation);
				getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW CHILD ENDO LEARNING WITH NEIGHBORS", ""+this.getName())) );
			}else{
				currentExp.setProposition(((LocalModelMillerRegression)this.getLocalModel()).getProposition(currentExp));
				getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW CHILD ENDO LEARNING WITHOUT NEIGHBORS", ""+this.getName())) );
			}

		}else{
			currentExp.setProposition(((LocalModelMillerRegression)this.getLocalModel()).getProposition(currentExp));
			getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW CHILD ENDO LEARNING WITHOUT NEIGHBORS", ""+this.getName())) );
		}

		/*currentExp.setOracleProposition(((LocalModelMillerRegression)this.getLocalModel()).getProposition(currentExp));
		getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW CHILD ENDO LEARNING WITHOUT NEIGHBORS", ""+this.getName())) );*/
		getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("LEARNED EXP", ""+currentExp)) );
		this.getLocalModel().updateModel(currentExp, getAmas().data.PARAM_exogenousLearningWeight);
	}

	private boolean perceptIsContainedBetweentContextsCenter(Context ctxtNeighbor, Percept pct) {
		return (this.getRanges().get(pct).getCenter() < pct.getValue() && pct.getValue() < ctxtNeighbor.getRanges().get(pct).getCenter() ) || (ctxtNeighbor.getRanges().get(pct).getCenter() < pct.getValue() && pct.getValue() < this.getRanges().get(pct).getCenter());
	}

	/*public void solveNCS_FitWithNeighbors(){

		Context otherCtxt = getNearestContextFromNeighbors();

		if (otherCtxt != this && !this.isDying() && !otherCtxt.isDying() *//*&& this.isNearby(otherCtxt)*//* ) {

			Pair<ArrayList<Pair<Experiment, Experiment>>, ArrayList<Pair<Experiment, Experiment>>> closestExperimentsAndPivots = getClosestExperimentsPairs(otherCtxt, 1);



			//if(otherCtxt.isChild()){
			if(closestExperimentsAndPivots != null){

				ArrayList<Pair<Experiment, Experiment>> closestExperiments = closestExperimentsAndPivots.getA();
				ArrayList<Pair<Experiment, Experiment>> pivots = closestExperimentsAndPivots.getB();

				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("EXPS " + closestExperiments.size(), "\n"+closestExperiments+"")));
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("CTXTS", this.getName(), otherCtxt.getName())));

				for(Pair<Experiment, Experiment> pivot : pivots){

					double prediction = ((LocalModelMillerRegression)this.getLocalModel()).getProposition(pivot.getA());
					double otherPrediction = ((LocalModelMillerRegression)otherCtxt.getLocalModel()).getProposition(pivot.getB());
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("PREDICTIONS PIVOTS", prediction+"", otherPrediction+"")));

					pivot.getA().setProposition(prediction);
					pivot.getB().setProposition(otherPrediction);
					this.getLocalModel().updateModel(pivot.getA(), getAmas().data.PARAM_learningSpeed);
					otherCtxt.getLocalModel().updateModel(pivot.getB(), getAmas().data.PARAM_learningSpeed);
				}

				*//*for(Pair<Experiment, Experiment> pairExp : closestExperiments){

					double prediction = ((LocalModelMillerRegression)this.getLocalModel()).getProposition(pairExp.getA());
					double otherPrediction = ((LocalModelMillerRegression)otherCtxt.getLocalModel()).getProposition(pairExp.getB());
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("PREDICTIONS", prediction+"", otherPrediction+"")));
					double meanPrediction = (prediction + otherPrediction)/2;
					getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("MEAN PREDICTION TO LEARN", meanPrediction+"")));
					pairExp.getA().setOracleProposition(otherPrediction);
					pairExp.getB().setOracleProposition(meanPrediction);
					this.getLocalModel().updateModel(pairExp.getA(), getAmas().data.learningSpeed);
					//otherCtxt.getLocalModel().updateModel(pairExp.getB(), getAmas().data.learningSpeed);
				}*//*











			}

		}

	}*/





	/*private Context getNearestContextFromNeighbors() {
		Context nearestContext = null;
		double nearestDistance = -1;
		for (Context otherCtxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {
			if (otherCtxt != this) {
				if (nearestContext == null) {
					nearestContext = otherCtxt;
					nearestDistance = this.externalDistance(nearestContext);
				} else {
					double currentExternalDistance = this.externalDistance(otherCtxt);
					if (currentExternalDistance < nearestDistance) {
						nearestContext = otherCtxt;
						nearestDistance = currentExternalDistance;
					}

				}


			}
		}
		return nearestContext;
	}*/

	private Pair<ArrayList<Pair<Experiment, Experiment>>, ArrayList<Pair<Experiment, Experiment>>> getClosestExperimentsPairs(Context otherCtxt, int nbPairs){

		ArrayList<Pair<Experiment, Experiment>> experimentsPairs = new ArrayList<>();
		ArrayList<Pair<Experiment, Experiment>> experimentsPairsPivots = new ArrayList<>();
		int overlapsCounts = 0;

		for(int i=0;i<nbPairs;i++){
			Experiment exp1 = new Experiment(this);
			Experiment exp2 = new Experiment(otherCtxt);
			Experiment pivotExp1 = new Experiment(this);
			Experiment pivotExp2 = new Experiment(this);

			for (Percept pct : getAmas().getPercepts()){

				if(this.distance(otherCtxt,pct) < 0){
					overlapsCounts++;
					Pair<Double,Double> bounds = this.overlapBounds(otherCtxt, pct);
					double valueBetweenBounds = getRandomValueInRange(bounds.getA(), bounds.getB() - bounds.getA());
					exp1.addDimension(pct, valueBetweenBounds);
					exp2.addDimension(pct, valueBetweenBounds);
					pivotExp1.addDimension(pct, valueBetweenBounds);
					pivotExp2.addDimension(pct, valueBetweenBounds);
				}else if (this.getRanges().get(pct).getCenter() < otherCtxt.getRanges().get(pct).getCenter()){
					double valueExp1 = getRandomValueInRange(this.getRanges().get(pct).getEnd() - pct.getMappingErrorAllowedMin(), pct.getMappingErrorAllowedMin());
					double valueExp2 = getRandomValueInRange( otherCtxt.getRanges().get(pct).getStart(), pct.getMappingErrorAllowedMin());
					exp1.addDimension(pct, valueExp1);
					exp2.addDimension(pct, valueExp2);
					pivotExp1.addDimension(pct, this.getRanges().get(pct).getCenter());
					pivotExp2.addDimension(pct, otherCtxt.getRanges().get(pct).getCenter());
				}else{
					double valueExp1 = getRandomValueInRange( this.getRanges().get(pct).getStart(), pct.getMappingErrorAllowedMin());
					double valueExp2 = getRandomValueInRange(otherCtxt.getRanges().get(pct).getEnd() - pct.getMappingErrorAllowedMin(), pct.getMappingErrorAllowedMin());
					exp1.addDimension(pct, valueExp1);
					exp2.addDimension(pct, valueExp2);
					pivotExp1.addDimension(pct, this.getRanges().get(pct).getCenter());
					pivotExp2.addDimension(pct, otherCtxt.getRanges().get(pct).getCenter());
				}

			}
			experimentsPairs.add(new Pair<>(exp1, exp2));
			experimentsPairsPivots.add(new Pair<>(pivotExp1, pivotExp2));
		}

		return new Pair<>(experimentsPairs, experimentsPairsPivots);
		/*if(overlapsCounts==1){
			return new Pair<>(experimentsPairs, experimentsPairsPivots);
		}else{
			return null;
		}*/


	}

	private double getRandomValueInRange(double start, double length) {
		return start + length*Math.random();
	}
	
	private Double getRandomValueInRangeNextToStartAndEnd(Percept pct) {
		if(Math.random()<0.5){
			return ranges.get(pct).getStart() + pct.getMappingErrorAllowedMin()*Math.random();
		}else{
			return ranges.get(pct).getEnd() - pct.getMappingErrorAllowedMin()*Math.random();
		}

	}

	private HashMap<Percept, Double> getRequestFromCounter(){
		HashMap<Percept, Double> request = new HashMap<>();
		for(Percept pct : getAmas().getPercepts()){
			int counterValue = childContextCounter[getAmas().getPercepts().indexOf(pct)];
			if(counterValue == 0){
				request.put(pct,getRanges().get(pct).getStart());
			}else{
				request.put(pct,getRanges().get(pct).getEnd());
			}

		}
		return request;
	}

	private Experiment getexperimentFromCounter(){
		Experiment experiment = new Experiment(this);
		for(Percept pct : getAmas().getPercepts()){
			int counterValue = childContextCounter[getAmas().getPercepts().indexOf(pct)];
			if(counterValue == 0){
				experiment.addDimension(pct,getRanges().get(pct).getStart());
			}else{
				experiment.addDimension(pct,getRanges().get(pct).getEnd());
			}

		}
		return experiment;
	}



	private HashMap<Percept, Double> getNextEtremityChildRequest() {

		if(childContextCounter == null){
			childContextCounter = new int[getAmas().getPercepts().size()];
			for(int i = 0;i<childContextCounter.length;i++){
				childContextCounter[i]=0;
			}
			return getRequestFromCounter();

		}else{
			nextMultiDimCounterForContextRanges(childContextCounter);
			return getRequestFromCounter();
		}

	}

	private HashMap<Percept, Double> getRandomRequestInRanges() {

		HashMap<Percept, Double> request = new HashMap<>();
		for(Percept pct : getAmas().getPercepts()){
			double rangeLength = getRanges().get(pct).getLenght();
			double startRange = getRanges().get(pct).getStart() ;
			request.put(pct,startRange + (Math.random()*rangeLength));


		}
		return request;

	}


	private Experiment getNextEtremityChildExperiment() {

		if(childContextCounter == null){
			childContextCounter = new int[getAmas().getPercepts().size()];
			for(int i = 0;i<childContextCounter.length;i++){
				childContextCounter[i]=0;
			}
			return getexperimentFromCounter();

		}else{
			nextMultiDimCounterForContextRanges(childContextCounter);
			return getexperimentFromCounter();
		}

	}

	private Experiment getRandomExperimentInRanges() {

		Experiment experiment = new Experiment(this);
		for(Percept pct : getAmas().getPercepts()){
			double rangeLength = getRanges().get(pct).getLenght();
			double startRange = getRanges().get(pct).getStart() ;
			experiment.addDimension(pct,startRange + (Math.random()*rangeLength));
		}
		return experiment;

	}





	private static boolean nextMultiDimCounterForContextRanges(int[] indices){



		for(int i = 0; i<indices.length;i++) {

			if(indices[i]==1) {
				if(i==indices.length-1) {
					indices[i]=0;
					return false;
				}
				else {
					indices[i]=0;
				}
			}
			else {
				indices[i] += 1;
				return true;
			}

		}

		return false;


	}

	private Double getRandomRangeBorder(Percept pct) {
		if(Math.random()<0.5){
			return ranges.get(pct).getStart();
		}else{
			return ranges.get(pct).getEnd();
		}

	}

	public Experiment getCurrentExperiment() {
		if(getAmas().getHeadAgent().getOracleValue() == null) return null;
		ArrayList<Percept> percepts = getAmas().getPercepts();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setProposition(getAmas().getHeadAgent().getOracleValue());

		return exp;
	}

	public Experiment getCurrentExperimentWithouOracle() {
		ArrayList<Percept> percepts = getAmas().getPercepts();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}


		return exp;
	}

	public Experiment getPerceptionsAsExperiment() {
		ArrayList<Percept> percepts = getAmas().getPercepts();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}

		return exp;
	}
	
	public Experiment getArtificialExperiment() {
		ArrayList<Percept> percepts = getAmas().getPercepts();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, this.getRanges().get(pct).getCenter());
		}
		

		return exp;
	}

	public double sumOfRangesLengths() {
		double sum = 0;

		for (Percept pct : getAmas().getPercepts()) {
			sum += this.getRanges().get(pct).getLenght();
		}

		return sum;
	}

	public double rangeLengthRatio(Percept pct) {
		return this.getRanges().get(pct).getLenght() / sumOfRangesLengths();
	}

	public Pair<Boolean, Double> tryAlternativeModel(LocalModel alternativeModel) {
		boolean betterModelTest = true;
		double sumError = 0.0;

		return new Pair<Boolean, Double>(betterModelTest, sumError);
	}

	public void solveNCS_BadPrediction(Head head) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS BAD PREDICTION")));
		getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);

		if (head.getNewContext() == this) {
			head.setNewContext(null);
		}

		setConfidenceVariation(-2);

		//confidence -= 2;
		getAmas().getHeadAgent().setBadCurrentCriticalityConfidence();
		getAmas().getHeadAgent().setBadCurrentCriticalityPrediction();

		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());

		Pair<Percept, Context> perceptForAdapatationAndOverlapingContext = getPerceptForAdaptationWithOverlapingContext(
				percepts);

		Percept p = perceptForAdapatationAndOverlapingContext.getA();
		Context overlapingContext = perceptForAdapatationAndOverlapingContext.getB();

		if(getAmas().data.PARAM_NCS_isConflictResolution){
			if(overlapingContext != null){
				setConfidenceVariation(-2);
				ranges.get(p).adapt(p.getValue(), true, overlapingContext);
			}else{
				ranges.get(p).adapt(p.getValue(), false, null);
			}
		}else{
			ranges.get(p).adapt(p.getValue(), false, null);
		}

		if(!ranges.get(p).contains2(p.getValue())){
			getAmas().getHeadAgent().activatedContexts.remove(this);
		}

		modified = true;





		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
	}

	/*public void updateAVT() {
		for (Percept p : ranges.keySet()) {
			if (ranges.get(p).getLastEndTickModification() != getAmas().getCycle()) {
				ranges.get(p).endogenousAdaptEndUsingAVT();
			}
			if (ranges.get(p).getLastStartTickModification() != getAmas().getCycle()) {
				ranges.get(p).endogenousAdaptStartUsingAVT();
			}
		}
	}*/
	
	
	private Percept getPerceptWithLesserImpactOnContextWithPerception(ArrayList<Percept> percepts) {
		
		Percept perceptForAdapation = null;
		double minDistanceToFrontier = Double.MAX_VALUE;
		double distanceToFrontier;
		
		for (Percept pct : percepts) {
			if (!ranges.get(pct).isPerceptEnum()) {

				distanceToFrontier = Math.min(ranges.get(pct).startDistance(pct.getValue()),
						ranges.get(pct).endDistance(pct.getValue()));
				
				for(Percept otherPct : percepts) {
					if(otherPct != pct) {
						distanceToFrontier*= this.getRanges().get(otherPct).getLenght();
					}
				}

				if (distanceToFrontier < minDistanceToFrontier) {
					minDistanceToFrontier = distanceToFrontier;
					perceptForAdapation = pct;
				}
			}
		}
		return perceptForAdapation;
	}

	private Percept getPerceptWithLesserImpactOnContextWithOverlap(ArrayList<Percept> percepts, Context overlapingContext) {

		Percept perceptForAdapation = null;
		double minDistanceToFrontier = Double.MAX_VALUE;
		double overlapToFrontier;

		for (Percept pct : percepts) {
			if (!ranges.get(pct).isPerceptEnum()) {

				overlapToFrontier = this.getRanges().get(pct).overlapDistance(overlapingContext.getRanges().get(pct));

				for(Percept otherPct : percepts) {
					if(otherPct != pct) {
						overlapToFrontier*= this.getRanges().get(otherPct).getLenght();
					}
				}

				if (overlapToFrontier < minDistanceToFrontier) {
					minDistanceToFrontier = overlapToFrontier;
					perceptForAdapation = pct;
				}
			}
		}
		return perceptForAdapation;
	}

	private Percept getPerceptWithBiggerImpactOnOverlap(ArrayList<Percept> percepts, Context bestContext) {
		Percept perceptWithBiggerImpact = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;

		for (Percept percept : percepts) {

			if (!ranges.get(percept).isPerceptEnum()) {
				Range bestContextRanges = bestContext.getRanges().get(percept);

				if (!(bestContextRanges.getStart() <= ranges.get(percept).getStart()
						&& ranges.get(percept).getEnd() <= bestContextRanges.getEnd())) {

					if (percept.contextOrder(this, bestContext)) {
						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
								Arrays.asList("ORDER :", percept.getName(), this.getName(), bestContext.getName())));
						vol = Math.abs(
								percept.getEndRangeProjection(this) - percept.getStartRangeProjection(bestContext));
						if (vol < volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					} else if (percept.contextOrder(bestContext, this)) {
						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(
								Arrays.asList("ORDER :", percept.getName(), bestContext.getName(), this.getName())));
						vol = Math.abs(
								percept.getEndRangeProjection(bestContext) - percept.getStartRangeProjection(this));
						if (vol < volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					} else if (percept.contextIncludedIn(bestContext, this)) {
						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("INCLUSION :", percept.getName(),
								bestContext.getName(), this.getName())));
						vol = Math.abs(percept.getEndRangeProjection(bestContext)
								- percept.getStartRangeProjection(bestContext));
						if (vol < volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					}
				}
			}
		}
		return perceptWithBiggerImpact;
	}

	public double getOverlappingVolume(Context overlappingCtxt) {
		double volume = 1.0;
		for (Percept pct : ranges.keySet()) {
			volume *= this.getRanges().get(pct).overlapDistance(overlappingCtxt.getRanges().get(pct));
		}
		return volume;
	}

	private Pair<Percept, Context> getPerceptForAdaptationWithOverlapingContext(ArrayList<Percept> percepts) {
		Percept perceptForLesserImpactOnCtxtWithOverlap = null;
		Percept perceptWithLesserImpactOnContext = null;
		Context overlapingContext = null;
		double minDistanceToFrontier = Double.MAX_VALUE;
		double distanceToFrontier;
		double maxOverlappingVolume = Double.NEGATIVE_INFINITY;
		double overlappingVolume;

		if (getAmas().getHeadAgent().getActivatedContexts().size() > 1) {

			for (Context ctxt : getAmas().getHeadAgent().getActivatedContexts()) {
				if (ctxt != this) {
					if (this.containedBy(ctxt)) {
						this.destroy();
					} else {
						overlappingVolume = this.getOverlappingVolume(ctxt);
						if (overlappingVolume > maxOverlappingVolume) {
							
							overlapingContext = ctxt;
						}
					}
				}
			}
			
			if(overlapingContext != null) {
				//perceptForBigerImpactOnOverlap = getPerceptWithBiggerImpactOnOverlap(percepts, overlapingContext);
				perceptForLesserImpactOnCtxtWithOverlap = getPerceptWithLesserImpactOnContextWithOverlap(percepts, overlapingContext);
				
			}
			
		}
		
		/*perceptWithLesserImpactOnContext = getPerceptWithLesserImpactOnContextWithPerception(percepts);
		if(perceptForBigerImpactOnOverlap != null) {
			
			if(perceptForBigerImpactOnOverlap == perceptWithLesserImpactOnContext) {
				return new Pair<Percept, Context>(perceptForBigerImpactOnOverlap, overlapingContext);
			}
			
		}*/

		if(perceptForLesserImpactOnCtxtWithOverlap != null) {
			return new Pair<>(perceptForLesserImpactOnCtxtWithOverlap, overlapingContext);
		}else{
			perceptWithLesserImpactOnContext = getPerceptWithLesserImpactOnContextWithPerception(percepts);
			return new Pair<>(perceptWithLesserImpactOnContext, overlapingContext);
		}
		

		
		

		
	}

	public boolean containedBy(Context ctxt) {
		boolean contained = true;

		for (Percept pct : ranges.keySet()) {

			contained = contained && ranges.get(pct).containedBy(ctxt.getRanges().get(pct));
		}

		return contained;
	}

	/**
	 * Sets the ranges.
	 *
	 * @param ranges the ranges
	 */
	public void setRanges(HashMap<Percept, Range> ranges) {
		this.ranges = ranges;
	}

	/**
	 * Gets the controler.
	 *
	 * @return the controler
	 */
	public Head getControler() {
		return getAmas().getHeadAgent();
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public double getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(double action) {
		this.action = action;
	}

	// -----------------------------------------------------------------------------------------------

	public double getNormalizedConfidence() {
		return 1 / (1 + Math.exp(-confidence));
		// return getParametrizedNormalizedConfidence(20.0);
	}

	public double getNormalizedConfidenceWithParams(double max, double min) {

		if(getAmas().data.maxConfidence == null || getAmas().data.minConfidence == null){
			getAmas().data.maxConfidence=1.0;
			getAmas().data.minConfidence=0.0;
		}
		double maxConfidence = getAmas().data.maxConfidence;
		double minConfidence = getAmas().data.minConfidence;
		double CNmaxMin = Math.log((1/max)-1)/Math.log((1/min)-1);
		double center = (maxConfidence - CNmaxMin*minConfidence)/(1 - CNmaxMin);
		double dispersion = (center - maxConfidence)/Math.log((1/max)-1);
		double result = 1 / (1 + Math.exp(-(confidence-center )/ dispersion));

		getEnvironment().print(TRACE_LEVEL.DEBUG,"normalizedConfidence","result",result,"minConfidence", minConfidence, "maxConfidence",maxConfidence, CNmaxMin,center,dispersion);
		return result;
	}

	public double getParametrizedNormalizedConfidence(double dispersion) {
		return 1 / (1 + Math.exp(-confidence / dispersion));
	}

	public double getInfluenceWithConfidence(HashMap<Percept, Double> situation) {
		Double influence = 1.0;

		for (Percept pct : situation.keySet()) {

			influence *= getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct));
		}

		return influence;
	}

	public double getInfluenceWithConfidenceAndVolume(HashMap<Percept, Double> situation) {
		return getVolume() * getInfluenceWithConfidence(situation);
	}

	public double getInfluence(HashMap<Percept, Double> situation) {
		Double influence = 1.0;

		for (Percept pct : situation.keySet()) {

			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}

		return influence;
	}

	public double getWorstInfluenceWithConfidence(HashMap<Percept, Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;

		for (Percept pct : situation.keySet()) {

			currentInfluence = getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct));
			if (currentInfluence < worstInfluence) {
				worstInfluence = currentInfluence;
			}
		}

		return worstInfluence;
	}

	public double getWorstInfluence(HashMap<Percept, Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;

		for (Percept pct : situation.keySet()) {

			currentInfluence = getInfluenceByPerceptSituation(pct, situation.get(pct));
			if (currentInfluence < worstInfluence) {
				worstInfluence = currentInfluence;
			}
		}

		return worstInfluence;
	}

	public double getWorstInfluenceWithVolume(HashMap<Percept, Double> situation) {

		return getVolume() * getWorstInfluence(situation);
	}

	public double getWorstInfluenceWithWorstRange(HashMap<Percept, Double> situation) {

		return getWorstRange() * getWorstInfluence(situation);
	}

	public double getInfluenceByPerceptSituation(Percept pct, double situation) {
		double center = getCenterByPercept(pct);
		double radius = getRadiusByPercept(pct);

		return Math.exp(-((situation - center)*(situation - center)) / (2 * (radius*radius)));
	}

	public double getInfluenceByPerceptSituationWithConfidence(Percept pct, double situation) {

		return getNormalizedConfidence() * getInfluenceByPerceptSituation(pct, situation);
	}

	public double getVolume() {
		double volume = 1.0;

		for (Percept pct : getRanges().keySet()) {
			volume *= 2 * getRadiusByPercept(pct);
		}
		return volume;
	}

	public double getVolumeAfterRestructuration(Percept pctRestructuration, double newRangeValue) {
		double volume = 1.0;

		for (Percept pct : getRanges().keySet()) {
			if(pct == pctRestructuration){
				volume *= newRangeValue;
			}else{
				volume *= 2 * getRadiusByPercept(pct);
			}

		}
		return volume;
	}

	public double getWorstRange() {
		Double volume = Double.POSITIVE_INFINITY;

		for (Percept pct : getRanges().keySet()) {
			// volume *= 2*getRadiusByPercept(pct);
			if (getRadiusByPercept(pct) < volume) {
				volume = getRadiusByPercept(pct);
			}
		}
		return volume;
	}

//	/**
//	 * Analyze results.
//	 *
//	 * @param head the head
//	 */
//	public void analyzeResults(Head head) {
//		if (head.getCriticity(this) > head.getErrorAllowed()) {
//			solveNCS_BadPrediction(head);
//			getAmas().addAlteredContext(this);
//		} else {
////			if (head.getCriticity(this) > head.getInexactAllowed()) {
////				solveNCS_ConflictInexact(head);
////			}
////			else {
////				confidence++;
////				//confidence = confidence * 2;
////			}
//		}
//	}

	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 * @param head the head
	 */
	public void growRanges() {
		
		ArrayList<Percept> allPercepts = getAmas().getPercepts();
		for (Percept pct : allPercepts) {
			boolean contain = ranges.get(pct).contains(pct.getValue()) == 0 ;
			getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(), "CONTAINED", ""+contain)));
			//if (!contain && !fusionned) {
			if (!contain) {
				if(ranges.get(pct).getLenght()<pct.getMappingErrorAllowedMax()) {
					ranges.get(pct).adapt(pct.getValue(), false, null);
				}
				
				//ranges.get(pct).extend(pct.getValue(), pct);
				//world.getScheduler().getHeadAgent().NCSMemories.add(new NCSMemory(world, new ArrayList<Context>(),"Grow Range "+ pct.getName()));
			}
		}
	}

	/**
	 * Shrink ranges to join borders.
	 *
	 * @param head the head
	 * @param c    the c
	 */
	public void shrinkRangesToJoinBorders(Context bestContext) {
		/*Percept perceptWithBiggerImpactOnOverlap = getPerceptWithBiggerImpactOnOverlap(getAmas().getPercepts(),
				bestContext);
		
		Percept perceptWithLesserImpactOnContext = getPerceptWithLesserImpactOnContext(getAmas().getPercepts());


		if (perceptWithBiggerImpactOnOverlap == null) {
			this.destroy();
		} else {

				if(perceptWithBiggerImpactOnOverlap == perceptWithLesserImpactOnContext) {
					ranges.get(perceptWithBiggerImpactOnOverlap).adapt(perceptWithBiggerImpactOnOverlap.getValue(), true, bestContext);
				}else {
					ranges.get(perceptWithLesserImpactOnContext).adapt(perceptWithLesserImpactOnContext.getValue(), true, bestContext);
				}

			

		}*/

		Percept perceptWithLesserImpactOnContext = getPerceptWithLesserImpactOnContextWithOverlap(getAmas().getPercepts(),bestContext);
		ranges.get(perceptWithLesserImpactOnContext).adapt(perceptWithLesserImpactOnContext.getValue(), true, bestContext);
	}

	public double distance(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distance(ctxt.getRanges().get(pct));
	}
	
	public Pair<Double,Double> overlapBounds(Context ctxt, Percept pct) {

		
		if (pct.contextIncludedIn(this, ctxt)) {
			
			return new Pair<Double, Double>(this.getRanges().get(pct).getStart(), this.getRanges().get(pct).getEnd());

		} else if (pct.contextIncludedIn(ctxt, this)) {
			
			return new Pair<Double, Double>(ctxt.getRanges().get(pct).getStart(), ctxt.getRanges().get(pct).getEnd());
			
		} else if (pct.contextOrder(this, ctxt)) {
			
			return new Pair<Double, Double>(ctxt.getRanges().get(pct).getStart(), this.getRanges().get(pct).getEnd());
			
		} else if (pct.contextOrder(ctxt, this)) {
			
			return new Pair<Double, Double>(this.getRanges().get(pct).getStart(), ctxt.getRanges().get(pct).getEnd());

		} else {

			return null;
		}
		

		
	}
	
	private Pair<Double,Double> voidBounds(Context ctxt, Percept pct) {

		
		
		if(this.getRanges().get(pct).getEnd() < ctxt.getRanges().get(pct).getStart()) {
			return new Pair<Double, Double>(this.getRanges().get(pct).getEnd(), ctxt.getRanges().get(pct).getStart());
		}
		else {
			return new Pair<Double, Double>(ctxt.getRanges().get(pct).getEnd(), this.getRanges().get(pct).getStart());
		}
		

		
	}

	private double distanceForVolume(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distanceForVolume(ctxt.getRanges().get(pct));
	}

	private double distanceForMaxOrMin(Context ctxt, Percept pct) {
		return Math.abs(this.getRanges().get(pct).distanceForMaxOrMin(ctxt.getRanges().get(pct)));
	}

	public Context getNearestContextBySortedPerceptAndRange(HashMap<String, ArrayList<Context>> sortedPossibleNeigbours,
			Percept percept, String range) {
		int indexOfCurrentContext = sortedPossibleNeigbours.get(range).indexOf(this);

		if (sortedPossibleNeigbours.get(range).size() > 1) {

			if ((indexOfCurrentContext > 0)
					&& (indexOfCurrentContext < sortedPossibleNeigbours.get(range).size() - 1)) {
				if (range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext + 1);
				} else if (range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext - 1);
				} else {
					return null;
				}
			}

			else if (indexOfCurrentContext == 0) {
				if (range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext + 1);
				} else {
					return null;
				}
			}

			else if (indexOfCurrentContext == sortedPossibleNeigbours.get(range).size() - 1) {
				if (range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext - 1);
				} else {
					return null;
				}
			}

			else {
				return null;
			}

		} else {
			return null;
		}
	}

	public double distance(Percept pct, double value) {
		return this.ranges.get(pct).distance(value);
	}

    public double centerDistanceFromExperiment() {
	    double distance = 0.0;
	    for(Percept pct : getAmas().getPercepts()){
            distance += Math.pow(this.getRanges().get(pct).centerDistance(pct.getValue()),2);
        }
        return Math.sqrt(distance);
    }

    public double externalDistanceFromExperiment() {
        double distance = 0.0;
        for(Percept pct : getAmas().getPercepts()){
            distance += Math.pow(this.distance(pct,pct.getValue()),2);
        }
        return Math.sqrt(distance);
    }

	public void NCSDetection_Uselessness() {
		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall() || ranges.get(v).isAnomaly()) {
				solveNCS_Uselessness();
				break;
			}
		}
//		if(!isDying) {
//			for(Context ctxt : world.getScheduler().getHeadAgent().getActivatedNeighborsContexts()) {
//				if(ctxt != this) {
//					if(this.getConfidence()<=ctxt.getConfidence()) {
//						if(this.containedBy(ctxt)) {
//							solveNCS_Uselessness();
//							break;
//						}
//					}
//				}
//			}
//		}
	}

	@Override
	protected void onAct() {
		
		onActOpitmized();
		//onActOld();

	}
	
	private void onActOpitmized() {
		if(amas.getNeighborContexts().contains(this)) {
			//logger().debug("CYCLE "+getAmas().getCycle(), "Context %s sent proposition %f", getName(), getActionProposal());
			isInNeighborhood=true;
			restructured = false;
			modified = false;
			fusionned = false;
			lastDistanceToModel = 0.0;
			getAmas().getHeadAgent().neigborhoodProposition(this);
		}else{
			NCSDetection_Uselessness();
		}
		if(amas.getValidContexts().contains(this)) {
			//logger().debug("CYCLE "+getAmas().getCycle(), "Context %s sent proposition %f", getName(), getActionProposal());
			activations++;
			isActivated=true;
			getAmas().getHeadAgent().proposition(this);
		}
	}

	/**
	 * Sets the confidence.
	 *
	 * @param confidence the new confidence
	 */
	public void setConfidence(double confidence) {
		getEnvironment().print(TRACE_LEVEL.DEBUG, getName(),"set Confidence",confidence);
		this.confidence = confidence;
		updateMinAndMaxConfidence();
	}

	public void setConfidenceVariation(double confidenceVariation) {
		getEnvironment().print(TRACE_LEVEL.DEBUG, getName(),"Confidence",this.confidence,"Variation", confidenceVariation);
		this.confidence += confidenceVariation;
		updateMinAndMaxConfidence();
	}

	private void updateMinAndMaxConfidence() {
		if(this.confidence>getAmas().data.maxConfidence){
			getAmas().data.maxConfidence = this.confidence;
			getEnvironment().print(TRACE_LEVEL.DEBUG,getName(),"Max Confidence",getAmas().data.maxConfidence);
		}
		if(this.confidence<getAmas().data.minConfidence){
			getAmas().data.minConfidence = this.confidence;
			getEnvironment().print(TRACE_LEVEL.DEBUG,getName(),"Min Confidence",getAmas().data.minConfidence);
		}

		getEnvironment().print(TRACE_LEVEL.DEBUG,getName(), "Confidence",this.confidence,"Min",getAmas().data.minConfidence,"Max",getAmas().data.maxConfidence );
	}

	@Override
	protected void onInitialization() {
		super.onInitialization();
		ranges = new HashMap<Percept, Range>();
		setName(String.valueOf(this.hashCode()));
	}

	@Override
	protected void onRenderingInitialization() {
		try {
			Constructor<? extends RenderStrategy> constructor = defaultRenderStrategy.getConstructor(Object.class);
			setRenderStrategy(constructor.newInstance(this));
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		super.onRenderingInitialization();
	}

	public String toString() {
		return "Context :" + this.getName();
	}

	public String toStringPierre() {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";	
		s += "Max Prediction " + getLocalModel().getMaxProposition() + "\n";
		s += "Min Prediction " + getLocalModel().getMinProposition() + "\n";
		return s;
	}
	
	public ArrayList<String> toStringArrayPierre() {
		ArrayList<String> array = new ArrayList<String>(); 
		array.add(getName());
		array.add(""+localModel.getCoef()[0] );
		for(int i =1;i<localModel.getCoef().length;i++) {
			array.add(""+localModel.getCoef()[i]);
		}
		array.add(""+ getLocalModel().getMinProposition());
		array.add(""+ getLocalModel().getMaxProposition());

		return array;
	}
	
	public String toStringFull() {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "Creation tick : " + tickCreation +"\n";
		s += "Confidence : " + confidence + "\n";
		s += "\n";

		s += "Model "+this.localModel.getType()+" :";
		s += this.localModel.getCoefsFormula() + "\n";
		s += "Last Predicition " + lastPrediction  +"\n";
		s += "Frist experiments"+"\n";
		for(Experiment exp : getLocalModel().getFirstExperiments()){
			s += exp+"\n";
		}
		s += "Nb updates " + ((LocalModelMillerRegression)getLocalModel()).experiemntNb  +"\n";
		//s += "Smoothed Predicition " + smoothedPrediction  +"\n";
		
		s += "\n";
		s += "Ranges :\n";
		for(Percept p : getRanges().keySet()) {
			s += p + " : " + getRangeByPercept(p)+"\n"; 
			s += p + " : " + getRangeByPercept(p).getStartIncrement()+"\n"; 
			s += p + " : " + getRangeByPercept(p).getEndIncrement()+"\n"; 
			
		}
		s += "\n";
		
		s += "Last Distance to Regression " + lastDistanceToModel + "\n";
		s += "Last Average Distance To Regression Allowed "  + lastAverageRegressionPerformanceIndicator +"\n";
		s += "Mean Distance To Regression " + criticalities.getCriticalityMean("distanceToRegression") + "\n";
		s += "Distance To Regression Allowed " + regressionPerformance.getPerformanceIndicator() +"\n\n";
		
		s += "Max Prediction " + getLocalModel().getMaxProposition() + "\n";
		s += "Min Prediction " + getLocalModel().getMinProposition() + "\n\n";
		
		s += "ASKED REQUEST " + waitingRequests.size() + "\n";
		for(EndogenousRequest rqt : waitingRequests) {
			s += rqt + "\n";
		}
		s += "\n";
		s += "Number of activations : " + activations + "\n";
		s += "Number of selection : " + nSelection +"\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}

		

		s += "\n";

		return s;
	}

	public String toStringReducted(HashMap<Percept, Double> situation) {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
		;

		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";

		}

		s += "Number of activations : " + activations + "\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Confidence : " + confidence + "\n";
		s += "Normalized confidence : " + getNormalizedConfidence() + "\n";
		s += "Influence :" + getInfluence(situation) + "\n";
		s += "Worst Influence :" + getWorstInfluence(situation) + "\n";
		s += "Worst Influence + Conf :" + getWorstInfluenceWithConfidence(situation) + "\n";
		s += "Worst Influence + Vol :" + getWorstInfluenceWithVolume(situation) + "\n";
		for (Percept pct : situation.keySet()) {
			s += "Influence " + pct.getName() + " : " + getInfluenceByPerceptSituation(pct, situation.get(pct)) + "\n";
		}
		s += "Global Influence * Confidence :" + getInfluenceWithConfidence(situation) + "\n";
		for (Percept pct : situation.keySet()) {
			s += "Influence * Confidence " + pct.getName() + " : "
					+ getInfluenceByPerceptSituationWithConfidence(pct, situation.get(pct)) + "\n";
		}

		s += "\n";

		return s;
	}

	@Override
	public void destroy() {
		getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(
				Arrays.asList("-----------------------------------------", this.getName(), "DIE")));
		
		getAmas().addToKillContextForUnityUI(this);

		for (Percept percept : getAmas().getPercepts()) {
			percept.deleteContextProjection(this);
		}
		
		super.destroy();
	}

	public double getActionProposal() {
		
		
		return localModel.getProposition();
	}

	public double getActionProposalWithSubPercepts() {


		return ((LocalModelMillerRegression)localModel).getPropositionWithouAllPercepts(getAmas().getPercepts(), getAmas().getSubPercepts());
	}

	public double getCenterByPercept(Percept pct) {
		Range rangeByPercept = this.getRangeByPercept(pct);
		return (rangeByPercept.getEnd()
				+ rangeByPercept.getStart()) / 2;
	}

	public double getConfidence() {
		return confidence;
	}

	public LocalModel getFunction() {
		return localModel;
	}

	public double getRadiusByPercept(Percept pct) {
		Range rangeByPercept = this.getRangeByPercept(pct);
		return (rangeByPercept.getEnd()
				- rangeByPercept.getStart()) / 2;
	}

	public Range getRangeByPerceptName(String perceptName) {
		for (Percept prct : ranges.keySet()) {
			if (prct.getName().equals(perceptName)) {
				return ranges.get(prct);
			}
		}
		return null;
	}
	
	public Range getRangeByPercept(Percept pct) {
		return ranges.get(pct);
	}

	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}

	public LocalModel getLocalModel() {
		return localModel;
	}

	/**
	 * Called by the head when a context is selected as the best context
	 */
	public void notifySelection() {
		nSelection += 1;
	}
	
	/**
	 * Set the local model. Used mostly during save loading
	 * @param localModel
	 */
	public void setLocalModel(LocalModel localModel) {
		this.localModel = localModel;
		this.localModel.setContext(this);
	}
	
	
	public void addWaitingRequest(EndogenousRequest request) {
		waitingRequests.add(request);
		getEnvironment().trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("ADDED WAITING REQUEST", this.getName(), ""+waitingRequests.size(), ""+request)));
	}
	
	public void deleteWaitingRequest(EndogenousRequest request) {
		waitingRequests.remove(request);
	}
	
	public double getDistanceToRegressionAllowed() {
		return regressionPerformance.getPerformanceIndicator();
	}
	
	
	
	
	/**
	 * Compute the color of a {@link Context} based on the coefficients of its {@link LocalModel}
	 * @param coefs
	 * @return
	 */
	public String getColorForUnity() {
		
		Double[] coefs = localModel.getCoef();
		
		double upperBound = 255;
		double dispersion = 100;
		
		
		Double r = 0.0;
		Double g = 0.0;
		Double b = 0.0;

		
		
		
		if(coefs.length>=3) {
			r =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[0]));
			g =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[1]));
			b =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[2]));
			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
				g = 0.0;
				b = 0.0;
			}
		}else if(coefs.length==2) {
			r =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[0]));
			g =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[1]));
			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
				g = 0.0;
			}
		}else if(coefs.length==1) {
			r =  normalizePositiveValues(upperBound, dispersion,  Math.abs(coefs[0]));
			
			if(r.isNaN() || g.isNaN() || b.isNaN()) {
				r = 255.0;
			}
		}else {
			r = 0.0;
			g = 255.0;
			b = 0.0;
		}
		
		
		Double[] ret = new Double[3];
		ret[0] = r / 255.0d;
		ret[1] = g / 255.0d;
		ret[2] = b / 255.0d;
		
		return ret[0] + "," + ret[1] + "," + ret[2] + ",200";
	}
	
	public static double normalizePositiveValues(double upperBound, double dispersion, double value) {
		return upperBound * 2 * (-0.5 + 1 / (1 + Math.exp(-value / dispersion)));
	}
	
	public boolean isFlat() {
		for(Percept pct:getAmas().getPercepts()) {
			if(ranges.get(pct).getLenght()<0.00001) {
				return true;
			}
		}
		return false;
	}

	public boolean isChild(){

		return !getLocalModel().finishedFirstExperiments();


	}


	
}
