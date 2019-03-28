package kernel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import agents.AmoebaAgent;
import agents.context.Context;
import agents.context.localModel.LocalModel;
import agents.context.localModel.LocalModelAverage;
import agents.context.localModel.LocalModelFirstExp;
import agents.context.localModel.LocalModelMillerRegression;
import agents.context.localModel.TypeLocalModel;
import agents.head.Head;
import agents.percept.Percept;
import fr.irit.smac.amak.Agent;
import fr.irit.smac.amak.Amas;
import fr.irit.smac.amak.Configuration;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.amak.tools.Log;
import fr.irit.smac.amak.ui.MainWindow;
import fr.irit.smac.amak.ui.SchedulerToolbar;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.Drawable;
import fr.irit.smac.lxplot.LxPlot;
import fr.irit.smac.lxplot.commons.ChartType;
import fr.irit.smac.lxplot.interfaces.ILxPlotChart;
import ncs.NCS;

public class AMOEBA extends Amas<World> implements IAMOEBA {
	// -- Attributes
	private Head head;
	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;
	private HashMap<String, Double> perceptionsAndActionState = new HashMap<String, Double>();
	private StudiedSystem studiedSystem;
	private boolean useOracle = true;
  
	private HashSet<Context> validContexts;
	private ReadWriteLock validContextLock = new ReentrantReadWriteLock();
	
	private boolean runAll = false;
	private boolean creationOfNewContext = true;
	private boolean renderUpdate = false;

	private Drawable point;
	private ILxPlotChart loopNCS;
	private ILxPlotChart allNCS;
	private ILxPlotChart nbAgent;
	private ILxPlotChart errors;
	private JToggleButton toggleRender;
	private SchedulerToolbar schedulerToolbar;

	/**
	 * Instantiates a new amoeba. Create an AMOEBA coupled with a studied system
	 * 
	 * @param studiedSystem the studied system
	 */
	public AMOEBA(World environment, StudiedSystem studiedSystem) {
		super(environment, Scheduling.HIDDEN);
		this.studiedSystem = studiedSystem;
	}

	@Override
	protected void onInitialConfiguration() {
	}

	@Override
	protected void onInitialAgentsCreation() {
	}

