package agents.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import agents.AmoebaAgent;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelMillerRegression;
import agents.context.localModel.TypeLocalModel;
import agents.head.Criticalities;
import agents.head.DynamicPerformance;
import agents.head.EndogenousRequest;
import agents.head.Head;
import agents.head.REQUEST;
import agents.percept.Percept;
import gui.ContextRendererFX;
import gui.RenderStrategy;
import kernel.AMOEBA;
import ncs.NCS;
import utils.Pair;
import utils.TRACE_LEVEL;





/**
 * The core agent of AMOEBA.
 * 
 */
public class Context extends AmoebaAgent {
	// STATIC ---
	public static Class<? extends RenderStrategy> defaultRenderStrategy = ContextRendererFX.class;
	// ----------

	private HashMap<Percept, Range> ranges;
	private LocalModel localModel;
	private double confidence = 0;

	/**
	 * The number of time the context was activated (present in validContext). Used
	 * for visualization.
	 */
	private int activations = 0;
	private int nSelection = 0;
	private int tickCreation;

	private double action;

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
	
	public boolean fusionned = false;
	public boolean isInNeighborhood = false;
	
	static final int VOID_CYCLE_START = 0;
	static final int OVERLAP_CYCLE_START = 0;
	
	public Context(AMOEBA amoeba) {
		super(amoeba);
		buildContext();
		criticalities = new Criticalities(5);
		
		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("CTXT CREATION", this.getName())));
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
	}

	public Context(AMOEBA amoeba, Context bestNearestContext) {
		super(amoeba);
		buildContext(bestNearestContext);
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER", this.getName())));
		criticalities = new Criticalities(5);
		
		regressionPerformance = new DynamicPerformance(successesBeforeDiminution, errorsBeforeAugmentation, getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator(), augmentationFactorError, diminutionFactorError, minError);
		getAmas().addSpatiallyAlteredContextForUnityUI(this);
	}

	public Context(AMOEBA amoeba, Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {
		super(amoeba);
		buildContext(fatherContext, contextDimensions);
		getAmas().getEnvironment()
				.trace(TRACE_LEVEL.EVENT, new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER AND DIM", this.getName())));
	}

	private void buildContextCommon() {
		this.tickCreation = getAmas().getCycle();
		action = getAmas().getHeadAgent().getOracleValue();
	}

	/**
	 * Builds the context.
	 */
	private void buildContext() {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r = null;

			//Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(p);
			//TODO use neihbors sizes to define radiuses for creation !!!!!!!!!!!
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getRadiusesForContextCreation(p);

			
					
			
			if(getAmas().getHeadAgent().activatedNeighborsContexts.size()>0 && getAmas().data.isActiveLearning) {
				
				if(getAmas().getHeadAgent().lastEndogenousRequest != null) {
					if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.VOID) {
						double startRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getA();
						double endRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getB();
						
						getAmas().getEnvironment()
						.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by VOID", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
						r = new Range(this, startRange, endRange, 0, true, true, p, getAmas().getHeadAgent().minMeanNeighborhoodStartIncrements, getAmas().getHeadAgent().minMeanNeighborhoodEndIncrements);
					
					}
				}
				if(r==null) {
					double radiusCreation = getAmas().getHeadAgent().minNeighborhoodRadius;
					//double radiusCreation = getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p);
					//double radiusCreation = getAmas().getHeadAgent().minMeanNeighborhoodRaduises;
					getAmas().getEnvironment()
					.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by mean", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
					r = new Range(this, p.getValue() - radiusCreation, p.getValue() + radiusCreation, 0, true, true, p, getAmas().getHeadAgent().minMeanNeighborhoodStartIncrements, getAmas().getHeadAgent().minMeanNeighborhoodEndIncrements);
				
				}
				
			
			
			}
			if(r==null){
				r = new Range(this, p.getValue() - radiuses.getA(), p.getValue() + radiuses.getB(), 0, true, true, p);
				getAmas().getEnvironment()
				.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by init", this.getName(), p.getName(), radiuses.getA().toString())));
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
		firstPoint.setOracleProposition(getAmas().getHeadAgent().getOracleValue());
		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"NEW EXP",
		// firstPoint.toString())));

		localModel.updateModel(this.getCurrentExperiment(), getAmas().data.learningSpeed);
		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
	}

	private void buildContext(Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {

		buildContextCommon();

		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept pct : var) {
			Range r;
			double center = contextDimensions.get(pct).getA();
			double length = contextDimensions.get(pct).getB();
			r = new Range(this, center - length / 2, center + length / 2, 0, true, true, pct);

			ranges.put(pct, r);
			ranges.get(pct).setValue(center);

			pct.addContextProjection(this);
		}

		// expand();

		this.confidence = fatherContext.confidence;
	
		this.localModel = getAmas().buildLocalModel(this);
		// this.formulaLocalModel = ((LocalModelMillerRegression)
		// bestNearestContext.localModel).getFormula(bestNearestContext);
		Double[] coef = fatherContext.localModel.getCoef();
		this.localModel.setCoef(coef);
		this.actionProposition = fatherContext.localModel.getProposition();

		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
	}

	private void buildContext(Context bestNearestContext) {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r = null;
			//Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(v);
			//TODO use neihbors sizes to define radiuses for creation !!!!!!!!!!!
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getRadiusesForContextCreation(p);
			

			if(getAmas().getHeadAgent().activatedNeighborsContexts.size()>0 && getAmas().data.isActiveLearning) {
				
				
				
				if(getAmas().getHeadAgent().lastEndogenousRequest != null) {
					if(getAmas().getHeadAgent().lastEndogenousRequest.getType() == REQUEST.VOID) {
						double startRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getA();
						double endRange = getAmas().getHeadAgent().lastEndogenousRequest.getBounds().get(p).getB();
						//System.out.println(startRange + "  " + endRange);
						getAmas().getEnvironment()
						.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by VOID", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
						r = new Range(this, startRange, endRange, 0, true, true, p, getAmas().getHeadAgent().minMeanNeighborhoodStartIncrements, getAmas().getHeadAgent().minMeanNeighborhoodEndIncrements);
					
					}
				}
				if(r==null) {
					double radiusCreation = getAmas().getHeadAgent().minNeighborhoodRadius;
					//double radiusCreation = getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p);
					//double radiusCreation = getAmas().getHeadAgent().minMeanNeighborhoodRaduises;
					getAmas().getEnvironment()
					.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by mean", this.getName(), p.getName(), getAmas().getHeadAgent().meanNeighborhoodRaduises.get(p).toString())));
					r = new Range(this, p.getValue() - radiusCreation, p.getValue() + radiusCreation, 0, true, true, p, getAmas().getHeadAgent().minMeanNeighborhoodStartIncrements, getAmas().getHeadAgent().minMeanNeighborhoodEndIncrements);
				
				}
				
			}
			if(r==null) {
				r = new Range(this, p.getValue() - radiuses.getA(), p.getValue() + radiuses.getB(), 0, true, true, p);
				getAmas().getEnvironment()
				.trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("Range creation by init", this.getName(), p.getName(), radiuses.getA().toString())));
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

		localModel.updateModel(this.getCurrentExperiment(), getAmas().data.learningSpeed);

		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

	}

	public ArrayList<Context> getContextsOnAPerceptDirectionFromContextsNeighbors(ArrayList<Context> contextNeighbors,
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
	}

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

	public void expand() {
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
	}

	public void matchSpatialContextRanges(SpatialContext biggerContextForCreation) {
		for (Percept pct : ranges.keySet()) {
			ranges.get(pct).setStart(biggerContextForCreation.getStart(pct));
			ranges.get(pct).setEnd(biggerContextForCreation.getEnd(pct));
		}
	}

	public Pair<Double, Double> getMaxExpansionsForContextExpansionAfterCreation(
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
	}

	// --------------------------------NCS
	// Resolutions-----------------------------------------

	/**
	 * Solve NC S incompetent head.
	 *
	 * @param head the head
	 */
	public void solveNCS_IncompetentHead(Head head) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
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
	public void solveNCS_Concurrence(Head head) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS CONCURENCE")));

		getEnvironment().raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders(head.getBestContext());

		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
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

	public void analyzeResults3(Head head, Context closestContextToOracle) {
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

	}

	public void analyzeResults4(Head head) {
		
		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("------------------------------------------------------------------------------------"
				+ "---------------------------------------- ANALYSE RESULTS " + this.getName())));
		
		lastDistanceToModel = getLocalModel().distance(this.getCurrentExperiment());
		lastAverageRegressionPerformanceIndicator = head.getAverageRegressionPerformanceIndicator();
		if(lastDistanceToModel <= lastAverageRegressionPerformanceIndicator) {
		//if(getLocalModel().distance(this.getCurrentExperiment()) < head.getAverageRegressionPerformanceIndicator()) {
		//if (head.getCriticity(this) < head.getErrorAllowed()) {
			confidence++;
			getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(), "CONFIDENCE ++")));
		} else {
			this.solveNCS_BadPrediction(head);
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
	
	public EndogenousRequest endogenousRequest(Context ctxt) {
		
		HashMap<Percept, Double> voidDistances = new HashMap<Percept, Double>();
		HashMap<Percept, Double> overlapDistances = new HashMap<Percept, Double>();
		HashMap<Percept, Pair<Double, Double>> bounds = new HashMap<Percept, Pair<Double, Double>>();
		
		double currentDistance = 0.0;

		int overlapCounts = 0;
		for (Percept pct : getAmas().getPercepts()) {
			currentDistance = this.distance(ctxt, pct);
			
			if(currentDistance<-pct.getMappingErrorAllowedMin() && getAmas().getCycle()>OVERLAP_CYCLE_START) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("OVERLAP",pct.getName(), ""+this,""+ctxt)) );
				overlapCounts+=1;
				overlapDistances.put(pct, Math.abs(currentDistance));
				bounds.put(pct, this.overlapBounds(ctxt, pct));
				
				
			}
			

			if (currentDistance > pct.getMappingErrorAllowedMin() && getAmas().getCycle()>VOID_CYCLE_START) {
				getEnvironment().trace(TRACE_LEVEL.DEBUG,new ArrayList<String>(Arrays.asList("VOID",pct.getName(), ""+this,""+ctxt, "distance", ""+currentDistance)) );
				voidDistances.put(pct, currentDistance);
				bounds.put(pct, this.voidBounds(ctxt, pct));
			}



			

		}

		if (overlapCounts == getAmas().getPercepts().size() && getAmas().getCycle() > OVERLAP_CYCLE_START) {
			
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList(getAmas().getPercepts().size() + "OVERLAPS", ""+this,""+ctxt)) );
			
			HashMap<Percept, Double> request = boundsToRequest(bounds);
			if(request != null) {
				
				double currentDistanceToOraclePrediction = this.getLocalModel().distance(this.getCurrentExperiment());
				double otherContextDistanceToOraclePrediction = ctxt.getLocalModel().distance(ctxt.getCurrentExperiment());
				
				Double averageDistanceToOraclePrediction = getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator();
				Double distanceDifference = Math.abs(currentDistanceToOraclePrediction-otherContextDistanceToOraclePrediction);
					
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(this.getName(),"currentDistanceToOraclePrediction",""+ currentDistanceToOraclePrediction,"otherContextDistanceToOraclePrediction",""+ otherContextDistanceToOraclePrediction, "distanceDifference", ""+distanceDifference)));
				
				if(distanceDifference<averageDistanceToOraclePrediction) {
					return new EndogenousRequest(request, bounds, 6, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.CONCURRENCE);
				}
				else {
					return new EndogenousRequest(request, bounds, 7, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.CONFLICT);
				}
				
				
				
			}		
		}
		else if(overlapCounts == getAmas().getPercepts().size()-1 && voidDistances.size() == 1 && getAmas().getCycle() > VOID_CYCLE_START) {
			
			getEnvironment().trace(TRACE_LEVEL.INFORM, new ArrayList<String>(Arrays.asList("VOID", ""+this,""+ctxt)) );
			
			updateBoundsWithNeighborhood(bounds);
			
			HashMap<Percept, Double> request = boundsToRequest(bounds);
			
			if(request != null) {
				
				if(getAmas().getHeadAgent().isRealVoid(request)) {
					return new EndogenousRequest(request, bounds, 5, new ArrayList<Context>(Arrays.asList(this,ctxt)), REQUEST.VOID);
				}		
			}
		}
		else {
			return null;
		}
	
		return null;	
	}
	
	private void updateBoundsWithNeighborhood(HashMap<Percept, Pair<Double, Double>> voidBounds) {

		
		
		for (HashMap.Entry<Percept,  Pair<Double, Double>> entry : voidBounds.entrySet()) {
			
			double neighborhoodRadius = entry.getKey().getRadiusContextForCreation()*2;
			
			if(entry.getValue().getA()<entry.getKey().getValue()-neighborhoodRadius) {
				entry.getValue().setA(entry.getKey().getValue()-neighborhoodRadius);
			}
			if(entry.getKey().getValue()+neighborhoodRadius < entry.getValue().getB()) {
				entry.getValue().setB(entry.getKey().getValue()+neighborhoodRadius);
			}
			
		    
		    
		}
		

		
	}
	

	public double distanceAsVolume(Context ctxt) {
		double totalDistanceAsVolume = 1.0;

		for (Percept pct : getAmas().getPercepts()) {
			double currentDistance = this.distanceForVolume(ctxt, pct);
			totalDistanceAsVolume *= currentDistance;


		}

		return Math.abs(totalDistanceAsVolume);
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

	public void NCSDetection_BetterNeighbor() {
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

	}

	public void solveNCS_BetterNeighbor(Context betterContext) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(), betterContext.getName(),
				"*********************************************************************************************************** SOLVE NCS BETTER NEIGHBOR")));
		localModel = new LocalModelMillerRegression(this, betterContext.getLocalModel().getCoef(),
				betterContext.getLocalModel().getFirstExperiments());
	}

	public void NCSDetection_OverMapping() {
		
		
		
		
		for(Context ctxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {
			
			
			if(ctxt != this && !ctxt.isDying()) {
				

	
				double currentDistanceToOraclePrediction = this.getLocalModel().distance(this.getCurrentExperiment());
				double otherContextDistanceToOraclePrediction = ctxt.getLocalModel().distance(ctxt.getCurrentExperiment());
				
				//double minDistanceToOraclePrediction = Math.min(getAmas().getHeadAgent().getDistanceToRegressionAllowed(), getAmas().getHeadAgent().getDistanceToRegressionAllowed());
				Double averageDistanceToOraclePrediction = getAmas().getHeadAgent().getAverageRegressionPerformanceIndicator();
				Double distanceDifference = Math.abs(currentDistanceToOraclePrediction-otherContextDistanceToOraclePrediction);
					
				getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>( Arrays.asList(this.getName(),"currentDistanceToOraclePrediction",""+ currentDistanceToOraclePrediction,"otherContextDistanceToOraclePrediction",""+ otherContextDistanceToOraclePrediction, "distanceDifference", ""+distanceDifference)));
				
				if(distanceDifference<averageDistanceToOraclePrediction) {
					
					 
					
					
					for(Percept pct : ranges.keySet()) {
						
						boolean fusionTest = true;
						
						getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),pct.getName(), ""+Math.abs(this.distance(ctxt, pct)), "DISTANCE", "" + getEnvironment().getMappingErrorAllowed())));
						if(Math.abs(this.distance(ctxt, pct)) < pct.getMappingErrorAllowedOverMapping()){		
														
							for(Percept otherPct : ranges.keySet()) {
								
								if(otherPct != pct) {
																		
									double lengthDifference = Math.abs(ranges.get(otherPct).getLenght() - ctxt.getRanges().get(otherPct).getLenght());
									double centerDifference = Math.abs(ranges.get(otherPct).getCenter() - ctxt.getRanges().get(otherPct).getCenter());
									getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),otherPct.getName(), ""+lengthDifference,""+centerDifference, "LENGTH & CENTER DIFF", ""  + getEnvironment().getMappingErrorAllowed())));
									fusionTest = fusionTest && (lengthDifference < otherPct.getMappingErrorAllowedOverMapping()) && (centerDifference< otherPct.getMappingErrorAllowedOverMapping());
								}
							}
							
							if(fusionTest) {
								solveNCS_OverMapping(ctxt);
							}
							
						}
					}
					
				}
								
			}
		}
		
	}

	private void solveNCS_OverMapping(Context fusionContext) {
		getEnvironment().trace(TRACE_LEVEL.NCS, new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS OVERMAPPING")));
		getEnvironment().raiseNCS(NCS.CONTEXT_OVERMAPPING);

		
		for(Percept pct : getAmas().getPercepts()) {
			this.getRanges().get(pct).setEnd(Math.max(this.getRanges().get(pct).getEnd(), fusionContext.getRanges().get(pct).getEnd()));
			this.getRanges().get(pct).setStart(Math.min(this.getRanges().get(pct).getStart(), fusionContext.getRanges().get(pct).getStart()));
		}
		
		this.setConfidence(2*Math.max(this.getConfidence(), fusionContext.getConfidence()));
		regressionPerformance.setPerformanceIndicator(Math.max(this.regressionPerformance.getPerformanceIndicator(), fusionContext.regressionPerformance.getPerformanceIndicator()));
		
		
		fusionContext.destroy();
		fusionned =  true;
		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	public void solveNCS_ChildContext() {
		HashMap<Percept, Double> request = new HashMap<Percept, Double>();
		for(Percept pct : getAmas().getPercepts()) {
			request.put(pct, getRandomValueInRange(pct));
		}
		getEnvironment().trace(TRACE_LEVEL.EVENT,new ArrayList<String>(Arrays.asList("NEW ENDO REQUEST","10", ""+request, ""+this.getName())));
		getAmas().getHeadAgent().addChildRequest(request, 10,this);
		
	}
	
	private Double getRandomValueInRange(Percept pct) {
		return ranges.get(pct).getStart() + ranges.get(pct).getLenght()*Math.random();
	}

	public Experiment getCurrentExperiment() {
		ArrayList<Percept> percepts = getAmas().getPercepts();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(getAmas().getHeadAgent().getOracleValue());

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
				"*********************************************************************************************************** SOLVE NCS CONFLICT")));
		getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);

		if (head.getNewContext() == this) {
			head.setNewContext(null);
		}
		;

		confidence -= 2;
		getAmas().getHeadAgent().setBadCurrentCriticalityConfidence();
		getAmas().getHeadAgent().setBadCurrentCriticalityPrediction();

		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());

		Pair<Percept, Context> perceptForAdapatationAndOverlapingContext = getPerceptForAdaptationWithOverlapingContext(
				percepts);
		Percept p = perceptForAdapatationAndOverlapingContext.getA();

