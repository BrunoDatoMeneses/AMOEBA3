package experiments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import agents.context.Context;
import agents.context.Experiment;
import agents.head.EndogenousRequest;
import fr.irit.smac.amak.tools.Log.Level;
 



public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub


		tableaux();

	}
	
	public static double fact(double n) {
		
		if(n==0) {
			return 1;
		}
		else {
			return n*fact(n-1);
		}
	}
	
	public enum Level {
		FATAL(60), ERROR(50), IMPORTANT(40), WARNING(30), INFORM(20), DEBUG(10);

		private final int order;

		Level(final int order) {
			this.order = order;
		}

		public boolean isGE(final Level _other) {
			return order >= _other.order;
		}
	}
	
	public static Level minLevel = Level.IMPORTANT;
	
	private static void trace(final Level _level, final String _message) {
			if (_level.isGE(minLevel)) {
				System.out.println(_message);
			}
	}
	
	public static void boucle2a2(){

		ArrayList<String> test = new ArrayList<>();
		for(int j=0;j<10;j++)
			test.add(""+j);

		int i = 1;
		for (String s : test) {
			for (String otherS : test.subList(i, test.size())) {
				System.out.println(s+otherS);

			}
			i++;
		}

	}


	public static  void logic(){

		boolean initTest = true;
		boolean test1 = initTest;
		boolean test2 = initTest;
		for(int i = 0; i<10;i++){
			boolean localTest;
			//boolean localTest = false;
			if(i==5) localTest = !initTest;
			else localTest = initTest;
			test1 = test1 && localTest;
			test2 = test2 || localTest;
		}

		System.out.println(test1 + " " + test2);

	}

	public static  void multiBoucle(double[] compteur, double[] limites){





	}

	public void incrementerCompteur(double[] compteur, double[] limites){


		/*int i = 0;
		boolean inc = false;
		while(i<compteur.length || inc){

			if(compteur[i]<limites[i]){
				compteur[i]+=1;
				inc = true;
			}

		}
		for(int i=0;i<compteur.length;i++){

			if (compteur[i]==)
		}*/

	}
	
	public static void testLvlTrace() {
		trace(Level.DEBUG, "DEBUG");
		trace(Level.WARNING, "WARNING");
		trace(Level.ERROR, "ERROR");
		trace(Level.FATAL, "FATAL");
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
		
		bounds[0]=2;
		bounds[1]=3;
		bounds[2]=4;
		
		boolean test = true;
		

		
		System.out.println(test + " "  + countIndices[0]  + " "  + countIndices[1]  + " "  + countIndices[2] );
		
		while(test) {
			
			test =  nextMultiDimCounter(countIndices,bounds);
			
			System.out.println(test + " "  + countIndices[0]  + " "  + countIndices[1]  + " "  + countIndices[2]  );

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

	private static void gaussianDistribution(){

		java.util.Random r = new java.util.Random();
		double variance = 3.0/5;
		double mean = 0.0;
		for(int i =0;i<10;i++){
			double noise = r.nextGaussian() * Math.sqrt(variance) + mean;
			System.out.println(noise);
		}



	}

	private static void tableaux(){

		double[] tab1 = new double[2];
		tab1[0] = 1;
		tab1[1] = 2;
		System.out.println(tab1[0] + "" + tab1[1]);
		double[] tab2 = tab1.clone();
		tab1[0] = 3;
		tab1[1] = 4;
		System.out.println(tab1[0] + "" + tab1[1]);
		System.out.println(tab2[0] + "" + tab2[1]);
	}

}
