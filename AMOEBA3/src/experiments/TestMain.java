package experiments;

import java.util.ArrayList;

import mas.agents.context.Context;

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

}
