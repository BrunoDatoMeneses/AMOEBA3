package mas.agents.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import mas.kernel.Config;
import mas.kernel.World;
import mas.ncs.NCS;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.head.Head;
import mas.agents.localModel.LocalModelAgent;
import mas.agents.localModel.LocalModelAverage;
import mas.agents.localModel.LocalModelFirstExp;
import mas.agents.localModel.LocalModelMillerRegression;
import mas.agents.localModel.TypeLocalModel;
import mas.agents.messages.Message;
import mas.agents.messages.MessageType;


// TODO: Auto-generated Javadoc
/**
 * The core agent of AMOEBA.
 * 
 * 
 */
public class Context extends AbstractContext implements Serializable{

	ArrayList<Percept> perceptSenders = new ArrayList<Percept>();
	
	private Head controller;
	private HashMap<Percept, Range> ranges = new HashMap<Percept, Range>();
	private ArrayList<Experiment> experiments = new ArrayList<Experiment>(); /*If memory is a concern, their is room for improvements here*/
	
	private LocalModelAgent localModel;
	
	private Double actionProposition = null;
	private String formulaLocalModel = null;
	
	private double action = -1.0;
	private double confidence = 0;
	
	private int nSelection = 0;
	private int maxActivationsRequired = 0;
	private int activations = 0;

	private boolean bestContext = false;
	private boolean valid = false;
	private boolean firstTimePeriod = true;
	
	private HashMap<Percept, Boolean> perceptValidities = new HashMap<Percept, Boolean>();
	public HashMap<Context, HashMap<Percept, Boolean>> contextOverlapsByPercept = new HashMap<Context, HashMap<Percept, Boolean>>();
	public HashMap<Context,String> overlaps = new HashMap<Context,String>();
	public ArrayList<ContextOverlap> contextOverlaps = new ArrayList<ContextOverlap>();
	
	
	public HashMap<Percept , HashMap<String, Context>> nearestNeighbours;
	
	public HashMap<Percept , HashMap<String, ArrayList<Context>>> sortedPossibleNeighbours = new HashMap<Percept , HashMap<String, ArrayList<Context>>>();
	
	public ArrayList<Context> possibleNeighbours = new  ArrayList<Context>();
	public ArrayList<Context> neighbours = new ArrayList<Context>();
	
	

	/**
	 * The main constructor, used by AMOEBA to build new context agent.
	 * @param world : the world where the agent must live.
	 * @param head : the head agent associated with the next context agent
	 */
	public Context(World world, Head head) {
		super(world);
		buildContext(head);
		
	}
	
	
	
	/**
	 * Builds the context.
	 *
	 * @param world the world
	 * @param controller the controller
	 */
	private void buildContext (Head controller) {
		
		ArrayList<Percept> var = world.getAllPercept();
		Experiment firstPoint = new Experiment();
		this.controller = controller;
		
		action = this.controller.getOracleValue();
		maxActivationsRequired = var.size();
		
		for (Percept v : var) {
			Range r;

			double length = Math.abs(v.getMinMaxDistance()) / 4.0;
			r = new Range(this, v.getValue() - length, v.getValue() + length, 0, true, true, v);
			ranges.put(v, r);
			ranges.get(v).setValue(v.getValue());
			sendExpressMessage(null, MessageType.REGISTER, v);
			firstPoint.addDimension(v, v.getValue());
			
			v.addContextProjection(this);
			v.addContextSortedRanges(this);
		}
		localModel = this.world.buildLocalModel(this);
		firstPoint.setProposition(this.controller.getOracleValue());
		experiments.add(firstPoint);
		localModel.updateModel(this);
		this.world.getScheduler().addAlteredContext(this);
		this.setName(String.valueOf(this.hashCode()));
		this.world.startAgent(this);
		
		perceptValidities = new HashMap<Percept, Boolean>();
		for(Percept percept : var) {
			perceptValidities.put(percept, false);
		}
		
		contextOverlapsByPercept = new HashMap<Context, HashMap<Percept, Boolean>>();
		nearestNeighbours = new HashMap<Percept , HashMap<String, Context>>();
		for(Percept p : ranges.keySet()) {
			nearestNeighbours.put(p, new HashMap<String, Context>());
			
			sortedPossibleNeighbours.put(p, new HashMap<String, ArrayList<Context>>());
			
			nearestNeighbours.get(p).put("start", null);
			nearestNeighbours.get(p).put("end", null);
			
			sortedPossibleNeighbours.get(p).put("start", new ArrayList<Context>() );
			sortedPossibleNeighbours.get(p).put("end", new ArrayList<Context>() );
			
			
		}
		
		overlaps =  new HashMap<Context,String>();
	}
	
