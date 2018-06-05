package experiments.Tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.apache.commons.math3.exception.OutOfRangeException;

import mas.agents.context.Context;
import mas.agents.context.Range;
import mas.agents.head.Head;
import mas.agents.percept.ContextProjection;
import mas.agents.percept.Percept;
import mas.kernel.World;

import mas.agents.context.CustomComparator;





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
		Comparator<Bidon> comparator = (Bidon c1, Bidon c2) -> (c1.getStart().compareTo(c2.getStart()));
		ArrayList<Bidon> list = new ArrayList<Bidon>();
		
		World world = new World();
		
		Bidon b1 = new Bidon(world, 200d,400d);
		Bidon b2 = new Bidon(world, 100d,400d);
		Bidon b3 = new Bidon(world, 300d,400d);
		Bidon b4 = new Bidon(world, 500d,400d);
		
	
		

		
		
		list.add(b1);
		list.add(b2);
		list.add(b3);
		list.add(b4);
		
		System.out.println(list);
		
		//list.sort(comparator);
		
		//Collections.sort(list, new CustomComparator());
		
		System.out.println(list);
		
		list.remove(b2);
		
		System.out.println(list);
		
		//b4.start = 0;
		
		//Collections.sort(list, new CustomComparator());
		
		System.out.println(list);
		

		
		
		ContextProjection cp1 = new ContextProjection(1d, 20d);
		ContextProjection cp2 = new ContextProjection(10d, 15d);
		ContextProjection cp3 = new ContextProjection(-3d, 10d);
		ContextProjection cp4 = new ContextProjection(400d, 6d);
		
		ArrayList<ContextProjection> sortedStartProjection = new ArrayList<ContextProjection>();
		
		sortedStartProjection.add(cp1);
		sortedStartProjection.add(cp2);
		sortedStartProjection.add(cp3);
		sortedStartProjection.add(cp4);

		System.out.println(sortedStartProjection);
		
		//Collections.sort(sortedStartProjection, new CustomComparator(new Percept(world), "start"));
		
		System.out.println(sortedStartProjection);
		
		cp1.setRanges(1000, 1000);
		
		System.out.println(sortedStartProjection);
		
		//Collections.sort(sortedStartProjection, new CustomComparator(new Percept(world), "start"));
		
		System.out.println(sortedStartProjection);
		
	}

}
