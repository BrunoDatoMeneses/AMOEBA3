package experiments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import agents.head.EndogenousRequest;
 



public class TestMain {
	
	
	public static double fact(double n) {
		
		if(n==0) {
			return 1;
		}
		else {
			return n*fact(n-1);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		testPriorityQueue();
		

		
	}
	
	public static void testPriorityQueue() {
		
		Queue<EndogenousRequest> endogenousRequest = new PriorityQueue<EndogenousRequest>(new Comparator<EndogenousRequest>(){
			   public int compare(EndogenousRequest r1, EndogenousRequest r2) {
				      return r2.getPriority().compareTo(r1.getPriority());
				   }
				});
		
		HashMap<String, Double> hm1 = new HashMap<String, Double>();
		hm1.put("p1", 50.0);
		HashMap<String, Double> hm2 = new HashMap<String, Double>();
		hm2.put("p2", 50.0);
		HashMap<String, Double> hm3 = new HashMap<String, Double>();
		hm3.put("p3", 50.0);
		HashMap<String, Double> hm4 = new HashMap<String, Double>();
		hm4.put("p4", 50.0);
		HashMap<String, Double> hm5 = new HashMap<String, Double>();
		hm5.put("p5", 50.0);
		HashMap<String, Double> hm6 = new HashMap<String, Double>();
		hm6.put("p6", 50.0);
		HashMap<String, Double> hm7 = new HashMap<String, Double>();
		hm7.put("p7", 50.0);
//		endogenousRequest.add(new EndogenousRequest(hm1, 5));
//		endogenousRequest.add(new EndogenousRequest(hm2, 3));
//		endogenousRequest.add(new EndogenousRequest(hm3, 1));
//		endogenousRequest.add(new EndogenousRequest(hm4, 2));
//		endogenousRequest.add(new EndogenousRequest(hm5, 0));
//		endogenousRequest.add(new EndogenousRequest(hm6, 5));
//		endogenousRequest.add(new EndogenousRequest(hm7, 3));
		
		System.out.println(endogenousRequest);
		
		while(endogenousRequest.size()>0){
			System.out.println(endogenousRequest.poll());
		}
		System.out.println(endogenousRequest.poll());
	}
	
	
	public static void testBruitGaussien() {
		
		double noiseVariance = 0.1;
		double noiseMean = 0;
		
		double max = 0;
		
		java.util.Random r = new java.util.Random();
		
		for(int i =0;i<100000;i++) {
			double noise = r.nextGaussian() * Math.sqrt(noiseVariance) + noiseMean;
			System.out.println(noise);
			if(Math.abs(noise)>max) {
				max = Math.abs(noise);
			}
		}
		
		System.out.println("-->" + max);
		
		
		
	}
	
	
	public static void testCombinaisons() {
		ArrayList<String> lettres = new ArrayList<String>();
		
		lettres.add("a");
		lettres.add("b");
		lettres.add("c");
		lettres.add("d");
		lettres.add("e");
		lettres.add("f");
		
		ArrayList<String> combinaisons = new ArrayList<String>();
		
		int i = 1;
		for(String lettre : lettres ) {
					
					for(String otherlettre : lettres.subList(i, lettres.size())) {
						
						combinaisons.add(lettre + otherlettre);
						
					}
					i++;
		
					
				}
				
		for(String comb : combinaisons) {
			System.out.println(comb);
		}
		System.out.println(combinaisons.size());
		System.out.println(fact(lettres.size()) / (2*(fact(lettres.size()-2))));
	}
	
	public static void testCompteurTailleN() {
		
		int[] countIndices = new int[3];
		int[] bounds = new int[3];
		
		countIndices[0]=0;
		countIndices[1]=0;
		countIndices[2]=0;
		
		bounds[0]=1;
		bounds[1]=3;
		bounds[2]=2;
		
		boolean test = true;
		
		int i = 0;
		
		System.out.println(test + " "  + countIndices[0]  + " "  + countIndices[1]  + " "  + countIndices[2] );
		
		while(i<100) {
			
			test =  nextMultiDimCounter(countIndices,bounds);
			
			System.out.println(test + " "  + countIndices[0]  + " "  + countIndices[1]  + " "  + countIndices[2]  );
			
			i++;
		}
		
	}
	
	public static boolean nextMultiDimCounter(ArrayList<Integer> indices, ArrayList<Integer> bounds){
		
		
		
		for(int i = 0; i<indices.size();i++) {
			
			if(indices.get(i)==bounds.get(i)-1) {
				if(i==indices.size()-1) {
					indices.set(i, 0);
					return false;
				}
				else {
					indices.set(i, 0);
				}				
			}
			else {
				indices.set(i, indices.get(i)+1);
				return true;
			}
			
		}
		
		return false;

		
	}
	
	private static boolean nextMultiDimCounter(int[] indices, int[] bounds){
		
		
		
		for(int i = 0; i<indices.length;i++) {
			
			if(indices[i]==bounds[i]-1) {
				if(i==indices.length-1) {
					indices[i]=0;
					return false;
				}
				else {
					indices[i]=0;
				}				
			}
			else {
				indices[i] += 1;
				return true;
			}
			
		}
		
		return false;

		
	}

}
