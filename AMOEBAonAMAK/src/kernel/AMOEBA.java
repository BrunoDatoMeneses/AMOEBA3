package kernel;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import agents.AmoebaAgent;
import agents.context.Context;
import agents.context.localModel.LocalModel;
import agents.context.localModel.TypeLocalModel;
import agents.head.Head;
import agents.head.REQUEST;
import agents.percept.Percept;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.AmoebaMultiUIWindow;
import gui.AmoebaWindow;
import gui.DimensionSelector;
import kernel.backup.IBackupSystem;
import kernel.backup.ISaveHelper;
import kernel.backup.SaveHelperDummy;
import kernel.backup.SaveHelperImpl;
import ncs.NCS;
import utils.PrintOnce;
import utils.TRACE_LEVEL;

/**
 * The AMOEBA amas
 *
 */
public class AMOEBA extends Amas<World> implements IAMOEBA {
	// -- Attributes
	
	
	public VUIMulti vuiMulti;
	/**
	 * Utility to save, autosave, and load amoebas.
	 */
	public ISaveHelper saver = new SaveHelperDummy();
	
	/**
	 * The system studied by the amoeba.
	 */
	public StudiedSystem studiedSystem;
	
	public AmoebaMultiUIWindow multiUIWindow;
	
	private Head head;
	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;
	private HashMap<String, Double> perceptions = new HashMap<String, Double>();
	private boolean useOracle = true;

	private boolean runAll = false;
	private boolean creationOfNewContext = true;
	private boolean renderUpdate;
	private boolean reinforcementMode = false;
	
	private int cycleWithoutRender = 0;

	private ArrayList<Context> spatiallyAlteredContext = new ArrayList<>();
	private ArrayList<Context> toKillContexts = new ArrayList<>();
	
	private ArrayList<Context> lastModifiedContext = new ArrayList<>();

	private ArrayList<Context> alteredContexts = new ArrayList<>();
	
	private HashSet<Context> validContexts;
	private ReadWriteLock validContextLock = new ReentrantReadWriteLock();
	
	private HashSet<Context> neighborContexts ;
	private ReadWriteLock neighborContextsLock = new ReentrantReadWriteLock();
	
	public AmoebaData data;
	private ArrayList<Percept> percepts;

	/**
	 * Instantiates a new, empty, amoeba.
	 * For ease of use, consider using {@link AMOEBA#AMOEBA(String, StudiedSystem)}.
	 *
	 * @param studiedSystem
	 *            the studied system
	 */
	public AMOEBA(AmoebaMultiUIWindow window, VUIMulti vui) {
		super(window, vui, new World(), Scheduling.HIDDEN);
		vuiMulti = vui;
	}
	
	/**
	 * Intantiate a default amoeba from a config file.
	 * 
	 * @param path path to the config file.
	 */
	public AMOEBA(AmoebaMultiUIWindow window, VUIMulti vui, String path, StudiedSystem studiedSystem) {
		super(window, vui, new World(), Scheduling.HIDDEN);
		vuiMulti = vui;
		this.studiedSystem = studiedSystem;
		setRenderUpdate(true);
		saver = new SaveHelperImpl(this, window);
		saver.load(path);
	}

	@Override
	protected void onInitialConfiguration() {
		super.onInitialConfiguration();
		if(Configuration.allowedSimultaneousAgentsExecution != 1) {
			PrintOnce.print("Warning ! Multithreading is not currently sopported !\n"
					+ "Please use Configuration.allowedSimultaneousAgentsExecution=1");
		}
		getEnvironment().setAmoeba(this);
		data = new AmoebaData();
		for(REQUEST rqt : REQUEST.values()){
			data.requestCounts.put(rqt, 0);
		}

	}
	
	@Override
	protected void onRenderingInitialization() {
		((AmoebaMultiUIWindow) amasMultiUIWindow).initialize(this);
	}

