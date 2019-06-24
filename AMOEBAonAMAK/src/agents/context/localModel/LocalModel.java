package agents.context.localModel;

import java.util.ArrayList;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;

/**
 * The abstract class of all agents in charge of the generation of the output
 * from Context Agent. For the sake of simplicity, it's not scheduled as agent
 * like other of the system.
 */
public abstract class LocalModel {

	public Context context;

	/**
	 * Instantiates a new local model agent.
	 */
	public LocalModel(Context associatedContext) {
		context = associatedContext;
	}

	/**
	 * Gets the proposition.
	 *
	 * @param context the context
	 * @return the proposition
	 */
	public abstract double getProposition(Context context);
	public abstract double getProposition(Experiment experiment);
	public abstract double getMaxProposition(Context context);
	public abstract double getMinProposition(Context context);
	

	
	public abstract double getProposition(ArrayList<Experiment> experimentsList, Experiment experiment);
	
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
	public abstract double getProposition(Context context, Percept p1, Percept p2, double v1, double v2);

	/**
	 * Gets the formula.
	 *
	 * @param context the context
	 * @return the formula
	 */
	public abstract String getFormula(Context context);
	public abstract String getCoefsFormula();

	/**
	 * Update model.
	 *
	 * @param context the context
	 */
	public abstract void updateModel(Context context);
	
	public abstract void updateModelWithExperiments(ArrayList<Experiment> experimentsList);
	public abstract void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight, int numberOfPointsForRegression);
	public abstract void updateModel(Experiment newExperiment, double weight, int numberOfPointsForRegression);
	public abstract String coefsToString();
	public abstract double distance(Experiment experiment);
	public abstract ArrayList<Experiment> getFirstExperiments();
	public abstract void setFirstExperiments( ArrayList<Experiment> frstExp);
	public abstract boolean finishedFirstExperiments();
	
	public abstract Double[] getCoef();
	public abstract void setCoef(Double[] coef);

	public abstract TypeLocalModel getType();
}
