package agents.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import agents.AmoebaAgent;
import agents.context.localModel.LocalModel;
import agents.head.Head;
import agents.percept.Percept;
import gui.RenderStrategy;
import gui.context.ContextRendererFX;
import kernel.AMOEBA;
import ncs.NCS;

/**
 * The core agent of AMOEBA.
 * 
 */
public class Context extends AmoebaAgent {
	// STATIC ---
	public static Class<? extends RenderStrategy> defaultRenderStrategy =  ContextRendererFX.class;
	// ----------
	
	private static final long serialVersionUID = 1L;
	private Head headAgent;
	private HashMap<Percept, Range> ranges = new HashMap<Percept, Range>();
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();
	private LocalModel localModel;
	private double confidence = 0;
	
	/**
	 * The number of time the context was activated (present in validContext).
	 * Used for visualization.
	 */
	private int activations = 0;

	public Context(AMOEBA amoeba, Head head) {
		super(amoeba);
		setName(String.valueOf(this.hashCode()));
		Experiment firstPoint = new Experiment();

		List<Percept> percepts = amoeba.getPercepts();
		for (Percept percept : percepts) {
			double length = Math.abs(percept.getMinMaxDistance()) / 4.0;
			Range range = new Range(this, percept.getValue() - length, percept.getValue() + length, 0, true, true,
					percept);
			ranges.put(percept, range);

			firstPoint.addDimension(percept, percept.getValue());
		}
		firstPoint.setProposition(amoeba.getHeads().get(0).getOracleValue());
		experiments.add(firstPoint);
		localModel = amoeba.buildLocalModel(this);

		buildModel(amoeba, head, amoeba.getPercepts());
	}

	/**
	 * Constructor used to create a new Context when loading a file (in Save State
	 * class)
	 * 
	 * @param amoeba      the main amas
	 * @param head
	 * @param name
	 * @param starts
	 * @param ends
	 * @param experiments
	 * @param localModel
	 * @param confidence
	 */
	public Context(AMOEBA amoeba, Head head, String name, Map<Percept, Double> starts, Map<Percept, Double> ends,
			ArrayList<Experiment> experiments, LocalModel localModel, double confidence) {
		super(amoeba);
		this.ranges = new HashMap<>();
		this.experiments = experiments;
		this.confidence = confidence;
		this.localModel = localModel;

		setName(name);

		List<Percept> percepts = new ArrayList<>(starts.keySet());
		for (Percept percept : percepts) {
			Range range = new Range(this, starts.get(percept), ends.get(percept), 0, true, true, percept);
			this.ranges.put(percept, range);
		}

		buildModel(amoeba, head, percepts);
	}

	private final void buildModel(AMOEBA amoeba, Head head, List<Percept> percepts) {
		this.headAgent = head;
		this.localModel.updateModel(this);

		for (Percept percept : percepts) {
			percept.addContextProjection(this);
		}
	}

	@Override
	protected void onAct() {
		
		if(amas.getValidContexts().contains(this)) {
			activations++;
			headAgent.proposition(this);
		}
		
		// Kill small contexts
		for (Percept percept : ranges.keySet()) {
			if (ranges.get(percept).isTooSmall()) {
				solveNCS_Uselessness(headAgent);
				break;
			}
		}
		
	}
	
	@Override
	protected void onRenderingInitialization() {
		try {
			Constructor<? extends RenderStrategy> constructor = defaultRenderStrategy.getConstructor(Object.class);
			setRenderStrategy(constructor.newInstance(this));
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		super.onRenderingInitialization();
	}

	// ---------------------------- NCS Resolutions ----------------------------

	public void solveNCS_IncompetentHead(Head head) {
		amas.getEnvironment().raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges();
	}

	public void solveNCS_Concurrence(Head head) {
		amas.getEnvironment().raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders(head.getBestContext());
	}

	private void solveNCS_Uselessness(Head head) {
		amas.getEnvironment().raiseNCS(NCS.CONTEXT_USELESSNESS);
		this.destroy();
	}

	private void solveNCS_ConflictInexact(Head head) {
		amas.getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		confidence--;
		updateExperiments();
	}

	private void solveNCS_Conflict(Head head) {

		amas.getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);

		if (head.getNewContext() == this) {
			head.setNewContext(null);
		}
		;

		// The conflict lowers confidence
		confidence -= 2;

		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());

		if (head.isContextFromPropositionWasSelected() && head.getCriticity() <= head.getErrorAllowed()) {
			Percept percept = this.getPerceptsWithLesserImpactOnVolumeNotIncludedIn(percepts, head.getBestContext());
			if (percept == null) {
				this.destroy();
			} else {
				ranges.get(percept).matchBorderWith(head.getBestContext());
			}
		} else {
			Percept percept = this.getPerceptsWithLesserImpactOnVolume(percepts);
			ranges.get(percept).adapt(this, percept.getValue(), percept);
		}