	@Override
	protected void onUpdateRender() {
		// Update statistics
		if(amasMultiUIWindow!=null) {

			AmakPlot loopNCS = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("This loop NCS");
			AmakPlot allNCS = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("All time NCS");
			AmakPlot nbAgent = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Number of agents");
			AmakPlot errors = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Errors");
			AmakPlot distancesToModels = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Distances to models");
			AmakPlot gloabalMappingCriticality = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Global Mapping Criticality");
			AmakPlot timeExecution = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Time Execution");
			AmakPlot criticalities = ((AmoebaMultiUIWindow)amasMultiUIWindow).getPlot("Criticalities");
			
			
			boolean notify = isRenderUpdate();
			
			HashMap<NCS, Integer> thisLoopNCS = environment.getThisLoopNCS();
			HashMap<NCS, Integer> allTimeNCS = environment.getAllTimeNCS();
			for (NCS ncs : NCS.values()) {
				loopNCS.addData(ncs, cycle, thisLoopNCS.get(ncs), notify);
				allNCS.addData(ncs, cycle, allTimeNCS.get(ncs), notify);
			}
			nbAgent.addData("Percepts", cycle, getPercepts().size(), notify);
			nbAgent.addData("Contexts", cycle, getContexts().size(), notify);
			nbAgent.addData("Activated", cycle, environment.getNbActivatedAgent(), notify);

			errors.addData("Criticality", cycle, head.getNormalizedCriticicality(), notify);
			errors.addData("Mean criticality", cycle, head.getAveragePredictionCriticity(), notify);
			errors.addData("Error allowed", cycle, head.getErrorAllowed(), notify);
			
			distancesToModels.addData("Distance to model", cycle, head.getLastMinDistanceToRegression(), notify);
			distancesToModels.addData("Average distance to model", cycle, head.criticalities.getCriticalityMean("distanceToRegression"), notify);
			distancesToModels.addData("Allowed distance to model", cycle, head.getDistanceToRegressionAllowed(), notify);
			
			gloabalMappingCriticality.addData("Current Value", cycle, head.getAverageSpatialCriticality(), notify);
			gloabalMappingCriticality.addData("Zero", cycle, 0.0, notify);
			
			timeExecution.addData("HeadPlay", cycle, data.executionTimesSums[0], notify);
			timeExecution.addData("EndogenousPlay", cycle, data.executionTimesSums[1], notify);
			timeExecution.addData("ContextSelfAnalisis", cycle, data.executionTimesSums[2], notify);
			timeExecution.addData("IncompetentNCS", cycle, data.executionTimesSums[3], notify);
			timeExecution.addData("ConcurrenceNCS", cycle, data.executionTimesSums[4], notify);
			timeExecution.addData("NewContextNCS", cycle, data.executionTimesSums[5], notify);
			timeExecution.addData("OvermappingNCS", cycle, data.executionTimesSums[6], notify);
			timeExecution.addData("Other", cycle, data.executionTimesSums[7], notify);
			timeExecution.addData("BestContextInNeighbors", cycle, data.executionTimesSums[8], notify);
			timeExecution.addData("CreateContext", cycle, data.executionTimesSums[9], notify);
			timeExecution.addData("UpdateStatitics", cycle, data.executionTimesSums[10], notify);
			timeExecution.addData("ChildContext", cycle, data.executionTimesSums[11], notify);
			timeExecution.addData("PotentialRequest", cycle, data.executionTimesSums[12], notify);
			
			criticalities.addData("Prediction", cycle, data.evolutionCriticalityPrediction, notify);
			criticalities.addData("Mapping", cycle, data.evolutionCriticalityMapping, notify);
			criticalities.addData("Confidence", cycle, data.evolutionCriticalityConfidence, notify);
		}
		
		if (isRenderUpdate()) {
			((AmoebaMultiUIWindow)amasMultiUIWindow).mainVUI.updateCanvas();
			updateAgentsVisualisation();
			RunLaterHelper.runLater(() -> {resetCycleWithoutRender();});
		}
	}

