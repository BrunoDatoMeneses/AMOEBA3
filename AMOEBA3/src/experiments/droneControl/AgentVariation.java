package experiments.droneControl;

import java.util.HashMap;

public class AgentVariation {
	private double criticity;
	private double maxRange;
	private double min;
	private double max;
	private int incrementalSign;
	private String name;
	
	public AgentVariation(String agentName){
		criticity = 0.0d;
		
		max = 200.0d;
		min = -200.0d;
		name = agentName;
		maxRange = max - min;
		
	}
	
	public void update(double perceptVariation){
		
		if(perceptVariation > max) {
			max = perceptVariation;
			maxRange = Math.abs(max-min);
		}
		if(perceptVariation < min) {
			min = perceptVariation;
			maxRange = Math.abs(max-min);
		}
		
		
		criticity = Math.abs(perceptVariation)/maxRange;
		
		Double variationDirection = (perceptVariation/Math.abs(perceptVariation));
		incrementalSign = variationDirection.intValue();
	}
	
public void adapt(double currentPerception, double wantedPerception, double goalDirection){
		
		if(currentPerception > max) {
			max = currentPerception;
			maxRange = Math.abs(max-min);
		}
		if(currentPerception < min) {
			min = currentPerception;
			maxRange = Math.abs(max-min);
		}
		
		
		//criticity = Math.abs(wantedPerception - currentPerception)/maxRange;
		criticity = Math.abs(wantedPerception - currentPerception);
		Double variationDirection = ((wantedPerception - currentPerception)/Math.abs(wantedPerception - currentPerception));
		if(goalDirection>0){
			variationDirection = ((wantedPerception - currentPerception)/Math.abs(wantedPerception - currentPerception));
		}else if(goalDirection<0){
			variationDirection = -((wantedPerception - currentPerception)/Math.abs(wantedPerception - currentPerception));
		}
		incrementalSign = variationDirection.intValue();
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println(name);
		System.out.println("WANTED PERCEPTION\t"+ wantedPerception);
		System.out.println("CURRENT PERCEPTION\t"+ currentPerception);
		System.out.println("MAX RANGE\t"+ maxRange);
		System.out.println("VARIATION\t"+ variationDirection);
		System.out.println("CRITICITY\t"+ criticity);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		
	}

	public HashMap<String, Double> getState(){
		HashMap<String, Double> agentState = new HashMap<String, Double>();
		
		agentState.put("Criticity", criticity);
		agentState.put("IncrementalSign", (double)incrementalSign);
		
		return agentState;
	}


	public double getCriticity(){
		return criticity;
	}
	
	public String getName(){
		return name;
	}
}
