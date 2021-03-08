package kernel;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import agents.EllsaAgent;
import agents.context.Context;
import agents.context.Experiment;
import agents.context.localModel.LocalModel;
import agents.context.localModel.TypeLocalModel;
import agents.head.EndogenousRequest;
import agents.head.Head;
import agents.head.REQUEST;
import agents.head.SITUATION;
import agents.percept.INPUT;
import agents.percept.Percept;
import experiments.nDimensionsLaunchers.F_N_Manager;
import experiments.nDimensionsLaunchers.PARAMS;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.tools.RunLaterHelper;
import fr.irit.smac.amak.ui.AmakPlot;
import fr.irit.smac.amak.ui.VUIMulti;
import gui.EllsaMultiUIWindow;
import gui.DimensionSelector;
import gui.DimensionSelector3D;
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
public class ELLSA extends Amas<World> implements IELLSA {
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
	
	public EllsaMultiUIWindow multiUIWindow;
	
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

	private HashSet<Context> subNeighborContexts ;
	private ReadWriteLock subNeighborContextsLock = new ReentrantReadWriteLock();
	
	public EllsaData data;
	private ArrayList<Percept> percepts;
	private ArrayList<Percept> subPercepts;
	private ArrayList<Percept> unconsideredPercepts;

	/**
	 * Instantiates a new, empty, amoeba.
	 * For ease of use, consider using {@link ELLSA#AMOEBA(String, StudiedSystem)}.
	 *
	 * @param studiedSystem
	 *            the studied system
	 */
	public ELLSA(EllsaMultiUIWindow window, VUIMulti vui) {
		super(window, vui, new World(), Scheduling.HIDDEN);
		vuiMulti = vui;
		multiUIWindow = window;
		subPercepts = new ArrayList<>();
		unconsideredPercepts = new ArrayList<>();
	}
	
	/**
	 * Intantiate a default amoeba from a config file.
	 * 
	 * @param path path to the config file.
	 */
	public ELLSA(EllsaMultiUIWindow window, VUIMulti vui, String path, StudiedSystem studiedSystem) {
		super(window, vui, new World(), Scheduling.HIDDEN);
		vuiMulti = vui;
		this.studiedSystem = studiedSystem;
		setRenderUpdate(true);
		saver = new SaveHelperImpl(this, window);
		saver.load(path);
		multiUIWindow = window;


		for(int i =0 ; i< 25;i++) {
			data.executionTimesSums[i]=0.0;
		}
	}

	@Override
	protected void onInitialConfiguration() {
		super.onInitialConfiguration();
		if(Configuration.allowedSimultaneousAgentsExecution != 1) {
			PrintOnce.print("Warning ! Multithreading is not currently sopported !\n"
					+ "Please use Configuration.allowedSimultaneousAgentsExecution=1");
		}
		getEnvironment().setEllsa(this);
		data = new EllsaData();
		for(REQUEST rqt : REQUEST.values()){
			data.requestCounts.put(rqt, 0);
		}
		for(SITUATION rqt : SITUATION.values()){
			data.situationsCounts.put(rqt, 0);
		}



	}
	
	@Override
	protected void onRenderingInitialization() {
		((EllsaMultiUIWindow) amasMultiUIWindow).initialize(this);
	}

