package mas.agents.head;

import java.util.ArrayList;

import mas.ncs.NCS;
import mas.kernel.Config;
import mas.kernel.Launcher;
import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;
import mas.blackbox.BlackBoxAgent;

// TODO: Auto-generated Javadoc
/**
 * The Class Head.
 */
public class Head extends AbstractHead {


	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;
	private String functionSelected;
	
	private BlackBoxAgent oracle;
	
	private ArrayList<Context> contexts = new ArrayList<Context>();
	private ArrayList<Double> xLastCriticityValues = new ArrayList<Double>();

	
	private int nPropositionsReceived;
	private int averagePredictionCriticityWeight = 0;
	private int numberOfCriticityValuesForAverage = 100;
	
	private int nConflictBeforeAugmentation = 1;
	private int nSuccessBeforeDiminution = 50;
	private int perfIndicator = 1;
	private int nConflictBeforeInexactAugmentation = 2;
	private int nSuccessBeforeInexactDiminution = 50;
	private int perfIndicatorInexact = 0;
	
	private double prediction;
	private double oracleValue;
	private double oldOracleValue;
	private double criticity;
	private double oldCriticity;
	private double averagePredictionCriticity;
	
	private double errorAllowed  = 1.0;
	private double augmentationFactorError = 1.05;
	private double diminutionFactorError = 0.9;
	private double minErrorAllowed = 1.00;
	private double inexactAllowed  = 0.4;
	private double augmentationInexactError = 1.8;
	private double diminutionInexactError = 0.6;
	private double minInexactAllowed = 0.5;
	
	
	private boolean noCreation = true;
	private boolean useOracle = true;
	private boolean firstContext = false;
	private boolean newContextWasCreated = false;
	private boolean contextFromPropositionWasSelected = false;
	
	//Endogenous feedback
	private boolean noBestContext;
	
	
	

