package agents.context.localModel;

import agents.context.Context;
import agents.percept.Percept;
import kernel.AMOEBA;

// TODO: Auto-generated Javadoc
/**
 * The abstract class of all agents in charge of the generation of the output from Context Agent.
 * For the sake of simplicity, it's not scheduled as agent like other of the system.
 */
public abstract class LocalModel implements Cloneable{

	/**
	 * Instantiates a new local model agent.
	 *
	 */
	public LocalModel() {
		
	}
	
	/**
	 * Gets the proposition.
	 *
	 * @param context the context
	 * @return the proposition
	 */
	public abstract double getProposition(AMOEBA amoeba,Context context);
	
	
	/**
	 * Version of getProposition for 2D display.
	 *
	 * @param context the context
	 * @param p1 the p 1
	 * @param p2 the p 2
	 * @param v1 the v 1
	 * @param v2 the v 2
	 * @return the proposition
	 */
	public abstract double getProposition(AMOEBA amoeba,Context context, Percept p1, Percept p2, double v1, double v2);

	/**
	 * Gets the formula.
	 *
	 * @param context the context
	 * @return the formula
	 */
	public abstract String getFormula(AMOEBA amoeba,Context context);
	public abstract String getCoefsFormula();

	/**
	 * Update model.
	 *
	 * @param context the context
	 */
	public abstract void updateModel(Context context);
	
	public abstract double[] getCoef();
	
	public LocalModel clone() throws CloneNotSupportedException{
		return (LocalModel)super.clone();
	}

}
