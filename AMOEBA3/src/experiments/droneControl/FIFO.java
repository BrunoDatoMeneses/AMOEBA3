package experiments.droneControl;

import java.util.LinkedList;

public class FIFO {

	private LinkedList<Double> list = new LinkedList<Double>();
	private int size;
	
	public FIFO(int listSize){
		size = listSize;
		
		
	}
	
	public void add(double value){
		if(list.size()<size){
			list.add(new Double(value));
		}
		else{
			list.removeFirst();
			list.add(new Double(value));
		}
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
	
	/*public static void main(String args[]) {
        char arr[] = {3,1,4,1,5,9,2,6,5,3,5,8,9};
        LinkedList<Integer> fifo = new LinkedList<Integer>();

        for (int i = 0; i < arr.length; i++)
            fifo.add (new Integer (arr[i]));

        System.out.print (fifo.removeFirst() + ".");
        
        fifo.add (new Integer (666));
        while (! fifo.isEmpty())
            System.out.print (fifo.removeFirst());
        System.out.println();
    }*/
	
}
