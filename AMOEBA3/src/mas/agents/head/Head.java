package mas.agents.head;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import mas.ncs.NCS;
import mas.kernel.Config;
import mas.kernel.Launcher;
import mas.kernel.NCSMemory;
import mas.kernel.World;
import mas.agents.AbstractPair;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.CustomComparator;
import mas.agents.localModel.LocalModelAgent;
import mas.agents.localModel.LocalModelMillerRegression;
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
	
	HashMap<Percept,Double> currentSituation = new HashMap<Percept,Double>();
	
	public Criticalities criticalities;
	public Criticalities endogenousCriticalities;
	
	private ArrayList<Context> activatedContexts = new ArrayList<Context>();
	private ArrayList<Context> activatedNeighborsContexts = new ArrayList<Context>();
	
	private HashMap<Percept,ArrayList<Context>> partiallyActivatedContextInNeighbors = new HashMap<Percept,ArrayList<Context>>();
	private HashMap<Percept,ArrayList<Context>> partiallyActivatedContexts = new HashMap<Percept,ArrayList<Context>>();
	private HashMap<Percept,ArrayList<Context>> partialNeighborContexts = new HashMap<Percept,ArrayList<Context>>();
	
	
	private ArrayList<Context> contextsNeighborsByInfluence = new ArrayList<Context>();
	
	private HashMap<Percept,Pair<Context,Context>> requestSurroundings = new HashMap<Percept,Pair<Context,Context>>();
	private HashMap<Percept,Pair<Context,Context>> sharedIncompetenceContextPairs = new HashMap<Percept,Pair<Context,Context>>();
	
	private ArrayList<Context> contextsInCompetition = new ArrayList<Context>();
	
	
	public ArrayList<NCSMemory> NCSMemories = new ArrayList<NCSMemory>();

	
	private int nPropositionsReceived;
	private int averagePredictionCriticityWeight = 0;
	private int numberOfCriticityValuesForAverage = 100;
	private int numberOfCriticityValuesForAverageforVizualisation = 300;
	
	private int nConflictBeforeAugmentation = 1;
	private int nSuccessBeforeDiminution = 50;
	private int perfIndicator = 1;
	private int perfMappingIndicator = 1;
	private int nConflictBeforeInexactAugmentation = 2;
	private int nSuccessBeforeInexactDiminution = 50;
	private int perfIndicatorInexact = 0;
	
	private double prediction;
	private Double endogenousPredictionActivatedContextsOverlaps =0.0;
	private Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluence=0.0;
	
	private Double endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence=0.0;
	private Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence=0.0;
	
	private Double endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume=0.0;
	
	private Double endogenousPredictionActivatedContextsSharedIncompetence=0.0;
	private Double endogenousPredictionNContexts=0.0;
	private Double endogenousPredictionNContextsByInfluence=0.0;
	
	private double oracleValue;
	private double oldOracleValue;
	private double criticity;
	private double oldCriticity;

	
	private double spatialGeneralizationScore = 0;
	
	private double errorAllowed  = 1.0;
	private double augmentationFactorError = 1.05;
	private double diminutionFactorError = 0.9;
	private double minErrorAllowed = 1.00;
	private double inexactAllowed  = 0.4;
	private double augmentationInexactError = 1.8;
	private double diminutionInexactError = 0.6;
	private double minInexactAllowed = 0.5;
	private double mappingErrorAllowed;
	
	
	private boolean noCreation = true;
	private boolean useOracle = true;
	private boolean firstContext = false;
	private boolean newContextWasCreated = false;
	private boolean contextFromPropositionWasSelected = false;
	
	Double maxConfidence;
	Double minConfidence;
	
	//Endogenous feedback
	private boolean noBestContext;
	
	static double lembda = 0.99;
	
	public double evolutionCriticalityPrediction = 0.5;
	public double evolutionCriticalityMapping = 0.5;
	public double evolutionCriticalityConfidence = 0.5;
	
	private int currentCriticalityPrediction = 0;
	private int currentCriticalityMapping = 0;
	private int currentCriticalityConfidence = 0;
	
	
	public long playExecutionTime;
	public long endogenousExecutionTime;
	public long contextSelfAnalisisExecutionTime;
	
	public long incompetentHeadNCSExecutionTime;
	public long concurrenceNCSExecutionTime;
	public long create_New_ContextNCSExecutionTime;
	public long overmappingNCSExecutionTime;
	public long memoryCreationExecutionTime;
	
	public long otherExecutionTime;
	
	public long playExecutionTimeSum = 0;
	public long endogenousExecutionTimeSum= 0;
	public long contextSelfAnalisisExecutionTimeSum= 0;
	
	public long incompetentHeadNCSExecutionTimeSum= 0;
	public long concurrenceNCSExecutionTimeSum= 0;
	public long create_New_ContextNCSExecutionTimeSum= 0;
	public long overmappingNCSExecutionTimeSum= 0;
	public long memoryCreationExecutionTimeSum= 0;
	
	public long otherExecutionTimeSum= 0;
	
	

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
		
		maxConfidence = Double.NEGATIVE_INFINITY;
		minConfidence = Double.POSITIVE_INFINITY;
		
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			partiallyActivatedContextInNeighbors.put(pct, new ArrayList<Context>());
			partiallyActivatedContexts.put(pct, new ArrayList<Context>());
			partialNeighborContexts.put(pct, new ArrayList<Context>());
			requestSurroundings.put(pct, new Pair<Context,Context>(null,null));
			sharedIncompetenceContextPairs.put(pct, new Pair<Context,Context>(null,null));
			}
		
		mappingErrorAllowed =world.getMappingErrorAllowed();// Math.pow(world.getMappingErrorAllowed(), world.getScheduler().getPercepts().size());
		
		criticalities = new Criticalities(numberOfCriticityValuesForAverage);
		
		endogenousCriticalities = new Criticalities(numberOfCriticityValuesForAverageforVizualisation);
		
		
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
	
	public void addPartiallyActivatedContext(Percept nonValidPercept,Context validContextExecptOnTheNonValidPercept) {
		partiallyActivatedContexts.get(nonValidPercept).add(validContextExecptOnTheNonValidPercept);
		//////////System.out.println(pct.getName() + " " + partialyActivatedContexts.get(pct).size());
	} 
	
	public void addPartiallyActivatedContextInNeighbors(Percept nonValidPercept,Context validContextExecptOnTheNonValidPercept) {
		partiallyActivatedContextInNeighbors.get(nonValidPercept).add(validContextExecptOnTheNonValidPercept);
		//////////System.out.println(pct.getName() + " " + partialyActivatedContexts.get(pct).size());
	} 
	
	public void addPartialRequestNeighborContext(Percept nonValidPercept,Context validContextNeighborExecptOnTheNonValidPercept) {
		partialNeighborContexts.get(nonValidPercept).add(validContextNeighborExecptOnTheNonValidPercept);
		//////////System.out.println(pct.getName() + " " + partialyActivatedContexts.get(pct).size());
	} 

	/**
	 * The core method of the head agent.
	 * Manage the whole behavior, and call method from context agents when needed. 
	 */
	public void play() {

		
		currentCriticalityPrediction = 0;
		currentCriticalityMapping = 0;
		currentCriticalityConfidence = 0;
		
		
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			currentSituation.put(pct, pct.getValue());
		}
		
		////////System.out.println("HEAD ACTIVATED CONTEXT :" + activatedContexts.size());
		nPropositionsReceived = activatedContexts.size();
		newContextWasCreated = false;
		setContextFromPropositionWasSelected(false);		
		oldOracleValue = oracleValue;
		oracleValue = this.getWorld().getScheduler().getPerceptionsOrAction("oracle");
		
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
			//updateStatisticalInformations(); ///regarder dans le d彋ail, possible que ce pas trop utile
		}
		//中
		
		
		
		updateStatisticalInformations(); ///regarder dans le d彋ail, possible que ce pas trop utile
		
		

		
		newContext = null;
		
		//////////System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Error allowded :" + errorAllowed);
		//////////System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Inexact allowded :" + inexactAllowed);
	}
	
	
	
	private void playWithOracle() {
		
		playExecutionTime = System.currentTimeMillis();	
		if (activatedContexts.size() > 0) {
			selectBestContext(); //using highest confidence 
		}
		else {
			bestContext = lastUsedContext;
		}

		if (bestContext != null) {
			setContextFromPropositionWasSelected(true);
			prediction = bestContext.getActionProposal();

		} else { // happens only at the beginning
			setNearestContextAsBestContext();
		}
		
		

		/*Compute the criticity. Will be used by context agents.*/
		criticity = Math.abs(oracleValue - prediction);
		criticalities.addCriticality("predictionCriticality", criticity);

		/*If we have a bestcontext, send a selection message to it*/
		if (bestContext != null) {
			//functionSelected = bestContext.getFunction().getFormula(bestContext);
			sendExpressMessage(this, MessageType.SELECTION, bestContext);
			world.trace(new ArrayList<String>(Arrays.asList(bestContext.getName(), "*********************************************************************************************************** BEST CONTEXT")));
		}
		playExecutionTime = System.currentTimeMillis() - playExecutionTime;	
		
		endogenousExecutionTime = System.currentTimeMillis();
		endogenousPlay();
		endogenousExecutionTime = System.currentTimeMillis() - endogenousExecutionTime;
		
		contextSelfAnalisisExecutionTime = System.currentTimeMillis();
		selfAnalysationOfContexts2();
		contextSelfAnalisisExecutionTime = System.currentTimeMillis() - contextSelfAnalisisExecutionTime;
		
		world.getAmoeba().PAUSE("BEFORE HEAD NCS ");

		incompetentHeadNCSExecutionTime =  System.currentTimeMillis();
		NCSDetection_IncompetentHead();		/*If there isn't any proposition or only bad propositions, the head is incompetent. It needs help from a context.*/
		incompetentHeadNCSExecutionTime =  System.currentTimeMillis() - incompetentHeadNCSExecutionTime;
		
		concurrenceNCSExecutionTime = System.currentTimeMillis();
		NCSDetection_Concurrence(); 		/*If result is good, shrink redundant context (concurrence NCS)*/
		concurrenceNCSExecutionTime = System.currentTimeMillis() - concurrenceNCSExecutionTime;
		
		world.getAmoeba().PAUSE("BEFORE HEAD NCS CONTEXT CREATION");
		
		create_New_ContextNCSExecutionTime = System.currentTimeMillis();
		NCSDetection_Create_New_Context();	/*Finally, head agent check the need for a new context agent*/
		create_New_ContextNCSExecutionTime = System.currentTimeMillis() - create_New_ContextNCSExecutionTime;
		
		overmappingNCSExecutionTime = System.currentTimeMillis();
		NCSDetection_Context_Overmapping();
		overmappingNCSExecutionTime = System.currentTimeMillis() - overmappingNCSExecutionTime;
		
		memoryCreationExecutionTime = System.currentTimeMillis();
		NCSMemories.add(new NCSMemory(world, new ArrayList<Context>(),"End cycle"));
		memoryCreationExecutionTime = System.currentTimeMillis() - memoryCreationExecutionTime;
		
		otherExecutionTime = System.currentTimeMillis();
		criticalities.addCriticality("spatialCriticality", (getMinMaxVolume() - getVolumeOfAllContexts())/getMinMaxVolume());
		
		
		spatialGeneralizationScore = getVolumeOfAllContexts()/world.getScheduler().getContexts().size();
		
		double globalConfidence = 0;
		
		for(Context ctxt : world.getScheduler().getContextsAsContext() ) {
			globalConfidence += ctxt.getConfidence();
		}
		globalConfidence = globalConfidence / world.getScheduler().getContextsAsContext().size();
		
		double localMappingCriticality = 0.0;

		
		
		System.out.println("-------------------------------------");
		if(activatedNeighborsContexts.size()>1) {
			
			for(Percept pct : world.getScheduler().getPercepts()) {
					
				if(partiallyActivatedContextInNeighbors.get(pct).size()>1) {
					pct.sortOnCenterOfRanges(partiallyActivatedContextInNeighbors.get(pct));
				}			
			}
			
			int i = 1;
			for(Context ctxt : activatedNeighborsContexts ) {
						
				for(Context otherCtxt : activatedNeighborsContexts.subList(i, activatedNeighborsContexts.size())) {
					
					//if(nearestLocalNeighbor(ctxt, otherCtxt)) {
						
						AbstractPair<Double, Percept> distanceAndPercept = ctxt.distance(otherCtxt);
						//System.out.println("DISTANCE : " + distanceAndPercept.getA() + " " + distanceAndPercept.getB());
						if(distanceAndPercept.getA()<0) {
							criticalities.addCriticality("localOverlapMappingCriticality", Math.abs(distanceAndPercept.getA()));
						}
						else if(distanceAndPercept.getA()>0 && distanceAndPercept.getB() != null) {
							criticalities.addCriticality("localVoidMappingCriticality", distanceAndPercept.getA());
						}
						else {
							criticalities.addCriticality("localOpenVoidMappingCriticality", distanceAndPercept.getA());
						}

					//}
					
				}
				i++;
			
						
			}
		
		}

		
		mappingErrorAllowed =world.getMappingErrorAllowed();// Math.pow(world.getMappingErrorAllowed(), world.getScheduler().getPercepts().size());
		
		
		evolutionCriticalityPrediction = (lembda * evolutionCriticalityPrediction) + ((1-lembda)*currentCriticalityPrediction);
		evolutionCriticalityMapping = (lembda * evolutionCriticalityMapping) + ((1-lembda)*currentCriticalityMapping);
		evolutionCriticalityConfidence = (lembda * evolutionCriticalityConfidence) + ((1-lembda)*currentCriticalityConfidence);
		
		otherExecutionTime = System.currentTimeMillis() - otherExecutionTime;
		
		playExecutionTimeSum += playExecutionTime;
		endogenousExecutionTimeSum += endogenousExecutionTime;
		contextSelfAnalisisExecutionTimeSum += contextSelfAnalisisExecutionTime;
		
		incompetentHeadNCSExecutionTimeSum += incompetentHeadNCSExecutionTime;
		concurrenceNCSExecutionTimeSum += concurrenceNCSExecutionTime;
		create_New_ContextNCSExecutionTimeSum += create_New_ContextNCSExecutionTime;
		overmappingNCSExecutionTimeSum += overmappingNCSExecutionTime;
		memoryCreationExecutionTimeSum += memoryCreationExecutionTime;
		
		otherExecutionTimeSum += otherExecutionTime;
	}
	
	public double getSpatialGeneralizationScore() {
		return spatialGeneralizationScore;
	}
	
	
	private boolean nearestLocalNeighbor(Context ctxt1, Context ctxt2) {
		
		boolean nearestLocalNeighborTest = false;
		
		for(Percept pct : world.getScheduler().getPercepts()) {
			
			if(partiallyActivatedContextInNeighbors.get(pct).contains(ctxt1) && partiallyActivatedContextInNeighbors.get(pct).contains(ctxt2)) {
				nearestLocalNeighborTest = nearestLocalNeighborTest || (Math.abs(partiallyActivatedContextInNeighbors.get(pct).indexOf(ctxt1) - partiallyActivatedContextInNeighbors.get(pct).indexOf(ctxt2)) == 1);
			}			
			 
		}
		
		return nearestLocalNeighborTest;		
	}
	
	
	
	public double getMinMaxVolume() {
		double minMaxVolume = 1;
		for(Percept pct : world.getScheduler().getPercepts()) {
			minMaxVolume *= pct.getMinMaxDistance();
		}
		return (minMaxVolume==0.0) ? 1 :minMaxVolume;
	}
	
	public double getVolumeOfAllContexts() {
		double allContextsVolume = 0;
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
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
		
		Config.print("Nombre de contextes activ廥: " + activatedContexts.size(), 1);
		
		selectBestContext();
		if (bestContext != this.lastUsedContext) {
			noBestContext = false;
			prediction = bestContext.getActionProposal();
		} else {
			////////System.out.println("NO BEST ...");
			noBestContext = true;
			ArrayList<Agent> allContexts = world.getScheduler().getContexts();
			Context nearestContext = this.getNearestContext(activatedNeighborsContexts);
			prediction = nearestContext.getActionProposal();
			bestContext = nearestContext;
		}
		Config.print("Best context selected without oracle is : " + bestContext.getName(),0);
	//	Config.print("With function : " + bestContext.getFunction().getFormula(bestContext), 0);
		Config.print("BestContext : " + bestContext.toStringFull() + " " + bestContext.getConfidence(), 2);
		//functionSelected = bestContext.getFunction().getFormula(bestContext);
		criticity = Math.abs(oracleValue - prediction);
		
		endogenousPlay();
	}
	
	private void endogenousPlay() {
		
		endogenousPredictionActivatedContextsOverlaps = null;
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = null;
		
		endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = null ;
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = null;
		
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = null;
		
		endogenousPredictionActivatedContextsSharedIncompetence = null;
		endogenousPredictionNContextsByInfluence = null;
		
		for(Percept pcpt : this.world.getScheduler().getPercepts()) {
			requestSurroundings.get(pcpt).clear();
			sharedIncompetenceContextPairs.get(pcpt).clear();
		}
		contextsInCompetition.clear();
		
		

		if(uniqueActivatedContext()) {
			endogenousPredictionActivatedContextsOverlaps = activatedContexts.get(0).getActionProposal();
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = activatedContexts.get(0).getActionProposal();
			endogenousPredictionActivatedContextsSharedIncompetence = activatedContexts.get(0).getActionProposal();
			endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = activatedContexts.get(0).getActionProposal();
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = activatedContexts.get(0).getActionProposal();
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = activatedContexts.get(0).getActionProposal();
			NCSMemories.add(new NCSMemory(world, activatedContexts,"Unique Context"));
		}
		else if(severalActivatedContexts()){
			NCS_EndogenousCompetition();
		}
		else {
			if(surroundingContexts()) {
				NCS_EndogenousSharedIncompetence();
			}
			else if(activatedContexts.size()>0){
				NCSMemories.add(new NCSMemory(world, activatedContexts,"Other activated"));
			}
			else if(activatedContexts.size()==0) {
				
				NCSMemories.add(new NCSMemory(world, new ArrayList<Context>(),"Other non activated"));
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
		////////System.out.println("NEIGHBORS : " + activatedNeighborsContexts.size());
		for(Context ctxt :activatedNeighborsContexts) {
			endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation)*ctxt.getActionProposal();
			endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
		}
		endogenousPredictionNContexts = endogenousSumTerm/endogenousNormalizationTerm;
		////////System.out.println("ENDO PRED N CTXT : " + endogenousPredictionNContexts);
		
		// Endogenous prediction N contexts by influence //
		
		////////System.out.println("憺~憺~憺~憺~憺~憺~憺~憺~憺~憺~憺~~ INFLUENCES" + currentSituation);
		
		maxConfidence = Double.NEGATIVE_INFINITY;
		minConfidence = Double.POSITIVE_INFINITY;
		
		for(Context ctxt : world.getScheduler().getContextsAsContext()) {
			
			if(ctxt.getConfidence() > maxConfidence) {
				maxConfidence = ctxt.getConfidence();
			}
			if(ctxt.getConfidence() < minConfidence) {
				minConfidence = ctxt.getConfidence();
			}
			
			if(ctxt.getInfluenceWithConfidence(currentSituation)> 0.5) {
				contextsNeighborsByInfluence.add(ctxt);
				////////System.out.println(ctxt);
			}
		}
		////////System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<" + minConfidence + " ; " + maxConfidence + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		
		endogenousSumTerm = 0.0;
		endogenousNormalizationTerm = 0.0;
		if(contextsNeighborsByInfluence.size()>0) {
			for(Context ctxt : contextsNeighborsByInfluence) {
				endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation)*ctxt.getActionProposal();
				endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
			}
			
			endogenousPredictionNContextsByInfluence = endogenousSumTerm/endogenousNormalizationTerm;
		}
		
		
		
		if(endogenousPredictionActivatedContextsOverlaps == null) {
			endogenousPredictionActivatedContextsOverlaps =  prediction;
		}
		
		if(endogenousPredictionActivatedContextsOverlapsWorstDimInfluence == null) {
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluence =  prediction;
		}
		
		
		if(endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence == null) {
			endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence =  prediction;
		}
		
		if(endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence == null) {
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence =  prediction;
		}
		
		
		if(endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume == null) {
			endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume =  prediction;
		}
		
		if(endogenousPredictionActivatedContextsSharedIncompetence == null) {
			endogenousPredictionActivatedContextsSharedIncompetence =  prediction;
		}
		
		if(endogenousPredictionNContextsByInfluence == null) {
			endogenousPredictionNContextsByInfluence =  prediction;
		}
	}
	
	
	private boolean noActivatedContext() {
		//Test if only one context is activated
		return activatedContexts.size() == 0;
	}
	
	public boolean uniqueActivatedContext() {
		//Test if only one context is activated
		return activatedContexts.size() == 1;
	}
	
	public boolean severalActivatedContexts() {
		//Test if several context are activated
		return activatedContexts.size() > 1;
	}
	
	public boolean surroundingContexts() {
		//Test if there are surrounding contexts
		boolean testSurroudingContext = false;
		
		//////////System.out.println("SURROUNDING CONTEXTS ...");
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			
			computeNearestContextsByPercept(pct);			
				 
			}
		

		//displayContexts();
		
		for(Percept pcpt : this.world.getScheduler().getPercepts()) {
			//////System.out.print("SURROUNDING CONTEXTS ... " + pcpt.getName() + " ");
			requestSurroundings.get(pcpt).print(pcpt);
			if(sharedIncompetenceContextPairs.get(pcpt) != null) {
				if(sharedIncompetenceContextPairs.get(pcpt).containTwoContexts()) {
					testSurroudingContext = true;
				}
			}
			
			
			
		}
		
		//////////System.out.println("TEST SURROUNDING CONTEXTS ..." + testSurroudingContext);
		return testSurroudingContext;
	}
	
	
	private void diplayListRanges(ArrayList<Context> list,Percept prct, String range) {
		
		
		
		////System.out.print(range + " ranges list" + "  ");
		for(Context ctxt : list) {
			////System.out.print(ctxt.getRanges().get(prct).getRange(range) + "  ");
		}
		////////System.out.println(" ");
		
	}
	
	private void computeNearestContextsByPercept(Percept pct) {
		Pair<Context,Context> nearestContexts = new Pair(null, null);
		boolean startNeighbor = false;
		boolean endNeighbor = false;
		
		
		
		ArrayList<Context> activatedContextInOtherPercepts = getAllActivatedContextsExeptForOnePercept(pct);
		
		
		
		if(activatedContextInOtherPercepts.size()>0) {
			
				
			//////////System.out.println("Partially activated on other percepts than " + pct.getName() + " : " + activatedContextInOtherPercepts.size());			
			//////////System.out.println("Value " + pct.getValue());		 
			
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
			//////////System.out.println("=====================================================");
		}
		
			
		
		
	}
				
			
		
	
	private ArrayList<Context> getAllActivatedContextsExeptForOnePercept(Percept onePercept){
		
		return partiallyActivatedContexts.get(onePercept);
	}
	
	private ArrayList<Context> getAllActivatedNeighborContextsExeptForOnePercept(Percept onePercept){
		
		return partialNeighborContexts.get(onePercept);
	}
	
	
	
	public void displayPartiallyActivatedContexts() {
		////////System.out.println("PARTIALLY ACTIVATED CONTEXTS");
		for(Percept pct : partiallyActivatedContexts.keySet()) {
			////System.out.print(pct.getName() + " : ");
			if(partiallyActivatedContexts.get(pct).size()>0)
			for(Context ctxt : partiallyActivatedContexts.get(pct)) {
				////System.out.print(ctxt.getName() + " ; ");
			}
			////////System.out.println(" ");
		}
	}
	
	public void displayContexts() {
		////////System.out.println("CONTEXTS");
		for(Context ctxt : this.world.getScheduler().getContextsAsContext()) {
			////System.out.print(ctxt.getName() + " ; ");
		}
		////////System.out.println(" ");
	}
	
	private Percept getDifferentPercept(Percept p) {
		for(Percept pct : partiallyActivatedContexts.keySet()) {
			if(p != pct) {
				return pct;
			}
		}
		return null;
	}
	
	private boolean contextActivateInOtherPerceptsThan(Context ctxt, Percept p1, Percept p2) {
		boolean test = true;
		for(Percept prct : partiallyActivatedContexts.keySet()) {
			if(prct != p1 && prct != p2) {
				test = test & partiallyActivatedContexts.get(prct).contains(ctxt);
			}
		}
		return test;
	}
	
	private void NCS_EndogenousCompetition() {
		////////System.out.println("NCS Comptetition " + world.getScheduler().getTick());
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
		for(Context ctxt :activatedContexts) {
			endogenousSumTerm += ctxt.getInfluenceWithConfidence(currentSituation)*ctxt.getActionProposal();
			endogenousSumTerm2 += ctxt.getWorstInfluenceWithConfidence(currentSituation)*ctxt.getActionProposal();
			//endogenousSumTerm3 += ctxt.getInfluence(currentSituation)*ctxt.getActionProposal();
			endogenousSumTerm3 += ctxt.getInfluenceWithConfidenceAndVolume(currentSituation)*ctxt.getActionProposal();
			endogenousSumTerm4 += ctxt.getWorstInfluence(currentSituation)*ctxt.getActionProposal();
			endogenousSumTerm5 += ctxt.getWorstInfluenceWithVolume(currentSituation)*ctxt.getActionProposal();
			//endogenousSumTerm5 += ctxt.getWorstInfluenceWithWorstRange(currentSituation)*ctxt.getActionProposal();
			
			
			endogenousNormalizationTerm += ctxt.getInfluenceWithConfidence(currentSituation);
			endogenousNormalizationTerm2 += ctxt.getWorstInfluenceWithConfidence(currentSituation);
			//endogenousNormalizationTerm3 += ctxt.getInfluence(currentSituation);
			endogenousNormalizationTerm3 += ctxt.getInfluenceWithConfidenceAndVolume(currentSituation);
			endogenousNormalizationTerm4 += ctxt.getWorstInfluence(currentSituation);
			endogenousNormalizationTerm5 += ctxt.getWorstInfluenceWithVolume(currentSituation);
			//endogenousNormalizationTerm5 += ctxt.getWorstInfluenceWithWorstRange(currentSituation);
			
			
			concernContexts.add(ctxt);
		}
		endogenousPredictionActivatedContextsOverlaps = endogenousSumTerm/endogenousNormalizationTerm;
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluence = endogenousSumTerm2/endogenousNormalizationTerm2;
		
		endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence = endogenousSumTerm3/endogenousNormalizationTerm3;
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence = endogenousSumTerm4/endogenousNormalizationTerm4;
		endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume = endogenousSumTerm5/endogenousNormalizationTerm5;
		
		
		
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
		
		
		
		

		
		double contextInfluenceL = closestContexts.getL().getInfluenceWithConfidence(currentSituation);
		double contextInfluenceR = closestContexts.getR().getInfluenceWithConfidence(currentSituation);
		
		////////System.out.println("--------------------------------------------------DIFFERENCE :" + compareClosestContextPairModels(closestContexts));
		
		if(compareClosestContextPairModels(closestContexts)<10) {
			endogenousPredictionActivatedContextsSharedIncompetence = (contextInfluenceL*closestContexts.getL().getActionProposal() + contextInfluenceR*closestContexts.getR().getActionProposal()) / (contextInfluenceL + contextInfluenceR);
		}
		else {
			endogenousPredictionActivatedContextsSharedIncompetence = prediction;
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
		NCSMemories.add(new NCSMemory(world, concernContexts,"SharedIncompetence"));
		
	}
	
	private Double compareClosestContextPairModels(Pair<Context,Context> closestContexts) {
		Double difference = 0.0;
		
		if(closestContexts.getL().getLocalModel().getCoef().length == closestContexts.getR().getLocalModel().getCoef().length) {
			double[] coefL = closestContexts.getL().getLocalModel().getCoef();
			double[] coefR = closestContexts.getR().getLocalModel().getCoef();
			for(int i=0;i<closestContexts.getL().getLocalModel().getCoef().length;i++) {
				difference += Math.abs(coefL[i] - coefR[i]);
			}
		}
		
		if(difference==0.0) {
			return Double.POSITIVE_INFINITY;
		}
		else {
			return difference;
		}
		
	}
	
	private void NCS_EndogenousIncompetence() {
		// Extrapolation of contexts by creating twin contexts that will give the prediction
	}
	
	
	private void NCSDetection_Create_New_Context() {
		/*Finally, head agent check the need for a new context agent*/
		
	
		boolean newContextCreated = false;
		AbstractPair<Context, Double> nearestGoodContext = getNearestGoodContextWithDistance(activatedNeighborsContexts);
		
		if(activatedContexts.size() == 0) {
			
			Context context;
			if(nearestGoodContext.getA() != null) {
				world.trace(new ArrayList<String>(Arrays.asList(nearestGoodContext.getA().getName(), "************************************* NEAREST GOOD CONTEXT")));
				context = createNewContext(nearestGoodContext.getA());
			}else {
				context = createNewContext();
			}
			//context = createNewContext();
			

			bestContext = context;
			newContext = context;
			newContextCreated = true;
		}
			
		if (!newContextCreated) {
			updateStatisticalInformations();
		}
		
	}
	
	private void NCSDetection_Context_Overmapping() {
		
		ArrayList<Context> activatedContextsCopy = new ArrayList<Context>();
		activatedContextsCopy.addAll(activatedContexts);
		
		for(Context ctxt : activatedContextsCopy) {
			ctxt.NCSDetection_OverMapping();
		}
	}
	
	private void NCSDetection_Concurrence() {
		/*If result is good, shrink redundant context (concurrence NCS)*/
		if (bestContext != null && criticity <= this.errorAllowed) {
			for (int i = 0 ; i < activatedContexts.size() ; i++) {
				if (activatedContexts.get(i) != bestContext && !activatedContexts.get(i).isDying() && this.getCriticity(activatedContexts.get(i)) <= this.errorAllowed) {
			//		////////System.out.println("Shrink context " + contexts.get(i).getName());
					activatedContexts.get(i).solveNCS_Concurrence(this);
				}
			}
		}
	}
	
	private void NCSDetection_IncompetentHead() {
		/*If there isn't any proposition or only bad propositions, the head is incompetent. It needs help from a context.*/
		if (activatedContexts.isEmpty() || (criticity > this.errorAllowed && !oneOfProposedContextWasGood())){
			
			//Context c = getNearestGoodContext(activatedNeighborsContexts);
			Context c = getSmallestGoodContext(activatedNeighborsContexts);
			if(c!=null) {
				////////System.out.println("Nearest good context : " + c.getName());
			}
			else {
				////////System.out.println("Nearest good context : null");
			}
			if (c!=null) c.solveNCS_IncompetentHead(this);
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
		////////System.out.println(world.getScheduler().getTick());
		/*All context which proposed itself must analyze its proposition*/
		for (int i = 0 ; i < activatedContexts.size() ; i++) {
			////////System.out.println(activatedContexts.get(i).getName());
			activatedContexts.get(i).analyzeResults2(this);
		}
		
		for (Context ctxt : activatedNeighborsContexts) {

			if(!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}
			
		}
	}
	
	private void selfAnalysationOfContexts2() {
		////////System.out.println(world.getScheduler().getTick());
		/*All context which proposed itself must analyze its proposition*/
		
		
		if(activatedContexts.size()>1) {
			Context closestContextToOracle = null;
			double minDistanceToOraclePrediction = Double.POSITIVE_INFINITY;
			double currentDistanceToOraclePrediction = 0.0;
			
			
			
			for (Context activatedContext : activatedContexts) {
				////////System.out.println(activatedContexts.get(i).getName());
				currentDistanceToOraclePrediction = activatedContext.getLocalModel().distance(activatedContext.getCurrentExperiment());
				if(currentDistanceToOraclePrediction<minDistanceToOraclePrediction) {
					minDistanceToOraclePrediction = currentDistanceToOraclePrediction;
					closestContextToOracle = activatedContext;
				}
					
			}
			
			System.out.println(closestContextToOracle.getLocalModel().distance(closestContextToOracle.getCurrentExperiment()) + " ******************************************************************DISTANCE TO MODEL : " );
			System.out.println("OLD COEFS " + closestContextToOracle.getLocalModel().coefsToString());
			closestContextToOracle.getLocalModel().updateModelWithExperimentAndWeight(closestContextToOracle.getCurrentExperiment(),0.5);
			System.out.println("NEW COEFS " + closestContextToOracle.getLocalModel().coefsToString());
			
			for (Context activatedContext : activatedContexts) {	
				activatedContext.analyzeResults3(this, closestContextToOracle);
				
				
			}
		}else if(activatedContexts.size() == 1) {
			
			double distanceToOracleForActivatedContext = activatedContexts.get(0).getLocalModel().distance(activatedContexts.get(0).getCurrentExperiment());
			System.out.println(distanceToOracleForActivatedContext + " ******************************************************************DISTANCE TO MODEL : " );
			if(activatedNeighborsContexts.size()>1) {
				
				Context closestContextToOracle = null;
				double minDistanceToOraclePredictionInNeighbors = Double.POSITIVE_INFINITY;
				double currentDistanceToOraclePrediction = 0.0; 
				for(Context contextNeighbor : activatedNeighborsContexts) {
					if(contextNeighbor != activatedContexts.get(0)) {
						currentDistanceToOraclePrediction = contextNeighbor.getLocalModel().distance(contextNeighbor.getCurrentExperiment());
						if(currentDistanceToOraclePrediction<minDistanceToOraclePredictionInNeighbors) {
							minDistanceToOraclePredictionInNeighbors = currentDistanceToOraclePrediction;
							closestContextToOracle = contextNeighbor;
						} 
					}
				}
				
				if(minDistanceToOraclePredictionInNeighbors>distanceToOracleForActivatedContext) {
					System.out.println("OLD COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
					activatedContexts.get(0).getLocalModel().updateModelWithExperimentAndWeight(activatedContexts.get(0).getCurrentExperiment(),0.5);
					System.out.println("NEW COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
					
				}else {
					//LocalModelAgent remplacementModel = new LocalModelMillerRegression(world, activatedContexts.get(0), closestContextToOracle.getLocalModel().getCoef().clone());
					//activatedContexts.get(0).setLocalModel(remplacementModel);
				}

				
			}else {
				System.out.println("OLD COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
				activatedContexts.get(0).getLocalModel().updateModelWithExperimentAndWeight(activatedContexts.get(0).getCurrentExperiment(),0.5);
				System.out.println("NEW COEFS " + activatedContexts.get(0).getLocalModel().coefsToString());
				
				
			}
			
			activatedContexts.get(0).analyzeResults3(this, activatedContexts.get(0));
			
			
		}
		
		
		
		for (Context ctxt : activatedNeighborsContexts) {

			if(!activatedContexts.contains(ctxt)) {
				ctxt.NCSDetection_Uselessness();
			}
			
		}
	}
	
	private void setNearestContextAsBestContext() {
		Context nearestContext = this.getNearestContext(activatedNeighborsContexts);

		if (nearestContext != null) {
			prediction = nearestContext.getActionProposal();
		} else {
			prediction = 0;
		}

		bestContext =  nearestContext;
	}
	
	private void setNearestGoodContextAsBestContext() {
		Context nearestContext = this.getNearestGoodContext(activatedNeighborsContexts);

		if (nearestContext != null) {
			prediction = nearestContext.getActionProposal();
		} else {
			prediction = 0;
		}

		bestContext =  nearestContext;
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
	public Context getNearestGoodContext(ArrayList<Context> allContext) {
		Context nearest = null;
		for (Context c : allContext) {
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (nearest == null || getExternalDistanceToContext(c) < getExternalDistanceToContext(nearest) ) {
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
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (smallest == null || currentVolume < minVolume ) {
					smallest = c;
				}
			}
		}
		
		
		return smallest;
		
	}
	
	public Context getBetterContext(Context selectedContext, ArrayList<Context> contextNeighbors, double currentError) {
		Context betterContext = null;
		Double lowestError = currentError + 0.0001;
		AbstractPair<Boolean, Double> betterContextAndError;
		for (Context c : contextNeighbors) {
			
			if(c!=selectedContext) {
				if(c.getExperiments().size()>world.getScheduler().getPercepts().size()) {
					betterContextAndError = selectedContext.tryAlternativeModel(c.getLocalModel());
					if (betterContextAndError.getA() && betterContextAndError.getB() <  lowestError &&  c != newContext && !c.isDying()) {
						betterContext = c;
						lowestError = betterContextAndError.getB();
					}
				}
				
			}
			
			
		}
		
		return betterContext;
		
	}
	
	/**
	 * Gets the distance to nearest good context.
	 *
	 * @param allContext the all context
	 * @return the distance to nearest good context
	 */
	private AbstractPair<Context, Double> getNearestGoodContextWithDistance(ArrayList<Context> contextNeighbors) {
		double d = Double.MAX_VALUE;
		Context nearestGoodContext = null;
		for (Context c : contextNeighbors) {
			if (Math.abs((c.getActionProposal() - oracleValue)) <= errorAllowed && c != newContext && !c.isDying()) {
				if (getExternalDistanceToContext(c) < d ) {
					d = getExternalDistanceToContext(c);
					nearestGoodContext = c;
				}
			}
		}
		return new AbstractPair<Context, Double>(nearestGoodContext,d);
		
	}
	
	
	
	
	/**
	 * Gets the nearest context.
	 *
	 * @param allContext the all context
	 * @return the nearest context
	 */
	private Context getNearestContext(ArrayList<Context> contextNeighboors) {
		Context nearest = null;
		double distanceToNearest = Double.MAX_VALUE;
		for (Context c : contextNeighboors) {
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
	
	private double getDistanceToContext (Context context) {
		double totalDistance = 0.0;
		for (Percept p : world.getAllPercept()) {
			double distance = context.distance(p, p.getValue());
			if(distance > 0) {
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
	//	////////System.out.println("Creation d'un nouveau contexte : " + contexts.size());
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
	
	private Context createNewContext(Context bestNearestCtxt) {
		//	////////System.out.println("Creation d'un nouveau contexte : " + contexts.size());
			newContextWasCreated = true;
//			if (contexts.size() != 0) {
//				System.exit(0);
//			}
			world.raiseNCS(NCS.CREATE_NEW_CONTEXT);
			Context context;
			if (firstContext) {
				//////System.out.println(bestNearestCtxt.toStringFull());
				context = new Context(world, this, bestNearestCtxt);
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
		
		criticalities.updateMeans();
		
		if(severalActivatedContexts()) {
			
			endogenousCriticalities.addCriticality("predictionCriticality", criticity);
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapspredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlaps));
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapsWorstDimInfluencepredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluence));
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidencepredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence));
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidencepredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence));
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumepredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume));
			endogenousCriticalities.addCriticality("endogenousPredictionActivatedContextsSharedIncompetencepredictionCriticality", Math.abs(oracleValue - endogenousPredictionActivatedContextsSharedIncompetence));

			endogenousCriticalities.updateMeans();
			
			
		}
		
		
		
		//////////System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ Average Prediction Criticity :" + averagePredictionCriticity);
		
		if (criticalities.getCriticalityMean("predictionCriticality") > errorAllowed) {
			perfIndicator--;
		} else {
			perfIndicator++;
		}
		
//		if (averageMappingCriticity > mappingErrorAllowed) {
//			perfMappingIndicator--;
//		}
//		else {
//			perfMappingIndicator++;
//		}
		

		
		
		if (perfMappingIndicator <= nConflictBeforeAugmentation * (-1)) {
			perfMappingIndicator = 0;
			//mappingErrorAllowed += augmentationFactorError * averageMappingCriticity;
			//world.setMappingErrorAllowed(mappingErrorAllowed);
			//////////System.out.println("中中中中中中中中中中中  augmentationFactorError :" + augmentationFactorError);
		}
		
		if (perfMappingIndicator >= nSuccessBeforeDiminution) {
			perfMappingIndicator = 0;
			//mappingErrorAllowed -= diminutionFactorError * averageMappingCriticity;
			//world.setMappingErrorAllowed(mappingErrorAllowed);
			//////////System.out.println("中中中中中中中中中中中  diminutionFactorError :" + diminutionFactorError);
		}
		
		
		if (perfIndicator <= nConflictBeforeAugmentation * (-1)) {
			perfIndicator = 0;
			errorAllowed += augmentationFactorError * criticalities.getCriticalityMean("predictionCriticality");
			//////////System.out.println("中中中中中中中中中中中  augmentationFactorError :" + augmentationFactorError);
		}
		
		if (perfIndicator >= nSuccessBeforeDiminution) {
			perfIndicator = 0;
			errorAllowed -= diminutionFactorError * criticalities.getCriticalityMean("predictionCriticality");
			//////////System.out.println("中中中中中中中中中中中  diminutionFactorError :" + diminutionFactorError);
			errorAllowed = Math.max(minErrorAllowed, errorAllowed);
		}
		
		if (perfIndicatorInexact <= nConflictBeforeInexactAugmentation * (-1)) {
			perfIndicatorInexact = 0;
			inexactAllowed *= augmentationInexactError;
			//////////System.out.println("中中中中中中中中中中中  augmentationInexactError :" + augmentationInexactError);
		}
		
		if (perfIndicatorInexact >= nSuccessBeforeInexactDiminution) {
			perfIndicatorInexact = 0;
			inexactAllowed *= diminutionInexactError;
			inexactAllowed = Math.max(minInexactAllowed, inexactAllowed);
			//////////System.out.println("中中中中中中中中中中中  diminutionInexactError :" + diminutionInexactError);

		}
		
//		inexactAllowed = averagePredictionCriticity / 2;
//		errorAllowed = 3 * inexactAllowed;
		
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
		

		bc = activatedContexts.get(0);
		double currentConfidence = bc.getConfidence();

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
		return criticalities.getCriticalityMean("predictionCriticality");
	}
	
	

	
	public double getAveragePredictionCriticityCopy() {
		return endogenousCriticalities.getCriticalityMean("predictionCriticality");
	}
	public double getAveragePredictionCriticityEndoActivatedContextsOverlaps() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsOverlapspredictionCriticality");
	}
	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluence() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsOverlapsWorstDimInfluencepredictionCriticality");
	}
	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsInfluenceWithoutConfidence() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidencepredictionCriticality");
	}
	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidencepredictionCriticality");
	}
	public double getAveragePredictionCriticityEndoActivatedContextsOverlapsWorstDimInfluenceWithVolume() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumepredictionCriticality");
	}
	
	
	public double getAveragePredictionCriticityEndoActivatedContextsSharedIncompetence() {
		return endogenousCriticalities.getCriticalityMean("endogenousPredictionActivatedContextsSharedIncompetencepredictionCriticality");
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
	
	public Double getEndogenousPredictionActivatedContextsOverlaps() {
		return endogenousPredictionActivatedContextsOverlaps;
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluence() {
		return endogenousPredictionActivatedContextsOverlapsWorstDimInfluence;
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence() {
		return endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence;
	}
	
	public Double getendogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence() {
		return endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence;
	}
	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume() {
		return endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume;
	}
	
	
	public Double getEndogenousPredictionActivatedContextsSharedIncompetence() {
		return endogenousPredictionActivatedContextsSharedIncompetence;
	}
	
	public Double getEndogenousPredictionNContextsByInfluence() {
		return endogenousPredictionNContextsByInfluence;
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlaps);
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluence);
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidenceCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsInfluenceWithoutConfidence);
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidenceCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithoutConfidence);
	}
	
	public Double getEndogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolumeCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsOverlapsWorstDimInfluenceWithVolume);
	}
	
	public Double getEndogenousPredictionActivatedContextsSharedIncompetenceCriticity() {
		return Math.abs(oracleValue - endogenousPredictionActivatedContextsSharedIncompetence);
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
		return partiallyActivatedContexts.get(pct);
	}
	
	public ArrayList<Context> getPartiallyActivatedNeighborContexts(Percept pct) {
		return partialNeighborContexts.get(pct);
	}
	
	
	public HashMap<Percept, Pair<Context, Context>> getRequestSurroundings() {
		return requestSurroundings;
	}
	
	
	public boolean requestSurroundingContains(Context ctxt) {
		

		for(Percept pct : requestSurroundings.keySet()) {
			//////////System.out.println("REQUEST SURROUNDINGS " +  requestSurroundings.get(pct).getL().getName() +  " ; " + requestSurroundings.get(pct).getR().getName());
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
	
	
	public void addRequestNeighbor(Context ctxt) {
		if(!activatedNeighborsContexts.contains(ctxt)) {
			////System.out.println(world.getScheduler().getTick() + " " + ctxt.getName() + " " + "VALID NEIGHBOR");
			activatedNeighborsContexts.add(ctxt);
		}	
	}
	
	public void addActivatedContext(Context ctxt) {
		if(!activatedContexts.contains(ctxt)) {
			activatedContexts.add(ctxt);
			
			//System.out.println(world.getScheduler().getTick() + " ACTIVATED CONTEXTS");
			for(Context cxt : activatedContexts) {
				//System.out.println("--> " + cxt.getName());
			}
		}	
	}
	
	public void removeActivatedContext(Context ctxt) {
		if(activatedContexts.contains(ctxt)) {
			activatedContexts.remove(ctxt);
		}	
	}
	
	public void removeRequestNeighbor(Context ctxt) {
		if(activatedNeighborsContexts.contains(ctxt)) {
			////System.out.println(world.getScheduler().getTick() + " " + ctxt.getName() + " " + "NOT VALID NEIGHBOR ANYMORE");
			activatedNeighborsContexts.remove(ctxt);
		}	
	}
	
	public ArrayList<Context> getActivatedNeighborsContexts(){
		return activatedNeighborsContexts;
	}
	
	public ArrayList<Context> getContextNeighborsByInfluence(){
		return contextsNeighborsByInfluence;
	}
	
	public void displayActivatedNeighborsContexts() {
		for(Context ctxt : activatedNeighborsContexts) {
			////////System.out.println(ctxt.getName());
		}
	}
	
	public void clearActivatedNeighborsContexts(){
		activatedNeighborsContexts.clear();
	}
	
	public void clearContextdNeighborsByInfluence(){
		contextsNeighborsByInfluence.clear();
	}
	
	public void clearAllUseableContextLists() {
		
		activatedContexts.clear();
		activatedNeighborsContexts.clear();
		contextsNeighborsByInfluence.clear();
		for(Percept pct : this.world.getScheduler().getPercepts()) {
			partiallyActivatedContexts.get(pct).clear();
			partiallyActivatedContextInNeighbors.get(pct).clear();
			partialNeighborContexts.get(pct).clear();
		}
	}
	
	public Double getMaxRadiusForContextCreation(Percept pct) {
		double maxRadius = pct.getRadiusContextForCreation();
		double currentRadius;
		
			
		//for(Context ctxt:partialNeighborContexts.get(pct)) {
		for(Context ctxt:activatedNeighborsContexts) {			
			
			currentRadius = ctxt.getRanges().get(pct).distance(pct.getValue());
			////System.out.println(ctxt.getName() + " " + pct.getName() + " " + currentRadius + " " + maxRadius);
			if(currentRadius<maxRadius && currentRadius>0 ) {
				maxRadius = currentRadius;
			}
		}
		
		return maxRadius;
		
		
	}
	
	public AbstractPair<Double,Double> getMaxRadiusesForContextCreation(Percept pct) {
		AbstractPair<Double,Double> maxRadiuses = new AbstractPair<Double,Double>(
				Math.min(pct.getRadiusContextForCreation(), 
						Math.abs(pct.getMin()- pct.getValue())),
				Math.min(pct.getRadiusContextForCreation(), 
						Math.abs(pct.getMax()-pct.getValue())));
		
		//AbstractPair<Double,Double> maxRadiuses = new AbstractPair<Double,Double>(pct.getRadiusContextForCreation(),pct.getRadiusContextForCreation());
		//AbstractPair<Double,Double> maxRadiuses = new AbstractPair<Double,Double>(Math.abs(pct.getMin()- pct.getValue()),Math.abs(pct.getMax()-pct.getValue()));
	
		double currentStartRadius;
		double currentEndRadius;
				
		//for(Context ctxt:partialNeighborContexts.get(pct)) {
		for(Context ctxt:activatedNeighborsContexts) {			
			
			if(ctxt.getRanges().get(pct).centerDistance(pct.getValue()) < 0) {
				// End radius
				currentEndRadius = ctxt.getRanges().get(pct).distance(pct.getValue());
				if(currentEndRadius < maxRadiuses.getB() && currentEndRadius > 0 ) {
					maxRadiuses.setB(currentEndRadius); 
				}
			}
			
			if(ctxt.getRanges().get(pct).centerDistance(pct.getValue()) > 0) {
				// Start radius
				currentStartRadius = ctxt.getRanges().get(pct).distance(pct.getValue());
				if(currentStartRadius < maxRadiuses.getA() && currentStartRadius > 0 ) {
					maxRadiuses.setA(currentStartRadius); 
				}
			}

			
			
		}
		
		return maxRadiuses;
		
		
	}
	
	
	public double getAverageSpatialCriticality() {
		return criticalities.getCriticalityMean("spatialCriticality");
	}
	
	public void setBadCurrentCriticalityPrediction() {
		currentCriticalityPrediction = 1;
	}
	
	public void setBadCurrentCriticalityConfidence() {
		currentCriticalityConfidence = 1;
	}
	
	public void setBadCurrentCriticalityMapping() {
		currentCriticalityMapping = 1;
	}
	

	public double fact(double n) {
		
		if(n==0) {
			return 1;
		}
		else {
			return n*fact(n-1);
		}
	}
	
	public double combinationsWithoutRepetitions(double n) {
		if(n==2) return 1;
		else return fact(n) / (2*(fact(n)-2));
	}
	
	public double getMappingErrorAllowed() {
		return mappingErrorAllowed;
	}
	
}











