package agents.context;

import java.awt.TrayIcon.MessageType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agents.AmoebaAgent;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelAverage;
import agents.context.localModel.LocalModelFirstExp;
import agents.context.localModel.LocalModelMillerRegression;
import agents.context.localModel.TypeLocalModel;
import agents.head.Head;
import agents.percept.Percept;
import gui.ContextRendererFX;
import gui.RenderStrategy;
import kernel.AMOEBA;
import ncs.NCS;
import utils.Pair;

/**
 * The core agent of AMOEBA.
 * 
 */
public class Context extends AmoebaAgent {
	// STATIC ---
	public static Class<? extends RenderStrategy> defaultRenderStrategy = ContextRendererFX.class;
	// ----------

	private HashMap<Percept, Range> ranges;
	private ArrayList<Experiment> experiments;
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

	private int maxActivationsRequired;

	private Double actionProposition = null;

	public HashMap<Percept, HashMap<String, Context>> nearestNeighbours;
	public HashMap<Context, HashMap<Percept, Pair<Double, Integer>>> otherContextsDistancesByPercept;
	public HashMap<Percept, HashMap<String, ArrayList<Context>>> sortedPossibleNeighbours = new HashMap<>();

	private ArrayList<Percept> nonValidPercepts = new ArrayList<Percept>();
	private ArrayList<Percept> nonValidNeightborPercepts = new ArrayList<Percept>();

	private boolean valid;

	private HashMap<Percept, Boolean> perceptValidities = new HashMap<>();
	private HashMap<Percept, Boolean> perceptNeighborhoodValidities = new HashMap<>();
	
	

	public Context(AMOEBA amoeba) {
		super(amoeba);
		buildContext();
		getAmas().getEnvironment().trace(new ArrayList<String>(Arrays.asList("CTXT CREATION", this.getName())));
	}

