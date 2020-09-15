package ncs;

import java.io.Serializable;

import kernel.World;


/**
	 * Non Cooperative Situation as defined in the AMAS theory.
	 */
public enum NCS implements Serializable {

	

	/*Context NCS*/
	CONTEXT_CONFLICT_FALSE, 
	CONTEXT_CONFLICT_INEXACT, 
	CONTEXT_USELESSNESS, 
	CONTEXT_CONCURRENCE,
	CONTEXT_OVERLAP_CONFLICT,
	CONTEXT_OVERLAP_REDUNDANCY,	
	CONTEXT_OVERMAPPING,
	CONTEXT_RESTRUCTURE,
	CONTEXT_OVERLAP,

	/*Head NCS*/
	HEAD_INCOMPETENT, 
	HEAD_IMPRODUCTIVE, 

	CREATE_NEW_CONTEXT;
	
	public static boolean a = false;
	
	/**
	 * Change the number of NCS in the world.
	 *
	 * @param world the world
	 */
	public void raiseNCS(World world) {
		world.changeNCSNumber(1, this);
		//System.out.println(this);
		if (this.equals(NCS.CONTEXT_CONFLICT_FALSE)) {
			a = true;
		}
	}
	
}