	@Override
	protected void onRenderingInitialization() {
		super.onRenderingInitialization();
		// scheduler toolbar
		schedulerToolbar = new SchedulerToolbar("AMOEBA", getScheduler());
		MainWindow.addToolbar(schedulerToolbar);

		// amoeba and agent
		VUI.get().setDefaultView(200, 0, 0);
		point = VUI.get().createPoint(0, 0);
		loopNCS = LxPlot.getChart("This loop NCS", ChartType.LINE, 1000);
		allNCS = LxPlot.getChart("All time NCS", ChartType.LINE, 1000);
		nbAgent = LxPlot.getChart("Number of agents", ChartType.LINE, 1000);
		errors = LxPlot.getChart("Errors", ChartType.LINE, 1000);

		// update render button
		toggleRender = new JToggleButton("Update Render");
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				int state = itemEvent.getStateChange();
				if (state == ItemEvent.SELECTED) {
					renderUpdate = true;
				} else {
					renderUpdate = false;
				}
			}
		};
		toggleRender.addItemListener(itemListener);
		toggleRender.setSelected(renderUpdate);

		JToolBar tb = new JToolBar();
		tb.add(toggleRender);
		MainWindow.addToolbar(tb);
	}

	@Override
	protected void onUpdateRender() {
		if (cycle % 1000 == 0) {
			Log.inform("AMOEBA", "Cycle " + cycle);
		}

		if (renderUpdate) {
			List<Percept> percepts = getPercepts();
			point.move(percepts.get(0).getValue(), percepts.get(1).getValue());

			HashMap<NCS, Integer> thisLoopNCS = environment.getThisLoopNCS();
			HashMap<NCS, Integer> allTimeNCS = environment.getAllTimeNCS();
			for (NCS ncs : NCS.values()) {
				loopNCS.add(ncs.name(), cycle, thisLoopNCS.get(ncs));
				allNCS.add(ncs.name(), cycle, allTimeNCS.get(ncs));
			}

			nbAgent.add("Percepts", cycle, getPercepts().size());
			nbAgent.add("Contexts", cycle, getContexts().size());
			nbAgent.add("Activated", cycle, environment.getNbActivatedAgent());

			errors.add("Mean criticity", cycle, head.getAveragePredictionCriticity());
			errors.add("Error Allowed", cycle, head.getErrorAllowed());
			errors.add("Inexact Allowed", cycle, head.getInexactAllowed());
			Vector<Double> sortedErrors = new Vector<>(head.getxLastCriticityValues());
			Collections.sort(sortedErrors);

			// @note (Labbeti) Test added to avoid crash when head has just been created.
			if (!sortedErrors.isEmpty()) {
				errors.add("Median criticity", cycle, sortedErrors.get(sortedErrors.size() / 2));
			}
		}
	}

	@Override
	protected void onSystemCycleBegin() {
		if (studiedSystem != null) {
			studiedSystem.playOneStep();
			perceptionsAndActionState = studiedSystem.getOutput();
		}
		environment.preCycleActions();
		head.clearAllUseableContextLists();
		validContexts = null;
		environment.resetNbActivatedAgent();
	}
	
	/**
	 * Define what is done during a cycle, 
	 * most importantly it launch agents.
	 * 
	 * Every 1000 cycles, all Context are launched, allowing
	 * delete themselves if they're too small. To change this behavior 
	 * this method. 
	 */
	@Override
	public void cycle() {
		cycle++;
		
		onSystemCycleBegin();
		
		//percepts
		List<Percept> synchronousPercepts = getPercepts().stream().filter(a -> a.isSynchronous())
				.collect(Collectors.toList());
		Collections.sort(synchronousPercepts, new AgentOrderComparator());

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
		if(cycle%1000 == 0) {
			runAll = true;
		}
		
		Stream<Context> contextStream = null;
		if(runAll) {
			contextStream = getContexts().stream(); //update all context
			runAll = false;
		} else {
			HashSet<Context> vcontexts = getValidContexts();
			if(vcontexts == null) {
				vcontexts = new HashSet<>();
			} 
			contextStream = vcontexts.stream(); //or only valid ones
		}
		List<Context> synchronousContexts = contextStream.filter(a -> a.isSynchronous())
				.collect(Collectors.toList());
		Collections.sort(synchronousContexts, new AgentOrderComparator());

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
		
		//head
		List<Head> heads = new ArrayList<>();
		heads.add(head);
		List<Head> synchronousHeads = heads.stream().filter(a -> a.isSynchronous())
				.collect(Collectors.toList());
		Collections.sort(synchronousHeads, new AgentOrderComparator());

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

		removePendingAgents();

		addPendingAgents();

		onSystemCycleEnd();
		
		if (!Configuration.commandLineMode)
			onUpdateRender();

	}

	@Override
	public void learn(HashMap<String, Double> perceptionsActionState) {
		setPerceptionsAndActionState(perceptionsActionState);
		this.cycle();
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

	public LocalModel buildLocalModel(Context context) {
		if (localModel == TypeLocalModel.MILLER_REGRESSION) {
			return new LocalModelMillerRegression(context.getRanges().size());
		}
		if (localModel == TypeLocalModel.FIRST_EXPERIMENT) {
			return new LocalModelFirstExp();
		}
		if (localModel == TypeLocalModel.AVERAGE) {
			return new LocalModelAverage();
		}
		return null;
	}

	/**
	 * Activate or deactivate the graphical scheduler. 
	 * Allowing ordDenying the user to change the simulation speed.
	 * @param allow
	 */
	public void allowGraphicalScheduler(boolean allow) {
		if (!Configuration.commandLineMode) {
			schedulerToolbar.getComponent(0).setEnabled(allow);
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
	
	public void onLoadEnded() {
		super.addPendingAgents();
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

	/**
	 * Activate or deactivate rendering of agents at runtime.
	 * @param renderUpdate
	 */
	public void setRenderUpdate(boolean renderUpdate) {
		if (!Configuration.commandLineMode) {
			this.renderUpdate = renderUpdate;
			toggleRender.setSelected(renderUpdate);
			nextCycleRunAllAgent();
		}
	}

	/**
	 * Set input used by percepts and oracle.
	 * @param perceptionsAndActions
	 */
	public void setPerceptionsAndActionState(HashMap<String, Double> perceptionsAndActions) {
		this.perceptionsAndActionState = perceptionsAndActions;
	}

	/**
	 * Get the last prediction from the system.
	 * @return
	 */
	public double getAction() {
		return head.getAction();
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
		ArrayList<Percept> percepts = new ArrayList<>();
		for (Agent<? extends Amas<World>, World> agent : getAgents()) {
			if ((agent instanceof Percept)) {
				percepts.add((Percept) agent);
			}
		}
		return percepts;
	}
	
	/**
	 * Update the set of valid context.
	 * The update is done with an intersect of the previous and new set.
	 * 
	 * Synchronized with a writeLock.
	 * @param new validContextsn set.
	 */
	public void updateValidContexts(HashSet<Context> validContexts){
		validContextLock.writeLock().lock();
		if(this.validContexts == null) {
			this.validContexts = validContexts;
		} else {
			this.validContexts.retainAll(validContexts);
		}
		validContextLock.writeLock().unlock();
	}
	
	/**
	 * Return the current set of valid contexts.
	 * 
	 * Synchronized with a readLock.
	 * @return
	 */
	public HashSet<Context> getValidContexts() {
		validContextLock.readLock().lock();
		HashSet<Context> ret = validContexts;
		validContextLock.readLock().unlock();
		return ret;
	}

	public Double getPerceptionsOrAction(String key) {
		return this.perceptionsAndActionState.get(key);
	}

	@Override
	public boolean isCreationOfNewContext() {
		return creationOfNewContext;
	}
	
	/**
	 * Tell AMOEBA to run all (contexts) agent for the next cycle.
	 */
	public void nextCycleRunAllAgent() {
		runAll = true;
	}

	public boolean isRenderUpdate() {
		return renderUpdate;
	}

	public boolean isUseOracle() {
		return useOracle;
	}
}
