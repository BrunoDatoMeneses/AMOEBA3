package kernel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
	private boolean creationOfNewContext = true;
	private boolean loadPresetContext = true;
	private boolean renderUpdate = false;

	private transient Drawable point;
	private transient ILxPlotChart loopNCS;
	private transient ILxPlotChart allNCS;
	private transient ILxPlotChart nbAgent;
	private transient ILxPlotChart errors;
	private transient JToggleButton toggleRender;
	private transient SchedulerToolbar schedulerToolbar;

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
	}

	/**
	 * Learn.
	 * 
	 * @param actions the actions
	 */
	@Override
	public void learn(HashMap<String, Double> perceptionsActionState) {
		setPerceptionsAndActionState(perceptionsActionState);
		this.cycle();
	}

	/**
	 * Request.
	 * 
	 * @param actions the actions
	 * @return the double
	 */
	@Override
	public double request(HashMap<String, Double> perceptionsActionState) {
		if (isUseOracle())
			head.changeOracleConnection();
		setPerceptionsAndActionState(perceptionsActionState);
		getScheduler().step();
		head.changeOracleConnection();
		return getAction();
	}

	public LocalModel buildLocalModel(Context context) {
		if (localModel == TypeLocalModel.MILLER_REGRESSION) {
			// @note (Labbeti) This constructor has changed because getPercept is not
			// initialized when we load agents from a file.
			// TODO (Labbeti) : change this with the new version of AMAK (when agents will be loaded
			// in the same cycle with addPendingAgents)
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

	public void allowGraphicalScheduler(boolean allow) {
		if (!Configuration.commandLineMode) {
			schedulerToolbar.getComponent(0).setEnabled(allow);
		}
	}

	public void clearAgents() {
		List<Agent<? extends Amas<World>, World>> agents = getAgents();
		for (Agent<? extends Amas<World>, World> agent : agents) {
			AmoebaAgent amoebaAgent = (AmoebaAgent) agent;
			amoebaAgent.destroy();
		}
		agents.clear();
	}

	public void setCreationOfNewContext(boolean creationOfNewContext) {
		this.creationOfNewContext = creationOfNewContext;
	}

	public void setHead(Head head) {
		this.head = head;
	}

	public void setLoadPresetContext(boolean loadPresetContext) {
		this.loadPresetContext = loadPresetContext;
	}

	@Override
	public void setLocalModel(TypeLocalModel localModel) {
		this.localModel = localModel;
	}

	public void setRenderUpdate(boolean renderUpdate) {
		if (!Configuration.commandLineMode) {
			this.renderUpdate = renderUpdate;
			toggleRender.setSelected(renderUpdate);
		}
	}

	public void setPerceptionsAndActionState(HashMap<String, Double> perceptionsAndActions) {
		this.perceptionsAndActionState = perceptionsAndActions;
	}

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

	public Double getPerceptionsOrAction(String key) {
		return this.perceptionsAndActionState.get(key);
	}

	public boolean isCreationOfNewContext() {
		return creationOfNewContext;
	}

	public boolean isLoadPresetContext() {
		return loadPresetContext;
	}

	public boolean isRenderUpdate() {
		return renderUpdate;
	}

	public boolean isUseOracle() {
		return useOracle;
	}
}