	/**
	 * Instantiates a new context.
	 *
	 * @param c the c
	 */
	public Context(Context c) {
		super(c.world);
		this.ID = c.ID;
		this.name = c.name;
		this.messages = c.messages;
		this.messagesBin = c.messagesBin;
		this.isDying = c.isDying;
		
		this.ranges = new HashMap<Percept, Range>();
		for(Entry<Percept, Range> entry : c.ranges.entrySet()) {
		   Percept percept = new Percept(entry.getKey());
		   Range range = new Range(entry.getValue());
		   this.ranges.put(percept, range);
		}
		this.controller = c.controller;
		this.action = c.action;
		this.nSelection = c.nSelection;
		this.bestContext = c.bestContext;
		this.valid = c.valid;
		this.firstTimePeriod = c.firstTimePeriod;
		this.perceptSenders = new ArrayList<Percept>();
		for(Percept obj: c.perceptSenders) {
			this.perceptSenders.add(new Percept(obj));
		}
		this.maxActivationsRequired = c.maxActivationsRequired;
		this.activations = c.activations;
		this.confidence = c.confidence;	
		if (c.world.getLocalModel() == TypeLocalModel.MILLER_REGRESSION) {
			this.localModel = new LocalModelMillerRegression(c.world);
			this.formulaLocalModel = ((LocalModelMillerRegression) c.localModel).getFormula(c);
			double[] coef = ((LocalModelMillerRegression) c.localModel).getCoef();
			((LocalModelMillerRegression) this.localModel).setCoef(coef);
			this.actionProposition = ((LocalModelMillerRegression) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.FIRST_EXPERIMENT) {
			this.localModel = new LocalModelFirstExp(c.world);
			this.formulaLocalModel = ((LocalModelFirstExp) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelFirstExp) c.localModel).getProposition(c);
		} else if (c.world.getLocalModel() == TypeLocalModel.AVERAGE) {
			this.localModel = new LocalModelAverage(c.world);
			this.formulaLocalModel = ((LocalModelAverage) c.localModel).getFormula(c);
			this.actionProposition = ((LocalModelAverage) c.localModel).getProposition(c);
		}

		this.experiments = new ArrayList<Experiment>();
		for(Experiment obj: c.experiments) {
			Experiment exp = new Experiment();
			exp.setProposition(obj.getProposition());
			LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
			for(Entry<Percept, Double> entry : obj.getValues().entrySet()) {
				Percept percept = new Percept(entry.getKey());
				Double value = new Double(entry.getValue());
				values.put(percept, value);
			}
			exp.setValues(values);
			this.experiments.add(exp);
		}
		
	}
	