	@Override
	public void onSystemCycleBegin() {
		cycle++;
		if (cycle % 1000 == 0) {
			//Log.defaultLog.inform("AMOEBA", "Cycle " + cycle + ". Nb agents: "+getAgents().size());
		}
		
		if(isRenderUpdate()) {
			incrementCycleWithoutRender();
			/* deactivate render update and stop simulation if UI is too far
			 * behind the simulation (10 cycles)
			 */
			if(getCycleWithoutRender() > 10) {
				setRenderUpdate(false);
				RunLaterHelper.runLater(()->{
					// we (sadly) have to put it inside a runlater to correctly update the slider
					getScheduler().stop(); 
				});
				Log.defaultLog.warning("AMOEBA UI", "Rendering cannot keep up with simulation, it has been deactivated. "
						+ "To reactiavte it, slow down the simulation and toggle the \"Allow Rendering\" button.");
			}
		}
		
		if (studiedSystem != null) {
			
			studiedSystem.playOneStep();
			
			perceptions = studiedSystem.getOutput();
			
			
			if(perceptions.get("oracle")==null) {
				data.useOracle = false;
			}else {
				data.useOracle = true;
			}
			
		}
		
		environment.preCycleActions();
		head.clearAllUseableContextLists();
		validContexts = null;
		neighborContexts = null;
		environment.resetNbActivatedAgent();
		spatiallyAlteredContext.clear();
		toKillContexts.clear();
		lastModifiedContext.clear();
		alteredContexts.clear();
		data.higherNeighborLastPredictionPercepts=null;
	}
	
	synchronized private void incrementCycleWithoutRender() {
		cycleWithoutRender += 1;
	}
	
	synchronized private void resetCycleWithoutRender() {
		cycleWithoutRender = 0;
	}
	
	synchronized private int getCycleWithoutRender() {
		return cycleWithoutRender;
	}
	
	@Override
	public void onSystemCycleEnd() {
		
		if(studiedSystem != null) {
			if(data.selfLearning) {
				data.selfLearning = false;
				studiedSystem.setSelfLearning(true);
				studiedSystem.setSelfRequest(head.getSelfRequest());
				 
			}
			else if(data.activeLearning) {
				data.activeLearning = false;
				studiedSystem.setActiveLearning(true);
				studiedSystem.setSelfRequest(head.getActiveRequest());
			}
		}
		
		super.onSystemCycleEnd();
		if(saver != null)
			saver.autosave();
	}

	public void onSystemCycleEndWithoutSave() {


		getEnvironment().trace(TRACE_LEVEL.DEBUG, new ArrayList<String>(Arrays.asList("studiedSystem",""+(studiedSystem!=null),"activeLearning",""+data.activeLearning,"selfLearning",""+data.selfLearning)));
		if(studiedSystem != null) {
			if(data.selfLearning) {
				data.selfLearning = false;
				studiedSystem.setSelfLearning(true);
				studiedSystem.setSelfRequest(head.getSelfRequest());

			}
			else if(data.activeLearning) {
				data.activeLearning = false;
				studiedSystem.setActiveLearning(true);
				studiedSystem.setSelfRequest(head.getActiveRequest());
			}
		}

		super.onSystemCycleEnd();
		//if(saver != null)
		//	saver.autosave();
	}

	/**
	 * Define what is done during a cycle, most importantly it launch agents.
	 *
	 * Every 1000 cycles, all Context are launched, allowing to delete themselves if
	 * they're too small. To change this behavior you have to modify this method.
	 */
	@Override
	public void cycle() {


		onSystemCycleBegin();

		runAgents();

		computePendingAgents();

		onSystemCycleEnd();

		renderUI();


	}

	public void computePendingAgents() {
		removePendingAgents();

		addPendingAgents();
	}

