package kernel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

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

	private Head head;

	private TypeLocalModel localModel = TypeLocalModel.MILLER_REGRESSION;

	private HashMap<String, Double> perceptionsAndActionState = new HashMap<String, Double>();

	private StudiedSystem studiedSystem;

	private boolean useOracle = true;

	// Imported from World -----------
	private boolean creationOfNewContext;
	private boolean loadPresetContext;
	public int testValue = 0;
	// --------------------------------

	private File ressourceFile;

	private Drawable point;
	private ILxPlotChart loopNCS;
	private ILxPlotChart allNCS;
	private ILxPlotChart nbAgent;
	private ILxPlotChart errors;
	private JToggleButton toggleRender;
	private SchedulerToolbar schedulerToolbar;

	private boolean noRenderUpdate = false;

	/**
	 * Instantiates a new amoeba. Create an AMOEBA coupled with a studied system
	 * 
	 * @param studiedSystem
	 *            the studied system
	 */
	public AMOEBA(World environment, File ressourceFile,
			StudiedSystem studiedSystem) {
		super(environment, Scheduling.HIDDEN, ressourceFile, studiedSystem);
	}

	@Override
	protected void onInitialConfiguration() {
		ressourceFile = (File) params[0];
		studiedSystem = (StudiedSystem) params[1];
	}

	@Override
	protected void onInitialAgentsCreation() {
		readRessourceFile(ressourceFile);

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
	 * @param actions
	 *            the actions
	 */
	public void learn(HashMap<String, Double> perceptionsActionState) {
		setPerceptionsAndActionState(perceptionsActionState);
		this.cycle();
	}

	/**
	 * Request.
	 * 
	 * @param actions
	 *            the actions
	 * @return the double
	 */
	public double request(HashMap<String, Double> perceptionsActionState) {
		if (isUseOracle())
			head.changeOracleConnection();
		setPerceptionsAndActionState(perceptionsActionState);
		getScheduler().step();
		head.changeOracleConnection();
		return getAction();
	}

	public double getAction() {
		return head.getAction();
	}

	public boolean isUseOracle() {
		return useOracle;
	}

	public void setPerceptionsAndActionState(
			HashMap<String, Double> perceptionsAndActions) {
		this.perceptionsAndActionState = perceptionsAndActions;
	}

	public Double getPerceptionsOrAction(String key) {
		return this.perceptionsAndActionState.get(key);
	}

	/**
	 * Read resource file and generate the AMOEBA described.
	 * 
	 * @param systemFile
	 *            the file XML file describing the AMOEBA.
	 */
	private void readRessourceFile(File systemFile) {
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			System.out.println(systemFile);
			document = sxb.build(systemFile);
			Element racine = document.getRootElement();
			System.out.println(racine.getName());

			creationOfNewContext = Boolean.parseBoolean(racine
					.getChild("Configuration").getChild("Learning")
					.getAttributeValue("creationOfNewContext"));
			loadPresetContext = Boolean.parseBoolean(racine
					.getChild("Configuration").getChild("Learning")
					.getAttributeValue("loadPresetContext"));

			// Initialize the sensor agents
			for (Element element : racine.getChild("StartingAgents")
					.getChildren("Sensor")) {
				Percept s = new Percept(this);
				s.setName(element.getAttributeValue("Name"));
			}

			// Initialize the controller agents
			for (Element element : racine.getChild("StartingAgents")
					.getChildren("Controller")) {
				Head a = new Head(this);
				a.setName(element.getAttributeValue("Name"));
				System.out.print("CREATION OF CONTEXT : "
						+ this.creationOfNewContext);
				a.setNoCreation(!creationOfNewContext);
				this.head = a;
			}

			/* Load preset context if no learning required */
			if (loadPresetContext) {

				for (Element element : racine.getChild("PresetContexts")
						.getChildren("Context")) {

					double[] start, end;
					int[] n;
					String[] percepts;

					double action;
					start = new double[element.getChildren("Range").size()];
					end = new double[element.getChildren("Range").size()];
					n = new int[element.getChildren("Range").size()];
					percepts = new String[element.getChildren("Range").size()];

					int i = 0;
					for (Element elem : element.getChildren("Range")) {
						start[i] = Double.parseDouble(elem
								.getAttributeValue("start"));
						end[i] = Double.parseDouble(elem
								.getAttributeValue("end"));
						n[i] = Integer.parseInt(elem.getAttributeValue("n"));
						percepts[i] = elem.getAttributeValue("Name");
						i++;
					}
					action = Double.parseDouble(element
							.getAttributeValue("Action"));

					Head c = head;

					createPresetContext(start, end, n, new int[0], 0, action,
							c, percepts);
				}

			}
		} catch (JDOMException | IOException e) {
			e.printStackTrace();
		}
	}

	private void createPresetContext(double[] start, double[] end, int[] n,
			int[] pos, int iteration, double action, Head controller,
			String[] percepts) {
		/*
		 * TODO Hugo says : There was some code here without impact, there was a
		 * comment saying "broken by criterion". I was not able to fix it, so I
		 * put an error message. If you need this, please check the original
		 * project (AMOEBA3)
		 */
		System.err
				.println("AMOEBA.createPresetContext (previously World.createPresetContext) is no longer supported");
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

	public ArrayList<Context> getContexts() {
		ArrayList<Context> contexts = new ArrayList<>();
		for (Agent<? extends Amas<World>, World> agent : getAgents()) {
			if ((agent instanceof Context)) {
				contexts.add((Context) agent);
			}
		}
		return contexts;
	}

	public LocalModel buildLocalModel(Context context) {

		if (localModel == TypeLocalModel.MILLER_REGRESSION) {
			return new LocalModelMillerRegression(this);
		}
		if (localModel == TypeLocalModel.FIRST_EXPERIMENT) {
			return new LocalModelFirstExp();
		}
		if (localModel == TypeLocalModel.AVERAGE) {
			return new LocalModelAverage();
		}
		return null;
	}

	@Override
	public void setLocalModel(TypeLocalModel localModel) {
		this.localModel = localModel;
	}

	@Override
	public void setDataForErrorMargin(double errorAllowed,
			double augmentationFactorError, double diminutionFactorError,
			double minErrorAllowed, int nConflictBeforeAugmentation,
			int nSuccessBeforeDiminution) {
		head.setDataForErrorMargin(errorAllowed, augmentationFactorError,
				diminutionFactorError, minErrorAllowed,
				nConflictBeforeAugmentation, nSuccessBeforeDiminution);
	}

	@Override
	public void setDataForInexactMargin(double inexactAllowed,
			double augmentationInexactError, double diminutionInexactError,
			double minInexactAllowed, int nConflictBeforeInexactAugmentation,
			int nSuccessBeforeInexactDiminution) {
		head.setDataForInexactMargin(inexactAllowed, augmentationInexactError,
				diminutionInexactError, minInexactAllowed,
				nConflictBeforeInexactAugmentation,
				nSuccessBeforeInexactDiminution);
	}

	public void setNoRenderUpdate(boolean noRenderUpdate) {
		if (!Configuration.commandLineMode) {
			this.noRenderUpdate = noRenderUpdate;
			toggleRender.setSelected(noRenderUpdate);
		}
	}

	public boolean isNoRenderUpdate() {
		return noRenderUpdate;
	}

	public void allowGraphicalScheduler(boolean allow) {
		if (!Configuration.commandLineMode) {
			schedulerToolbar.getComponent(0).setEnabled(allow);
		}
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
		toggleRender = new JToggleButton("No Update Render");
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				int state = itemEvent.getStateChange();
				if (state == ItemEvent.SELECTED) {
					noRenderUpdate = true;
				} else {
					noRenderUpdate = false;
				}
			}
		};
		toggleRender.setSelected(noRenderUpdate);
		toggleRender.addItemListener(itemListener);
		JToolBar tb = new JToolBar();
		tb.add(toggleRender);
		MainWindow.addToolbar(tb);
	}

	protected void onUpdateRender() {
		if (cycle % 1000 == 0)
			Log.inform("AMOEBA", "Cycle " + cycle);
		if (!noRenderUpdate) {
			ArrayList<Percept> percepts = getPercepts();
			point.move(percepts.get(0).getValue(), percepts.get(1).getValue());

			HashMap<NCS, Integer> thisLoopNCS = environment.getThisLoopNCS();
			HashMap<NCS, Integer> allTimeNCS = environment.getAllTimeNCS();
			for (NCS ncs : NCS.values()) {
				loopNCS.add(ncs.name(), cycle, thisLoopNCS.get(ncs));
				allNCS.add(ncs.name(), cycle, allTimeNCS.get(ncs));
			}

			nbAgent.add("Percepts", cycle, getPercepts().size());
			nbAgent.add("Contexts", cycle, getContexts().size());

			errors.add("Mean criticity", cycle,
					head.getAveragePredictionCriticity());
			errors.add("Error Allowed", cycle, head.getErrorAllowed());
			errors.add("Inexact Allowed", cycle, head.getInexactAllowed());
			Vector<Double> sortedErrors = new Vector<>(
					head.getxLastCriticityValues());
			Collections.sort(sortedErrors);
			errors.add("Median criticity", cycle,
					sortedErrors.get(sortedErrors.size() / 2));
		}
	}

	public boolean isCreationOfNewContext() {
		return creationOfNewContext;
	}

	public boolean isLoadPresetContext() {
		return loadPresetContext;
	}
}