	/**
	 * Gets the value action proposition.
	 *
	 * @return the value action proposition
	 */
	public Double getValueActionProposition() {
		return this.actionProposition;
	}

	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {

		if (m.getType() == MessageType.VALIDATE) {
			
			computeAMessageTypeValidate(m);
			
		} else if (m.getType() == MessageType.SELECTION) { // ++ number of selection only

			computeAMessageTypeSelection(m);
		}
	}
	
	
	private void computeAMessageTypeValidate(Message m) {
		
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
	
	private void computeAMessageTypeSelection(Message m) {
		nSelection++;
	}


	/* (non-Javadoc)
	 * @see agents.SystemAgent#play()
	 */
	public void play() {
		super.play();
		
		if(computeValidityByPercepts()) {
			//System.out.println("Valid context by Percepts "+this.name);
		}
		
		if (computeValidity()) {
			sendMessage(getActionProposal(), MessageType.PROPOSAL, controller);
			Config.print("Message envoy�", 4);
			//System.out.println("Valid context by Context "+this.name);
		}
		
		this.activations = 0;
		this.valid = false;

		// Reset percepts validities
		for(Percept percept : perceptValidities.keySet()) {
			perceptValidities.put(percept, false);
		}
		
		NCSDetections();
	}


	private void NCSDetections() {
		
		NCSDetection_Overlap();

		
	}
	
	
	private void NCSDetection_Overlap() {
		

		computeOverlapsByPercepts();
		for(Context ctxt: overlaps.keySet()) {
			ctxt.getOverlapType(this);
		}
		
	}


	private void getOverlapType(Context context) {
		
		
		
	}



//--------------------------------NCS Resolutions-----------------------------------------
	
	/**
	 * Solve NC S incompetent head.
	 *
	 * @param head the head
	 */
	public void solveNCS_IncompetentHead(Head head) {
		world.raiseNCS(NCS.HEAD_INCOMPETENT);
		growRanges(head);
	}
	
	/**
	 * Solve NC S concurrence.
	 *
	 * @param head the head
	 */
	public void solveNCS_Concurrence(Head head) {
		world.raiseNCS(NCS.CONTEXT_CONCURRENCE);
		this.shrinkRangesToJoinBorders(head, head.getBestContext());
	}
	
	/**
	 * Solve NC S uselessness.
	 *
	 * @param head the head
	 */
	private void solveNCS_Uselessness(Head head) {
		world.raiseNCS(NCS.CONTEXT_USELESSNESS);
		this.die();
	}
	
	/**
	 * Solve NC S conflict inexact.
	 *
	 * @param head the head
	 */
	private void solveNCS_ConflictInexact(Head head) {
		world.raiseNCS(NCS.CONTEXT_CONFLICT_INEXACT);
		confidence--;
		updateExperiments();
	}
	
	/**
	 * Solve NC S conflict.
	 *
	 * @param head the head
	 */
	private void solveNCS_Conflict(Head head) {

		world.raiseNCS(NCS.CONTEXT_CONFLICT_FALSE);		
		
		if (head.getNewContext() == this) {
			head.setNewContext(null);
		};
		
		//The conflict lowers confidence
		confidence -= 2;


			ArrayList<Percept> percepts = new ArrayList();
			percepts.addAll(ranges.keySet());
			//Percept p = getPerceptWithLargerImpactOnAVT(percepts);
			//Percept p = getPerceptWithLesserImpactOnAVT(percepts);
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
	
	/**
	 * Gets the percept with lesser impact on volume not included in.
	 *
	 * @param containingRanges the containing ranges
	 * @param c the c
	 * @return the percept with lesser impact on volume not included in
	 */
	private Percept getPerceptWithLesserImpactOnVolumeNotIncludedIn(ArrayList<Percept> containingRanges, Context c) {
		Percept p = null;
		double volumeLost = Double.MAX_VALUE;
		double vol;
		
		for (Percept v : containingRanges) {
			if (!ranges.get(v).isPerceptEnum()) {
				Range r = c.getRanges().get(v);
				if (!(r.getStart() <= ranges.get(v).getStart() && r.getEnd() >= ranges.get(v).getEnd())) {
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
		}
		return p;
	}
	
	

/**
 * Gets the percept with lesser impact on volume.
 *
 * @param containingRanges the containing ranges
 * @return the percept with lesser impact on volume
 */
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
	
	/**
	 * Gets the percept with lesser impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with lesser impact on AVT
	 */
	private Percept getPerceptWithLesserImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.MAX_VALUE;
		double tempImpact;
		
		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue()));

				if (tempImpact < impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}
	
	/**
	 * Gets the percept with larger impact on AVT.
	 *
	 * @param percepts the percepts
	 * @return the percept with larger impact on AVT
	 */
	private Percept getPerceptWithLargerImpactOnAVT(ArrayList<Percept> percepts) {
		Percept p = null;
		double impact = Double.NEGATIVE_INFINITY;
		double tempImpact;
		
		for (Percept v : percepts) {
			if (!ranges.get(v).isPerceptEnum()) {
				tempImpact = (-1) * Math.abs(ranges.get(v).getAVTwillToReduce(ranges.get(v).getNearestLimit(v.getValue())));

				if (tempImpact > impact) {
					impact = tempImpact;
					p = v;
				}
			}
		}
		return p;
	}

	
	/**
	 * Gets the action proposal.
	 *
	 * @return the action proposal
	 */
	public double getActionProposal() {
		return localModel.getProposition(this);
	}


	/**
	 * Compute validity.
	 *
	 * @return true, if successful
	 */
	private boolean computeValidity() {
		boolean b = true;
		for (Percept p : ranges.keySet()) {
			if (ranges.get(p).contains(p.getValue()) != 0) {
				b = false;
				break;
			}
		}
		return b;
	}

	/**
	 * Gets the ranges.
	 *
	 * @return the ranges
	 */
	public HashMap<Percept, Range> getRanges() {
		return ranges;
	}

	/**
	 * Sets the ranges.
	 *
	 * @param ranges the ranges
	 */
	public void setRanges(HashMap<Percept, Range> ranges) {
		this.ranges = ranges;
	}

	/**
	 * Gets the controler.
	 *
	 * @return the controler
	 */
	public Head getControler() {
		return controller;
	}

	/**
	 * Sets the controler.
	 *
	 * @param controler the new controler
	 */
	public void setControler(Head controler) {
		this.controller = controler;
	}

	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public double getAction() {
		return action;
	}

	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(double action) {
		this.action = action;
	}

	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		ArrayList<Agent> arrayList = new ArrayList<Agent>();
		arrayList.add(controller);
		return arrayList;
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String s = "";
		s += "Context : " + getName() + "\n";
		for (Percept v : ranges.keySet()) {
			s += v.getName() + " : " + ranges.get(v).toString() + "\n";
			
			s += "Neighbours : \n";
			
			if(nearestNeighbours.get(v).get("start") != null) {
				s+= "START :" + nearestNeighbours.get(v).get("start").getName() + "\n";
			}
			else {
				s+= "START : \n";
			}
			s += "Sorted start possible neighbours :\n";
			if(sortedPossibleNeighbours.get(v).get("start").size()>0) {
				for(Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {
					
					if(ctxt.equals(this)) {
						s += "# " + ctxt.getName() + " --> " + ctxt.getRanges().get(v).getStart() + "\n";
					}
					else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getStart() + "\n";
					}
						
					
				}
			}
			s += "Sorted end possible neighbours :\n";
			if(sortedPossibleNeighbours.get(v).get("end").size()>0) {
				for(Context ctxt : sortedPossibleNeighbours.get(v).get("start")) {
					
					if(ctxt.equals(this)) {
						s += "# " +ctxt.getName()+ " --> " + ctxt.getRanges().get(v).getEnd() + "\n";
					}
					else {
						s += ctxt.getName() + " ---> " + ctxt.getRanges().get(v).getEnd() + "\n";
					}
					
				}
			}
			
			if(nearestNeighbours.get(v).get("end") != null) {
				s+= "END :" + nearestNeighbours.get(v).get("end").getName() + "\n";
			}
			else {
				s+= "END : \n";
			}
			
			

		}
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
		
		s += "Possible neighbours : \n";
		for(Context ctxt : possibleNeighbours) {
			s += ctxt.getName() + "\n";
		}

		return s;
	}

	/**
	 * Gets the n selection.
	 *
	 * @return the n selection
	 */
	public int getNSelection() {
		return activations;
	}

	/**
	 * Sets the n selection.
	 *
	 * @param nSelection the new n selection
	 */
	public void setnSelection(int nSelection) {
		this.nSelection = nSelection;
	}


	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the valid.
	 *
	 * @param valid the new valid
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Checks if is first time period.
	 *
	 * @return true, if is first time period
	 */
	public boolean isFirstTimePeriod() {
		return firstTimePeriod;
	}

	/**
	 * Sets the first time period.
	 *
	 * @param firstTimePeriod the new first time period
	 */
	public void setFirstTimePeriod(boolean firstTimePeriod) {
		this.firstTimePeriod = firstTimePeriod;
	}


	/**
	 * Checks if is best context.
	 *
	 * @return true, if is best context
	 */
	public boolean isBestContext() {
		return bestContext;
	}


	/**
	 * Sets the best context.
	 *
	 * @param bestContext the new best context
	 */
	public void setBestContext(boolean bestContext) {
		this.bestContext = bestContext;
	}

	/**
	 * Gets the function.
	 *
	 * @return the function
	 */
	public LocalModelAgent getFunction() {
		return localModel;
	}


	/**
	 * Sets the function.
	 *
	 * @param function the new function
	 */
	public void setFunction(LocalModelAgent function) {
		this.localModel = function;
	}

	/**
	 * Gets the experiments.
	 *
	 * @return the experiments
	 */
	public ArrayList<Experiment> getExperiments() {
		return experiments;
	}

	/**
	 * Sets the experiments.
	 *
	 * @param experiments the new experiments
	 */
	public void setExperiments(ArrayList<Experiment> experiments) {
		this.experiments = experiments;
	}

	/**
	 * Gets the confidence.
	 *
	 * @return the confidence
	 */
	public double getConfidence() {
		//This is a test to use confidence as directly linked to size of context.
	/*	double d = 1.0;
		ArrayList<Percept> percepts = world.getAllPercept();

		for (Percept p : percepts) {
			d *= ranges.get(p).getLenght();
		}
		
		return 1/d;*/
		return confidence;

	}

	/**
	 * Sets the confidence.
	 *
	 * @param confidence the new confidence
	 */
	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}


