package mas.agents.head;

import java.util.ArrayList;
import java.util.HashMap;

public class Criticalities implements Cloneable{
	
	HashMap<String, Double> currentValues;
	
	HashMap<String, Double> currentMeanValues;
	
	HashMap<String, ArrayList<Double>> lastValues;
	
	int temporalWindowSize;
	
	
	
	public Criticalities(int temporalWindowSizeValue) {
		
		temporalWindowSize = temporalWindowSizeValue;
		currentValues = new HashMap<String, Double>();
		currentMeanValues = new HashMap<String, Double>();
		lastValues = new HashMap<String, ArrayList<Double>>();
		
		
	}
	

	
	public void addCriticality(String name, double value) {
		currentValues.put(name, value);
		
		if(lastValues.get(name)==null) {
			lastValues.put(name, new ArrayList<Double>());
		}
		lastValues.get(name).add(value);
		
		
		if (lastValues.get(name).size() >= temporalWindowSize) {
			lastValues.get(name).remove(0);
		}
	}
	
	public double getCriticality(String name) {
		return currentValues.get(name);
	}
	
	public Double getCriticalityMean(String name) {
		return currentMeanValues.get(name);
	}
	
	public void updateMeans() {
		
		for(String criticalityName : currentValues.keySet()) {
			
			double valuesSum = 0.0;
			for (Double d : lastValues.get(criticalityName)) {
				valuesSum += d;
			}

			currentMeanValues.put(criticalityName,valuesSum / lastValues.get(criticalityName).size());
			
			
			
		}
		
		
	}

}
