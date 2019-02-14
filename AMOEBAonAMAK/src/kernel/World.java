package kernel;
import java.util.HashMap;

import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import fr.irit.smac.lxplot.LxPlot;
import ncs.NCS;

/**
 * Store some data about the world
 *
 */
public class World extends Environment {
	
	private HashMap<NCS,Integer> numberOfNCS = new HashMap<NCS,Integer>();
	private HashMap<NCS,Integer> allNCS = new HashMap<NCS,Integer>();
	private HashMap<NCS,Integer> thisLoopNCS = new HashMap<NCS,Integer>();
	
	private double AVT_acceleration = 2;
	private double AVT_deceleration = 1./3.0;
	private double AVT_percentAtStart = 0.2;
	
	/**
	 * Instantiates a new world.
	 *
	 */
	public World () {
		super(Scheduling.DEFAULT);
		
		for (NCS ncs : NCS.values()) {
			numberOfNCS.put(ncs, 0);			
		}

		
		for (NCS ncs : NCS.values()) {
			allNCS.put(ncs, 0);
			thisLoopNCS.put(ncs, 0);
		}
	}
	
	public void raiseNCS(NCS ncs) {
		thisLoopNCS.put(ncs, thisLoopNCS.get(ncs) + 1);
		
		if (ncs.equals(NCS.CONTEXT_CONFLICT_FALSE) || ncs.equals(NCS.HEAD_INCOMPETENT)) {
			NCS.a = true;
		}
	}
	
	public void changeNCSNumber(int x, NCS ncs) {
		if (numberOfNCS.containsKey(ncs)) {
			numberOfNCS.put(ncs, numberOfNCS.get(ncs) + x);
		} else {
			numberOfNCS.put(ncs, x);
		}
	}
	
	public double getAVT_acceleration() {
		return AVT_acceleration;
	}

	public double getAVT_deceleration() {
		return AVT_deceleration;
	}

	public double getAVT_percentAtStart() {
		return AVT_percentAtStart;
	}
	
	public void preCycleActions() {
		for(NCS ncs : NCS.values()) {
			allNCS.put(ncs, allNCS.get(ncs)+thisLoopNCS.get(ncs));
			thisLoopNCS.put(ncs, 0);
		}
	}
	
	public void updatePlot(int cycle) {
		cycle -= 1; //We want to start plot at 0
		int totalNCS = 0;
		int thisLoopNCSs = 0;
		for(NCS ncs : NCS.values()) {
			totalNCS += allNCS.get(ncs);
			thisLoopNCSs += thisLoopNCS.get(ncs);
			LxPlot.getChart("This loop NCS").add(ncs.name(),cycle, thisLoopNCS.get(ncs));
			LxPlot.getChart("All time NCS").add(ncs.name(),cycle, allNCS.get(ncs));
		}
		LxPlot.getChart("This loop NCS").add("Total",cycle, thisLoopNCSs);
		LxPlot.getChart("All time NCS").add("Total",cycle, totalNCS);
	}
}