	/**
	 * Sets the data for error margin.
	 *
	 * @param errorAllowed the error allowed
	 * @param augmentationFactorError the augmentation factor error
	 * @param diminutionFactorError the diminution factor error
	 * @param minErrorAllowed the min error allowed
	 * @param nConflictBeforeAugmentation the n conflict before augmentation
	 * @param nSuccessBeforeDiminution the n success before diminution
	 */
	public void setDataForErrorMargin(double errorAllowed, double augmentationFactorError, double diminutionFactorError, double minErrorAllowed, int nConflictBeforeAugmentation, int nSuccessBeforeDiminution) {
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
	 * @param inexactAllowed the inexact allowed
	 * @param augmentationInexactError the augmentation inexact error
	 * @param diminutionInexactError the diminution inexact error
	 * @param minInexactAllowed the min inexact allowed
	 * @param nConflictBeforeInexactAugmentation the n conflict before inexact augmentation
	 * @param nSuccessBeforeInexactDiminution the n success before inexact diminution
	 */
	public void setDataForInexactMargin(double inexactAllowed, double augmentationInexactError, double diminutionInexactError, double minInexactAllowed, int nConflictBeforeInexactAugmentation, int nSuccessBeforeInexactDiminution) {
		this.inexactAllowed = inexactAllowed;
		this.augmentationInexactError = augmentationInexactError;
		this.diminutionInexactError = diminutionInexactError;
		this.minInexactAllowed = minInexactAllowed;
		this.nConflictBeforeInexactAugmentation = nConflictBeforeInexactAugmentation;
		this.nSuccessBeforeInexactDiminution = nSuccessBeforeInexactDiminution;
	}

	/**
	 * Instantiates a new head.
	 *
	 * @param world the world
	 */
	public Head(World world) {
		super(world);
	}

	/* (non-Javadoc)
	 * @see agents.head.AbstractHead#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		// contexts.clear();

		if (m.getType() == MessageType.PROPOSAL) { // Value useless
			contexts.add((Context) m.getSender());
		}
	}

	/**
	 * The core method of the head agent.
	 * Manage the whole behavior, and call method from context agents when needed. 
	 */
	public void play() {

		nPropositionsReceived = contexts.size();
		newContextWasCreated = false;
		setContextFromPropositionWasSelected(false);		
		oldOracleValue = oracleValue;
		oracleValue = oracle.getValue();
		
		/*The head memorize last used context agent*/
		lastUsedContext = bestContext;
		bestContext = null;
		
		super.play();

		/* useOracle means that data are labeled*/
		if (useOracle) {	
			playWithOracle();
		}
		else {
			playWithoutOracle();
			updateStatisticalInformations(); ///regarder dans le détail, possible que ce pas trop utile
		}
		
		contexts.clear();
		newContext = null;
	}
	
	
	
	private void playWithOracle() {
			
		if (contexts.size() > 0) {
			selectBestContext(); //using highest confidence 
		}

		if (bestContext != null) {
			setContextFromPropositionWasSelected(true);
			prediction = bestContext.getActionProposal();

		} else if (!noCreation) { /*noCreation is only used to disable creation of contexts, for research purposes*/
			getNearestContextAsBestContext();
		}

		/*Compute the criticity. Will be used by context agents.*/
		criticity = Math.abs(oracleValue - prediction);

		/*If we have a bestcontext, send a selection message to it*/
		if (bestContext != null) {
			functionSelected = bestContext.getFunction().getFormula(bestContext);
			sendExpressMessage(this, MessageType.SELECTION, bestContext);
		}

		selfAnalysationOfContexts();

		NCSDetection_IncompetentHead();		/*If there isn't any proposition or only bad propositions, the head is incompetent. It needs help from a context.*/
		NCSDetection_Concurrence(); 		/*If result is good, shrink redundant context (concurrence NCS)*/
		NCSDetection_Create_New_Context();	/*Finally, head agent check the need for a new context agent*/
		
		
	}
	
	/**
	 * Play without oracle.
	 */
	private void playWithoutOracle() {
		
		Config.print("Nombre de contexte : " + contexts.size(), 1);
		
		selectBestContext();
		if (bestContext != this.lastUsedContext) {
			noBestContext = false;
			prediction = bestContext.getActionProposal();
		} else {
			System.out.println("NO BEST ...");
			noBestContext = true;
			ArrayList<Agent> allContexts = world.getScheduler().getContexts();
			Context nearestContext = this.getNearestContext(allContexts);
			prediction = nearestContext.getActionProposal();
			bestContext = nearestContext;
		}
		Config.print("Best context selected without oracle is : " + bestContext.getName(),0);
	//	Config.print("With function : " + bestContext.getFunction().getFormula(bestContext), 0);
		Config.print("BestContext : " + bestContext.toString() + " " + bestContext.getConfidence(), 2);
		functionSelected = bestContext.getFunction().getFormula(bestContext);
		criticity = Math.abs(oracleValue - prediction);
	}
	
	private void NCSDetection_Create_New_Context() {
		/*Finally, head agent check the need for a new context agent*/
		
		boolean newContextCreated = false;
		ArrayList<Agent> allContexts = world.getScheduler().getContexts();
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
		/*If result is good, shrink redundant context (concurrence NCS)*/
		if (bestContext != null && criticity <= this.errorAllowed) {
			for (int i = 0 ; i < contexts.size() ; i++) {
				if (contexts.get(i) != bestContext && !contexts.get(i).isDying() && this.getCriticity(contexts.get(i)) <= this.errorAllowed) {
			//		System.out.println("Shrink context " + contexts.get(i).getName());
					contexts.get(i).solveNCS_Concurrence(this);
				}
			}
		}
	}
	
	private void NCSDetection_IncompetentHead() {
		/*If there isn't any proposition or only bad propositions, the head is incompetent. It needs help from a context.*/
		if (contexts.isEmpty() || (criticity > this.errorAllowed && !oneOfProposedContextWasGood())){
			ArrayList<Agent> allContexts = world.getScheduler().getContexts();
			
			Context c = getNearestGoodContext(allContexts);
			if (c!=null) c.solveNCS_IncompetentHead(this);;
			bestContext = c;
			
		/* This allow to test for all contexts rather than the nearest*/
		/*	for (Agent a : allContexts) {
				Context c = (Context) a;
				if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying() && c != bestContext && !contexts.contains(c)) {
					c.growRanges(this);
					
				}
			} */
			
		}
	}
	
	private void selfAnalysationOfContexts() {
		/*All context which proposed itself must analyze its proposition*/
		for (int i = 0 ; i < contexts.size() ; i++) {
				contexts.get(i).analyzeResults(this);
		}
	}
	
	private void getNearestContextAsBestContext() {
		ArrayList<Agent> allContexts = world.getScheduler().getContexts();
		Context nearestContext = this.getNearestContext(allContexts);

		if (nearestContext != null) {
			prediction = nearestContext.getActionProposal();
		} else {
			prediction = 0;
		}

		bestContext = nearestContext;
	}
	
	
	/**
	 * Endogenous feedback.
	 */
	private void endogenousFeedback(){
		//bestContext.growRanges(this);
	}
	
	
	/**
	 * Gets the nearest good context.
	 *
	 * @param allContext the all context
	 * @return the nearest good context
	 */
	private Context getNearestGoodContext(ArrayList<Agent> allContext) {
		Context nearest = null;
		for (Agent a : allContext) {
			Context c = (Context) a;
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < getExternalDistanceToContext(nearest) ) {
					nearest = c;
				}
			}
		}
		
		
		return nearest;
		
	}
	
	/**
	 * Gets the distance to nearest good context.
	 *
	 * @param allContext the all context
	 * @return the distance to nearest good context
	 */
	private double getDistanceToNearestGoodContext(ArrayList<Agent> allContext) {
		double d = Double.MAX_VALUE;
		for (Agent a : allContext) {
			Context c = (Context) a;
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (getExternalDistanceToContext(c) < d ) {
					d = getExternalDistanceToContext(c);
				}
			}
		}
		return d;
		
	}
	
	
	/**
	 * Gets the nearest context.
	 *
	 * @param allContext the all context
	 * @return the nearest context
	 */
	private Context getNearestContext(ArrayList<Agent> allContext) {
		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Agent a : allContext) {
			Context c = (Context) a;
			if (c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < distanceToNearest ) {
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
	 * @param context the context
	 * @return the external distance to context
	 */
	private double getExternalDistanceToContext (Context context) {
		double d = 0.0;
		ArrayList<Percept> percepts = world.getAllPercept();
		for (Percept p : percepts) {
			if (p.isEnum()) {
				if (!(context.getRanges().get(p).getStart() == p.getValue())) {
					d += Double.MAX_VALUE;
				}
			} else {
				double min = context.getRanges().get(p).getStart();
				double max = context.getRanges().get(p).getEnd();
				
				if (min > p.getValue() || max < p.getValue()) {
					d += Math.min(Math.abs(p.getValue() - min),Math.abs(p.getValue() - max));
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
		for (Context c : contexts) {
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
	//	System.out.println("Creation d'un nouveau contexte : " + contexts.size());
		newContextWasCreated = true;
//		if (contexts.size() != 0) {
//			System.exit(0);
//		}
		world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
		Context context;
		if (firstContext) {
			context = new Context(world, this);
			Config.print("new context agent", 3);
		}
		else {
			context = new Context(world, this);
			firstContext = true;
		}

		return context;
	}

	/**
	 * Update statistical informations.
	 */
	private void updateStatisticalInformations() {
		xLastCriticityValues.add(criticity);
	//	averagePredictionCriticity = ((averagePredictionCriticity * averagePredictionCriticityWeight) + criticity) / (averagePredictionCriticityWeight + 1);
	//	averagePredictionCriticityWeight++;
		
		averagePredictionCriticity = 0;
		for (Double d : xLastCriticityValues) {
			averagePredictionCriticity += d;
		}
		averagePredictionCriticity /= xLastCriticityValues.size();
		
		if (xLastCriticityValues.size() >= numberOfCriticityValuesForAverage) {
			xLastCriticityValues.remove(0);
		}
		
		if (criticity > errorAllowed) {
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
		
		if (criticity > inexactAllowed) {
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
		//numberOfCriticityValuesForAverage
	}



	/**
	 * Gets the contexts.
	 *
	 * @return the contexts
	 */
	public ArrayList<Context> getContexts() {
		return contexts;
	}

	/**
	 * Sets the contexts.
	 *
	 * @param contexts the new contexts
	 */
	public void setContexts(ArrayList<Context> contexts) {
		this.contexts = contexts;
	}


	/**
	 * Select best context.
	 */
	private void selectBestContext() {
		
		Context bc;
		if (contexts.isEmpty()) {
			bc = lastUsedContext;
		} else {
			bc = contexts.get(0);
		}
		double currentConfidence = Double.NEGATIVE_INFINITY;

		for (Context context : contexts) {
			if (context.getConfidence() > currentConfidence) {
				bc  = context;
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
	 * @param bestContext the new best context
	 */
	public void setBestContext(Context bestContext) {
		this.bestContext = bestContext;
	}

	/* (non-Javadoc)
	 * @see agents.head.AbstractHead#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		return contexts;
	}

	/**
	 * Gets the active contexts.
	 *
	 * @return the active contexts
	 */
	public ArrayList<Context> getActiveContexts() {
		return contexts;
	}
	
	/**
	 * Gets the criticity.
	 *
	 * @return the criticity
	 */
	public double getCriticity() {
		return criticity;
	}
	
	/**
	 * Gets the no best context.
	 *
	 * @return the no best context
	 */
	public boolean getNoBestContext(){
		return noBestContext;
	}

	/**
	 * Gets the criticity.
	 *
	 * @param context the context
	 * @return the criticity
	 */
	public double getCriticity(Context context) {
		return Math.abs(oracleValue - context.getActionProposal());
	}
	
	/**
	 * Sets the criticity.
	 *
	 * @param criticity the new criticity
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
	 * @param action the new action
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
		return noCreation;
	}

	/**
	 * Sets the no creation.
	 *
	 * @param noCreation the new no creation
	 */
	public void setNoCreation(boolean noCreation) {
		this.noCreation = noCreation;
	}

	/**
	 * Gets the oracle.
	 *
	 * @return the oracle
	 */
	public BlackBoxAgent getOracle() {
		return oracle;
	}

	/**
	 * Sets the oracle.
	 *
	 * @param oracle the new oracle
	 */
	public void setOracle(BlackBoxAgent oracle) {
		this.oracle = oracle;
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
	 * @param oracleValue the new oracle value
	 */
	public void setOracleValue(double oracleValue) {
		this.oracleValue = oracleValue;
	}

	/**
	 * Gets the old oracle value.
	 *
	 * @return the old oracle value
	 */
	public double getOldOracleValue() {
		return oldOracleValue;
	}

	/**
	 * Sets the old oracle value.
	 *
	 * @param oldOracleValue the new old oracle value
	 */
	public void setOldOracleValue(double oldOracleValue) {
		this.oldOracleValue = oldOracleValue;
	}

	/**
	 * Gets the old criticity.
	 *
	 * @return the old criticity
	 */
	public double getOldCriticity() {
		return oldCriticity;
	}

	/**
	 * Sets the old criticity.
	 *
	 * @param oldCriticity the new old criticity
	 */
	public void setOldCriticity(double oldCriticity) {
		this.oldCriticity = oldCriticity;
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
	 * @param errorAllowed the new error allowed
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
	 * @param averagePredictionCriticity the new average prediction criticity
	 */
	public void setAveragePredictionCriticity(double averagePredictionCriticity) {
		this.averagePredictionCriticity = averagePredictionCriticity;
	}

	/**
	 * Gets the average prediction criticity weight.
	 *
	 * @return the average prediction criticity weight
	 */
	public int getAveragePredictionCriticityWeight() {
		return averagePredictionCriticityWeight;
	}

	/**
	 * Sets the average prediction criticity weight.
	 *
	 * @param averagePredictionCriticityWeight the new average prediction criticity weight
	 */
	public void setAveragePredictionCriticityWeight(
			int averagePredictionCriticityWeight) {
		this.averagePredictionCriticityWeight = averagePredictionCriticityWeight;
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
	 * @param functionSelected the new function selected
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
	 * @param inexactAllowed the new inexact allowed
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
	 * @param augmentationFactorError the new augmentation factor error
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
	 * @param diminutionFactorError the new diminution factor error
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
	 * @param minErrorAllowed the new min error allowed
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
	 * @param augmentationInexactError the new augmentation inexact error
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
	 * @param diminutionInexactError the new diminution inexact error
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
	 * @param minInexactAllowed the new min inexact allowed
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
	 * @param contextFromPropositionWasSelected the new context from proposition was selected
	 */
	public void setContextFromPropositionWasSelected(
			boolean contextFromPropositionWasSelected) {
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
	 * @param prediction the new prediction
	 */
	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}

	
	
	
	
}