	private void runAgents() {
		// run percepts
		List<Percept> synchronousPercepts = getPercepts().stream().filter(a -> a.isSynchronous())
				.collect(Collectors.toList());
		//Collections.sort(synchronousPercepts, new AgentOrderComparator());

		for (Percept agent : synchronousPercepts) {
			executor.execute(agent);
		}
		try {
			perceptionPhaseSemaphore.acquire(synchronousPercepts.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			decisionAndActionPhasesSemaphore.acquire(synchronousPercepts.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// it is sometime useful to run all context agent
		// especially to check if they're not too small,
		// or after reactivating rendering.
		if (cycle % 1000 == 0) {
			runAll = true;
		}

		Stream<Context> contextStream = null;
		if (runAll) {
			contextStream = getContexts().stream(); // update all context
			runAll = false;
		} else {
			HashSet<Context> vcontexts = getValidContexts();
			if (vcontexts == null) {
				vcontexts = new HashSet<>();
			}
			contextStream = vcontexts.stream(); // or only valid ones
		}

		getHeadAgent().setActivatedNeighborsContexts(new ArrayList<Context>(getNeighborContexts()));


		// run contexts
		List<Context> synchronousContexts = contextStream.filter(a -> a.isSynchronous()).collect(Collectors.toList());
		//Collections.sort(synchronousContexts, new AgentOrderComparator());

		for (Context agent : synchronousContexts) {
			executor.execute(agent);
		}
		try {
			perceptionPhaseSemaphore.acquire(synchronousContexts.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			decisionAndActionPhasesSemaphore.acquire(synchronousContexts.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// run head
		List<Head> heads = new ArrayList<>();
		heads.add(head);
		List<Head> synchronousHeads = heads.stream().filter(a -> a.isSynchronous()).collect(Collectors.toList());
		//Collections.sort(synchronousHeads, new AgentOrderComparator());

		for (Head agent : synchronousHeads) {
			executor.execute(agent);
		}
		try {
			perceptionPhaseSemaphore.acquire(synchronousHeads.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
			decisionAndActionPhasesSemaphore.acquire(synchronousHeads.size());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void renderUI() {

		if (!Configuration.commandLineMode) {
			onUpdateRender();

			if(Configuration.waitForGUI) {
				// we put an action in JavaFX rendering queue
				RunLaterHelper.runLater(() -> {
					renderingPhaseSemaphore.release();
				});
				// and wait for it to finish
				try {
					renderingPhaseSemaphore.acquire();
				} catch (InterruptedException e) {
					Log.defaultLog.error("[AMAS GUI]", "Failed to wait for GUI update to finish.");
					e.printStackTrace();
				}
				// now the queue should be clear
			}
		}


	}

	@Override
	public HashMap<String, Double> learn(HashMap<String, Double> perceptionsActionState) {
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		studiedSystem = ss;
		
		return null;
	}

	@Override
	public double request(HashMap<String, Double> perceptionsActionState) {
		boolean usingOracle = isUseOracle();
		if (usingOracle)
			head.changeOracleConnection();
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		if (usingOracle)
			head.changeOracleConnection();
		studiedSystem = ss;
		return getAction();
	}
	
	
	public HashMap<String, Double> reinforcementRequest(HashMap<String, Double> perceptionsActionState) {
		boolean usingOracle = isUseOracle();
		if (usingOracle)
			head.changeOracleConnection();
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		if (usingOracle)
			head.changeOracleConnection();
		studiedSystem = ss;
		return getHigherNeighborLastPredictionPercepts();
	}
	
	@Override
	public HashMap<String, Double> maximize(HashMap<String, Double> fixedPercepts){
		ArrayList<Percept> percepts = getPercepts();
		ArrayList<Percept> freePercepts = new ArrayList<>(percepts);
		freePercepts.removeIf(p ->fixedPercepts.containsKey(p.getName()));
		//System.out.println("known : "+known.keySet());
		//System.out.println("unknow : "+unknown);
		if(freePercepts.isEmpty()) {
			return null;
		}
		
		//get partially activated context
		ArrayList<Context> partiallyActivatedCtxts = new ArrayList<>();
		for(Context ctxt : getContexts()) {
			boolean good = true;
			for(String pctString : fixedPercepts.keySet()) {
				
				if(!ctxt.getRangeByPerceptName(pctString).contains2(fixedPercepts.get(pctString))) {
					good = false;
					break;
				}
			}
			if(good) partiallyActivatedCtxts.add(ctxt);
		}
		
		ArrayList<HashMap<String, Double>> posibleSolutions = new ArrayList<>();
		for(Context ctxt : partiallyActivatedCtxts) {
			posibleSolutions.add(ctxt.getLocalModel().getMaxWithConstraint(fixedPercepts));
		}
		HashMap<String, Double> maxSolution = new HashMap<>();

		Double maxValue = Double.NEGATIVE_INFINITY;
		maxSolution.put("oracle", maxValue);
		//find best solution
		for(HashMap<String, Double> s : posibleSolutions) {
			if(s.get("oracle") > maxValue) {
				maxValue = s.get("oracle");
				maxSolution = s;
			}
		}
		return maxSolution;
	}

	public LocalModel buildLocalModel(Context context, TypeLocalModel type) {
		return type.factory.buildLocalModel(context);
	}
	
	public LocalModel buildLocalModel(Context context) {
		return buildLocalModel(context, localModel);
	}

	/**
	 * Activate or deactivate the graphical scheduler. Allowing or denying the user
	 * to change the simulation speed.
	 *
	 * @param allow
	 */
	public void allowGraphicalScheduler(boolean allow) {
		if (!Configuration.commandLineMode) {
			((AmoebaMultiUIWindow)amasMultiUIWindow).schedulerToolbar.setDisable(!allow);
		}
	}

	@Override
	public void clearAgents() {
		List<Agent<? extends Amas<World>, World>> agents = getAgents();
		for (Agent<? extends Amas<World>, World> agent : agents) {
			AmoebaAgent amoebaAgent = (AmoebaAgent) agent;
			amoebaAgent.destroy();
		}
		this.head = null;
		super.removePendingAgents();
	}

	/**
	 * Called when a {@link IBackupSystem} has finished loading the amoeba.
	 */
	public void onLoadEnded() {
		super.addPendingAgents();
		nextCycleRunAllAgents();
		if(!Configuration.commandLineMode) {
			((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.update(getPercepts());
			updateAgentsVisualisation();
		}
	}

	@Override
	public void setCreationOfNewContext(boolean creationOfNewContext) {
		this.creationOfNewContext = creationOfNewContext;
	}

	@Override
	public void setHead(Head head) {
		this.head = head;
	}

	@Override
	public void setLocalModel(TypeLocalModel localModel) {
		this.localModel = localModel;
	}
	
	public void setReinforcement(boolean value) {
		reinforcementMode = value;
	}
	
	public boolean isReinforcement() {
		return reinforcementMode;
	}

	/**
	 * Activate or deactivate rendering of agents at runtime.
	 *
	 * @param renderUpdate
	 */
	public void setRenderUpdate(boolean renderUpdate) {
		if (!Configuration.commandLineMode) {
			this.renderUpdate = renderUpdate;
			((AmoebaMultiUIWindow)amasMultiUIWindow).toggleRender.setSelected(renderUpdate);
			if(renderUpdate == true)
				nextCycleRunAllAgents();
		}
	}

	/**
	 * Set input used by percepts and oracle.
	 *
	 * @param perceptionsAndActions
	 */
	public void setPerceptionsAndActionState(HashMap<String, Double> perceptionsAndActions) {
		this.perceptions = perceptionsAndActions;
	}

	/**
	 * Get the last prediction from the system.
	 *
	 * @return
	 */
	public double getAction() {
		return head.getAction();
	}
	
	
	
	public HashMap<String, Double> getHigherNeighborLastPredictionPercepts() {
		return head.getHigherNeighborLastPredictionPercepts();
	}

	public ArrayList<Context> getContexts() {
		ArrayList<Context> contexts = new ArrayList<>();
		for (Agent<? extends Amas<World>, World> agent : getAgents()) {
			if ((agent instanceof Context)) {
				contexts.add((Context) agent);
			}
		}
		return contexts;
	}

	public ArrayList<Head> getHeads() {
		ArrayList<Head> heads = new ArrayList<>();
		heads.add(head);
		return heads;
	}

	public ArrayList<Percept> getPercepts() {
		if(percepts == null || percepts.size()==0) {
			setPercepts();
		}	
		return percepts;
	}

	/**
	 * Get the value for a perception
	 * @param key key of the perception
	 * @return the value of the perception
	 */
	public Double getPerceptions(String key) {
		return this.perceptions.get(key);
	}

	public HashMap<String, Double> getPerceptions() {
		return this.perceptions;
	}

	@Override
	public boolean isCreationOfNewContext() {
		return creationOfNewContext;
	}

	/**
	 * Tell AMOEBA to run all (contexts) agent for the next cycle.
	 */
	public void nextCycleRunAllAgents() {
		runAll = true;
	}

	/**
	 * Is rendering activated ?
	 * @return
	 */
	public boolean isRenderUpdate() {
		return (!Configuration.commandLineMode) && renderUpdate;
	}

	/**
	 * Should AMOEBA use the oracle ? If false then AMOEBA will not learn.
	 * @return
	 */
	public boolean isUseOracle() {
		return useOracle;
	}
	
	/**
	 * Ask the agents to update their visualization, and update some UI element related to them.
	 */
	public void updateAgentsVisualisation() {
		for(Agent<? extends Amas<World>, World> a : getAgents()) {
			a.onUpdateRender();
		}
		((AmoebaMultiUIWindow)amasMultiUIWindow).point.move(((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1().getValue(), ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2().getValue());
		((AmoebaMultiUIWindow)amasMultiUIWindow).rectangle.setHeight(2*getEnvironment().getContextCreationNeighborhood(null, ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2()));
		((AmoebaMultiUIWindow)amasMultiUIWindow).rectangle.setWidth(2*getEnvironment().getContextCreationNeighborhood(null, ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1()));
		((AmoebaMultiUIWindow)amasMultiUIWindow).rectangle.move(((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1().getValue() - getEnvironment().getContextCreationNeighborhood(null, ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1()), ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2().getValue() - getEnvironment().getContextCreationNeighborhood(null, ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2()));
		((AmoebaMultiUIWindow)amasMultiUIWindow).mainVUI.updateCanvas();
		((AmoebaMultiUIWindow)amasMultiUIWindow).point.toFront();
		((AmoebaMultiUIWindow)amasMultiUIWindow).point.setInfo(getCursorInfo());
		if(getHeadAgent() != null){
			if(getHeadAgent().currentEndogenousRequest != null){
				((AmoebaMultiUIWindow)amasMultiUIWindow).rectangle.setInfo(""+getHeadAgent().currentEndogenousRequest.getType());
			}else{
				((AmoebaMultiUIWindow)amasMultiUIWindow).rectangle.setInfo(""+REQUEST.RDM);
			}
		}


	}
	
	/**
	 * The tool telling which dimension to display
	 * @return
	 */
	public DimensionSelector getDimensionSelector() {
		return ((AmoebaMultiUIWindow)amasMultiUIWindow).dimensionSelector;
	}
	
	/**
	 * Get the last perception.
	 * @return
	 */
	public HashMap<String, Double> getPerceptionsAndActionState() {
		return perceptions;
	}
	
	/**
	 * Set the studied system that will be used to learn with the internal scheduler.
	 * @param studiedSystem
	 */
	public void setStudiedSystem(StudiedSystem studiedSystem) {
		this.studiedSystem = studiedSystem;
	}
	
	public Head getHeadAgent() {
		return head;
	}
	
	public ArrayList<Context> getSpatiallyAlteredContextForUnityUI() {
		return spatiallyAlteredContext;
	}
	
	public void addSpatiallyAlteredContextForUnityUI(Context ctxt) {
		if(!ctxt.isFlat())
			spatiallyAlteredContext.add(ctxt);
	}
	
	public ArrayList<Context> getToKillContextsForUnityUI() {
		return toKillContexts;
	}
	
	public void addToKillContextForUnityUI(Context ctxt) {
		toKillContexts.add(ctxt);
	}
	
	public void addLastmodifiedContext(Context context) {
		if(!lastModifiedContext.contains(context)) {
			lastModifiedContext.add(context);
		}
	}
	
	public ArrayList<Context> getLastModifiedContexts(){
		return lastModifiedContext;
	}
	
	/**
	 * Adds the altered context.
	 *
	 * @param context the context
	 */
	public void addAlteredContext(Context context) {
		alteredContexts.add(context);
	}
	
	/**
	 * Return the current set of valid contexts.
	 *
	 * Synchronized with a readLock.
	 *
	 * @return
	 */
	public HashSet<Context> getValidContexts() {
		HashSet<Context> ret;
		validContextLock.readLock().lock();
		if (validContexts == null) {
			ret = null;
		} else {
			ret = new HashSet<>(validContexts);
		}
		validContextLock.readLock().unlock();
		return ret;
	}
	
	/**
	 * Update the set of valid context. The update is done with an intersect of the
	 * previous and new set.
	 *
	 * Synchronized with a writeLock.
	 * @param validContexts new validContexts set.
	 */
	@SuppressWarnings("unchecked")
	public void updateNeighborContexts(HashSet<Context> neighborContexts) {
		neighborContextsLock.writeLock().lock();
		if (this.neighborContexts == null) {
			this.neighborContexts = (HashSet<Context>) neighborContexts.clone();
		} else {
			this.neighborContexts.retainAll(neighborContexts);
		}
		neighborContextsLock.writeLock().unlock();
	}
	
	public HashSet<Context> getNeighborContexts() {
		HashSet<Context> ret;
		neighborContextsLock.readLock().lock();
		if (neighborContexts == null) {
			ret = null;
		} else {
			ret = new HashSet<>(neighborContexts);
		}
		neighborContextsLock.readLock().unlock();
		return ret;
	}
	
	/**
	 * Update the set of valid context. The update is done with an intersect of the
	 * previous and new set.
	 *
	 * Synchronized with a writeLock.
	 * @param validContexts new validContexts set.
	 */
	@SuppressWarnings("unchecked")
	public void updateValidContexts(HashSet<Context> validContexts) {
		validContextLock.writeLock().lock();
		if (this.validContexts == null) {
			this.validContexts = (HashSet<Context>) validContexts.clone();
		} else {
			this.validContexts.retainAll(validContexts);
		}
		validContextLock.writeLock().unlock();
	}
	
	private String getCursorInfo() {
		String message = "";
		for(Percept pct : getPercepts()) {
			message += pct.getName() + "\t" + pct.getValue() +"\t[ " + pct.getMin() +" ; " + pct.getMax() + " ]\n" ;
		}
		return message;
	}
	
	public void setPercepts() {
		percepts = new ArrayList<Percept>();
		for (Agent<? extends Amas<World>, World> agent : getAgents()) {
			if ((agent instanceof Percept)) {
				Percept p = (Percept) agent;
				if(!p.isDying()) {
					percepts.add(p);
				}
			}
		}
	}
	
	public void addPercept(Percept pct) {
		percepts = null;
	}
	
}
