package mas.agents.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import mas.agents.percept.Percept;

// TODO: Auto-generated Javadoc
/**
 * The Class Experiment.
 */
public class Experiment implements Serializable {

	/** The values. */
	private LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
	
	/** The proposition. */
	private double proposition;

	
	/**
	 * Instantiates a new experiment.
	 */
	public Experiment() {
		
	}

	/**
	 * Gets the ranges.
	 *
	 * @return the ranges
	 */
	public HashMap<Percept, Double> getRanges() {
		return values;
	}

	/**
	 * Sets the values.
	 *
	 * @param ranges the ranges
	 */
	public void setValues(LinkedHashMap<Percept, Double> ranges) {
		this.values = ranges;
	}
	
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