	/**
	 * Update experiments.
	 */
	private void updateExperiments() {
	//	System.out.println("Update experiments");
		ArrayList<Percept> var = world.getAllPercept();
		maxActivationsRequired = var.size();
		Experiment exp = new Experiment();
		for (Percept v : var) {
			exp.addDimension(v, v.getValue());
		}
		exp.setProposition(controller.getOracleValue());
		
		experiments.add(exp);
		this.world.getScheduler().addAlteredContext(this);
		localModel.updateModel(this);
	}
	




	
	
	/**
	 * Analyze results.
	 *
	 * @param ctrl the ctrl
	 */
	public void analyzeResults(Head ctrl) {

			if (ctrl.getCriticity(this) > ctrl.getErrorAllowed()) {
				solveNCS_Conflict(ctrl);
				this.world.getScheduler().addAlteredContext(this);
			}
			else {		
				if (ctrl.getCriticity(this) > ctrl.getInexactAllowed()) {
					solveNCS_ConflictInexact(ctrl);
				}
				else {
					confidence++;
				}
			}

		}
	
	/**
	 * Grow every ranges allowing to includes current situation.
	 *
	 * @param head the head
	 */
	public void growRanges(Head head) {
		ArrayList<Percept> var = world.getAllPercept();
		for (Percept v : var) {
			boolean contain = ranges.get(v).contains(v.getValue()) == 0 ? true : false;
			if (!contain) {
				ranges.get(v).adapt(this, v.getValue(), v);
			}
		}
	}
	
	
	
