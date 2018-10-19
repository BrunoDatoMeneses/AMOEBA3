package experiments.badContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import mas.agents.context.Context;
import mas.agents.head.Head;
import mas.kernel.StudiedSystem;
import mas.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextManager.
 */
public class F_XY_linearManager implements StudiedSystem, Serializable{

	/** The x. */
	double x = 0;
	
	/** The y. */
	double y = 0;
	
	/** The result. */
	double result = 0;
	
	/** The first step. */
	boolean firstStep = true;
	
	double spaceSize;
	
	/** The world. */
	World world;
	Random generator;
	
	
	public F_XY_linearManager(double size) {
		this.spaceSize= size;
	}
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public void playOneStep(double action) {

			
		if (generator == null)	generator = new Random(29);
			
		
		x = (generator.nextDouble() - 0.5) * spaceSize * 4;
		y = (generator.nextDouble()- 0.5) * spaceSize * 4;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */
	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();


		result = (2*x) + (4*y);
	//	result = (x > 2*y) ? 0.0 : 1.0;
		
		out.put("px",x);
		out.put("py",y);
		out.put("oracle",result);
		return out;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#switchControlMode()
	 */
	@Override
	public void switchControlMode() {
		
	}
	
	/**
	 * Sets the world.
	 *
	 * @param world the new world
	 */
	public void setWorld(World world) {
		this.world = world;
	}

	



}