package agents.context.localModel;

import java.util.ArrayList;
import java.util.HashMap;

import agents.context.Context;
import agents.context.Experiment;

/**
 * A LocalModel is used by a Context to store information and generate prediction.
 */
public interface LocalModel {

	/**
	 * Sets the context that use the LocalModel
	 * @param context
	 */
	public void setContext(Context context);
	
	/**
	 * gets the context that use the LocalModel
	 * @return
	 */
	public Context getContext();
	
	/**
	 * Gets the proposition.
	 *
	 * @return the proposition
	 */
	public double getProposition();
	
	/**
	 * Gets the proposition with the highest value possible
	 * @return
	 */
	public double getMaxProposition();
	
	/**
	 * Return the point (percept value) that produce the max proposition, considering some percepts are fixed.
	 * @return a HashMap with percept names as key, and their corresponding value. The oracle is the max proposition
	 * @see LocalModel#getMaxProposition(Context)  
	 */
	public HashMap<String, Double> getMaxWithConstraint(HashMap<String, Double> fixedPercepts);;
	
	/**
	 * Gets the proposition with the lowest value possible
	 * @return
	 */
	public double getMinProposition();

	/**
	 * Gets the formula of the model
	 * @return
	 */
	public String getCoefsFormula();
	
	/**
	 * Update the model with a new experiment.
	 * @param newExperiment
	 * @param weight the weight of the new experiment in the compute of the model
	 */
	public void updateModel(Experiment newExperiment, double weight);
	
	public String coefsToString();
	
	/**
	 * The distance between an experiment and the model.
	 * @param experiment
	 * @return
	 */
	public double distance(Experiment experiment);
	
	/**
	 * Gets the experiments used to properly initialize the model.
	 * @return
	 */
	public ArrayList<Experiment> getFirstExperiments();
	
	/**
	 * Sets the experiments used to properly initialize the model.
	 * This may not trigger an update of the model.
	 * @param frstExp
	 */
	public void setFirstExperiments( ArrayList<Experiment> frstExp);
	
	/**
	 * Tells if the model has enough experiments to produce a good prediction.
	 * For example, a regression need a number of experiments equals or superior to the number of dimension.
	 * @return
	 */
	public boolean finishedFirstExperiments();
	
	/**
	 * Gets coefficients of the model
	 * @return
	 */
	public Double[] getCoef();
	
	/**
	 * Sets coefficients of the model
	 * @return
	 */
	public void setCoef(Double[] coef);

	/**
	 * Gets the {@link TypeLocalModel} corresponding to this LocalModel
	 */
	public TypeLocalModel getType();
}
