package agents.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import agents.MessageType;
import agents.context.localModel.LocalModel;
import agents.head.Head;
import agents.percept.Percept;
import kernel.AMOEBA;
import ncs.NCS;

public class Context extends AmoebaAgent {
	
	private Head headAgent;
	private HashMap<Percept, Range> ranges = new HashMap<Percept, Range>();
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>();
	
	private LocalModel localModel;
	
	private double action = -1.0;
	private double confidence = 0;
	
	private int nSelection = 0;
	private int maxActivationsRequired = 0;
	private int activations = 0;
	
	private boolean valid = false;
	private boolean isDying = false;
	
	private HashMap<Percept, Boolean> perceptValidities = new HashMap<Percept, Boolean>();

	public Context(AMOEBA amas, Head head) {
		super(amas);
		buildContext(head);
	}
	
	@Override
	protected int computeExecutionOrderLayer() {
		return 1;
	}
	
	private void buildContext (Head headAgent) {
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment();
		this.headAgent = headAgent;
		
		action = this.headAgent.getOracleValue();
		maxActivationsRequired = var.size();
		
		
		for (Percept v : var) {
			Range r;

			double length = Math.abs(v.getMinMaxDistance()) / 4.0;
			r = new Range(this, v.getValue() - length, v.getValue() + length, 0, true, true, v);
			ranges.put(v, r);
			sendExpressMessage(null, MessageType.REGISTER, v); //TODO check if amak support express message
			firstPoint.addDimension(v, v.getValue());
			
			v.addContextProjection(this);
		}
		localModel = this.world.buildLocalModel(this);
		firstPoint.setProposition(this.headAgent.getOracleValue());
		experiments.add(firstPoint);
		localModel.updateModel(this);
		this.world.getScheduler().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));
		this.world.startAgent(this);
		
		perceptValidities = new HashMap<Percept, Boolean>();
		for(Percept percept : var) {
			perceptValidities.put(percept, false);
		}
	}

	//TODO copy constructor, check usefulness
	
	//getValueActionProposition only used by visualization -> removed (for now)
	
	@Override
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
			else {
				activations--;

				if (activations < 0)  {
					System.out.println("Activation lower than 0 : exit");
					System.exit(-2);
				}
				if (valid) {
					world.getScheduler().removeValidContextFromList(this);
					valid = false;
				}
			}
				
			if (activations == maxActivationsRequired) {
				valid = true;
				world.getScheduler().registerAgent(this);
			}
		}
	}
	
	private void computeAMessageTypeSelection(AmoebaMessage m) {
		nSelection++;
	}
	
	@Override
	protected void onAct() {
		if(computeValidityByPercepts()) {
			sendMessage(getActionProposal(), MessageType.PROPOSAL, headAgent);
			Config.print("Message envoyé", 4);
		}
		
	
		this.activations = 0;
		this.valid = false;

		
		// Reset percepts validities
		for(Percept percept : perceptValidities.keySet()) {
			perceptValidities.put(percept, false);
		}
		
		//Kill small contexts
		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()){
				solveNCS_Uselessness(headAgent);
				break;
			}
		}
	}
	
	
	//--------------------------------NCS Resolutions-----------------------------------------
	
	public void solveNCS_IncompetentHead(Head head) {
		world.raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges();
	}
	
	public void solveNCS_Concurrence(Head head) {
		world.raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders( head.getBestContext());
	}
	
	private void solveNCS_Uselessness(Head head) {
		world.raiseNCS(NCS.CONTEXT_USELESSNESS);
		this.die();
	}
	
	private void solveNCS_ConflictInexact(Head head) {
		world.raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		if(true) {
			confidence--;
		}
		updateExperiments();
	}
	
	private void solveNCS_Conflict(Head head) {

		world.raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);		
		
		if (head.getNewContext() == this) {
			head.setNewContext(null);
		};
		
		//The conflict lowers confidence
		if(true) {
			confidence -= 2;
		}
		
		ArrayList<Percept> percepts = new ArrayList();
		percepts.addAll(ranges.keySet());
		Percept p;
		if (head.isContextFromPropositionWasSelected() &&
				head.getCriticity() <= head.getErrorAllowed()){
				p = this.getPerceptWithLesserImpactOnVolumeNotIncludedIn(percepts, head.getBestContext());
			if (p == null) {
				this.die();
			}else {		
			ranges.get(p).matchBorderWith(head.getBestContext());
			}
		} else {
			p = this.getPerceptWithLesserImpactOnVolume(percepts);
			ranges.get(p).adapt(this, p.getValue(), p);
		}

		for (Percept v : ranges.keySet()) {
			if (ranges.get(v).isTooSmall()){
				solveNCS_Uselessness(head);
				break;
			}
		}
	}
	
	//-----------------------------------------------------------------------------------------------
	
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges, Context c) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;
		
		for (Percept percept : containingRanges) {
			if (!ranges.get(percept).isPerceptEnum()) {
				Range r = c.getRanges().get(percept);
				if (!(r.getStart() <= ranges.get(percept).getStart() && r.getEnd() >= ranges.get(percept).getEnd())) {
					if (ranges.get(percept).getNearestLimit(percept.getValue()) == false) {
						vol = ranges.get(percept).getEnd() - ranges.get(percept).simulateNegativeAVTFeedbackMin(percept.getValue());
					} else {
						vol = ranges.get(percept).simulateNegativeAVTFeedbackMax(percept.getValue()) - ranges.get(percept).getStart();
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
	
	private Percept getPerceptWithLesserImpactOnVolume(ArrayList<Percept> containingRanges) {
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
	
	//getPerceptWithLesserImpactOnAVT never used -> removed
	
	//getPerceptWithLargerImpactOnAVT never used -> removed
	
	public double getActionProposal() {
		return localModel.getProposition(this);
	}
	
	//computeValidity never used -> removed
	
	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}
	
	public Range getRangeByPerceptName(String percetName) {
		for(Percept prct : ranges.keySet()) {
			if(prct.getName().equals(percetName)) {
				return ranges.get(prct);
			}
		}
		return null;
	}
	
	//setRanges never used -> removed
	
	//getControler never used -> removed
	
	//setControler never used -> removed
	
	//getAction never used -> removed
	
	//setAction never used -> removed
	
	//getTargets used in visualization, removed (for now)
	
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
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Number of experiments : " + experiments.size() + "\n";
		s += "Confidence : " + confidence + "\n";
		if (formulaLocalModel != null) {
			s += "Local model : " + this.formulaLocalModel + "\n";
		} else {
			s += "Local model : " + localModel.getFormula(this) + "\n";
		}
		
		s += "\n";
		s += "Possible neighbours : \n";


		
		
		return s;
	}
	
	public String toStringReducted(HashMap<Percept,Double> situation) {
		String s = "";
		s += "Context : " + getName() + "\n";
		s += "Model : ";
		s += this.localModel.getCoefsFormula() + "\n";
;
		
		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";
			
		}

		s += "Number of activations : " + activations + "\n";
		if (actionProposition != null) {
			s += "Action proposed : " + this.actionProposition + "\n";
		} else {
			s += "Action proposed : " + this.getActionProposal() + "\n";
		}
		s += "Number of experiments : " + experiments.size() + "\n";
		s += "Confidence : " + confidence + "\n";
		s += "Normalized confidence : " + getNormalizedConfidence() + "\n";
		s += "Influnce :" + getInfluence(situation) + "\n";
		if (formulaLocalModel != null) {
			s += "Local model : " + this.formulaLocalModel + "\n";
		} else {
			s += "Local model : " + localModel.getFormula(this) + "\n";
		}
		
		s += "\n";

		
		
		return s;
	}
	
	//getNSelection used in visualization, removed (for now)
	
	//setnSelection used in visualization, removed (for now)
	
	public boolean isValid() {
		return valid;
	}
	
	//setValid never used -> removed
	
	//isFirstTimePeriod used in visualization, removed (for now)
	
	//setFirstTimePeriod never used -> removed
	
	//isBestContext used in visualization, removed (for now)
	
	//setBestContext never used -> removed
	
	public LocalModel getFunction() {
		return localModel;
	}
	
	//setFunction never used -> removed
	
	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}
	
	//setExperiments never used -> removed
	
	public double getConfidence() {
		return confidence;
	}
	
	//setConfidence never used -> removed
	
	public double getNormalizedConfidence() {
		return 1/(1+Math.exp(-confidence));
	}
	
	public double getParametrizedNormalizedConfidence(double dispersion) {
		return 1/(1+Math.exp(-confidence/dispersion));
	}
	
	public double getInfluence(HashMap<Percept,Double> situation) {
		Double influence = 1.0;
		
		for(Percept pct : situation.keySet()) {
			influence *= getInfluenceByPerceptSituation(pct, situation.get(pct));
		}
		
		return influence;
	}
	
	public double getInfluenceByPerceptSituation(Percept pct, double situation) {
		double center = getCenterByPercept(pct);
		double radius = getRadiusByPercept(pct);
				
		return getNormalizedConfidence()* Math.exp(- Math.pow(situation-center, 2)/(2*Math.pow(radius, 2)));
	}
	
	public double getCenterByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd() + this.getRangeByPerceptName(pct.getName()).getStart()) /2;
	}
	
	public double getRadiusByPercept(Percept pct) {
		return (this.getRangeByPerceptName(pct.getName()).getEnd() - this.getRangeByPerceptName(pct.getName()).getStart()) /2;
	}
	
	private void updateExperiments() {
		ArrayList<Percept> var = world.getAllPercept();
		maxActivationsRequired = var.size();
		Experiment exp = new Experiment();
		for (Percept v : var) {
			exp.addDimension(v, v.getValue());
		}
		exp.setProposition(headAgent.getOracleValue());
		
		experiments.add(exp);
		this.world.getScheduler().addAlteredContext(this);
		localModel.updateModel(this);
	}
	
	public void analyzeResults(Head head) {

		if (head.getCriticity(this) > head.getErrorAllowed()) {
			solveNCS_Conflict(head);
			this.world.getScheduler().addAlteredContext(this);
		}
		else {		
			if (head.getCriticity(this) > head.getInexactAllowed()) {
				solveNCS_ConflictInexact(head);
			}
			else {
				confidence++;
			}
		}

	}
	
	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 */
	public void growRanges() {
		ArrayList<Percept> allPercepts = world.getAllPercept();
		for (Percept pct : allPercepts) {
			boolean contain = ranges.get(pct).contains(pct.getValue()) == 0 ? true : false;
			if (!contain) {
				ranges.get(pct).adapt(this, pct.getValue(), pct);
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
		
		Percept perceptWithLesserImpact = getPerceptWithLesserImpactOnVolumeNotIncludedIn(containingRanges,consideredContext);
		if (perceptWithLesserImpact == null) {
			this.die();
		}else {
			ranges.get(perceptWithLesserImpact).matchBorderWith(consideredContext);
		}
	}
	
	public void die () {
		for(Percept percept : world.getScheduler().getPercepts()) {
			percept.deleteContextProjection(this);
		}
		localModel.die();
		super.die();
	}
	
	public void setPerceptValidity(Percept percept) {
		perceptValidities.put(percept, true);
	}
	
	public Boolean computeValidityByPercepts() {
		Boolean test = true;
		for(Percept percept : perceptValidities.keySet()) {
			test = test && perceptValidities.get(percept);
		}
		return test;
	}
	
	//getLocalModel used in visualization, removed (for now)
	
	public boolean isDying() {
		return isDying;
	}
}
