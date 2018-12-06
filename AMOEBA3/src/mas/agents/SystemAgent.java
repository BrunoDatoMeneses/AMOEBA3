package mas.agents;

import java.io.Serializable;
import java.util.ArrayList;

import mas.agents.context.Context;
import mas.kernel.World;

// TODO: Auto-generated Javadoc
/**
 * The Class SystemAgent.
 */
public abstract class SystemAgent extends Agent implements Serializable,Cloneable {


	protected World world;


	public SystemAgent(World world) {
		this.world = world;
		world.changeAgentNumber(1, this.getClass().getSimpleName());
	}


	// --- BEHAVIOR --- //
	
	public void play() {
		super.play();
	}
	

	/*
	 * Return all possible agents for message sending
	 */
	abstract public ArrayList<? extends Agent> getTargets();


	
	public World getWorld() {
		return world;
	}


	public void setWorld(World world) {
		this.world = world;
	}

	public SystemAgent clone() throws CloneNotSupportedException{
		return (SystemAgent)super.clone();
	}
}