	@Override
	protected void onUpdateRender() {
		// Update statistics
		if(amasMultiUIWindow!=null) {

			if(getCycle()==1){
				for(int i =0;i<getPercepts().size();i++){
					multiUIWindow.VUInDimensions.createAndAddString(10, 10 + getPercepts().size()*10 - 10*i,getPercepts().get(i).getName()).setFixed().setShowInExplorer(false);
				}
			}

			AmakPlot loopNCS = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("This loop NCS");
			AmakPlot allNCS = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("All time NCS");
			AmakPlot nbAgent = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Number of agents");
			AmakPlot errors = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Errors");
			AmakPlot distancesToModels = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Distances to models");
			AmakPlot gloabalMappingCriticality = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Global Mapping Criticality");
			AmakPlot timeExecution = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Time Execution");
			AmakPlot criticalities = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Criticalities");
			AmakPlot nbNeighbors = ((EllsaMultiUIWindow)amasMultiUIWindow).getPlot("Number of neighbors");
			
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
			
			timeExecution.addData("CycleBegin", cycle, data.executionTimesSums[0], notify);
			timeExecution.addData("Percepts", cycle, data.executionTimesSums[1], notify);
			timeExecution.addData("Contexts", cycle, data.executionTimesSums[2], notify);
			timeExecution.addData("Head", cycle, data.executionTimesSums[3], notify);
			timeExecution.addData("computePendingAgents", cycle, data.executionTimesSums[4], notify);
			timeExecution.addData("CycleEnd", cycle, data.executionTimesSums[5], notify);

			timeExecution.addData("HeadPlay", cycle, data.executionTimesSums[6], notify);
			timeExecution.addData("HeadOther", cycle, data.executionTimesSums[7], notify);

			timeExecution.addData("NCS", cycle, data.executionTimesSums[8], notify);

			timeExecution.addData("NCS_Uslessness", cycle, data.executionTimesSums[9], notify);
			timeExecution.addData("NCS_IncompetendHead", cycle, data.executionTimesSums[10], notify);
			timeExecution.addData("NCS_ConcurrenceAndConflict", cycle, data.executionTimesSums[11], notify);
			timeExecution.addData("NCS_Create_New_Context", cycle, data.executionTimesSums[12], notify);
			timeExecution.addData("NCS_Overmapping", cycle, data.executionTimesSums[13], notify);
			timeExecution.addData("NCS_ChildContext", cycle, data.executionTimesSums[14], notify);
			timeExecution.addData("NCS_PotentialRequest", cycle, data.executionTimesSums[15], notify);
			timeExecution.addData("NCS_Dream", cycle, data.executionTimesSums[16], notify);

			timeExecution.addData("NCS_NewCtxt_getBestNeighbor", cycle, data.executionTimesSums[17], notify);
			timeExecution.addData("NCS_NewCtxt_CreationWithNeighbor", cycle, data.executionTimesSums[18], notify);
			timeExecution.addData("NCS_NewCtxt_CreationAlone", cycle, data.executionTimesSums[19], notify);

			timeExecution.addData("NCS_NewCtxt_BuildAlone", cycle, data.executionTimesSums[20], notify);
			timeExecution.addData("NCS_NewCtxt_BeforeBuildAlone", cycle, data.executionTimesSums[21], notify);
			timeExecution.addData("NCS_NewCtxt_AfterBuildAlone", cycle, data.executionTimesSums[22], notify);
			timeExecution.addData("NCS_NewCtxt_LogBuildAlone", cycle, data.executionTimesSums[23], notify);
			timeExecution.addData("NCS_NewCtxt_RAISEAlone", cycle, data.executionTimesSums[24], notify);
			
			criticalities.addData("Prediction", cycle, data.evolutionCriticalityPrediction, notify);
			criticalities.addData("Mapping", cycle, data.evolutionCriticalityMapping, notify);
			criticalities.addData("Confidence", cycle, data.evolutionCriticalityConfidence, notify);

			nbNeighbors.addData("Neighbors", cycle, data.neighborsCounts, notify);
		}
		
		if (isRenderUpdate()) {
			((EllsaMultiUIWindow)amasMultiUIWindow).mainVUI.updateCanvas();
			((EllsaMultiUIWindow)amasMultiUIWindow).VUInDimensions.updateCanvas();
			updateAgentsVisualisation();
			RunLaterHelper.runLater(() -> {resetCycleWithoutRender();});
		}

		if(getCycle()%data.STOP_UI_cycle==0 && data.STOP_UI){
			getScheduler().stop();
			System.out.println(getHeadAgent().getMappingScores());
			System.out.println(data.requestCounts);
			System.out.println(data.situationsCounts);
		}
	}

