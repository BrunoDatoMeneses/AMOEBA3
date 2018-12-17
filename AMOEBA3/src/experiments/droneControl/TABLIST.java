package experiments.droneControl;

import java.util.LinkedList;

public class TABLIST {

	private LinkedList<Double[]> list = new LinkedList<Double[]>();
	
	public TABLIST(){
		
		
		
	}
	
	public void add(Double[] value){
		list.add(value);


	}
	
	public double[] getSum(){
		double[] sum = new double[list.get(0).length];
		
		for(int j=0; j<list.get(0).length; j++){
			for (int i = 0; i < list.size(); i++){
				sum[j] += list.get(i)[j];
			}
			
		}
		
		return sum;
	}
	
	public int size(){
		return list.size();
	}
	
	
	public double[] getMean(){
		if(list.size()>0){
			double[] mean = new double[list.get(0).length];
			double[] sum = getSum();
			
			for(int j=0; j<list.get(0).length; j++){
				mean[j] = sum[j]/list.size();
			}
			
			return mean;
		}
		else return null;
	}
	
	public double[] getVariance(){
		if(list.size()>0){
			double[] var = new double[list.get(0).length];
			double[] mean = getMean();
			
			for(int j=0; j<list.get(0).length; j++){
				for (int i = 0; i < list.size(); i++){
					var[j] +=  Math.pow(list.get(i)[j] - mean[j], 2);
				}
				var[j] /= list.size();
			}
			
			return var;
		}
		else return null;
		
	}
	
	public void display(){
		System.out.println(list);
	}
	
}
