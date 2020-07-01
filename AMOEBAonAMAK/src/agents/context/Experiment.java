package agents.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

import agents.context.Context;
import agents.percept.Percept;

/**
 * The Class Experiment.
 */
public class Experiment implements Serializable {

	/** The values. */
	private LinkedHashMap<Percept, Double> values = new LinkedHashMap<Percept, Double>();
	
	/** The proposition. */
	private double oracleProposition;

	private Context context;
	
	/**
	 * Instantiates a new experiment.
	 */
	public Experiment(Context ctxt) {
		context = ctxt;
	}

	/**
	 * Gets the ranges.
	 *
	 * @return the ranges
	 */
	public HashMap<Percept, Double> getValuesAsHashMap() {
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
	public LinkedHashMap<Percept, Double> getValuesAsLinkedHashMap() {
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
	public double getOracleProposition() {
		return oracleProposition;
	}


	/**
	 * Sets the proposition.
	 *
	 * @param proposition the new proposition
	 */
	public void setOracleProposition(double oracleProposition) {
		this.oracleProposition = oracleProposition;
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
	
	
	public String toString() {
		String string = "";
		for(Percept pct : values.keySet()) {
			string += pct.getName() + " " + values.get(pct) + " ";
		}
		string += "Oracle : " + oracleProposition ;
		return string;
	}
	
	@Override
	public boolean equals(Object obj) {
		// if it look like a duck, and quack like a duck, it's a duck
		return getClass().equals(obj.getClass()) && toString().equals(obj.toString());
	}

	public double distance(Experiment exp){
		double distance = 0;

		for(Percept pct  : values.keySet()){
			distance +=  Math.pow(values.get(pct)-exp.values.get(pct),2);
		}
		return Math.sqrt(distance);
	}

	public double distance(HashMap<Percept, Double> request){
		double distance = 0;

		for(Percept pct  : values.keySet()){
			distance +=  Math.pow(values.get(pct)-request.get(pct),2);
		}
		return Math.sqrt(distance);
	}

}