	@Override
	public void onSystemCycleBegin() {



		data.executionTimes[0]=System.nanoTime();

		cycle++;
		if (cycle % 1000 == 0) {
			//Log.defaultLog.inform("AMOEBA", "Cycle " + cycle + ". Nb agents: "+getAgents().size());
		}
		if(studiedSystem != null){
			((F_N_Manager)studiedSystem).cycle = cycle;
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
			

			if(PARAMS.setActiveExploitation){
				if(cycle< PARAMS.nbLearningCycle){
					if(perceptions.get("oracle")==null) {
						data.useOracle = false;
						data.currentINPUT = INPUT.ENDOGENOUS_EXPLOITATION;

					}else {
						data.useOracle = true;
						data.currentINPUT = INPUT.EXOGENOUS_LEARNING_SITUATION;
					}
				}else {
					if (perceptions.get("oracle") == null) {
						data.useOracle = false;
						data.currentINPUT = INPUT.ENDOGENOUS_EXPLOITATION;

					} else {
						data.useOracle = false;
						data.currentINPUT = INPUT.EXOGENOUS_EXPLOITATION;

					}
				}
			}else{
				if(perceptions.get("oracle")==null) {
					data.useOracle = false;
					data.currentINPUT = INPUT.ENDOGENOUS_EXPLOITATION;

				}else {
					data.useOracle = true;
					data.currentINPUT = INPUT.EXOGENOUS_LEARNING_SITUATION;
				}
			}



			
		}
		
		environment.preCycleActions();
		head.clearAllUseableContextLists();
		validContexts = null;
		neighborContexts = null;
		subNeighborContexts = null;
		environment.resetNbActivatedAgent();
		spatiallyAlteredContext.clear();
		toKillContexts.clear();
		lastModifiedContext.clear();
		alteredContexts.clear();
		data.higherNeighborLastPredictionPercepts=null;

		data.executionTimes[0]=System.nanoTime()- data.executionTimes[0];
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

		data.executionTimes[5]=System.nanoTime();

		
		if(studiedSystem != null) {
			if(data.selfLearning) {
				data.selfLearning = false;
				studiedSystem.setSelfLearning(true);
				studiedSystem.setSelfRequest(head.getSelfRequest());
				data.situationsCounts.put(SITUATION.ACTIVE_EXPLOITATION,data.situationsCounts.get(SITUATION.ACTIVE_EXPLOITATION)+1);
				data.situationsCounts.put(SITUATION.ENDOGENOUS_EXPLOITATION,data.situationsCounts.get(SITUATION.ENDOGENOUS_EXPLOITATION)+1);
				 
			}
			else if(data.activeLearning) {
				data.activeLearning = false;
				studiedSystem.setActiveLearning(true);
				studiedSystem.setSelfRequest(head.getActiveRequest());
				data.situationsCounts.put(SITUATION.ACTIVE_LEARNING,data.situationsCounts.get(SITUATION.ACTIVE_LEARNING)+1);
				data.situationsCounts.put(SITUATION.EXOGENOUS_LEARNING,data.situationsCounts.get(SITUATION.EXOGENOUS_LEARNING)+1);
			}else{


				if(cycle<PARAMS.nbLearningCycle){
					data.situationsCounts.put(SITUATION.RDM_LEARNING,data.situationsCounts.get(SITUATION.RDM_LEARNING)+1);
					data.situationsCounts.put(SITUATION.EXOGENOUS_LEARNING,data.situationsCounts.get(SITUATION.EXOGENOUS_LEARNING)+1);
				}else{
					data.situationsCounts.put(SITUATION.RDM_EXPLOITATION,data.situationsCounts.get(SITUATION.RDM_EXPLOITATION)+1);
					data.situationsCounts.put(SITUATION.EXOGENOUS_EXPLOITATION,data.situationsCounts.get(SITUATION.EXOGENOUS_EXPLOITATION)+1);
				}

			}
		}
		
		super.onSystemCycleEnd();
		if(saver != null)
			saver.autosave();

		data.executionTimes[5]=System.nanoTime()- data.executionTimes[5];
		for(int i = 0 ; i<25;i++) {
			data.executionTimesSums[i] += data.executionTimes[i]/1000000;
		}



		getEnvironment().print(TRACE_LEVEL.INFORM, "Number of Agents : ",getContexts().size());

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
		data.executionTimes[4]=System.nanoTime();
		removePendingAgents();
		addPendingAgents();
		data.executionTimes[4]=System.nanoTime()- data.executionTimes[4];
	}

	private void runAgents() {
		// run percepts
		data.executionTimes[1]=System.nanoTime();
		Stream<Context> contextStream = runPercepts();
		data.executionTimes[1]=System.nanoTime()- data.executionTimes[1];

		// run contexts
		data.executionTimes[2]=System.nanoTime();
		runContexts(contextStream);
		data.executionTimes[2]=System.nanoTime()- data.executionTimes[2];

		// run head
		data.executionTimes[3]=System.nanoTime();
		runHeads();
		data.executionTimes[3]=System.nanoTime()- data.executionTimes[3];
	}




