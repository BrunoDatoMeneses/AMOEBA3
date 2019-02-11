package agents.context;

import java.util.LinkedHashMap;

import agents.percept.Percept;

/**
 * The Class Experiment.
 */
public class Experiment {

	/** The values. */
	private LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
	
	/** The proposition. */
	private double proposition;

	
	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public LinkedHashMap<Percept, Double> getValues() {
		return values;
	}


	/**
	 * Adds the dimension.
	 *
	 * @param p the p
	 * @param d the d
	 */
	public void addDimension(Percept p, double d) {
		values.put(p, d);
	}


	/**
	 * Gets the proposition.
	 *
	 * @return the proposition
	 */
	public double getProposition() {
		return proposition;
	}


	/**
	 * Sets the proposition.
	 *
	 * @param proposition the new proposition
	 */
	public void setProposition(double proposition) {
		this.proposition = proposition;
	}
	
	/**
	 * Gets the values as array.
	 *
	 * @return the values as array
	 */
	public double[] getValuesAsArray() {
		double [] tab = new double[values.size()];
		int i = 0;
		for (Percept p : values.keySet()) {
			tab[i] = values.get(p);
			i++;
		}
		
		return tab;
	}
}
