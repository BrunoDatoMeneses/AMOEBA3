package kernel;

import java.util.ArrayList;
import java.util.HashMap;

import fr.irit.smac.amak.Environment;
import fr.irit.smac.amak.Scheduling;
import agents.context.Context;
import agents.percept.Percept;
import experiments.TestMain.Level;
import ncs.NCS;
import utils.TRACE_LEVEL;

/**
 * Store some data about the world
 * 
 */
public class World extends Environment {

	private HashMap<NCS, Integer> numberOfNCS = new HashMap<NCS, Integer>();
	private HashMap<NCS, Integer> allTimeNCS = new HashMap<NCS, Integer>();
	private HashMap<NCS, Integer> thisLoopNCS = new HashMap<NCS, Integer>();

	public double increment_up = 0.05;
	
	private int nbActivatedAgent;
	private double AVT_acceleration = 2;
	private double AVT_deceleration = 1. / 3.0;
	private double AVT_percentAtStart = 0.2;

	public double mappingErrorAllowed = 0.04;// TODO remove from here --> head agent
	
	public int regressionPoints = 0; // TODO remove from here
	
	
	
	public static TRACE_LEVEL minLevel = TRACE_LEVEL.INFORM;
	
	private AMOEBA amoeba;

	/**
	 * Instantiates a new world.
	 * 
	 */
	public World() {
		super(Scheduling.HIDDEN);

		for (NCS ncs : NCS.values()) {
			numberOfNCS.put(ncs, 0);
		}

		for (NCS ncs : NCS.values()) {
			allTimeNCS.put(ncs, 0);
			thisLoopNCS.put(ncs, 0);
		}
	}

	public double getMappingErrorAllowed() {
		return mappingErrorAllowed;
	}
	
	public void setMappingErrorAllowed(double value) {
		mappingErrorAllowed = value;
	}

	public synchronized void raiseNCS(NCS ncs) {
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
	
	public void trace(TRACE_LEVEL lvl, ArrayList<String> infos) {
		if (lvl.isGE(minLevel)) {
			String message = "[" +amoeba.getCycle() + "]";
			for(String info : infos) {
				message += " " + info;
			}
			System.out.println(message);
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
		for (NCS ncs : NCS.values()) {
			allTimeNCS.put(ncs, allTimeNCS.get(ncs) + thisLoopNCS.get(ncs));
			thisLoopNCS.put(ncs, 0);
		}
	}

	public HashMap<NCS, Integer> getThisLoopNCS() {
		return thisLoopNCS;
	}

	public HashMap<NCS, Integer> getAllTimeNCS() {
		return allTimeNCS;
	}

	public synchronized void incrementNbActivatedAgent() {
		nbActivatedAgent += 1;
	}

	public int getNbActivatedAgent() {
		return nbActivatedAgent;
	}

	public void resetNbActivatedAgent() {
		nbActivatedAgent = 0;
	}
	
	public void setAmoeba(AMOEBA amoeba) {
		this.amoeba = amoeba;
	}
	
	public double getIncrements() {
		return increment_up ;
	}
	
	public double getContextCreationNeighborhood(Context ctxt, Percept pct) {
		//return 2*ctxt.getRanges().get(pct).getRadius();
		return pct.getRadiusContextForCreation()*2;
	}
	
	public double getContextNeighborhood(Context ctxt, Percept pct) {
		//return 2*ctxt.getRanges().get(pct).getRadius();
		return ctxt.getRanges().get(pct).getRadius();
	}
}
