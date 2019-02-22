package agents.context;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import agents.MessageType;
import agents.context.localModel.LocalModel;
import agents.head.Head;
import agents.percept.Percept;
import fr.irit.smac.amak.ui.VUI;
import fr.irit.smac.amak.ui.drawables.DrawableRectangle;
import kernel.AMOEBA;
import ncs.NCS;

public class Context extends AmoebaAgent {

	private Head headAgent;
	private HashMap<Percept, Range> ranges = new HashMap<Percept, Range>();
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();

	private LocalModel localModel;

	// Note Labbeti : keep these attributes for now. (according to Steven)
	private double action = -1.0;
	private double confidence = 0;
	private int nSelection = 0;
	private int maxActivationsRequired = 0;
	private int activations = 0;

	private boolean valid = false;
	private boolean isDying = false;

	private DrawableRectangle drawable;

	private HashMap<Percept, Boolean> perceptValidities = new HashMap<Percept, Boolean>();

	public Context(AMOEBA amas, Head head) {
		super(amas);
		buildContext(head, amas);
	}

	@Override
	protected int computeExecutionOrderLayer() {
		return 1;
	}

	private void buildContext(Head headAgent, AMOEBA amoeba) {

		ArrayList<Percept> var = amoeba.getPercepts();
		Experiment firstPoint = new Experiment();
		this.headAgent = headAgent;

		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();

		for (Percept v : var) {
			Range r;

			double length = Math.abs(v.getMinMaxDistance()) / 4.0;
			r = new Range(this, v.getValue() - length, v.getValue() + length, 0, true, true, v);
			ranges.put(v, r);
			sendExpressMessage(null, MessageType.REGISTER, v); // TODO check if amak support express message
			firstPoint.addDimension(v, v.getValue());

			v.addContextProjection(this);
		}
		localModel = amoeba.buildLocalModel(this);
		// TODO see if possible to message
		firstPoint.setProposition(this.headAgent.getOracleValue());
		experiments.add(firstPoint);
		localModel.updateModel(this);
		this.setName(String.valueOf(this.hashCode()));

		perceptValidities = new HashMap<Percept, Boolean>();
		for (Percept percept : var) {
			perceptValidities.put(percept, false);
		}
	}

	public void computeAMessage(AmoebaMessage m) {

		if (m.getType() == MessageType.VALIDATE) {

			computeAMessageTypeValidate(m);

		} else if (m.getType() == MessageType.SELECTION) { // ++ number of selection only

			computeAMessageTypeSelection(m);
		}
	}

	private void computeAMessageTypeValidate(AmoebaMessage m) {

		if (m.getSender() instanceof Percept) {
			boolean activate = (boolean) m.getContent();
			if (activate) {
				activations++;
			}
		}
	}

	private void computeAMessageTypeSelection(AmoebaMessage m) {
		nSelection++;
	}

