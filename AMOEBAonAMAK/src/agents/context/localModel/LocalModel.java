package agents.context.localModel;

import java.util.ArrayList;
import java.util.HashMap;

import agents.context.Context;
import agents.context.Experiment;

/**
 * A LocalModel is used by a Context to store information and generate prediction.
 */
public abstract class LocalModel {
	
	protected LocalModel modifier = null;
	protected LocalModel modified = null; // Be careful ! One letter and it's totally a different thing !
	
	
	/**
	 * Sets the context that use the LocalModel
	 * @param context
	 */
	public abstract void setContext(Context context);
	
	/**
	 * gets the context that use the LocalModel
	 * @return
	 */
	public abstract Context getContext();
	
	/**
	 * Gets the proposition.
	 *
	 * @return the proposition
	 */
	public abstract double getProposition();
	
	/**
	 * Gets the proposition with the highest value possible
	 * @return
	 */
	public abstract double getMaxProposition();
	
	/**
	 * Return the point (percept value) that produce the max proposition, considering some percepts are fixed.
	 * @return a HashMap with percept names as key, and their corresponding value. The oracle is the max proposition
	 * @see LocalModel#getMaxProposition(Context)  
	 */
	public abstract HashMap<String, Double> getMaxWithConstraint(HashMap<String, Double> fixedPercepts);;
	
	/**
	 * Gets the proposition with the lowest value possible
	 * @return
	 */
	public abstract double getMinProposition();

	/**
	 * Gets the formula of the model
	 * @return
	 */
	public String getCoefsFormula() {
		Double[] coefs = getCoef();
		String result = "" +coefs[0];
		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			
			result += "\t" + coefs[i] + " (" + getContext().getAmas().getPercepts().get(i-1) +")";
			
		}
		
		return result;
	}
	
	/**
	 * Update the model with a new experiment.
	 * @param newExperiment
	 * @param weight the weight of the new experiment in the compute of the model
	 */
	public abstract void updateModel(Experiment newExperiment, double weight);
	
	public String coefsToString() {
		String coefsString = "";
		Double[] coefs = getCoef();
		if(coefs != null) {
			for(int i=0; i<coefs.length; i ++) {
				coefsString += coefs[i]  + "\t";
			}
		}
		return coefsString;
	}
	
	/**
	 * The distance between an experiment and the model.
	 * @param experiment
	 * @return
	 */
	public abstract double distance(Experiment experiment);
	
	/**
	 * Gets the experiments used to properly initialize the model.
	 * @return
	 */
	public abstract ArrayList<Experiment> getFirstExperiments();
	
	/**
	 * Sets the experiments used to properly initialize the model.
	 * This may not trigger an update of the model.
	 * @param frstExp
	 */
	public abstract void setFirstExperiments( ArrayList<Experiment> frstExp);
	
	/**
	 * Tells if the model has enough experiments to produce a good prediction.
	 * For example, a regression need a number of experiments equals or superior to the number of dimension.
	 * @return
	 */
	public abstract boolean finishedFirstExperiments();
	
	/**
	 * Gets coefficients of the model
	 * @return
	 */
	public abstract Double[] getCoef();
	
	/**
	 * Sets coefficients of the model
	 * @return
	 */
	public abstract void setCoef(Double[] coef);

	/**
	 * Gets the {@link TypeLocalModel} corresponding to this LocalModel
	 */
	public abstract TypeLocalModel getType();
	
	/**
	 * Sets the type of the LocalModel, if it ever has to change.
	 */
	public abstract void setType(TypeLocalModel type);
	
	/**
	 * Set an LocalModel that modify the behavior of the current LocalModel.<br/>
	 * The modifier can then be used by calling {@link LocalModel#getModifier()} on the modified LocalModel.
	 * @param Modifier a LocalModel
	 * @see LocalModel#getModifier()
	 * @see LocalModel#hasModifier()
	 */
	public void setModifier(LocalModel modifier) {
		this.modifier = modifier;
		modifier.modified = this;
	}
	
	/**
	 * @return true if the LocalModel has an usable modifier.
	 */
	public boolean hasModifier() {
		return modifier != null;
	}
	
	/**
	 * Get the LocalModel that modify the behavior of the current LocalModel.
	 * @return a LocalModel or null
	 */
	public LocalModel getModifier() {
		return modifier;
	}
	
	/**
	 * 
	 * @return true if the LocalModel is a modifier, it means that {@link LocalModel#getModified()} is not null
	 */
	public boolean hasModified() {
		return modified != null;
	}
	
	/**
	 * If the LocalModel is a modifier, return the modified LocalModel
	 * @return a LocalModel or null
	 */
	public LocalModel getModified() {
		return modified;
	}
}
