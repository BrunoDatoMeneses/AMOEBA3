package ncs;

import java.io.Serializable;

import kernel.World;


	// TODO: Auto-generated Javadoc
/**
	 * Non Cooperative Situation as defined in the AMAS theory.
	 */
public enum NCS implements Serializable {

	
	/** The context conflict false. */
	/*Context NCS*/
	CONTEXT_CONFLICT_FALSE, 
 /** The context conflict inexact. */
 CONTEXT_CONFLICT_INEXACT, 
 /** The context uselessness. */
 CONTEXT_USELESSNESS, 
 /** The context concurrence. */
 CONTEXT_CONCURRENCE,
		
	/** The head incompetent. */
	/*Head NCS*/
	HEAD_INCOMPETENT, 
 /** The head improductive. */
 HEAD_IMPRODUCTIVE, 
 /** The create new context. */
 CREATE_NEW_CONTEXT;
	
	public static boolean a = false;
	
	/**
	 * Change the number of NCS in the world.
	 *
	 * @param world the world
	 */
	public void raiseNCS(World world) {
		world.changeNCSNumber(1, this);
		System.out.println(this);
		if (this.equals(NCS.CONTEXT_CONFLICT_FALSE)) {
			a = true;
		}
	}
	
}