	/**
	 * Shrink ranges to join borders.
	 *
	 * @param head the head
	 * @param c the c
	 */
	public void shrinkRangesToJoinBorders(Head head, Context c) {
		Set<Percept> var = ranges.keySet();
		ArrayList<Percept> containingRanges = new ArrayList<Percept>();
		
		for (Percept v : var) {
			boolean contain = ranges.get(v).contains(v.getValue()) == 0 ? true : false;
			if (contain) {
				containingRanges.add(v);
			}
		}
		
		Percept p = getPerceptWithLesserImpactOnVolumeNotIncludedIn(containingRanges,c);
		if (p == null) {
			this.die();
		}else {
			ranges.get(p).matchBorderWith(c);
		}
	}	

	
	/* (non-Javadoc)
	 * @see agents.context.AbstractContext#die()
	 */
	public void die () {
		for(Percept percept : perceptSenders) {
			percept.deleteContextProjection(this);
		}
		
		localModel.die();
		super.die();
	}
	
	
	public void setPerceptValidity(Percept percept) {
		perceptValidities.put(percept, true);
	}
	
	public void setPerceptOverlap(Percept percept, Context context) {
		if(!contextOverlapsByPercept.keySet().contains(context)) {
			contextOverlapsByPercept.put(context, new HashMap<Percept,Boolean>());
			
			for(Percept p : ranges.keySet()) {
				contextOverlapsByPercept.get(context).put(p, false);
			}
		}
		
		contextOverlapsByPercept.get(context).put(percept, true);
	}
	
	
	
