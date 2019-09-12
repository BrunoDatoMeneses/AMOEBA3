package agents.context.localModel;

import agents.context.Context;
import agents.percept.Percept;

/**
 * The abstract class of all agents in charge of the generation of the output
 * from Context Agent. For the sake of simplicity, it's not scheduled as agent
 * like other of the system.
 */
public abstract class LocalModel {

	/**
	 * Instantiates a new local model agent.
	 */
	public LocalModel() {
	}

	/**
	 * Update model.
	 *
	 * @param context the context
	 */
	public abstract void updateModel(Context context);

	/**
	 * Gets the proposition.
	 *
	 * @param context the context
	 * @return the proposition
	 */
	public abstract double getProposition(Context context);

	/**
	 * Version of getProposition for 2D display.
	 *
	 * @param context the context
	 * @param p1      the p 1
	 * @param p2      the p 2
	 * @param v1      the v 1
	 * @param v2      the v 2
	 * @return the proposition
	 */
	public abstract double getProposition(Context context, Percept p1, Percept p2, double v1, double v2);

	/**
	 * Gets the formula.
	 *
	 * @param context the context
	 * @return the formula
	 */
	public abstract String getFormula(Context context);

	public abstract String getCoefsFormula();

	public abstract double[] getCoefs();

	public abstract TypeLocalModel getType();
}
