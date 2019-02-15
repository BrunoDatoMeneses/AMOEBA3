package agents.head;

import java.util.ArrayList;
import java.util.HashMap;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import agents.MessageType;
import agents.context.Context;
import agents.percept.Percept;
import kernel.AMOEBA;
import ncs.NCS;

/**
 * The Class Head.
 */
public class Head extends AmoebaAgent {

	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;
	// functionSelected value never used -> removed

	HashMap<Percept, Double> currentSituation = new HashMap<>();

	private ArrayList<Context> activatedContexts = new ArrayList<>();

	private ArrayList<Double> xLastCriticityValues = new ArrayList<>();

	// nPropositionsReceived value never used -> removed
	// averagePredictionCriticityWeight never used -> removed
	private int numberOfCriticityValuesForAverage = 100;

	private int nConflictBeforeAugmentation = 1;
	private int nSuccessBeforeDiminution = 50;
	private int perfIndicator = 1;
	private int nConflictBeforeInexactAugmentation = 2;
	private int nSuccessBeforeInexactDiminution = 50;
	private int perfIndicatorInexact = 0;

	private double prediction;
	private double oracleValue;
	// oldOracleValue never used -> removed
	private double criticity;
	// oldCriticity never used -> removed
	private double averagePredictionCriticity;

	private double errorAllowed = 1.0;
	private double augmentationFactorError = 1.05;
	private double diminutionFactorError = 0.9;
	private double minErrorAllowed = 1.00;
	private double inexactAllowed = 0.4;
	private double augmentationInexactError = 1.8;
	private double diminutionInexactError = 0.6;
	private double minInexactAllowed = 0.5;

	private boolean noCreation = true;
	private boolean useOracle = true;
	private boolean firstContext = false;
	// newContextWasCreated value never used -> removed
	private boolean contextFromPropositionWasSelected = false;

	Double maxConfidence;
	Double minConfidence;

	// noBestContext value never used -> removed

	/**
	 * Instantiates a new head.
	 *
	 * @param world
	 *            the world
	 */
	public Head(AMOEBA amas) {
		super(amas);
		maxConfidence = Double.NEGATIVE_INFINITY;
		minConfidence = Double.POSITIVE_INFINITY;
	}

	@Override
	protected int computeExecutionOrderLayer() {
		return 2;
	}