	public Boolean computeValidityByPercepts() {
		Boolean test = true;
		for(Percept percept : perceptValidities.keySet()) {
			//System.out.println(percept.getName()+"--->"+perceptValidities.get(percept));
			test = test && perceptValidities.get(percept);
		}
		return test;
	}
	
	public Boolean computeOverlapsByPercepts() {
		Boolean test = true;
		
		for(Context context : contextOverlapsByPercept.keySet()) {
			test = true;
			for(Percept percept : ranges.keySet()) {
				test = test && contextOverlapsByPercept.get(context).get(percept);
			}
			if(test) {
				
				overlaps.put(context, "Overlap");
				
				HashMap<Percept,HashMap<String,Double>> overlapRanges = new HashMap<Percept,HashMap<String,Double>>();
				for(Percept percept : ranges.keySet()) {
					overlapRanges.put(percept, new HashMap<String,Double>());
					double startRange = percept.getOverlapRangesBetweenContexts(this, context).get("start");
					double endRange = percept.getOverlapRangesBetweenContexts(this, context).get("end");
					overlapRanges.get(percept).put("start", startRange);
					overlapRanges.get(percept).put("end", endRange);
				}
				
				contextOverlaps.add(new ContextOverlap(this, context, overlapRanges));		
			}
		}
		
		
		return test;
	}
	
	public Boolean computeOverlapsBySelectedPercepts(ArrayList<Percept> selectedPercepts, Context context) {
		Boolean test = true;
		
		test = true;
		for(Percept percept : selectedPercepts) {
			test = test && contextOverlapsByPercept.get(context).get(percept);
		}
		if(test) {
			//neigbours.put(context, "Overlap");
		}
		
		
		return test;
	}
	
