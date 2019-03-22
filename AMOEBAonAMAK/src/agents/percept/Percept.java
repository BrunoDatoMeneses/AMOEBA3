package agents.percept;

import java.util.ArrayList;
import java.util.HashMap;

import agents.AmoebaAgent;
import agents.context.Context;
import kernel.AMOEBA;

public class Percept extends AmoebaAgent {
	private HashMap<Context, ContextProjection> contextProjections = new HashMap<Context, ContextProjection>();
	private ArrayList<Context> validContextProjection = new ArrayList<Context>();

	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;

	private double value;
	private boolean isEnum;

	public Percept(AMOEBA amas) {
		super(amas);
		this.isEnum = false;
	}

	@Override
	protected void onAct() { // play
		value = amas.getPerceptionsOrAction(name);
		ajustMinMax();
		computeContextProjectionValidity();
	}

	public void computeContextProjectionValidity() {
		validContextProjection = new ArrayList<Context>();

		for (ContextProjection contextProjection : contextProjections.values()) {
			if (contextProjection.contains(this.value)) {
				validContextProjection.add(contextProjection.getContext());
			}
		}
		for (Context context : validContextProjection) {
			context.setPerceptValidity(this);
		}

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
	
	public void setEnum(boolean isEnum) {
		this.isEnum = isEnum;
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
	 * Checks if is enum.
	 *
	 * @return true, if is enum
	 */
	public boolean isEnum() {
		return isEnum;
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
	protected int computeExecutionOrderLayer() {
		return 0;
	}
}
