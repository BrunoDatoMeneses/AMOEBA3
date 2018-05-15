package experiments.badContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import MAS.agents.context.Context;
import MAS.agents.head.Head;
import MAS.kernel.StudiedSystem;
import MAS.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextManager.
 */
public class BadContextManager implements StudiedSystem, Serializable{

	/** The x. */
	double x = 0;
	
	/** The y. */
	double y = 0;
	
	/** The result. */
	double result = 0;
	
	/** The first step. */
	boolean firstStep = true;
	
	/** The world. */
	World world;
	Random generator;
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public void playOneStep(double action) {

			
		if (generator == null)	generator = new Random(29);
			
		
		x = (generator.nextDouble() - 0.5) * 200;
		y = (generator.nextDouble()- 0.5) * 200;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */
	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = (y > -55 && y < 55 && x < 55 && x > -55) ? 0.0 : 1.0;
	//	result = (x > 2*y) ? 0.0 : 1.0;
		
		out.put("x",x);
		out.put("y",y);
		out.put("test",result);
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