	public HashMap<String , ArrayList<Context>> getSortedPossibleNeigbours(Percept percept) {
		
		ArrayList<Percept> otherPercetps = new ArrayList<Percept>();; 
		ArrayList<Context> contextOverlapedInOtherPercepts = new ArrayList<Context>();
		boolean contextOverlapedInOtherPerceptsTest = true;
		
		for(Percept p : ranges.keySet()) {
			if(p != percept) {
				otherPercetps.add(p);
			}
		}
		
		
		for(Context ctxt : contextOverlapsByPercept.keySet()) {
			contextOverlapedInOtherPerceptsTest = true;
			for(Percept otherPctpt: otherPercetps) {
				contextOverlapedInOtherPerceptsTest = contextOverlapedInOtherPerceptsTest && contextOverlapsByPercept.get(ctxt).get(otherPctpt);
			}
			if(contextOverlapedInOtherPerceptsTest) {
				contextOverlapedInOtherPercepts.add(ctxt);
				if(!possibleNeighbours.contains(ctxt)) {
					possibleNeighbours.add(ctxt);
				}
				
			}
		}
		
		 
		 HashMap<String , ArrayList<Context>> sortedRangesSubGroup = new HashMap<String , ArrayList<Context>>();
		 sortedRangesSubGroup.put("start", percept.getSortedRangesSubGroup(contextOverlapedInOtherPercepts, "start"));
		 sortedRangesSubGroup.put("end", percept.getSortedRangesSubGroup(contextOverlapedInOtherPercepts, "end"));
		
		 return sortedRangesSubGroup;
	}
	
	public void getNearestNeighbours(){
		
		HashMap<Percept,  HashMap<String , ArrayList<Context>>> localSortedPossibleNeigbours = new HashMap<Percept,  HashMap<String , ArrayList<Context>>>();
		
		
		for(Percept p : ranges.keySet()) {
			
			localSortedPossibleNeigbours.put(p,getSortedPossibleNeigbours(p));
			localSortedPossibleNeigbours.get(p).get("start").add(this);
			localSortedPossibleNeigbours.get(p).get("end").add(this);
			
			
			
			Collections.sort(localSortedPossibleNeigbours.get(p).get("start"), p.customRangeComparators.get("start"));
			Collections.sort(localSortedPossibleNeigbours.get(p).get("end"), p.customRangeComparators.get("end"));
			
			sortedPossibleNeighbours.get(p).put("start", localSortedPossibleNeigbours.get(p).get("start"));
			sortedPossibleNeighbours.get(p).put("end", localSortedPossibleNeigbours.get(p).get("end"));
			
			
			
		}
		
		for(Percept p : ranges.keySet()) {
			
			
			Context startNeighbour = getNearestContextBySortedPerceptAndRange(localSortedPossibleNeigbours.get(p), p, "start");
			Context endNeighbour = getNearestContextBySortedPerceptAndRange(localSortedPossibleNeigbours.get(p), p, "end");
			
			
			nearestNeighbours.get(p).put("end", startNeighbour);
			nearestNeighbours.get(p).put("start", endNeighbour);
			
			neighbours.add(startNeighbour);
			neighbours.add(endNeighbour);
		}
		
	
	}
	
	public Context getNearestContextBySortedPerceptAndRange(HashMap<String , ArrayList<Context>> sortedPossibleNeigbours, Percept percept, String range) {
		
		
		int indexOfCurrentContext = sortedPossibleNeigbours.get(range).indexOf(this);
		
		if(sortedPossibleNeigbours.get(range).size()>1) {
			
			if((indexOfCurrentContext > 0) && ( indexOfCurrentContext < sortedPossibleNeigbours.get(range).size()-1)) {
				if(range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext+1);
				}
				else if(range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext-1);
				}
				else {
					return null;
				}
			}
			
			else if(indexOfCurrentContext == 0 ) {
				if(range.equals("start")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext+1);
				}
				else {
					return null;
				}
			}
			
			else if( indexOfCurrentContext == sortedPossibleNeigbours.get(range).size()-1)  {
				if(range.equals("end")) {
					return sortedPossibleNeigbours.get(range).get(indexOfCurrentContext-1);
				}
				else {
					return null;
				}
			}
			
			else {
				return null;
			}
			
		}
		else {
			return null;
		}

		
			
		
		
	}
























	



	 

}
