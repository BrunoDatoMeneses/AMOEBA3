package experiments.Tests;

import java.util.ArrayList;

import org.apache.commons.math3.exception.OutOfRangeException;

import mas.agents.context.Context;



public class Test {

	public static ArrayList<Integer> list = new ArrayList<Integer>();
	
	public static void swapListElements(ArrayList<Integer> list, int indexFirstElement) {
		try {
			list.add(indexFirstElement, list.get(indexFirstElement+1));
			//System.out.println(list);
			list.remove(indexFirstElement+2);
		} catch (OutOfRangeException e) {
			// TODO: handle exception
		}
		
	}
	
	
	public static void updateSortedRanges(Integer context, String range) {
		int contextIndex = list.indexOf(context);
		boolean rightPlace = false;
		
		if(contextIndex<list.size()-1) {
			
			if(list.get(contextIndex) > list.get(contextIndex+1)) {
				
				while(contextIndex<list.size()-1 && !rightPlace){
					if((list.get(contextIndex) > list.get(contextIndex+1))) {
						swapListElements(list, contextIndex);
						contextIndex +=1;
						
						if(contextIndex<list.size()-1) {
							if(list.get(contextIndex) < list.get(contextIndex+1)) {
								rightPlace = true;
							}
						}
						else {
							rightPlace = true;
						}
						
						
					}
						
				}
			}
			
			
		}

		if(contextIndex>0) {
			
			rightPlace = false;
			
			if(list.get(contextIndex) < list.get(contextIndex-1)) {
				
				while(contextIndex> 0 && !rightPlace){
					if(list.get(contextIndex) < list.get(contextIndex-1)) {
						swapListElements(list, contextIndex -1);
						contextIndex -=1;
						
						if(contextIndex> 0) {
							if(list.get(contextIndex) > list.get(contextIndex-1)) {
								rightPlace = true;
							}
						}
						else {
							rightPlace = true;
						}
						
						
					}
						
				}
			}
		}
		
		
		
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		list.add(9);
		list.add(2);
		list.add(4);
		list.add(6);
		list.add(8);
		list.add(10);
		
		System.out.println(list);
		
		updateSortedRanges(9, "");
		
		System.out.println(list);
		
		
		
		
	}

}
