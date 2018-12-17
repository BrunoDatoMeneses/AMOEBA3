package experiments.droneControl;

import java.util.LinkedList;

public class LIST {
	
	private LinkedList<Double> list = new LinkedList<Double>();
	
	public LIST(){
		
		
		
	}
	
	public void add(double value){
		list.add(new Double(value));


	}
	
	public double getSum(){
		double sum = 0.0d;
		
		for (int i = 0; i < list.size(); i++){
			sum += list.get(i);
		}
		
		return sum;
	}
	
	public double getMean(){
		return this.getSum()/list.size();
	}
	
	public double getVariance(){
		double var = 0.0d;
		
		for (int i = 0; i < list.size(); i++){
			var +=  Math.pow(list.get(i) - this.getMean(), 2);
		}
		
		return var/list.size();
	}
	
	public void display(){
		System.out.println(list);
	}

}