	/**
	 * Sets the data for error margin.
	 *
	 * @param errorAllowed
	 *            the error allowed
	 * @param augmentationFactorError
	 *            the augmentation factor error
	 * @param diminutionFactorError
	 *            the diminution factor error
	 * @param minErrorAllowed
	 *            the min error allowed
	 * @param nConflictBeforeAugmentation
	 *            the n conflict before augmentation
	 * @param nSuccessBeforeDiminution
	 *            the n success before diminution
	 */
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError,
			double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution) {
		this.errorAllowed = errorAllowed;
		this.augmentationFactorError = augmentationFactorError;
		this.diminutionFactorError = diminutionFactorError;
		this.minErrorAllowed = minErrorAllowed;
		this.nConflictBeforeAugmentation = nConflictBeforeAugmentation;
		this.nSuccessBeforeDiminution = nSuccessBeforeDiminution;
	}

	/**
	 * Sets the data for inexact margin.
	 *
	 * @param inexactAllowed
	 *            the inexact allowed
	 * @param augmentationInexactError
	 *            the augmentation inexact error
	 * @param diminutionInexactError
	 *            the diminution inexact error
	 * @param minInexactAllowed
	 *            the min inexact allowed
	 * @param nConflictBeforeInexactAugmentation
	 *            the n conflict before inexact augmentation
	 * @param nSuccessBeforeInexactDiminution
	 *            the n success before inexact diminution
	 */
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError,
			double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation,
			int nSuccessBeforeInexactDiminution) {
		this.inexactAllowed = inexactAllowed;
		this.augmentationInexactError = augmentationInexactError;
		this.diminutionInexactError = diminutionInexactError;
		this.minInexactAllowed = minInexactAllowed;
		this.nConflictBeforeInexactAugmentation = nConflictBeforeInexactAugmentation;
		this.nSuccessBeforeInexactDiminution = nSuccessBeforeInexactDiminution;
	}

	@Override
	public void computeAMessage(AmoebaMessage m) {
		if (m.getType() == MessageType.PROPOSAL) { // Value useless
			activatedContexts.add((Context) m.getSender());
		}
	}

	/**
	 * The core method of the head agent. Manage the whole behavior, and call method
	 * from context agents when needed.
	 */
	@Override
	protected void onAct() { // play
		for (Percept pct : this.amas.getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}

		activatedContexts.size();
		setContextFromPropositionWasSelected(false);
		oracleValue = this.amas.getPerceptionsOrAction("oracle");

		/* The head memorize last used context agent */
		lastUsedContext = bestContext;
		bestContext = null;

		/* useOracle means that data are labeled */
		if (useOracle) {
			playWithOracle();
		} else {
			playWithoutOracle();
		}

		updateStatisticalInformations();

		newContext = null;
	}

	private void playWithOracle() {

		if (activatedContexts.size() > 0) {
			selectBestContext(); // using highest confidence
		}

		if (bestContext != null) {
			setContextFromPropositionWasSelected(true);
			prediction = bestContext.getActionProposal();

		} else if (!noCreation) { /*
									 * noCreation is only used to disable creation of contexts, for research
									 * purposes
									 */
			getNearestContextAsBestContext();
		}

		/* Compute the criticity. Will be used by context agents. */
		criticity = Math.abs(oracleValue - prediction);

		/* If we have a bestcontext, send a selection message to it */
		if (bestContext != null) {
			sendExpressMessage(this, MessageType.SELECTION, bestContext);
		}

		selfAnalysationOfContexts();

		NCSDetection_IncompetentHead(); /*
										 * If there isn't any proposition or only bad propositions, the head is
										 * incompetent. It needs help from a context.
										 */
		NCSDetection_Concurrence(); /* If result is good, shrink redundant context (concurrence NCS) */
		NCSDetection_Create_New_Context(); /* Finally, head agent check the need for a new context agent */
	}

	/**
	 * Play without oracle.
	 */
	private void playWithoutOracle() {

		selectBestContext();
		if (bestContext != this.lastUsedContext) {
			prediction = bestContext.getActionProposal();
		} else {
			ArrayList<Context> allContexts = amas.getContexts();
			Context nearestContext = this.getNearestContext(allContexts);
			prediction = nearestContext.getActionProposal();
			bestContext = nearestContext;
		}
		bestContext.getFunction().getFormula(bestContext);
		criticity = Math.abs(oracleValue - prediction);
	}

	private void NCSDetection_Create_New_Context() {
		/* Finally, head agent check the need for a new context agent */

		boolean newContextCreated = false;
		ArrayList<Context> allContexts = amas.getContexts();
		if (getDistanceToNearestGoodContext(allContexts) > 0) {
			Context context = createNewContext();

			bestContext = context;
			newContext = context;
			newContextCreated = true;
		}

		if (!newContextCreated) {
			updateStatisticalInformations();
		}
	}

	private void NCSDetection_Concurrence() {
		/* If result is good, shrink redundant context (concurrence NCS) */
		if (bestContext != null && criticity <= this.errorAllowed) {
			for (int i = 0; i < activatedContexts.size(); i++) {
				if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying()
						&& this.getCriticity(activatedContexts.get(i)) <= this.errorAllowed) {
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
		if (activatedContexts.isEmpty() || (criticity > this.errorAllowed && !oneOfProposedContextWasGood())) {
			ArrayList<Context> allContexts = amas.getContexts();

			Context c = getNearestGoodContext(allContexts);
			if (c != null)
				c.solveNCS_IncompetentHead(this);
			;
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

	private void selfAnalysationOfContexts() {
		/* All context which proposed itself must analyze its proposition */
		for (int i = 0; i < activatedContexts.size(); i++) {
			if(activatedContexts.get(i).isDying()) {
				activatedContexts.remove(i);
			}else
				activatedContexts.get(i).analyzeResults(this);//message TODO
		}
	}

	private void getNearestContextAsBestContext() {
		ArrayList<Context> allContexts = amas.getContexts();
		Context nearestContext = this.getNearestContext(allContexts);

		if (nearestContext != null) {
			prediction = nearestContext.getActionProposal();//message TODO
		} else {
			prediction = 0;
		}

		bestContext = nearestContext;
	}

	/**
	 * Gets the nearest good context.
	 *
	 * @param allContext
	 *            the all context
	 * @return the nearest good context
	 */
	private Context getNearestGoodContext(ArrayList<Context> allContext) {
		Context nearest = null;
		for (AmoebaAgent a : allContext) {
			Context c = (Context) a;
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < getExternalDistanceToContext(nearest)) {
					nearest = c;
				}
			}
		}
		return nearest;
	}

	/**
	 * Gets the distance to nearest good context.
	 *
	 * @param allContext
	 *            the all context
	 * @return the distance to nearest good context
	 */
	private double getDistanceToNearestGoodContext(ArrayList<Context> allContext) {
		double d = Double.MAX_VALUE;
		for (AmoebaAgent a : allContext) {
			Context c = (Context) a;
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (getExternalDistanceToContext(c) < d) {
					d = getExternalDistanceToContext(c);
				}
			}
		}
		return d;
	}

	/**
	 * Gets the nearest context.
	 *
	 * @param allContext
	 *            the all context
	 * @return the nearest context
	 */
	private Context getNearestContext(ArrayList<Context> allContext) {
		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (AmoebaAgent a : allContext) {
			Context c = (Context) a;
			if (c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < distanceToNearest) {
					nearest = c;
					distanceToNearest = getExternalDistanceToContext(c);
				}
			}
		}

		return nearest;
	}

	/**
	 * Gets the external distance to context.
	 *
	 * @param context
	 *            the context
	 * @return the external distance to context
	 */
	private double getExternalDistanceToContext(Context context) {
		double d = 0.0;
		ArrayList<Percept> percepts = amas.getPercepts();
		for (Percept p : percepts) {

			// isEnum deleted -> see Percept.java (here deletion of an if branch)
			
			//TODO see if possible to message 
			double min = context.getRanges().get(p).getStart();
			double max = context.getRanges().get(p).getEnd();

			if (min > p.getValue() || max < p.getValue()) {
				d += Math.min(Math.abs(p.getValue() - min), Math.abs(p.getValue() - max));
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
			//TODO see if possible to message 
			if (oracleValue - c.getActionProposal() < errorAllowed) {
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
		amas.getEnvironment().raiseNCS(NCS.CREATE_NEW_CONTEXT);
		Context context;
		if (firstContext) {
			context = new Context(amas, this);
		} else {
			context = new Context(amas, this);
			firstContext = true;
		}

		return context;
	}

	/**
	 * Update statistical informations.
	 */
	private void updateStatisticalInformations() {
		xLastCriticityValues.add(criticity);

		averagePredictionCriticity = 0;
		for (Double d : xLastCriticityValues) {
			averagePredictionCriticity += d;
		}
		averagePredictionCriticity /= xLastCriticityValues.size();

		if (xLastCriticityValues.size() >= numberOfCriticityValuesForAverage) {
			xLastCriticityValues.remove(0);
		}

		if (averagePredictionCriticity > errorAllowed) {
			perfIndicator--;
		} else {
			perfIndicator++;
		}

		if (perfIndicator <= nConflictBeforeAugmentation * (-1)) {
			perfIndicator = 0;
			errorAllowed *= augmentationFactorError;
		}

		if (perfIndicator >= nSuccessBeforeDiminution) {
			perfIndicator = 0;
			errorAllowed *= diminutionFactorError;
			errorAllowed = Math.max(minErrorAllowed, errorAllowed);
		}

		if (averagePredictionCriticity > inexactAllowed) {
			perfIndicatorInexact--;
		} else {
			perfIndicatorInexact++;
		}

		if (perfIndicatorInexact <= nConflictBeforeInexactAugmentation * (-1)) {
			perfIndicatorInexact = 0;
			inexactAllowed *= augmentationInexactError;
		}

		if (perfIndicatorInexact >= nSuccessBeforeInexactDiminution) {
			perfIndicatorInexact = 0;
			inexactAllowed *= diminutionInexactError;
			inexactAllowed = Math.max(minInexactAllowed, inexactAllowed);
		}
	}

	/**
	 * Select best context.
	 */
	private void selectBestContext() {
		Context bc;
		if (activatedContexts.isEmpty()) {
			bc = lastUsedContext;
		} else {
			bc = activatedContexts.get(0);
		}
		double currentConfidence = Double.NEGATIVE_INFINITY;

		for (Context context : activatedContexts) {
			if (context.getConfidence() > currentConfidence) {
				bc = context;
				//TODO see if possible to message 
				currentConfidence = bc.getConfidence();
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
	 * Gets the criticity.
	 *
	 * @return the criticity
	 */
	public double getCriticity() {
		return criticity;
	}

	// getNoBestContext never used -> removed

	/**
	 * Gets the criticity.
	 *
	 * @param context
	 *            the context
	 * @return the criticity
	 */
	public double getCriticity(Context context) {
		return Math.abs(oracleValue - context.getActionProposal());
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public double getAction() {
		return prediction;
	}

	/**
	 * Sets the no creation.
	 *
	 * @param noCreation
	 *            the new no creation
	 */
	public void setNoCreation(boolean noCreation) {
		this.noCreation = noCreation;
	}

	/**
	 * Gets the oracle value.
	 *
	 * @return the oracle value
	 */
	public double getOracleValue() {
		return oracleValue;
	}

	/**
	 * Gets the error allowed.
	 *
	 * @return the error allowed
	 */
	public double getErrorAllowed() {
		return errorAllowed;
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
	 * @param newContext
	 *            the new new context
	 */
	public void setNewContext(Context newContext) {
		this.newContext = newContext;
	}

	/**
	 * Change oracle connection.
	 */
	public void changeOracleConnection() {
		useOracle = !useOracle;
	}

	/**
	 * Gets the inexact allowed.
	 *
	 * @return the inexact allowed
	 */
	public double getInexactAllowed() {
		return inexactAllowed;
	}

	/**
	 * Checks if is context from proposition was selected.
	 *
	 * @return true, if is context from proposition was selected
	 */
	public boolean isContextFromPropositionWasSelected() {
		return contextFromPropositionWasSelected;
	}

	/**
	 * Sets the context from proposition was selected.
	 *
	 * @param contextFromPropositionWasSelected
	 *            the new context from proposition was selected
	 */
	public void setContextFromPropositionWasSelected(boolean contextFromPropositionWasSelected) {
		this.contextFromPropositionWasSelected = contextFromPropositionWasSelected;
	}

	public Head clone() throws CloneNotSupportedException {
		return (Head) super.clone();
	}
	
}