	public Context(AMOEBA amoeba, Context bestNearestContext) {
		super(amoeba);
		buildContext(bestNearestContext);
		getAmas().getEnvironment()
				.trace(new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER", this.getName())));
		//TODO in amak, cannot kill a agent before its 1st cycle
		//NCSDetection_Uselessness();

	}

	public Context(AMOEBA amoeba, Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {
		super(amoeba);
		buildContext(fatherContext, contextDimensions);
		getAmas().getEnvironment()
				.trace(new ArrayList<String>(Arrays.asList("CTXT CREATION WITH GODFATHER AND DIM", this.getName())));
	}

	private void buildContextCommon() {
		this.tickCreation = getAmas().getCycle();

		ArrayList<Percept> var = getAmas().getPercepts();
		Experiment firstPoint = new Experiment(this);

		action = getAmas().getHeadAgent().getOracleValue();
		maxActivationsRequired = var.size();

		for (Context ctxt : getAmas().getContexts()) {

			ctxt.addContext(this);
		}
	}

	/**
	 * Builds the context.
	 *
	 * @param world     the world
	 * @param headAgent the headAgent
	 */
	private void buildContext() {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept p : var) {
			Range r;

			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(p);

			r = new Range(this, p.getValue() - radiuses.getA(), p.getValue() + radiuses.getB(), 0, true, true, p);

			// r = new Range(this, v.getValue() - radius, v.getValue() + radius, 0, true,
			// true, v, world);
			ranges.put(p, r);
			ranges.get(p).setValue(p.getValue());
			// TODO
			// sendExpressMessage(null, MessageType.REGISTER, p);
			firstPoint.addDimension(p, p.getValue());

			p.addContextProjection(this);
			p.addContextSortedRanges(this);
		}

		expand();

		localModel = getAmas().buildLocalModel(this);
		firstPoint.setOracleProposition(getAmas().getHeadAgent().getOracleValue());
		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"NEW EXP",
		// firstPoint.toString())));
		experiments.add(firstPoint);

		localModel.updateModel(this.getCurrentExperiment(), getAmas().getHeadAgent().learningSpeed,
				getAmas().getHeadAgent().numberOfPointsForRegression);
		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		perceptValidities = new HashMap<Percept, Boolean>();
		for (Percept percept : var) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}

		nearestNeighbours = new HashMap<Percept, HashMap<String, Context>>();
		otherContextsDistancesByPercept = new HashMap<Context, HashMap<Percept, Pair<Double, Integer>>>();

		for (Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());

			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());

			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);

			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>());
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>());

		}

		//// System.out.println("NEW CONTEXT " + this.getName());

		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
		for (Experiment exp : experiments) {
			// System.out.println(exp.toString());
		}
	}

	private void buildContext(Context fatherContext, HashMap<Percept, Pair<Double, Double>> contextDimensions) {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept pct : var) {
			Range r;
			double center = contextDimensions.get(pct).getA();
			double length = contextDimensions.get(pct).getB();
			r = new Range(this, center - length / 2, center + length / 2, 0, true, true, pct);

			ranges.put(pct, r);
			ranges.get(pct).setValue(center);
			// TODO
			// sendExpressMessage(null, MessageType.REGISTER, pct);

			pct.addContextProjection(this);
			pct.addContextSortedRanges(this);
		}

		// expand();

		this.confidence = fatherContext.confidence;
		if (fatherContext.getLocalModel().getType() == TypeLocalModel.MILLER_REGRESSION) {

			this.localModel = new LocalModelMillerRegression(this);
			// this.formulaLocalModel = ((LocalModelMillerRegression)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			Double[] coef = ((LocalModelMillerRegression) fatherContext.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) fatherContext.localModel)
					.getProposition(fatherContext);

		} else if (fatherContext.getLocalModel().getType() == TypeLocalModel.FIRST_EXPERIMENT) {

			this.localModel = new LocalModelFirstExp(this);
			// this.formulaLocalModel = ((LocalModelFirstExp)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelFirstExp) fatherContext.localModel).getProposition(fatherContext);

		} else if (fatherContext.getLocalModel().getType() == TypeLocalModel.AVERAGE) {

			this.localModel = new LocalModelAverage(this);
			// this.formulaLocalModel = ((LocalModelAverage)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelAverage) fatherContext.localModel).getProposition(fatherContext);

		}

		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(fatherContext.getExperiments());

		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		perceptValidities = new HashMap<Percept, Boolean>();
		for (Percept percept : var) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}

		nearestNeighbours = new HashMap<Percept, HashMap<String, Context>>();
		otherContextsDistancesByPercept = new HashMap<Context, HashMap<Percept, Pair<Double, Integer>>>();

		for (Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());

			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());

			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);

			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>());
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>());

		}

		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(), "EXPS")));
		for (Experiment exp : experiments) {
			// System.out.println(exp.toString());
		}
	}

	private void buildContext(Context bestNearestContext) {

		buildContextCommon();

		Experiment firstPoint = new Experiment(this);
		ArrayList<Percept> var = getAmas().getPercepts();
		for (Percept v : var) {
			Range r;
			Pair<Double, Double> radiuses = getAmas().getHeadAgent().getMaxRadiusesForContextCreation(v);

			r = new Range(this, v.getValue() - radiuses.getA(), v.getValue() + radiuses.getB(), 0, true, true, v);

			ranges.put(v, r);
			ranges.get(v).setValue(v.getValue());
			// TODO
			// sendExpressMessage(null, MessageType.REGISTER, v);
			firstPoint.addDimension(v, v.getValue());

			v.addContextProjection(this);
			v.addContextSortedRanges(this);
		}

		expand();

		this.confidence = bestNearestContext.confidence;
		if (bestNearestContext.getLocalModel().getType() == TypeLocalModel.MILLER_REGRESSION) {

			this.localModel = new LocalModelMillerRegression(this);
			// this.formulaLocalModel = ((LocalModelMillerRegression)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			Double[] coef = ((LocalModelMillerRegression) bestNearestContext.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) bestNearestContext.localModel)
					.getProposition(bestNearestContext);

		} else if (bestNearestContext.getLocalModel().getType() == TypeLocalModel.FIRST_EXPERIMENT) {

			this.localModel = new LocalModelFirstExp(this);
			// this.formulaLocalModel = ((LocalModelFirstExp)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelFirstExp) bestNearestContext.localModel)
					.getProposition(bestNearestContext);

		} else if (bestNearestContext.getLocalModel().getType() == TypeLocalModel.AVERAGE) {

			this.localModel = new LocalModelAverage(this);
			// this.formulaLocalModel = ((LocalModelAverage)
			// bestNearestContext.localModel).getFormula(bestNearestContext);
			this.actionProposition = ((LocalModelAverage) bestNearestContext.localModel)
					.getProposition(bestNearestContext);

		}

		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(bestNearestContext.getExperiments());

		localModel.updateModel(this.getCurrentExperiment(), getAmas().getHeadAgent().learningSpeed,
				getAmas().getHeadAgent().numberOfPointsForRegression);

		getAmas().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));

		perceptValidities = new HashMap<Percept, Boolean>();
		for (Percept percept : var) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
		}

		nearestNeighbours = new HashMap<Percept, HashMap<String, Context>>();
		otherContextsDistancesByPercept = new HashMap<Context, HashMap<Percept, Pair<Double, Integer>>>();

		for (Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());

			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());

			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);

			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>());
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>());

		}

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
					//// System.out.println("DISTANCE " + ctxtNeigbor.getName()+ " " +
					//// pct.getName()+ " " + expandingContext.distance(pct,
					//// ctxtNeigbor.getRanges().get(pct)) + " " + ( expandingContext.distance(pct,
					//// ctxtNeigbor.getRanges().get(pct))<0)) ;
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
			double startExpansion = Math.abs(ranges.get(pct).getStart() - biggerContextForCreation.getStart(pct));
			double endExpansion = Math.abs(ranges.get(pct).getEnd() - biggerContextForCreation.getEnd(pct));
			//// System.out.println("EXPANSION " + pct.getName() +" < " + startExpansion + "
			//// , " + endExpansion + " > / < " +pct.getMin() + " , " +pct.getMax() + " >");

			ranges.get(pct).setStart(biggerContextForCreation.getStart(pct));
			ranges.get(pct).setEnd(biggerContextForCreation.getEnd(pct));
		}
	}

	public Pair<Double, Double> getMaxExpansionsForContextExpansionAfterCreation(
			ArrayList<Context> contextNeighborsInOneDirection, Percept pct) {
		double startRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getStart());
		double endRadiusFromCreation = Math.abs(pct.getValue() - this.getRanges().get(pct).getEnd());
		Pair<Double, Double> maxExpansions = new Pair<Double, Double>(
				Math.min(pct.getRadiusContextForCreation() - startRadiusFromCreation,
						Math.abs(pct.getMin() - ranges.get(pct).getStart())),
				Math.min(pct.getRadiusContextForCreation() - endRadiusFromCreation,
						Math.abs(pct.getMax() - ranges.get(pct).getEnd())));

		double currentStartExpansion;
		double currentEndExpansion;

		// for(Context ctxt:partialNeighborContexts.get(pct)) {
		for (Context ctxt : contextNeighborsInOneDirection) {
			//// System.out.println("DISTANCE " + pct.getName() + " " +
			//// ctxt.getRanges().get(pct).centerDistance(pct.getValue()));
			if (ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				currentEndExpansion = ctxt.getRanges().get(pct).distance(ranges.get(pct));
				//// System.out.println("DISTANCE 2 " + pct.getName() + " " +
				//// ctxt.getRanges().get(pct).distance(ranges.get(pct)));
				////// System.out.println(ctxt.getName() + " " + pct.getName() + " " +
				//// currentRadius + " " + maxRadius);
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
				////// System.out.println(ctxt.getName() + " " + pct.getName() + " " +
				////// currentRadius + " " + maxRadius);
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

	// ---------
	// TODO these methods look very similar, maybe factorization is possible ?
	public void updateRequestNeighborState() { // faire le update dans le head attention partial et full
		if (nonValidNeightborPercepts.size() == 0) {
			////// System.out.println("VALID NEIGHBOR : " + this.getName());
			getAmas().getHeadAgent().addRequestNeighbor(this);
		} else {
			getAmas().getHeadAgent().removeRequestNeighbor(this);
		}
	}

	public void updateActivatedContexts() { // faire le update dans le head attention partial et full
		if (nonValidPercepts.size() == 0) {
			////// System.out.println("VALID NEIGHBOR : " + this.getName());
			getAmas().getHeadAgent().addActivatedContext(this);
		} else {
			getAmas().getHeadAgent().removeActivatedContext(this);
		}
	}

	public void updateActivatedContextsCopyForUpdate() { // faire le update dans le head attention partial et full
		if (nonValidPercepts.size() == 0) {
			////// System.out.println("VALID NEIGHBOR : " + this.getName());
			getAmas().getHeadAgent().addActivatedContextCopy(this);
		} else {
			getAmas().getHeadAgent().removeActivatedContextCopy(this);
		}

	}
	// --------

	public void clearNonValidPerceptNeighbors() {
		nonValidNeightborPercepts.clear();
	}

	public void clearNonValidPercepts() {
		nonValidPercepts.clear();
	}

	// --------------------------------NCS
	// Resolutions-----------------------------------------

	/**
	 * Solve NC S incompetent head.
	 *
	 * @param head the head
	 */
	public void solveNCS_IncompetentHead(Head head) {
		getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS INCOMPETENT HEAD")));
		////// System.out.println(world.getScheduler().getTick() +" " + this.getName()+
		////// " " +"solveNCS_IncompetentHead");
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
		getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS CONCURENCE")));
		////// System.out.println(world.getScheduler().getTick() +" " + this.getName()+
		////// " " + "solveNCS_Concurrence");
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
			getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),
					"*********************************************************************************************************** SOLVE NCS USELESSNESS")));
			getEnvironment().raiseNCS(NCS.CONTEXT_USELESSNESS);
			this.destroy();
			getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
		}

	}

	/**
	 * Solve NC S conflict inexact.
	 *
	 * @param head the head
	 */
	private void solveNCS_ConflictInexact(Head head) {
		////// System.out.println(world.getScheduler().getTick() +" " + this.getName()+
		////// " " + "solveNCS_ConflictInexact");
		getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		if (true) {
			confidence--;
		}
		// confidence = confidence * 0.5;
		updateExperiments();
	}

	private void setModelFromBetterContext(Context betterContext) {
		localModel = new LocalModelMillerRegression(this);

		this.confidence = betterContext.getConfidence();

		Double[] coef = ((LocalModelMillerRegression) betterContext.getLocalModel()).getCoef();

		((LocalModelMillerRegression) this.localModel).setCoef(coef);

		this.actionProposition = ((LocalModelMillerRegression) betterContext.getLocalModel())
				.getProposition(betterContext);

		this.experiments = new ArrayList<Experiment>();
		experiments.addAll(betterContext.getExperiments());
	}

	public void analyzeResults2(Head head) {
		// addNewExperiment();

		System.out.println(localModel.distance(getCurrentExperiment())
				+ "******************************************************************DISTANCE TO MODEL : ");

		if (head.getCriticity(this) > head.getErrorAllowed()) {

			Context betterContext = null;// head.getBetterContext(this,head.getActivatedNeighborsContexts(),
											// getErrorOnAllExperiments());

			if (betterContext != null) {
				System.out.println(this.getName() + "<---" + betterContext.getName());
				this.setModelFromBetterContext(betterContext);
				getAmas().getHeadAgent().setBadCurrentCriticalityPrediction();
			} else {
				System.out.println("OLD COEFS " + localModel.coefsToString());
				LocalModel newBetterModel = tryNewExperiment();

				if (newBetterModel != null) {
					localModel = newBetterModel;
					System.out.println("NEW COEFS " + localModel.coefsToString());
					getAmas().getHeadAgent().setBadCurrentCriticalityPrediction();

				} else {
					solveNCS_BadPrediction(head);
					getAmas().addAlteredContext(this);
				}
			}

		} else {
			System.out.println("OLD COEFS " + localModel.coefsToString());
			// localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(),0.5);
			// addCurrentExperimentTo(experiments);
			System.out.println("NEW COEFS " + localModel.coefsToString());
			confidence++;

		}

//		mappingCriticality = 0.0;
//		if(head.getActivatedNeighborsContexts().size()>0) {
//			for(Context ctxt : head.getActivatedNeighborsContexts()) {
//				mappingCriticality += this.distance(ctxt);
//			}
//			mappingCriticality /= head.getActivatedNeighborsContexts().size();
//		}

		// NCSDetection_OverMapping();

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

	public void analyzeResults4(Head head, Context closestContextToOracle) {
		if (head.getCriticity(this) < head.getErrorAllowed()) {
			confidence++;
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

		for (Percept pct : getAmas().getPercepts()) {
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

		if (overlapCounts == getAmas().getPercepts().size()) {
			return new Pair<Double, Percept>(-minDistance, null);
		} else if (overlapCounts == (getAmas().getPercepts().size() - 1)) {
			return new Pair<Double, Percept>(voidDistance, voidPercept);
		} else {
			return new Pair<Double, Percept>(maxDistance, null);
		}

	}

	public double distanceAsVolume(Context ctxt) {
		double totalDistanceAsVolume = 1.0;

		for (Percept pct : getAmas().getPercepts()) {
			double currentDistance = this.distanceForVolume(ctxt, pct);
			totalDistanceAsVolume *= currentDistance;
			// System.out.println(pct.getName() + " " + currentDistance);

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
			// System.out.println(pct.getName() + " " + currentDistance);

		}

		return maxDistance;
	}

	public double minDistance(Context ctxt) {
		double minDistance = Double.POSITIVE_INFINITY;

		for (Percept pct : getAmas().getPercepts()) {
			minDistance = Math.min(minDistance, this.distanceForMaxOrMin(ctxt, pct) / pct.getMinMaxDistance());
			// System.out.println(pct.getName() + " " + currentDistance);
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
		getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(), betterContext.getName(),
				"*********************************************************************************************************** SOLVE NCS BETTER NEIGHBOR")));
		localModel = new LocalModelMillerRegression(this, betterContext.getLocalModel().getCoef(),
				betterContext.getLocalModel().getFirstExperiments());
	}

	public void NCSDetection_OverMapping() {
		
		boolean fusionAcomplished = false;
		
		
		for(Context ctxt : getAmas().getHeadAgent().getActivatedNeighborsContexts()) {
			
			
			if(ctxt != this && !ctxt.isDying()) {
				
				fusionAcomplished = false;
	
				double currentDistanceToOraclePrediction = this.getLocalModel().distance(this.getCurrentExperiment());
				double otherContextDistanceToOraclePrediction = ctxt.getLocalModel().distance(ctxt.getCurrentExperiment());
				
				
				
	
				
				//if(this.sameModelAs(ctxt, world.getScheduler().getHeadAgent().getErrorAllowed()/10) ) {
				if((currentDistanceToOraclePrediction<getAmas().getHeadAgent().getDistanceToRegressionAllowed()) && (otherContextDistanceToOraclePrediction<getAmas().getHeadAgent().getDistanceToRegressionAllowed())) {
					
					getEnvironment().trace(new ArrayList<String>(Arrays.asList("currentDistanceToOraclePrediction",""+ currentDistanceToOraclePrediction,"otherContextDistanceToOraclePrediction",""+ otherContextDistanceToOraclePrediction))); 
					
					
					for(Percept pct : ranges.keySet()) {
						
						boolean fusionTest = true;
						
						getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),pct.getName(), ""+Math.abs(this.distance(ctxt, pct)), "DISTANCE", "" + getEnvironment().getMappingErrorAllowed())));
						if(Math.abs(this.distance(ctxt, pct)) < pct.getMappingErrorAllowed()){		
														
							for(Percept otherPct : ranges.keySet()) {
								
								if(otherPct != pct) {
																		
									double lengthDifference = Math.abs(ranges.get(otherPct).getLenght() - ctxt.getRanges().get(otherPct).getLenght());
									double centerDifference = Math.abs(ranges.get(otherPct).getCenter() - ctxt.getRanges().get(otherPct).getCenter());
									getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),otherPct.getName(), ""+lengthDifference,""+centerDifference, "LENGTH & CENTER DIFF", ""  + getEnvironment().getMappingErrorAllowed())));
									fusionTest = fusionTest && (lengthDifference < otherPct.getMappingErrorAllowed()/2) && (centerDifference< otherPct.getMappingErrorAllowed()/2);
								}
							}
							
							if(fusionTest) {
								solveNCS_OverMapping(ctxt,pct);
								fusionAcomplished = true;
							}
							
						}
					}
					
				}
								
			}
		}
		
	}

	private void solveNCS_OverMapping(Context fusionContext, Percept perceptFusion) {
		getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS OVERMAPPING")));
		getEnvironment().raiseNCS(NCS.CONTEXT_OVERMAPPING);

		if (this.getRanges().get(perceptFusion).getCenter() < fusionContext.getRanges().get(perceptFusion)
				.getCenter()) {
			this.getRanges().get(perceptFusion).setEnd(fusionContext.getRanges().get(perceptFusion).getEnd());
		} else {
			this.getRanges().get(perceptFusion).setStart(fusionContext.getRanges().get(perceptFusion).getStart());
		}

		this.setConfidence(Math.max(this.getConfidence(), fusionContext.getConfidence()));

		fusionContext.destroy();
		getAmas().getHeadAgent().setBadCurrentCriticalityMapping();
	}
	
	public void solveNCS_ChildContext() {
		getAmas().getHeadAgent().setActiveLearning(true);
		HashMap<String, Double> request = new HashMap<String, Double>();
		for(Percept pct : getAmas().getPercepts()) {
			request.put(pct.getName(), getRandomValueInRange(pct));
		}
		
		getAmas().getHeadAgent().setSelfRequest(request);
	}
	
	private Double getRandomValueInRange(Percept pct) {
		return ranges.get(pct).getStart() + ranges.get(pct).getLenght()*Math.random();
	}

	private boolean sameModelAs(Context ctxt, double errorAllowed) {
		if (this.getLocalModel().getCoef().length != ctxt.getLocalModel().getCoef().length) {
			return false;
		} else {
			double modelsDifference = 0.0;

			for (int i = 0; i < this.getLocalModel().getCoef().length; i++) {
				modelsDifference += Math.abs(this.getLocalModel().getCoef()[i] - ctxt.getLocalModel().getCoef()[i]);
			}

			// world.trace(new
			// ArrayList<String>(Arrays.asList(this.getName(),ctxt.getName(),
			// ""+modelsDifference, "MODELS DIFFERENCE", ""+ errorAllowed)));
			return modelsDifference < errorAllowed;
		}
	}

	private void addCurrentExperiment() {
		addCurrentExperimentTo(experiments);
	}

	private void addCurrentExperimentTo(ArrayList<Experiment> experimentsList) {
		ArrayList<Percept> percepts = getAmas().getPercepts();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(getAmas().getHeadAgent().getOracleValue());

		experimentsList.add(exp);
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

	private LocalModel tryNewExperiment() {

		LocalModel possibleNewlocalModel = new LocalModelMillerRegression(this);

		ArrayList<Experiment> newExperimentsList = new ArrayList<Experiment>();
		newExperimentsList.addAll(experiments);
		addCurrentExperimentTo(newExperimentsList);
		possibleNewlocalModel.updateModelWithExperiments(newExperimentsList);
		boolean betterModelTest = true;

		for (Experiment exp : experiments) {
			double oldModelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			double newModelError = Math
					.abs(possibleNewlocalModel.getProposition(newExperimentsList, exp) - exp.getOracleProposition());
			// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),"OLD MODEL",
			// oldModelError+"", "NEW MODEL", "" + newModelError)));
			betterModelTest = betterModelTest && (newModelError <= 0.00001 + oldModelError);
		}

		if (betterModelTest || (experiments.size() < (getAmas().getPercepts().size() + 1))) { // size
			experiments = newExperimentsList;
			return possibleNewlocalModel;
		} else
			return null;
	}

	private boolean tryNewExperiment2() {
		if (localModel.distance(getCurrentExperiment()) < 10.0) {
			localModel.updateModelWithExperimentAndWeight(getCurrentExperiment(), 0.5, 100);
			return true;
		}
		return false;
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

		for (Experiment exp : experiments) {
			double modelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			double alternativeModelError = Math
					.abs(alternativeModel.getProposition(experiments, exp) - exp.getOracleProposition());
			betterModelTest = betterModelTest && (alternativeModelError <= 0.00001 + modelError);
			sumError += alternativeModelError;
		}

		return new Pair<Boolean, Double>(betterModelTest, sumError);
	}

	public Double getErrorOnAllExperiments() {
		double sumError = 0.0;

		for (Experiment exp : experiments) {
			double modelError = Math.abs(localModel.getProposition(experiments, exp) - exp.getOracleProposition());
			sumError += modelError;
		}

		return sumError;
	}

	public double getPropositionOnExperiment(Experiment exp) {
		return localModel.getProposition(experiments, exp);
	}

	private void addNewExperiment() {
		addCurrentExperiment();
		localModel.updateModel(this);
	}

	public void solveNCS_BadPrediction(Head head) {
		getEnvironment().trace(new ArrayList<String>(Arrays.asList(this.getName(),
				"*********************************************************************************************************** SOLVE NCS CONFLICT")));
		getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);

		if (head.getNewContext() == this) {
			head.setNewContext(null);
		}
		;

		confidence -= 2;
		getAmas().getHeadAgent().setBadCurrentCriticalityConfidence();

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

		ranges.get(p).adapt(p.getValue());

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

	private boolean testIfOtherContextShouldFinalyShrink(Context otherContext, Percept shrinkingPercept) {
		boolean test = true;

		for (Percept pct : ranges.keySet()) {
			if (pct != shrinkingPercept) {
				test = test && (getRanges().get(pct).getLenght() > otherContext.getRanges().get(pct).getLenght());
			}
		}

		return test;
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

	/**
	 * Gets the percept with lesser impact on volume not included in.
	 *
	 * @param containingRanges the containing ranges
	 * @param c                the c
	 * @return the percept with lesser impact on volume not included in
	 */
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges,
			Context otherContext) { // Conflict or concurence
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;

		////// System.out.println("PerceptWithLesserImpactOnVolumeNotIncludedIn ...");
		for (Percept percept : containingRanges) {

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);

				if (!(otherRanges.getStart() <= ranges.get(percept).getStart()
						&& ranges.get(percept).getEnd() <= otherRanges.getEnd())) {

					////// System.out.println(percept.getName());

					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						////// System.out.println("end simu : " +
						////// ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue()) +
						////// " start : " + ranges.get(percept).getStart());
						vol = ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue())
								- ranges.get(percept).getStart();
					} else {
						////// System.out.println("end : " + ranges.get(percept).getEnd() + " start simu
						////// : " +
						////// ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue()));
						vol = ranges.get(percept).getEnd()
								- ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue());
					}

					////// System.out.println("Vol1 : " + vol);

					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							////// System.out.println(p2.getName());
							vol *= ranges.get(p2).getLenght();
							////// System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + " "
							////// + getName());
						}
					}
					////// System.out.println("Vol2 : " + vol);

					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
					////// System.out.println("Vol lost : " + volumeLost + "percept " +
					////// p.getName());
				}
			}
		}
		return p;
	}

	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn3(ArrayList<Percept> containingRanges,
			Context otherContext) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol = 1.0;

		//////// System.out.println("LESSER ...");
		for (Percept percept : containingRanges) {

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);

				if (!(otherRanges.getStart() <= ranges.get(percept).getStart()
						&& ranges.get(percept).getEnd() <= otherRanges.getEnd())) {

					//////// System.out.println(percept.getName());

					vol = Math.abs(Math.abs(otherRanges.getCenter() - ranges.get(percept).getCenter())
							- otherRanges.getRadius() - ranges.get(percept).getRadius());
//					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
//						////////System.out.println("end simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMax(percept.getValue()) + " start : " + ranges.get(percept).getStart());
//						vol = percept.getValue() - ranges.get(percept).getStart();
//					} else {
//						////////System.out.println("end : " + ranges.get(percept).getEnd() + " start simu : " + ranges.get(percept).simulateNegativeAVTFeedbackMin(percept.getValue()));
//						vol = ranges.get(percept).getEnd() - percept.getValue();
//					}

					//////// System.out.println("Vol1 : " + vol);

					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							//////// System.out.println(p2.getName());
							vol *= ranges.get(p2).getLenght();
							//////// System.out.println(p2.getName() + " " + ranges.get(p2).getLenght() + "
							//////// " + getName());
						}
					}
					//////// System.out.println("Vol2 : " + vol);

					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}

					//////// System.out.println("Vol lost : " + volumeLost);

				}
			}
		}
		return p;
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
						getEnvironment().trace(new ArrayList<String>(
								Arrays.asList("ORDER :", percept.getName(), this.getName(), bestContext.getName())));
						vol = Math.abs(
								percept.getEndRangeProjection(this) - percept.getStartRangeProjection(bestContext));
						if (vol < volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					} else if (percept.contextOrder(bestContext, this)) {
						getEnvironment().trace(new ArrayList<String>(
								Arrays.asList("ORDER :", percept.getName(), bestContext.getName(), this.getName())));
						vol = Math.abs(
								percept.getEndRangeProjection(bestContext) - percept.getStartRangeProjection(this));
						if (vol < volumeLost) {
							volumeLost = vol;
							perceptWithBiggerImpact = percept;
						}
					} else if (percept.contextIncludedIn(bestContext, this)) {
						getEnvironment().trace(new ArrayList<String>(Arrays.asList("INCLUSION :", percept.getName(),
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

	private Percept getPerceptWithLesserImpactOnVolumeOnOverlap(ArrayList<Percept> containingRanges,
			Context otherContext) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;

		for (Percept percept : containingRanges) {

			if (!ranges.get(percept).isPerceptEnum()) {
				Range otherRanges = otherContext.getRanges().get(percept);

				if (!(otherRanges.getStart() <= ranges.get(percept).getStart()
						&& ranges.get(percept).getEnd() <= otherRanges.getEnd())) {

					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						vol = ranges.get(percept).simulateNegativeAVTFeedbackEnd(percept.getValue())
								- ranges.get(percept).getStart();
					} else {
						vol = ranges.get(percept).getEnd()
								- ranges.get(percept).simulateNegativeAVTFeedbackStart(percept.getValue());
					}

					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							vol *= ranges.get(p2).getLenght();
						}
					}
					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
				}
			}
		}
		return p;
	}

	/**
	 * Gets the percept with lesser impact on volume.
	 *
	 * @param containingRanges the containing ranges
	 * @return the percept with lesser impact on volume
	 */
	private Percept getPerceptWithLesserImpactOnVolume(ArrayList<Percept> containingRanges) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;

		for (Percept v : containingRanges) {
			if (!ranges.get(v).isPerceptEnum()) {

				if (ranges.get(v).getNearestLimit(v.getValue()) == false) {
					vol = ranges.get(v).simulateNegativeAVTFeedbackEnd(v.getValue()) - ranges.get(v).getStart();
				} else {
					vol = ranges.get(v).getEnd() - ranges.get(v).simulateNegativeAVTFeedbackStart(v.getValue());
				}

				for (Percept v2 : ranges.keySet()) {
					if (!ranges.get(v).isPerceptEnum() && v2 != v) {
						vol *= ranges.get(v2).getLenght();
					}
				}
				if (vol < volumeLost) {
					volumeLost = vol;
					p = v;
				}
			}
		}
		//////// System.out.println("percept " + p.getName());
		return p;
	}

	public double getOverlappingVolume(Context overlappingCtxt) {
		double volume = 1.0;
		for (Percept pct : ranges.keySet()) {
			volume *= this.getRanges().get(pct).overlapDistance(overlappingCtxt.getRanges().get(pct));
		}
		return volume;
	}

	private Pair<Percept, Context> getPerceptForAdaptationWithOverlapingContext(ArrayList<Percept> percepts) {
		Percept perceptForAdapation = null;
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
							perceptForAdapation = getPerceptWithBiggerImpactOnOverlap(percepts, ctxt);
							overlapingContext = ctxt;
						}
					}
				}
			}
		}
		if (perceptForAdapation == null) {
			for (Percept pct : percepts) {
				if (!ranges.get(pct).isPerceptEnum()) {

					distanceToFrontier = Math.min(ranges.get(pct).startDistance(pct.getValue()),
							ranges.get(pct).endDistance(pct.getValue()));

					if (distanceToFrontier < minDistanceToFrontier) {
						minDistanceToFrontier = distanceToFrontier;
						perceptForAdapation = pct;
					}
				}
			}
		}

		return new Pair<Percept, Context>(perceptForAdapation, overlapingContext);
	}

	public boolean containedBy(Context ctxt) {
		boolean contained = true;

		for (Percept pct : ranges.keySet()) {

			contained = contained && ranges.get(pct).containedBy(ctxt.getRanges().get(pct));
		}

		return contained;
	}

	/**
	 * Gets the percept with lesser impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with lesser impact on AVT
	 */
	private Percept getPerceptWithLesserImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.MAX_VALUE;
		double tempImpact;

		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue()));

				if (tempImpact < impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}

	/**
	 * Gets the percept with larger impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with larger impact on AVT
	 */
	private Percept getPerceptWithLargerImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.NEGATIVE_INFINITY;
		double tempImpact;

		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = (-1)
						* Math.abs(ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue())));

				if (tempImpact > impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}

	/**
	 * Compute validity.
	 *
	 * @return true, if successful
	 */
	private boolean computeValidity() {
		boolean b = true;
		for (Percept p : ranges.keySet()) {
			if (ranges.get(p).contains(p.getValue()) != 0) {
				b = false;
				break;
			}
		}
		return b;
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
			////////// System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct,
			////////// situation.get(pct)));
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
			////////// System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct,
			////////// situation.get(pct)));
			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}

		return influence;
	}

	public double getWorstInfluenceWithConfidence(HashMap<Percept, Double> situation) {
		Double worstInfluence = Double.POSITIVE_INFINITY;
		Double currentInfluence = 0.0;

		for (Percept pct : situation.keySet()) {
			////////// System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct,
			////////// situation.get(pct)));
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
			////////// System.out.println("INFLUTEST " + getInfluenceByPerceptSituation(pct,
			////////// situation.get(pct)));
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

		return Math.exp(-Math.pow(situation - center, 2) / (2 * Math.pow(radius, 2)));
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
	 * Update experiments.
	 */
	private void updateExperiments() {
		// ////////System.out.println("Update experiments");
		ArrayList<Percept> percepts = getAmas().getPercepts();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment(this);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}
		exp.setOracleProposition(getAmas().getHeadAgent().getOracleValue());

		experiments.add(exp);
		getAmas().addAlteredContext(this);
		localModel.updateModel(this);
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
		////////System.out.println("Grow " + this.getName() );
		ArrayList<Percept> allPercepts = getAmas().getPercepts();
		for (Percept pct : allPercepts) {
			boolean contain = ranges.get(pct).contains(pct.getValue()) == 0 ;
			////////System.out.println(pct.getName() + " " + contain);
			if (!contain) {
				if(ranges.get(pct).getLenght()<(pct.getMappingErrorAllowed()*1.5)) {
					ranges.get(pct).adapt(pct.getValue());
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
		// if(perceptWithLesserImpact!=null) world.trace(new
		// ArrayList<String>(Arrays.asList(this.getName(),perceptWithLesserImpact.getName(),
		// "PERCEPT BIGGER IMPACT")));

		if (perceptWithBiggerImpactOnOverlap == null) {
			this.destroy();
		} else {
			// ranges.get(perceptWithBiggerImpactOnOverlap).matchBorderWithBestContext(bestContext);
			// ranges.get(perceptWithBiggerImpactOnOverlap).adaptTowardsBorder(bestContext);

			ranges.get(perceptWithBiggerImpactOnOverlap).adapt(perceptWithBiggerImpactOnOverlap.getValue());

//			if(testIfOtherContextShouldFinalyShrink(bestContext, perceptWithLesserImpact)){
//				bestContext.getRanges().get(perceptWithLesserImpact).adaptTowardsBorder(this);
//			}
//			else {
//				ranges.get(perceptWithLesserImpact).adaptTowardsBorder(bestContext);
//			}
		}
	}

	private ArrayList<Percept> getOverlapingPercepts(Context bestContext) {
		ArrayList<Percept> overlapingPercepts = new ArrayList<Percept>();

		for (Percept pct : ranges.keySet()) {
			if (distance(bestContext, pct) < 0) {
				overlapingPercepts.add(pct);
			}
		}

		return overlapingPercepts;
	}

	private double distance(Context ctxt, Percept pct) {
		return this.getRanges().get(pct).distance(ctxt.getRanges().get(pct));
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

	public void addContext(Context ctxt) {
		if (ctxt != this) {
			otherContextsDistancesByPercept.put(ctxt, new HashMap<Percept, Pair<Double, Integer>>());
		}
		for (Percept pct : getAmas().getPercepts()) {
			otherContextsDistancesByPercept.get(ctxt).put(pct, new Pair<>(null, getAmas().getCycle()));
		}
	}

	public void addContextDistance(Context ctxt, Percept percept, double distance) {
		if (ctxt != this) {

			if (otherContextsDistancesByPercept.get(ctxt) == null) {
				addContext(ctxt);
			}
			otherContextsDistancesByPercept.get(ctxt).put(percept, new Pair<>(distance, getAmas().getCycle()));
		}
	}

	public void removeContext(Context ctxt) {
		otherContextsDistancesByPercept.remove(ctxt);
	}

	public Integer getContextDistanceUpdateTick(Context ctxt, Percept pct) {
		if (otherContextsDistancesByPercept.get(ctxt) != null) {
			if (otherContextsDistancesByPercept.get(ctxt).get(pct) != null) {
				return otherContextsDistancesByPercept.get(ctxt).get(pct).getB();
			}
		}
		return null;
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

	public void addNonValidPercept(Percept pct) {
		// world.trace(new ArrayList<String>(Arrays.asList(this.getName(),pct.getName(),
		// "NON VALID")));
		nonValidPercepts.add(pct);
	}

	public ArrayList<Percept> getNonValidPercepts() {
		return nonValidPercepts;
	}

	public void removeNonValidPercept(Percept pct) {
		nonValidPercepts.remove(pct);
	}

	public void addNonValidNeighborPercept(Percept pct) {

		nonValidNeightborPercepts.add(pct);

	}

	public ArrayList<Percept> getNonValidNeighborPercepts() {

		return nonValidNeightborPercepts;
	}

	public void removeNonValidNeighborPercept(Percept pct) {
		nonValidNeightborPercepts.remove(pct);
	}

	@Override
	protected void onAct() {

		if (nonValidPercepts.size() == 0) {

			getAmas().getHeadAgent().proposition(this);

			for (Percept pct : getAmas().getPercepts()) {
				getAmas().getHeadAgent().addPartiallyActivatedContextInNeighbors(pct, this);
			}

		} else if (nonValidPercepts.size() == 1) {
			getAmas().getHeadAgent().addPartiallyActivatedContext(nonValidPercepts.get(0), this);
		}

		if (nonValidNeightborPercepts.size() == 0) {

			getAmas().getHeadAgent().addRequestNeighbor(this);
		} else if (nonValidNeightborPercepts.size() == 1) {
			getAmas().getHeadAgent().addPartialRequestNeighborContext(nonValidNeightborPercepts.get(0), this);
		}

		if ((nonValidNeightborPercepts.size() == 0) && (nonValidPercepts.size() == 1)) {

			getAmas().getHeadAgent().addPartiallyActivatedContextInNeighbors(nonValidPercepts.get(0), this);

		}

		this.activations = 0;
		this.valid = false;

		// Reset percepts validities
		for (Percept percept : perceptValidities.keySet()) {
			perceptValidities.put(percept, false);
			perceptNeighborhoodValidities.put(percept, false);
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
	public void onInitialization() {
		super.onInitialization();
		ranges = new HashMap<Percept, Range>();
		experiments = new ArrayList<Experiment>();
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
		s += "Max Prediction " + getLocalModel().getMaxProposition(this) + "\n";
		s += "Min Prediction " + getLocalModel().getMinProposition(this) + "\n";
		return s;
	}
	
	public String toStringFull() {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "\n";

		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
		// double[] coefs = ((LocalModelMillerRegression) this.localModel).getCoef();
		// for (int i = 1 ; i < coefs.length ; i++) {
		/*
		 * if (Double.isNaN(coefs[i])) { s += "0.0" + "\t"; } else { s += coefs[i] +
		 * "\t"; }
		 */
		// s += coefs[i] + "\t";
		// }
		// s += "\n";
		s += "\n";
		
		s += "Max Prediction " + getLocalModel().getMaxProposition(this) + "\n";
		s += "Min Prediction " + getLocalModel().getMinProposition(this) + "\n";
		
		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";

			s += "\n";
			s += "Neighbours : \n";

			if (nearestNeighbours.get(v).get("start") != null) {
				s += "START :" + nearestNeighbours.get(v).get("start").getName() + "\n";
			} else {
				s += "START : \n";
			}
			s += "Sorted start possible neighbours :\n";
			if (sortedPossibleNeighbours.get(v).get("start").size() > 0) {
				for (Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {

					if (ctxt.equals(this)) {
						s += "# " + ctxt.getName() + " --> " + ctxt.getRanges().get(v).getStart() + "\n";
					} else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getStart() + "\n";
					}
				}
			}
			s += "Sorted end possible neighbours :\n";
			if (sortedPossibleNeighbours.get(v).get("end").size() > 0) {
				for (Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {

					if (ctxt.equals(this)) {
						s += "# " + ctxt.getName() + " --> " + ctxt.getRanges().get(v).getEnd() + "\n";
					} else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getEnd() + "\n";
					}
				}
			}

			if (nearestNeighbours.get(v).get("end") != null) {
				s += "END :" + nearestNeighbours.get(v).get("end").getName() + "\n";
			} else {
				s += "END : \n";
			}

		}
		s += "\n";
		s += "Number of activations : " + activations + "\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Number of experiments : " + experiments.size() + "\n";
		for (Experiment exp : experiments) {
			s += exp.toString();
		}

		s += "Confidence : " + confidence + "\n";
//		if (formulaLocalModel != null) {
//			s += "Local model : " + this.formulaLocalModel + "\n";
//		} else {
//			s += "Local model : " + localModel.getFormula(this) + "\n";
//		}

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
		s += "Number of experiments : " + experiments.size() + "\n";
		for (Experiment exp : experiments) {
			s += exp.toString();
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
		getEnvironment().trace(new ArrayList<String>(
				Arrays.asList("-----------------------------------------", this.getName(), "DIE")));
		for (Context ctxt : getAmas().getContexts()) {
			ctxt.removeContext(this);
		}

		for (Percept percept : getAmas().getPercepts()) {
			percept.deleteContextProjection(this);
		}

		super.destroy();
	}

	public double getActionProposal() {
		return localModel.getProposition(this);
	}

	public double getCenterByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				+ this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	public double getConfidence() {
		return confidence;
	}

	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}

	public LocalModel getFunction() {
		return localModel;
	}

	public double getRadiusByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				- this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	public Range getRangeByPerceptName(String perceptName) {
		for (Percept prct : ranges.keySet()) {
			if (prct.getName().equals(perceptName)) {
				return ranges.get(prct);
			}
		}
		return null;
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
	
	public void addExperiment(Experiment exp) {
		experiments.add(exp);
	}
	
	/**
	 * Set the local model. Used mostly during save loading
	 * @param localModel
	 */
	public void setLocalModel(LocalModel localModel) {
		this.localModel = localModel;
		this.localModel.context = this;
	}
}