		for (Percept percept : percepts) {
			if (ranges.get(percept).isTooSmall()) {
				solveNCS_Uselessness(head);
				break;
			}
		}
	}

	// -----------------------------------------------------------------------------------------------

	public String toString() {
		return "Context :" + this.getName();
	}
	
	public String toStringFull() {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "\n";
		
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
		s += "\n";
		
		s += "Number of activations : " + activations + "\n";
		s += "Action proposed : " + this.getActionProposal() + "\n";
		s += "Number of experiments : " + experiments.size() + "\n";
		s += "Confidence : " + confidence + "\n";
		s += "Local model : " + localModel.getFormula(this) + "\n";
		
		s += "\n";
		s += "Possible neighbours : \n";
		
		return s;
	}

	private void updateExperiments() {
		ArrayList<Percept> var = amas.getPercepts();
		Experiment exp = new Experiment();
		for (Percept v : var) {
			exp.addDimension(v, v.getValue());
		}
		exp.setProposition(headAgent.getOracleValue());

		experiments.add(exp);
		localModel.updateModel(this);
	}

	public void analyzeResults(Head head) {
		if (head.getCriticity(this) > head.getErrorAllowed()) {
			solveNCS_Conflict(head);

		} else {
			if (head.getCriticity(this) > head.getInexactAllowed()) {
				solveNCS_ConflictInexact(head);
			} else {
				confidence++;
			}
		}

	}

	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 */
	public void growRanges() {
		ArrayList<Percept> percepts = amas.getPercepts();
		for (Percept percept : percepts) {
			boolean contain = ranges.get(percept).contains(percept.getValue()) == 0 ? true : false;
			if (!contain) {
				ranges.get(percept).adapt(this, percept.getValue(), percept);
			}
		}
	}

	public void shrinkRangesToJoinBorders(Context consideredContext) {
		Set<Percept> percetList = ranges.keySet();
		ArrayList<Percept> containingRanges = new ArrayList<Percept>();

		for (Percept percept : percetList) {
			boolean contain = ranges.get(percept).contains(percept.getValue()) == 0 ? true : false;
			if (contain) {
				containingRanges.add(percept);
			}
		}

		Percept perceptWithLesserImpact = getPerceptsWithLesserImpactOnVolumeNotIncludedIn(containingRanges,
				consideredContext);
		if (perceptWithLesserImpact == null) {
			this.destroy();
		} else {
			ranges.get(perceptWithLesserImpact).matchBorderWith(consideredContext);
		}
	}

	@Override
	public void destroy() {
		ArrayList<Percept> percepts = amas.getPercepts();
		for (Percept percept : percepts) {
			percept.deleteContextProjection(this);
		}
		super.destroy();
	}

	public double getActionProposal() {
		return localModel.getProposition(this);
	}

	public double getCenterByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				+ this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	public double getConfidence() {
		return confidence;
	}

	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}

	public LocalModel getFunction() {
		return localModel;
	}

	private Percept getPerceptsWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges, Context c) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;

		for (Percept percept : containingRanges) {
			if (!ranges.get(percept).isPerceptEnum()) {
				Range r = c.getRanges().get(percept);
				if (!(r.getStart() <= ranges.get(percept).getStart() && r.getEnd() >= ranges.get(percept).getEnd())) {
					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						vol = ranges.get(percept).getEnd()
								- ranges.get(percept).simulateNegativeAVTFeedbackMin(percept.getValue());
					} else {
						vol = ranges.get(percept).simulateNegativeAVTFeedbackMax(percept.getValue())
								- ranges.get(percept).getStart();
					}

					for (Percept p2 : ranges.keySet()) {
						if (!ranges.get(p2).isPerceptEnum() && p2 != percept) {
							vol *= ranges.get(p2).getLenght();
						}
					}
					if (vol < volumeLost) {
						volumeLost = vol;
						p = percept;
					}
				}
			}
		}
		return p;
	}

	private Percept getPerceptsWithLesserImpactOnVolume(ArrayList<Percept> containingRanges) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;

		for (Percept v : containingRanges) {
			if (!ranges.get(v).isPerceptEnum()) {

				if (ranges.get(v).getNearestLimit(v.getValue()) == false) {
					vol = ranges.get(v).getEnd() - ranges.get(v).simulateNegativeAVTFeedbackMin(v.getValue());
				} else {
					vol = ranges.get(v).simulateNegativeAVTFeedbackMax(v.getValue()) - ranges.get(v).getStart();
				}

				for (Percept v2 : ranges.keySet()) {
					if (!ranges.get(v).isPerceptEnum() && v2 != v) {
						vol *= ranges.get(v2).getLenght();
					}
				}
				if (vol < volumeLost) {
					volumeLost = vol;
					p = v;
				}

			}

		}
		return p;
	}

	public double getRadiusByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				- this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	public Range getRangeByPerceptName(String perceptName) {
		for (Percept prct : ranges.keySet()) {
			if (prct.getName().equals(perceptName)) {
				return ranges.get(prct);
			}
		}
		return null;
	}

	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}
	
	public LocalModel getLocalModel() {
		return localModel;
	}
}
