package mas.agents.head;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mas.ncs.NCS;
import mas.kernel.Config;
import mas.kernel.Launcher;
import mas.kernel.NCSMemory;
import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.CustomComparator;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;
import mas.agents.context.CustomComparator;

//import mas.blackbox.BlackBoxAgent;

// TODO: Auto-generated Javadoc
/**
 * The Class Head.
 */
public class Head extends AbstractHead implements Cloneable{


	private Context bestContext;
	private Context lastUsedContext;
	private Context newContext;
	private String functionSelected;
	
	//private BlackBoxAgent oracle;
	
	private ArrayList<Context> activatedContexts = new ArrayList<Context>();
	private HashMap<Percept,ArrayList<Context>> partialyActivatedContexts = new HashMap<Percept,ArrayList<Context>>();
	private HashMap<Percept,Pair<Context,Context>> requestSurroundings = new HashMap<Percept,Pair<Context,Context>>();
	private HashMap<Percept,Pair<Context,Context>> sharedIncompetenceContextPairs = new HashMap<Percept,Pair<Context,Context>>();
	
	private ArrayList<Context> contextsInCompetition = new ArrayList<Context>();
	
	
	private ArrayList<Double> xLastCriticityValues = new ArrayList<Double>();
	
	public ArrayList<NCSMemory> NCSMemories = new ArrayList<NCSMemory>();

	
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
	private Double endogenousPrediction;
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
		
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			partialyActivatedContexts.put(pct, new ArrayList<Context>());
			requestSurroundings.put(pct, new Pair<Context,Context>(null,null));
			sharedIncompetenceContextPairs.put(pct, new Pair<Context,Context>(null,null));
			}
	}

	/* (non-Javadoc)
	 * @see agents.head.AbstractHead#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
		// contexts.clear();

		if (m.getType() == MessageType.PROPOSAL) { // Value useless
			activatedContexts.add((Context) m.getSender());
		}
	}
	
	public void addPartiallyActivatedContext(Percept pct,Context partialyactivatedContext) {
		partialyActivatedContexts.get(pct).add(partialyactivatedContext);
		//System.out.println(pct.getName() + " " + partialyActivatedContexts.get(pct).size());
	} 

	/**
	 * The core method of the head agent.
	 * Manage the whole behavior, and call method from context agents when needed. 
	 */
	public void play() {

		nPropositionsReceived = activatedContexts.size();
		newContextWasCreated = false;
		setContextFromPropositionWasSelected(false);		
		oldOracleValue = oracleValue;
		//oracleValue = oracle.getValue();
		oracleValue = this.getWorld().getScheduler().getPerceptionsOrAction("oracle");
		
		/*The head memorize last used context agent*/
		lastUsedContext = bestContext;
		bestContext = null;
		
		super.play();

		endogenousPlay();
		
		/* useOracle means that data are labeled*/
		if (useOracle) {	
			playWithOracle();
		}
		else {
			playWithoutOracle();
			//updateStatisticalInformations(); ///regarder dans le d彋ail, possible que ce pas trop utile
		}
		//中
		
		if(endogenousPrediction == null) {
			endogenousPrediction =  prediction;
		}
		
		updateStatisticalInformations(); ///regarder dans le d彋ail, possible que ce pas trop utile
		
		activatedContexts.clear();
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			partialyActivatedContexts.get(pct).clear();
			
		}
		//displayPartiallyActivatedContexts();
		
		newContext = null;
		
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Error allowded :" + errorAllowed);
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Inexact allowded :" + inexactAllowed);
	}
	
	
	
	private void playWithOracle() {
			
		if (activatedContexts.size() > 0) {
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
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Error :" + criticity);

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
		
		Config.print("Nombre de contextes activ廥: " + activatedContexts.size(), 1);
		
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
	
	private void endogenousPlay() {
		
		endogenousPrediction = null;
		
		for(Percept pcpt : this.world.getScheduler().getPercepts()) {
			requestSurroundings.get(pcpt).clear();
			sharedIncompetenceContextPairs.get(pcpt).clear();
		}
		contextsInCompetition.clear();
		
		
		if(uniqueActivatedContext()) {
			endogenousPrediction = activatedContexts.get(0).getActionProposal();
		}
		else if(severalActivatedContexts()){
			NCS_EndogenousCompetition();
		}
		else {
			if(surroundingContexts()) {
				NCS_EndogenousSharedIncompetence();
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
	}
	
	
	private boolean noActivatedContext() {
		//Test if only one context is activated
		return activatedContexts.size() == 0;
	}
	
	private boolean uniqueActivatedContext() {
		//Test if only one context is activated
		return activatedContexts.size() == 1;
	}
	
	private boolean severalActivatedContexts() {
		//Test if several context are activated
		return activatedContexts.size() > 1;
	}
	
	private boolean surroundingContexts() {
		//Test if there are surrounding contexts
		boolean testSurroudingContext = false;
		
		//System.out.println("SURROUNDING CONTEXTS ...");
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			
			computeNearestContextsByPercept(pct);			
				 
			}
		
		//displayPartiallyActivatedContexts();
		//displayContexts();
		
		for(Percept pcpt : this.world.getScheduler().getPercepts()) {
			//System.out.print("SURROUNDING CONTEXTS ... " + pcpt.getName() + " ");
			requestSurroundings.get(pcpt).print(pcpt);
			if(sharedIncompetenceContextPairs.get(pcpt) != null) {
				if(sharedIncompetenceContextPairs.get(pcpt).containTwoContexts()) {
					testSurroudingContext = true;
				}
			}
			
			
			
		}
		
		//System.out.println("TEST SURROUNDING CONTEXTS ..." + testSurroudingContext);
		return testSurroudingContext;
	}
	
	
	private void diplayListRanges(ArrayList<Context> list,Percept prct, String range) {
		
		
		
		System.out.print(range + " ranges list" + "  ");
		for(Context ctxt : list) {
			System.out.print(ctxt.getRanges().get(prct).getRange(range) + "  ");
		}
		System.out.println(" ");
		
	}
	
	private void computeNearestContextsByPercept(Percept pct) {
		Pair<Context,Context> nearestContexts = new Pair(null, null);
		boolean startNeighbor = false;
		boolean endNeighbor = false;
		
		
		
		ArrayList<Context> activatedContextInOtherPercepts = getAllActivatedContextsExeptForOnePercept(pct);
		
		
		
		if(activatedContextInOtherPercepts.size()>0) {
			
				
			//System.out.println("Partially activated on other percepts than " + pct.getName() + " : " + activatedContextInOtherPercepts.size());			
			//System.out.println("Value " + pct.getValue());		 
			
			CustomComparator rangeStartComparator =  new CustomComparator(pct, "start");
			Collections.sort(activatedContextInOtherPercepts, rangeStartComparator);
			//diplayListRanges(activatedContextInOtherPercepts, pct, "start");
			
			
			for(Context ctxt : activatedContextInOtherPercepts) {
				if(ctxt.getRanges().get(pct).getRange("start")>pct.getValue() && !startNeighbor) {
					nearestContexts.setR(ctxt);
					startNeighbor = true;
				}

			}
			
			
			CustomComparator rangeEndComparator =  new CustomComparator(pct, "end");
			Collections.sort(activatedContextInOtherPercepts, rangeEndComparator);
			Collections.reverse(activatedContextInOtherPercepts);
			//diplayListRanges(activatedContextInOtherPercepts, pct, "end");
			
			

			for(Context ctxt : activatedContextInOtherPercepts) {
				if(ctxt.getRanges().get(pct).getRange("end")<pct.getValue() && !endNeighbor) {
					nearestContexts.setL(ctxt);
					endNeighbor = true;
				}
			}
			
			//nearestContexts.print(pct);
			requestSurroundings.put(pct, nearestContexts);
			
			if(nearestContexts.getL() != null && nearestContexts.getR() != null) {
				sharedIncompetenceContextPairs.put(pct, nearestContexts);
			}
		}
		else {
			//System.out.println("=====================================================");
		}
		
			
		
		
	}
				
			
		
	
	private ArrayList<Context> getAllActivatedContextsExeptForOnePercept(Percept onePercept){
		ArrayList<Context> activatedContexts = new ArrayList<Context>();
		
		Percept otherPercept = getDifferentPercept(onePercept);
		
		if(partialyActivatedContexts.get(otherPercept)!=null) {
			for(Context ctxt : partialyActivatedContexts.get(otherPercept)) {
				if(this.world.getScheduler().getPercepts().size()>2) {
					if(contextActivateInOtherPerceptsThan(ctxt, onePercept, otherPercept)) {
						activatedContexts.add(ctxt);
					}
				}
				else {
					activatedContexts.add(ctxt);
				}
//				if(!world.getScheduler().getContexts().contains(ctxt)) {
//					System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ " + ctxt.getName());
//				}
			}
			
		}
		
		return activatedContexts;
	}
	
	public void displayPartiallyActivatedContexts() {
		System.out.println("PARTIALLY ACTIVATED CONTEXTS");
		for(Percept pct : partialyActivatedContexts.keySet()) {
			System.out.print(pct.getName() + " : ");
			if(partialyActivatedContexts.get(pct).size()>0)
			for(Context ctxt : partialyActivatedContexts.get(pct)) {
				System.out.print(ctxt.getName() + " ; ");
			}
			System.out.println(" ");
		}
	}
	
	public void displayContexts() {
		System.out.println("CONTEXTS");
		for(Context ctxt : this.world.getScheduler().getContextsAsContext()) {
			System.out.print(ctxt.getName() + " ; ");
		}
		System.out.println(" ");
	}
	
	private Percept getDifferentPercept(Percept p) {
		for(Percept pct : partialyActivatedContexts.keySet()) {
			if(p != pct) {
				return pct;
			}
		}
		return null;
	}
	
	private boolean contextActivateInOtherPerceptsThan(Context ctxt, Percept p1, Percept p2) {
		boolean test = true;
		for(Percept prct : partialyActivatedContexts.keySet()) {
			if(prct != p1 && prct != p2) {
				test = test & partialyActivatedContexts.get(prct).contains(ctxt);
			}
		}
		return test;
	}
	
	private void NCS_EndogenousCompetition() {
		System.out.println("NCS Comptetition " + world.getScheduler().getTick());
		// Creation of twin contexts to give the endogenous prediction
		Context highestConfidenceContext = null;
		Context secondHighestConfidenceContext = null;
		HashMap<Percept,Double> currentSituation = new HashMap<Percept,Double>();
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}
		
		if(activatedContexts.get(0).getInfluence(currentSituation)>activatedContexts.get(1).getInfluence(currentSituation)) {
			highestConfidenceContext = activatedContexts.get(0);
			secondHighestConfidenceContext = activatedContexts.get(1);
		}
		else {
			highestConfidenceContext = activatedContexts.get(1);
			secondHighestConfidenceContext = activatedContexts.get(0);
		}
		
		
		for(int i=2; i<activatedContexts.size();i++) {
			if(activatedContexts.get(i).getInfluence(currentSituation)>highestConfidenceContext.getInfluence(currentSituation)) {		
				secondHighestConfidenceContext = highestConfidenceContext;
				highestConfidenceContext = activatedContexts.get(i);
			}
			else if(activatedContexts.get(i).getInfluence(currentSituation)>secondHighestConfidenceContext.getInfluence(currentSituation)) {
				secondHighestConfidenceContext = activatedContexts.get(i);
			}
		}
		
		contextsInCompetition.add(highestConfidenceContext);
		contextsInCompetition.add(secondHighestConfidenceContext);
		
		//endogenousPrediction = (highestConfidenceContext.getActionProposal() + secondHighestConfidenceContext.getActionProposal()) / 2;
		//endogenousPrediction = (highestConfidenceContext.getNormalizedConfidence()*highestConfidenceContext.getActionProposal() + secondHighestConfidenceContext.getNormalizedConfidence()*secondHighestConfidenceContext.getActionProposal()) / (highestConfidenceContext.getNormalizedConfidence() + secondHighestConfidenceContext.getNormalizedConfidence());
		
		
		
		
		
		double highestConfidenceContextInfluence = highestConfidenceContext.getInfluence(currentSituation);
		double secondHighestConfidenceContextInfluence = secondHighestConfidenceContext.getInfluence(currentSituation);
		
		
		endogenousPrediction = (highestConfidenceContextInfluence*highestConfidenceContext.getActionProposal() + secondHighestConfidenceContextInfluence*secondHighestConfidenceContext.getActionProposal()) / (highestConfidenceContextInfluence + secondHighestConfidenceContextInfluence);
		
		//System.out.println("EP " + endogenousPrediction + " I1 " + highestConfidenceContextInfluence + " AP1 " + highestConfidenceContext.getActionProposal() + " I2 " + secondHighestConfidenceContextInfluence + " AP2 " + secondHighestConfidenceContext.getActionProposal());
		Double test = endogenousPrediction;
//		if(test.isNaN() || test.isInfinite()) {
//			System.out.println(highestConfidenceContext.getNormalizedConfidence() + " " + highestConfidenceContext.getActionProposal() + " " + secondHighestConfidenceContext.getNormalizedConfidence()+ " " +secondHighestConfidenceContext.getActionProposal());
//			endogenousPrediction = prediction;
//		}
		
		ArrayList<Context> concernContexts = new ArrayList<Context>();
		concernContexts.add(highestConfidenceContext);
		concernContexts.add(secondHighestConfidenceContext);
		NCSMemories.add(new NCSMemory(world, concernContexts,"Competition"));
	}
	
	private void NCS_EndogenousSharedIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the prediction
		
		;
		
		Pair<Context, Context> closestContexts = new Pair<Context,Context>(null, null);
		double smallestDistanceBetweenContexts = Double.POSITIVE_INFINITY;
		double currentDistance;
		
		for(Percept pct : sharedIncompetenceContextPairs.keySet()) {
			if(sharedIncompetenceContextPairs.get(pct) != null) {
				if(sharedIncompetenceContextPairs.get(pct).containTwoContexts()) {
					currentDistance = sharedIncompetenceContextPairs.get(pct).rangeToRangeDistance(pct);
					if(currentDistance < smallestDistanceBetweenContexts) {
						closestContexts = sharedIncompetenceContextPairs.get(pct);
						smallestDistanceBetweenContexts = currentDistance;
					}
				}
			}
		}
		
		HashMap<Percept,Double> currentSituation = new HashMap<Percept,Double>();
		
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}
		
		double contextInfluenceL = closestContexts.getL().getInfluence(currentSituation);
		double contextInfluenceR = closestContexts.getR().getInfluence(currentSituation);
		
		
		endogenousPrediction = (contextInfluenceL*closestContexts.getL().getActionProposal() + contextInfluenceR*closestContexts.getR().getActionProposal()) / (contextInfluenceL + contextInfluenceR);
		
		
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
		NCSMemories.add(new NCSMemory(world, concernContexts,"SharedIncompetence"));
		
	}
	
	private void NCS_EndogenousIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the prediction
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
			for (int i = 0 ; i < activatedContexts.size() ; i++) {
				if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && this.getCriticity(activatedContexts.get(i)) <= this.errorAllowed) {
			//		System.out.println("Shrink context " + contexts.get(i).getName());
					activatedContexts.get(i).solveNCS_Concurrence(this);
				}
			}
		}
	}
	
	private void NCSDetection_IncompetentHead() {
		/*If there isn't any proposition or only bad propositions, the head is incompetent. It needs help from a context.*/
		if (activatedContexts.isEmpty() || (criticity > this.errorAllowed && !oneOfProposedContextWasGood())){
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
		for (int i = 0 ; i < activatedContexts.size() ; i++) {
			activatedContexts.get(i).analyzeResults(this);
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
		
		//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Average Prediction Criticity :" + averagePredictionCriticity);
		
		if (averagePredictionCriticity > errorAllowed) {
			perfIndicator--;
		} else {
			perfIndicator++;
		}
		/*if (criticity > errorAllowed) {
			perfIndicator--;
		} else {
			perfIndicator++;
		}*/
		//System.out.println("中中中中中中中中中中中  PERF INDICATOR :" + perfIndicator);
		
		if (perfIndicator <= nConflictBeforeAugmentation * (-1)) {
			perfIndicator = 0;
			errorAllowed *= augmentationFactorError;
			//System.out.println("中中中中中中中中中中中  augmentationFactorError :" + augmentationFactorError);
		}
		
		if (perfIndicator >= nSuccessBeforeDiminution) {
			perfIndicator = 0;
			errorAllowed *= diminutionFactorError;
			//System.out.println("中中中中中中中中中中中  diminutionFactorError :" + diminutionFactorError);
			errorAllowed = Math.max(minErrorAllowed, errorAllowed);
		}
		
		
		if (averagePredictionCriticity > inexactAllowed) {
			perfIndicatorInexact--;
		} else {
			perfIndicatorInexact++;
		}
		
		/*if (criticity > inexactAllowed) {
			perfIndicatorInexact--;
		} else {
			perfIndicatorInexact++;
		}*/
		//System.out.println("中中中中中中中中中中中  PERF INDICATOR INEXACT :" + perfIndicator);
		
		if (perfIndicatorInexact <= nConflictBeforeInexactAugmentation * (-1)) {
			perfIndicatorInexact = 0;
			inexactAllowed *= augmentationInexactError;
			//System.out.println("中中中中中中中中中中中  augmentationInexactError :" + augmentationInexactError);
		}
		
		if (perfIndicatorInexact >= nSuccessBeforeInexactDiminution) {
			perfIndicatorInexact = 0;
			inexactAllowed *= diminutionInexactError;
			inexactAllowed = Math.max(minInexactAllowed, inexactAllowed);
			//System.out.println("中中中中中中中中中中中  diminutionInexactError :" + diminutionInexactError);

		}
		
		//numberOfCriticityValuesForAverage
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
		
		Context bc;
		if (activatedContexts.isEmpty()) {
			bc = lastUsedContext;
		} else {
			bc = activatedContexts.get(0);
		}
		double currentConfidence = Double.NEGATIVE_INFINITY;

		for (Context context : activatedContexts) {
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
	
	public double getEndogenousPrediction() {
		return endogenousPrediction;
	}

	/**
	 * Sets the prediction.
	 *
	 * @param prediction the new prediction
	 */
	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}

	
	public ArrayList<Context> getPartiallyActivatedContexts(Percept pct) {
		return partialyActivatedContexts.get(pct);
	}
	
	
	public HashMap<Percept, Pair<Context, Context>> getRequestSurroundings() {
		return requestSurroundings;
	}
	
	
	public boolean requestSurroundingContains(Context ctxt) {
		

		for(Percept pct : requestSurroundings.keySet()) {
			//System.out.println("REQUEST SURROUNDINGS " +  requestSurroundings.get(pct).getL().getName() +  " ; " + requestSurroundings.get(pct).getR().getName());
			if(requestSurroundings.get(pct).contains(ctxt)) {
				return true;
			}
		}
		return false;
		
	}
	
	public ArrayList<Context> getContextsInCompetition(){
		return contextsInCompetition;
	}
	
	public Head clone() throws CloneNotSupportedException{
		return (Head)super.clone();
	}
	
	public ArrayList<NCSMemory> getNCSMemories() {
		return NCSMemories;
	}
	
	public NCSMemory getMemoryByTick(int tick) {
		for(NCSMemory ncsMemory : NCSMemories) {
			if(ncsMemory.getTick() == tick) {
				return ncsMemory;
			}
		}
		return null;
	}
}











