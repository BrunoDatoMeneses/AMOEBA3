package agents.percept;

import java.util.ArrayList;
import java.util.HashMap;

import agents.AmoebaAgent;
import agents.AmoebaMessage;
import agents.MessageType;
import agents.context.Context;
import kernel.AMOEBA;

public class Percept extends AmoebaAgent {

	//protected ArrayList<Agent> activatedContext = new ArrayList<Agent>(); never updated -> removed (see get/setActivatedContext)
	
	public HashMap<Context, ContextProjection> contextProjections = new HashMap<Context, ContextProjection>();
	public ArrayList<Context> validContextProjection = new ArrayList<Context>();
	
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	
	private double oldValue;
	private double value;
	
	public Percept(AMOEBA amas) {
		super(amas);
	}
	
	//TODO copy constructor. Is it really useful ?
	
	@Override
	protected void onAct() { //play
		oldValue = value;
		value = 0; //TODO getPerceptionsOrAction(this.name) in world or in scheduler ?
		ajustMinMax();
		computeContextProjectionValidity();
	}
	
	//displayContextProjections never used -> removed
	
	public void computeContextProjectionValidity() {
		validContextProjection = new ArrayList<Context>();
		
		for(ContextProjection contextProjection : contextProjections.values()) {
			if(contextProjection.contains(this.value)) {
				validContextProjection.add(contextProjection.getContext());
			}
		}
		
		for(Context context : validContextProjection) {
			context.setPerceptValidity(this); //TODO we may want to use a message instead
		}
		
	}
	
	/**
	 * Allow the percept to record the lower and higher value perceived.
	 */
	public void ajustMinMax() {
		if (value < min) min = value;
		if (value > max) max = value;
		
		/* In order to avoid big gap in min-max value in order to adapt with the system dynamic
		 * It's also a warranty to avoid to flaw AVT with flawed value */
		double dist = max - min;
		min += 0.05*dist;
		max -= 0.05*dist;
	}
	
	@Override
	public void computeAMessage(AmoebaMessage m) {
		//TODO the original code has no use, I don't know what it was supposed to do
		// We may want to add things here in the future.
	}
	
	/**
	 * Gets the min max distance.
	 *
	 * @return the min max distance
	 */
	public double getMinMaxDistance() {
		if (min == Double.MAX_VALUE || max == Double.MIN_VALUE) return 0;
		return Math.abs(max - min);
	}
	
	//getMin never used -> removed
	
	//getMax never used -> removed
	
	//getActivatedContext use activatedContext, but it is never updated -> removed (suspected inutility)
	
	//setActivatedContext never used -> removed (this impact getActivatedContext)
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	
	//setValue never used -> removed
	
	//TODO getTargets : related to GrapheSystemPanel.update() -> check usefulness
	
	//setTargets never used -> removed
	
	//getOldValue never used -> removed
	
	//setOldValue never used -> removed
	
	//isEnum use isEnum (a class variable), which is never updated -> removed
	
	//setEnum nerver used -> removed
	
	//swapListElements never used -> removed
	
	/*
	 * Context projection methods
	 */
	
	public void addContextProjection(Context context) {
		ContextProjection newContextProjection = new ContextProjection(this, context);
		contextProjections.put(context, newContextProjection);
	}
	
	public void deleteContextProjection(Context context) {
		contextProjections.remove(context);
	}
	
	public void updateContextProjectionStart(Context context) {
		contextProjections.get(context).updateStart();
	}
	
	public void updateContextProjectionEnd(Context context) {
		contextProjections.get(context).updateEnd();
	}
	
	//overlapBetweenContexts never used -> removed
	
	//getRangeProjection never used -> removed
	
	//getEndRangeProjection never used outside Percept, should be private
	
	//getStartRangeProjection never used outside Percept, should be private
	
	//getOverlapRangesBetweenContexts never used -> removed
	
	//contextIncludedIn used only in getOverlapRangesBetweenContexts -> removed
	
	//contextOrder used only in getOverlapRangesBetweenContexts -> removed	

	@Override
	protected int computeExecutionOrderLayer() {
		return 0;
	}

}