	private void runHeads() {
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

	private void runContexts(Stream<Context> contextStream) {
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
	}

	private Stream<Context> runPercepts() {
		List<Percept> synchronousPercepts;


		if(data.isSubPercepts){
			synchronousPercepts = getSubPercepts().stream().filter(a -> a.isSynchronous())
					.collect(Collectors.toList());
		}else{
			synchronousPercepts = getPercepts().stream().filter(a -> a.isSynchronous())
					.collect(Collectors.toList());
		}

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
			//HashSet<Context> contextsToBeAwaken = getValidContexts();
			HashSet<Context> contextsToBeAwaken = getNeighborContexts();
			if (contextsToBeAwaken == null) {
				contextsToBeAwaken = new HashSet<>();
			}
			contextStream = contextsToBeAwaken.stream(); // or only valid ones
		}

		//getHeadAgent().setActivatedNeighborsContexts(new ArrayList<>(getNeighborContexts()));
		getHeadAgent().setActivatedSubNeighborsContexts(new ArrayList<>(getSubNeighborContexts()));

		return contextStream;
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
					getEnvironment().print(TRACE_LEVEL.ERROR, "[AMAS GUI]", "Failed to wait for GUI update to finish.");
					//Log.defaultLog.error("[AMAS GUI]", "Failed to wait for GUI update to finish.");
					e.printStackTrace();
				}
				// now the queue should be clear
			}
		}


	}

	@Override
	public HashMap<String, Double> learn(HashMap<String, Double> perceptionsActionState) {
		data.currentINPUT = INPUT.EXOGENOUS_LEARNING_SITUATION;
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		studiedSystem = ss;

		getEnvironment().print(TRACE_LEVEL.INFORM,"LEARN");
		while(data.selfLearning && data.PARAM_isAutonomousMode) {
			data.selfLearning = false;
			endogenousRequest(convertRequestPerceptToString(head.getSelfRequest()));
		}




		return null;
	}

	/*public HashMap<String, Double> learn(HashMap<String, Double> perceptionsActionState, ArrayList<String> unconsideredPerceptsString) {
		unconsideredPercepts = convertStringToPercept(unconsideredPerceptsString);
		subPercepts = new ArrayList<>(percepts);
		subPercepts.removeAll(unconsideredPercepts);
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		studiedSystem = ss;

		while(data.selfLearning && data.isAutonomousMode) {
			data.selfLearning = false;
			request(convertRequestPerceptToString(head.getSelfRequest()));
		}



		return null;
	}*/

	public void setSubPercepts(ArrayList<String> unconsideredPerceptsString){
		unconsideredPercepts = convertStringToPercept(unconsideredPerceptsString);
		subPercepts = new ArrayList<>(percepts);
		subPercepts.removeAll(unconsideredPercepts);
	}




	private HashMap<String, Double> convertRequestPerceptToString(HashMap<Percept, Double> selfRequest) {
		HashMap<String,Double> newRequest = new HashMap<String,Double>();

		for(Percept pct : selfRequest.keySet()) {
			newRequest.put(pct.getName(), selfRequest.get(pct));
		}
		return newRequest;
	}

	private ArrayList<Percept> convertStringToPercept(ArrayList<String> perceptsStrings) {
		ArrayList<Percept> perceptsToReturn = new ArrayList<>();

		for(Percept pct : getPercepts()) {
			for(String pctName : perceptsStrings){
				if(pct.getName().equals(pctName)){
					perceptsToReturn.add(pct);
				}
			}

		}

		return perceptsToReturn;
	}

	@Override
	public double request(HashMap<String, Double> perceptionsActionState) {
		data.currentINPUT = INPUT.EXOGENOUS_EXPLOITATION;
		boolean usingOracle = isUseOracle();
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		data.useOracle = false;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		if (usingOracle)
			data.useOracle = true;
		studiedSystem = ss;
		double action = getAction();

		getEnvironment().print(TRACE_LEVEL.INFORM,"REQUEST");

		while(data.selfLearning && data.PARAM_isAutonomousMode && data.PARAM_isExploitationActive) {
			data.selfLearning = false;
			endogenousRequest(convertRequestPerceptToString(head.getSelfRequest()));
		}

		return action;
	}

	public double endogenousRequest(HashMap<String, Double> perceptionsActionState) {
		data.currentINPUT = INPUT.ENDOGENOUS_EXPLOITATION;
		boolean usingOracle = isUseOracle();
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		data.useOracle = false;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		if (usingOracle)
			data.useOracle = true;
		studiedSystem = ss;
		double action = getAction();
		getEnvironment().print(TRACE_LEVEL.INFORM,"ENDO REQUEST");
		while(data.selfLearning && data.PARAM_isAutonomousMode) {
			data.selfLearning = false;
			endogenousRequest(convertRequestPerceptToString(head.getSelfRequest()));
		}


		return action;
	}

	public HashMap<String,Double> requestWithLesserPercepts(HashMap<String, Double> perceptionsActionState) {
		HashMap<String,Double> actions = new HashMap<>();
		boolean usingOracle = isUseOracle();
		if (usingOracle)
			head.changeOracleConnection();
		StudiedSystem ss = studiedSystem;
		studiedSystem = null;
		/*unconsideredPercepts = convertStringToPercept(unconsideredPerceptsString);
		subPercepts = new ArrayList<>(percepts);
		subPercepts.removeAll(unconsideredPercepts);*/
		data.isSubPercepts = true;
		setPerceptionsAndActionState(perceptionsActionState);
		cycle();
		if (usingOracle)
			head.changeOracleConnection();
		studiedSystem = ss;
		actions.put("action",getAction());
		for(Percept pct : unconsideredPercepts){
			actions.put(pct.getName(), data.nonCondireredPerceptsSyntheticValues.get(pct));
		}
		data.isSubPercepts = false;


		getEnvironment().print(TRACE_LEVEL.INFORM,"SUB REQUEST");
		return actions;
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
			((EllsaMultiUIWindow)amasMultiUIWindow).schedulerToolbar.setDisable(!allow);
		}
	}

	@Override
	public void clearAgents() {
		List<Agent<? extends Amas<World>, World>> agents = getAgents();
		for (Agent<? extends Amas<World>, World> agent : agents) {
			EllsaAgent ellsaAgent = (EllsaAgent) agent;
			ellsaAgent.destroy();
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
			((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.update(getPercepts());
			((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector3D.update(getPercepts());
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
			((EllsaMultiUIWindow)amasMultiUIWindow).toggleRender.setSelected(renderUpdate);
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

	public ArrayList<Percept> getSubPercepts() {
		return subPercepts;
	}

	public ArrayList<Percept> getUnconsideredPercepts() {
		return unconsideredPercepts;
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
		double d1DimensionSelector = ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1().getValue();
		double d2DimensionSelector = ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2().getValue();
		((EllsaMultiUIWindow)amasMultiUIWindow).point.move(d1DimensionSelector, d2DimensionSelector);
		((EllsaMultiUIWindow)amasMultiUIWindow).pointHorizontalLine.move(d1DimensionSelector-10000,d2DimensionSelector);
		((EllsaMultiUIWindow)amasMultiUIWindow).pointVerticalLine.move(d1DimensionSelector,d2DimensionSelector-10000);
		((EllsaMultiUIWindow)amasMultiUIWindow).rectangle.setHeight(2*getEnvironment().getContextNeighborhoodRadius(null, ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2()));
		((EllsaMultiUIWindow)amasMultiUIWindow).rectangle.setWidth(2*getEnvironment().getContextNeighborhoodRadius(null, ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1()));
		((EllsaMultiUIWindow)amasMultiUIWindow).rectangle.move(d1DimensionSelector - getEnvironment().getContextNeighborhoodRadius(null, ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d1()), d2DimensionSelector - getEnvironment().getContextNeighborhoodRadius(null, ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector.d2()));
		((EllsaMultiUIWindow)amasMultiUIWindow).mainVUI.updateCanvas();
		((EllsaMultiUIWindow)amasMultiUIWindow).VUInDimensions.updateCanvas();
		((EllsaMultiUIWindow)amasMultiUIWindow).point.toFront();
		((EllsaMultiUIWindow)amasMultiUIWindow).pointHorizontalLine.toFront();
		((EllsaMultiUIWindow)amasMultiUIWindow).pointVerticalLine.toFront();
		((EllsaMultiUIWindow)amasMultiUIWindow).point.setInfo(getCursorInfo());
		if(getHeadAgent() != null){
			if(getHeadAgent().currentEndogenousRequest != null){
				((EllsaMultiUIWindow)amasMultiUIWindow).rectangle.setInfo(""+getHeadAgent().currentEndogenousRequest.getType());
			}else{
				((EllsaMultiUIWindow)amasMultiUIWindow).rectangle.setInfo(""+SITUATION.RDM_LEARNING);
			}
		}


		if(getCycle()>0 && cycle % multiUIWindow.guiData.nbCycleRefresh3DView == 0){


			if(multiUIWindow.view3D != null){
				multiUIWindow.view3D.updateContextChart();
				multiUIWindow.view3DContexts.updateContextChart();
			}


		}

		/*try
		{
			Thread.sleep(1000);
		}
		catch(InterruptedException ex)
		{
			Thread.currentThread().interrupt();
		}*/
	}
	
	/**
	 * The tool telling which dimension to display
	 * @return
	 */
	public DimensionSelector getDimensionSelector() {
		return ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector;
	}

	public DimensionSelector3D getDimensionSelector3D() {
		return ((EllsaMultiUIWindow)amasMultiUIWindow).dimensionSelector3D;
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
		//if(!ctxt.isFlat())
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

	public HashSet<Context> getSubNeighborContexts() {
		HashSet<Context> ret;
		subNeighborContextsLock.readLock().lock();
		if (subNeighborContexts == null) {
			ret = null;
		} else {
			ret = new HashSet<>(subNeighborContexts);
		}
		subNeighborContextsLock.readLock().unlock();
		return ret;
	}

	public void updateSubNeighborContexts(HashSet<Context> subNeighborContexts) {
		subNeighborContextsLock.writeLock().lock();
		if (this.subNeighborContexts == null) {
			this.subNeighborContexts = (HashSet<Context>) subNeighborContexts.clone();
		} else {
			this.subNeighborContexts.retainAll(subNeighborContexts);
		}
		subNeighborContextsLock.writeLock().unlock();
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

	public Experiment getCurrentExperimentWithoutProposition() {
		ArrayList<Percept> percepts = getPercepts();
		Experiment exp = new Experiment(null);
		for (Percept pct : percepts) {
			exp.addDimension(pct, pct.getValue());
		}

		return exp;
	}

	public EndogenousRequest getSubrequest(){
		if(!getHeadAgent().endogenousSubRequests.isEmpty()){
			//System.out.println(getHeadAgent().endogenousSubRequests);
			System.out.println("endogenousSubRequests " + getHeadAgent().endogenousSubRequests.size());
			EndogenousRequest endoRequest = getHeadAgent().endogenousSubRequests.poll();

			return endoRequest;
		}else{
			return null;
		}

	}

	public void resetSubrequest(){
		if(!getHeadAgent().endogenousSubRequests.isEmpty()) {
			getHeadAgent().endogenousSubRequests.clear();
		}

	}

	public void updateMinAndMaxRegressionCoefs(double minCoef, double maxCoef,double confidence){

		if(!(data.maxModelCoef == null || data.minModelCoef == null)){
			getEnvironment().print(TRACE_LEVEL.DEBUG,"UPDATE min and max coefs before","min",data.minModelCoef,"max", data.maxModelCoef);
		}


		if(data.maxModelCoef == null || data.minModelCoef == null){

			data.maxModelCoef = maxCoef;
			data.minModelCoef = minCoef;
			getEnvironment().print(TRACE_LEVEL.DEBUG,"UPDATE min and max coefs new","min",minCoef,"max", maxCoef);
		}else{
			double halfConfidence = 0.5 *confidence;
			getEnvironment().print(TRACE_LEVEL.DEBUG,"UPDATE min and max coefs new","min",minCoef,"max", maxCoef,"halfConfidence",halfConfidence);
			data.maxModelCoef = halfConfidence*maxCoef + ((1-halfConfidence)*data.maxModelCoef);
			data.minModelCoef = halfConfidence*minCoef + ((1-halfConfidence)*data.minModelCoef);
		}
		getEnvironment().print(TRACE_LEVEL.DEBUG,"UPDATE min and max coefs after","min",data.minModelCoef,"max", data.maxModelCoef);
		/*if(value>data.maxModelCoef){
			data.maxModelCoef = value;
			getEnvironment().print(TRACE_LEVEL.DEBUG,"minRegressionCoef",data.minModelCoef,"maxRegressionCoef",data.maxModelCoef);
		}
		if(value<data.minModelCoef){
			data.minModelCoef = value;
			getEnvironment().print(TRACE_LEVEL.DEBUG,"minRegressionCoef",data.minModelCoef,"maxRegressionCoef",data.maxModelCoef);
		}*/
	}
	
}