//		if(perceptForAdapatationAndOverlapingContext.getB()!=null) {
//			world.trace(new ArrayList<String>(Arrays.asList(this.getName(),p.getName(),perceptForAdapatationAndOverlapingContext.getB().getName(), "*********************************************************************************************************** CONFLICT OVERLAP")));
//			Range overlapingRange = perceptForAdapatationAndOverlapingContext.getB().getRanges().get(p);
//			
//			
//			if (Math.abs(overlapingRange.getStart() - this.getRanges().get(p).getEnd()) >= Math.abs(overlapingRange.getEnd() - this.getRanges().get(p).getStart())) {
//						
//				this.getRanges().get(p).adaptOnOverlap(overlapingRange, overlapingRange.getEnd());
//			} else {
//				this.getRanges().get(p).adaptOnOverlap(overlapingRange, overlapingRange.getStart());
//			}
//			
//			//ranges.get(p).adapt(p.getValue(), Math.abs(this.distance(overlapingContext, p))); 
//			
//		}else {
//			ranges.get(p).adapt(p.getValue()); 
//		}

		ranges.get(p).adapt(p.getValue(), false, null);

//		if(perceptForAdapatationAndOverlapingContext.getB()!=null) {
//			if(testIfOtherContextShouldFinalyShrink(perceptForAdapatationAndOverlapingContext.getB(), perceptForAdapatationAndOverlapingContext.getA())){
//				perceptForAdapatationAndOverlapingContext.getB().getRanges().get(p).adapt(p.getValue());
//			}
//		}

