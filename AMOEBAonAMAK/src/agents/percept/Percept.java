package agents.percept;

import java.util.HashMap;
import java.util.HashSet;

import agents.AmoebaAgent;
import agents.context.Context;
import kernel.AMOEBA;

/**
 * Percept agent is in charge of the communication with the environment.
 * Each Percept agent must be connected to one data source.
 *
 */
public class Percept extends AmoebaAgent {
	private HashMap<Context, ContextProjection> contextProjections = new HashMap<Context, ContextProjection>();
	private HashSet<Context> validContextProjection = new HashSet<Context>();

	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	private double value;
	private boolean isEnum;

	public Percept(AMOEBA amas) {
		super(amas);
		this.isEnum = false;
	}

	@Override
	protected void onAct() {
		value = amas.getPerceptionsOrAction(name);
		ajustMinMax();
		computeContextProjectionValidity();
	}

	public void computeContextProjectionValidity() {
		validContextProjection = new HashSet<Context>();
		
		// To avoid unnecessary tests, we only compute validity on context
		// validated by percepts that had finished before us
		HashSet<Context> contexts = amas.getValidContexts();
		if(contexts == null) {
			// If we are one of the first percept to run, we compute validity on all contexts
			contexts = new HashSet<>(amas.getContexts());
		}
		
		if(!contexts.isEmpty()) {
			for (Context c : contexts) {
				if (contextProjections.get(c).contains(this.value)) {
					validContextProjection.add(c);
				}
			} 
			amas.updateValidContexts(validContextProjection);
		}
		
		logger().debug("CYCLE "+getAmas().getCycle(), "%s's valid contexts : %s", toString(), validContextProjection.toString());
	}

	/**
	 * Allow the percept to record the lower and higher value perceived.
	 */
	public void ajustMinMax() {
		if (value < min) {
			min = value;
		}
		if (value > max) {
			max = value;
		}

		/*
		 * In order to avoid big gap in min-max value in order to adapt with the system
		 * dynamic It's also a warranty to avoid to flaw AVT with flawed value
		 */
		double dist = max - min;
		min += 0.05 * dist;
		max -= 0.05 * dist;
	}

	/**
	 * Gets the min max distance.
	 *
	 * @return the min max distance
	 */
	public double getMinMaxDistance() {
		if (min == Double.MAX_VALUE || max == Double.MIN_VALUE)
			return 0;
		return Math.abs(max - min);
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	
	/**
	 * Set the value of the percept.
	 * Useful when loading a save.
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Checks if is enum.
	 *
	 * @return true, if is enum
	 */
	public boolean isEnum() {
		return isEnum;
	}
	
	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
	}

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
	@Override
	public String toString() {
		return getName();
	}
}
