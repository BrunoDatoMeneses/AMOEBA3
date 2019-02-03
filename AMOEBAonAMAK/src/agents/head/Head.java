package agents.head;

import java.util.ArrayList;
import java.util.HashMap;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import agents.MessageType;
import agents.context.Context;
import agents.percept.Percept;
import kernel.AMOEBA;

/**
 * The Class Head.
 */
public class Head extends AbstractHead {

	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;
	private String functionSelected;

	HashMap<Percept, Double> currentSituation = new HashMap<>();

	private ArrayList<Context> activatedContexts = new ArrayList<>();

	private ArrayList<Double> xLastCriticityValues = new ArrayList<>();

	private int nPropositionsReceived;
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
	private boolean newContextWasCreated = false;
	private boolean contextFromPropositionWasSelected = false;

	Double maxConfidence;
	Double minConfidence;

	// Endogenous feedback
	private boolean noBestContext;

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
		for (Percept pct : this.world.getScheduler().getPercepts()) {// TODO what became World ?
			currentSituation.put(pct, pct.getValue());
		}

		nPropositionsReceived = activatedContexts.size();
		newContextWasCreated = false;
		setContextFromPropositionWasSelected(false);
		oracleValue = this.getWorld().getScheduler().getPerceptionsOrAction("oracle"); // TODO what became World ?

		/* The head memorize last used context agent */
		lastUsedContext = bestContext;
		bestContext = null;

		super.onAct();

		/* useOracle means that data are labeled */
		if (useOracle) {
			playWithOracle();
		} else {
			playWithoutOracle();
		}

		updateStatisticalInformations(); /// TODO regarder dans le détail, possible que ce soit pas trop utile

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
			functionSelected = bestContext.getFunction().getFormula(bestContext);
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
		// TODO : Is it usefull to print that way?
		// Config.print("Nombre de contextes activés: " + activatedContexts.size(), 1);