//		if (head.isContextFromPropositionWasSelected() && head.getCriticity() <= head.getErrorAllowed()){
//			
//			//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//			
//				p = this.getPerceptWithLesserImpactOnVolumeNotIncludedIn2(percepts, head.getBestContext());
//
//			if (p == null) {
//				this.die();
//			}else {	
//				ranges.get(p).adaptTowardsBorder(head.getBestContext());
//			}
//		} else {
//			p = this.getPerceptWithLesserImpactOnVolume2(percepts);
//			ranges.get(p).adapt(p.getValue()); 
//		}

//		for (Percept v : ranges.keySet()) {
//			if (ranges.get(v).isTooSmall()){
//				solveNCS_Uselessness();
//				break;
//			}
//		}
		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
	}

	public void updateAVT() {
		for (Percept p : ranges.keySet()) {
			if (ranges.get(p).getLastEndTickModification() != getAmas().getCycle()) {
				ranges.get(p).endogenousAdaptEndUsingAVT();
			}
			if (ranges.get(p).getLastStartTickModification() != getAmas().getCycle()) {
				ranges.get(p).endogenousAdaptStartUsingAVT();
			}
		}
	}
	
	
	private Percept getPerceptWithLesserImpactOnContext(ArrayList<Percept> percepts) {
		
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
		Percept perceptForBigerImpactOnOverlap = null;
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
				perceptForBigerImpactOnOverlap = getPerceptWithBiggerImpactOnOverlap(percepts, overlapingContext);
				
			}
			
		}
		
		perceptWithLesserImpactOnContext = getPerceptWithLesserImpactOnContext(percepts);
		if(perceptForBigerImpactOnOverlap != null) {
			
			if(perceptForBigerImpactOnOverlap == perceptWithLesserImpactOnContext) {
				return new Pair<Percept, Context>(perceptForBigerImpactOnOverlap, overlapingContext);
			}
			
		}
		
		return new Pair<Percept, Context>(perceptWithLesserImpactOnContext, overlapingContext);
		
		

		
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

	/**
	 * Analyze results.
	 *
	 * @param head the head
	 */
	public void analyzeResults(Head head) {
		if (head.getCriticity(this) > head.getErrorAllowed()) {
			solveNCS_BadPrediction(head);
			getAmas().addAlteredContext(this);
		} else {
//			if (head.getCriticity(this) > head.getInexactAllowed()) {
//				solveNCS_ConflictInexact(head);
//			}
//			else {
//				confidence++;
//				//confidence = confidence * 2;
//			}
		}
	}

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
			if (!contain && !fusionned) {
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
		Percept perceptWithBiggerImpactOnOverlap = getPerceptWithBiggerImpactOnOverlap(getAmas().getPercepts(),
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

			

		}
	}

	private double distance(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distance(ctxt.getRanges().get(pct));
	}
	
	private Pair<Double,Double> overlapBounds(Context ctxt, Percept pct) {

		
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

	public void NCSDetection_Uselessness() {
		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()) {
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
		if(amas.getValidContexts().contains(this)) {
			logger().debug("CYCLE "+getAmas().getCycle(), "Context %s sent proposition %f", getName(), getActionProposal());
			activations++;
			getAmas().getHeadAgent().proposition(this);
		}
	}

	/**
	 * Sets the confidence.
	 *
	 * @param confidence the new confidence
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
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
	
}