	protected void onAct() {
		if (computeValidityByPercepts()) {
			AmoebaMessage message = new AmoebaMessage(getActionProposal(), MessageType.PROPOSAL, this);
			sendMessage(message, headAgent.getAID());
		}

		this.activations = 0;
		this.valid = false;

		// Reset percepts validities
		for (Percept percept : perceptValidities.keySet()) {
			perceptValidities.put(percept, false);
		}

		// Kill small contexts
		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()) {
				solveNCS_Uselessness(headAgent);
				break;
			}
		}
	}

	// -------------------------------- NCS
	// Resolutions-----------------------------------------

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
		this.die();
	}

	private void solveNCS_ConflictInexact(Head head) {
		amas.getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		if (true) {
			confidence--;
		}
		updateExperiments();
	}

	private void solveNCS_Conflict(Head head) {

		amas.getEnvironment().raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);

		// TODO see if possible to message
		if (head.getNewContext() == this) {
			head.setNewContext(null);
		}
		;

		// The conflict lowers confidence
		if (true) {
			confidence -= 2;
		}

		// TODO see if possible to message
		ArrayList<Percept> percepts = new ArrayList<Percept>();
		percepts.addAll(ranges.keySet());
		Percept p;
		if (head.isContextFromPropositionWasSelected() && head.getCriticity() <= head.getErrorAllowed()) {
			p = this.getPerceptsWithLesserImpactOnVolumeNotIncludedIn(percepts, head.getBestContext());
			if (p == null) {
				this.die();
			} else {
				ranges.get(p).matchBorderWith(head.getBestContext());
			}
		} else {
			p = this.getPerceptsWithLesserImpactOnVolume(percepts);
			ranges.get(p).adapt(this, p.getValue(), p);
		}

		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()) {
				solveNCS_Uselessness(head);
				break;
			}
		}
	}

	// -----------------------------------------------------------------------------------------------

	private Percept getPerceptsWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges, Context c) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;

		// TODO see if possible to message
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
		// TODO see if possible to message
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

	public double getActionProposal() {
		return localModel.getProposition(this);
	}

	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}

	public Range getRangeByPerceptName(String perceptName) {
		// TODO see if possible to message
		for (Percept prct : ranges.keySet()) {
			if (prct.getName().equals(perceptName)) {
				return ranges.get(prct);
			}
		}
		return null;
	}

	public String toString() {
		return "Context :" + this.getName();// Percept name
	}

	// TODO : keep these debug function for display datas ?
	/*
	 * public String toStringFull() { String s = ""; s += "Context : " + getName() +
	 * "\n";//Percept name s += "\n";
	 * 
	 * s += "Model : "; s += this.localModel.getCoefsFormula() + "\n"; s += "\n";
	 * 
	 * 
	 * s += "Number of activations : " + activations + "\n"; if (actionProposition
	 * != null) { s += "Action proposed : " + this.actionProposition + "\n"; } else
	 * { s += "Action proposed : " + this.getActionProposal() + "\n"; } s +=
	 * "Number of experiments : " + experiments.size() + "\n"; s += "Confidence : "
	 * + confidence + "\n"; if (formulaLocalModel != null) { s += "Local model : " +
	 * this.formulaLocalModel + "\n"; } else { s += "Local model : " +
	 * localModel.getFormula(this) + "\n"; }
	 * 
	 * s += "\n"; s += "Possible neighbours : \n";
	 * 
	 * return s; }
	 * 
	 * public String toStringReducted(HashMap<Percept,Double> situation) { String s
	 * = ""; s += "Context : " + getName() + "\n"; s += "Model : "; s +=
	 * this.localModel.getCoefsFormula() + "\n"; for (Percept v : ranges.keySet()) {
	 * s += v.getName() + " : " + ranges.get(v).toString() + "\n";
	 * 
	 * }
	 * 
	 * s += "Number of activations : " + activations + "\n"; if (actionProposition
	 * != null) { s += "Action proposed : " + this.actionProposition + "\n"; } else
	 * { s += "Action proposed : " + this.getActionProposal() + "\n"; } s +=
	 * "Number of experiments : " + experiments.size() + "\n"; s += "Confidence : "
	 * + confidence + "\n"; s += "Normalized confidence : " +
	 * getNormalizedConfidence() + "\n"; s += "Influnce :" + getInfluence(situation)
	 * + "\n"; if (formulaLocalModel != null) { s += "Local model : " +
	 * this.formulaLocalModel + "\n"; } else { s += "Local model : " +
	 * localModel.getFormula(this) + "\n"; }
	 * 
	 * s += "\n";
	 * 
	 * return s; }
	 */

	public LocalModel getFunction() {
		return localModel;
	}

	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}

	public double getConfidence() {
		return confidence;
	}

	public double getNormalizedConfidence() {
		return 1 / (1 + Math.exp(-confidence));
	}

	public double getParametrizedNormalizedConfidence(double dispersion) {
		return 1 / (1 + Math.exp(-confidence / dispersion));
	}

	public double getInfluence(HashMap<Percept, Double> situation) {
		Double influence = 1.0;

		for (Percept pct : situation.keySet()) {
			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}

		return influence;
	}

	public double getInfluenceByPerceptSituation(Percept pct, double situation) {
		double center = getCenterByPercept(pct);
		double radius = getRadiusByPercept(pct);

		return getNormalizedConfidence() * Math.exp(-Math.pow(situation - center, 2) / (2 * Math.pow(radius, 2)));
	}

	public double getCenterByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				+ this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	public double getRadiusByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd()
				- this.getRangeByPerceptName(pct.getName()).getStart()) / 2;
	}

	private void updateExperiments() {
		ArrayList<Percept> percepts = amas.getPercepts();
		maxActivationsRequired = percepts.size();
		Experiment exp = new Experiment();
		// TODO see if possible to message
		for (Percept percept : percepts) {
			exp.addDimension(percept, percept.getValue());
		}
		exp.setProposition(headAgent.getOracleValue());

		experiments.add(exp);
		localModel.updateModel(this);
	}

	public void analyzeResults(Head head) {
		// TODO see if possible to message
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
			this.die();
		} else {
			ranges.get(perceptWithLesserImpact).matchBorderWith(consideredContext);
		}
	}

	public void die() {
		isDying = true;
		for (Percept percept : amas.getPercepts()) { // see if is compatible //Pred: world.getScheduler().getPerceptss()
			percept.deleteContextProjection(this);
		}
		drawable.hide();
		amas._removeAgent(this);
	}

	public void setPerceptValidity(Percept percept) {
		perceptValidities.put(percept, true);
	}

	public Boolean computeValidityByPercepts() {
		Boolean test = true;
		for (Percept percept : perceptValidities.keySet()) {
			test = test && perceptValidities.get(percept);
		}
		return test;
	}

	public boolean isDying() {
		return isDying;
	}

	/**
	 * @warning: for now just in 2 dimensions
	 */
	@Override
	protected void onRenderingInitialization() {
		amas.getAgents();

		drawable = VUI.get().createRectangle(0, 0, 10, 10);
		drawable.setLayer(1);
		drawable.setColor(new Color(173, 79, 9, 90));
	}

	@Override
	protected void onUpdateRender() {
		Set<Percept> sP = ranges.keySet();
		Iterator<Percept> iter = sP.iterator();
		Percept p1 = iter.next();
		Percept p2 = iter.next();
		double x = ranges.get(p1).getStart() + (ranges.get(p1).getLenght() / 2);
		double y = ranges.get(p2).getStart() + (ranges.get(p2).getLenght() / 2);
		drawable.move(x, y);
		drawable.setWidth(ranges.get(p1).getLenght());
		drawable.setHeight(ranges.get(p2).getLenght());

		// Normalization of the color
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		amas.getAgents();
		for (Context c : amas.getContexts()) {
			double val = c.getActionProposal();
			if (val < min) {
				min = val;
			}
			if (val > max) {
				max = val;
			}

		}

		int green;
		int blue;
		double normalizedValue = (getActionProposal() - min) / (max - min);
		green = (int) (normalizedValue * 255);
		blue = (int) ((1 - normalizedValue) * 255);

		drawable.setColor(new Color(0, green, blue, 90));
	}
}