		selectBestContext();
		if (bestContext != this.lastUsedContext) {
			noBestContext = false;
			prediction = bestContext.getActionProposal();
		} else {
			noBestContext = true;
			ArrayList<AmoebaAgent> allContexts = world.getScheduler().getContexts(); // TODO what becomes World?
			Context nearestContext = this.getNearestContext(allContexts);
			prediction = nearestContext.getActionProposal();
			bestContext = nearestContext;
		}
		// Config.print("Best context selected without oracle is : " +
		// bestContext.getName(),0);
		// Config.print("BestContext : " + bestContext.toStringFull() + " " +
		// bestContext.getConfidence(), 2);
		functionSelected = bestContext.getFunction().getFormula(bestContext);
		criticity = Math.abs(oracleValue - prediction);
	}

	private void NCSDetection_Create_New_Context() {
		/* Finally, head agent check the need for a new context agent */

		boolean newContextCreated = false;
		ArrayList<AmoebaAgent> allContexts = world.getScheduler().getContexts(); // TODO : What becames World ?
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
			ArrayList<AmoebaAgent> allContexts = world.getScheduler().getContexts(); // TODO What becames World ?

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
			activatedContexts.get(i).analyzeResults(this);
		}
	}

	private void getNearestContextAsBestContext() {
		ArrayList<AmoebaAgent> allContexts = world.getScheduler().getContexts(); // TODO What becames world ?
		Context nearestContext = this.getNearestContext(allContexts);

		if (nearestContext != null) {
			prediction = nearestContext.getActionProposal();
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
	private Context getNearestGoodContext(ArrayList<AmoebaAgent> allContext) {
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
	private double getDistanceToNearestGoodContext(ArrayList<AmoebaAgent> allContext) {
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
	private Context getNearestContext(ArrayList<AmoebaAgent> allContext) {
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
		ArrayList<Percept> percepts = world.getAllPercept();
		for (Percept p : percepts) {

			// isEnum deleted -> see Percept.java (here deletion of an if branch)
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
		newContextWasCreated = true;
		world.raiseNCS(NCS.CREATE_NEW_CONTEXT); // TODO : What becames World ?
		Context context;
		if (firstContext) {
			context = new Context(world, this); // TODO : What becames World ?

			// TODO Is it usefull ?
			// Config.print("new context agent", 3);
		} else {
			context = new Context(world, this); // TODO : What becames World ?
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
	 * @param contexts
	 *            the new contexts
	 */
	public void setActivatesContexts(ArrayList<Context> contexts) {
		this.activatedContexts = contexts;
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
	 * Sets the best context.
	 *
	 * @param bestContext
	 *            the new best context
	 */
	public void setBestContext(Context bestContext) {
		this.bestContext = bestContext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see agents.head.AbstractHead#getTargets()
	 */
	@Override
	public ArrayList<? extends AmoebaAgent> getTargets() {
		return activatedContexts;
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
	 * Sets the criticity.
	 *
	 * @param criticity
	 *            the new criticity
	 */
	public void setCriticity(double criticity) {
		this.criticity = criticity;
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
	 * Sets the action.
	 *
	 * @param action
	 *            the new action
	 */
	public void setAction(double action) {
		this.prediction = action;
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
	 * Checks if is no creation.
	 *
	 * @return true, if is no creation
	 */
	public boolean isNoCreation() {
		return noCreation;
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
	 * Sets the oracle value.
	 *
	 * @param oracleValue
	 *            the new oracle value
	 */
	public void setOracleValue(double oracleValue) {
		this.oracleValue = oracleValue;
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
	 * Sets the error allowed.
	 *
	 * @param errorAllowed
	 *            the new error allowed
	 */
	public void setErrorAllowed(double errorAllowed) {
		this.errorAllowed = errorAllowed;
	}

	/**
	 * Gets the average prediction criticity.
	 *
	 * @return the average prediction criticity
	 */
	public double getAveragePredictionCriticity() {
		return averagePredictionCriticity;
	}

	/**
	 * Sets the average prediction criticity.
	 *
	 * @param averagePredictionCriticity
	 *            the new average prediction criticity
	 */
	public void setAveragePredictionCriticity(double averagePredictionCriticity) {
		this.averagePredictionCriticity = averagePredictionCriticity;
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
	 * @param functionSelected
	 *            the new function selected
	 */
	public void setFunctionSelected(String functionSelected) {
		this.functionSelected = functionSelected;
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
	 * Sets the inexact allowed.
	 *
	 * @param inexactAllowed
	 *            the new inexact allowed
	 */
	public void setInexactAllowed(double inexactAllowed) {
		this.inexactAllowed = inexactAllowed;
	}

	/**
	 * Gets the augmentation factor error.
	 *
	 * @return the augmentation factor error
	 */
	public double getAugmentationFactorError() {
		return augmentationFactorError;
	}

	/**
	 * Sets the augmentation factor error.
	 *
	 * @param augmentationFactorError
	 *            the new augmentation factor error
	 */
	public void setAugmentationFactorError(double augmentationFactorError) {
		this.augmentationFactorError = augmentationFactorError;
	}

	/**
	 * Gets the diminution factor error.
	 *
	 * @return the diminution factor error
	 */
	public double getDiminutionFactorError() {
		return diminutionFactorError;
	}

	/**
	 * Sets the diminution factor error.
	 *
	 * @param diminutionFactorError
	 *            the new diminution factor error
	 */
	public void setDiminutionFactorError(double diminutionFactorError) {
		this.diminutionFactorError = diminutionFactorError;
	}

	/**
	 * Gets the min error allowed.
	 *
	 * @return the min error allowed
	 */
	public double getMinErrorAllowed() {
		return minErrorAllowed;
	}

	/**
	 * Sets the min error allowed.
	 *
	 * @param minErrorAllowed
	 *            the new min error allowed
	 */
	public void setMinErrorAllowed(double minErrorAllowed) {
		this.minErrorAllowed = minErrorAllowed;
	}

	/**
	 * Gets the augmentation inexact error.
	 *
	 * @return the augmentation inexact error
	 */
	public double getAugmentationInexactError() {
		return augmentationInexactError;
	}

	/**
	 * Sets the augmentation inexact error.
	 *
	 * @param augmentationInexactError
	 *            the new augmentation inexact error
	 */
	public void setAugmentationInexactError(double augmentationInexactError) {
		this.augmentationInexactError = augmentationInexactError;
	}

	/**
	 * Gets the diminution inexact error.
	 *
	 * @return the diminution inexact error
	 */
	public double getDiminutionInexactError() {
		return diminutionInexactError;
	}

	/**
	 * Sets the diminution inexact error.
	 *
	 * @param diminutionInexactError
	 *            the new diminution inexact error
	 */
	public void setDiminutionInexactError(double diminutionInexactError) {
		this.diminutionInexactError = diminutionInexactError;
	}

	/**
	 * Gets the min inexact allowed.
	 *
	 * @return the min inexact allowed
	 */
	public double getMinInexactAllowed() {
		return minInexactAllowed;
	}

	/**
	 * Sets the min inexact allowed.
	 *
	 * @param minInexactAllowed
	 *            the new min inexact allowed
	 */
	public void setMinInexactAllowed(double minInexactAllowed) {
		this.minInexactAllowed = minInexactAllowed;
	}

	/**
	 * Gets the n propositions received.
	 *
	 * @return the n propositions received
	 */
	public int getnPropositionsReceived() {
		return nPropositionsReceived;
	}

	/**
	 * Checks if is new context was created.
	 *
	 * @return true, if is new context was created
	 */
	public boolean isNewContextWasCreated() {
		return newContextWasCreated;
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

	/**
	 * Gets the prediction.
	 *
	 * @return the prediction
	 */
	public double getPrediction() {
		return prediction;
	}

	/**
	 * Sets the prediction.
	 *
	 * @param prediction
	 *            the new prediction
	 */
	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}

	// TODO check if usefull
	public Head clone() throws CloneNotSupportedException {
		return (Head) super.clone();
	}

	public void clearAllUseableContextLists() {
		activatedContexts.clear();
	}

}
