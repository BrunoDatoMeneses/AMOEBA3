package visualization.observation;

import java.io.Serializable;
import java.util.ArrayList;

import mas.agents.percept.Percept;
import mas.agents.context.Context;

// TODO: Auto-generated Javadoc
/**
 * The Class Observation.
 */
public class Observation implements Serializable {
	
	/** The tick. */
	private int tick=0;
	
	/** The percept list. */
	private ArrayList<Percept> perceptList = new ArrayList<Percept>();
	
	/** The context list. */
	private ArrayList<Context> contextList = new ArrayList<Context>();
	
	/**
	 * Sets the tick.
	 *
	 * @param tick the new tick
	 */
	public void setTick(int tick) {
		this.tick = tick;
	}
	
	/**
	 * Adds the percept list.
	 *
	 * @param p the p
	 */
	public void addPerceptList(Percept p) {
		perceptList.add(p);
	}
	
	/**
	 * Adds the context list.
	 *
	 * @param c the c
	 */
	public void addContextList(Context c) {
		contextList.add(c);
	}
	
	/**
	 * Gets the tick.
	 *
	 * @return the tick
	 */
	public int getTick() {
		return tick;
	}
	
	/**
	 * Gets the percept list.
	 *
	 * @return the percept list
	 */
	public ArrayList<Percept> getPerceptList() {
		return perceptList;
	}
	
	/**
	 * Gets the context list.
	 *
	 * @return the context list
	 */
	public ArrayList<Context> getContextList() {
		return contextList;
	}
	
	/**
	 * Gets the context by id.
	 *
	 * @param id the id
	 * @return the context by id
	 */
	public Context getContextById(String id) {
		for(Context c: contextList) {
			if(c.getName().equals(id)) {
				return c;
			}
		}
		return null;
	}

}
