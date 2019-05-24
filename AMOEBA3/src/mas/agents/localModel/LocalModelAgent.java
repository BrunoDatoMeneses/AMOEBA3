package mas.agents.localModel;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.SystemAgent;
import mas.agents.context.Context;
import mas.agents.context.ContextOverlap;
import mas.agents.context.Experiment;
import mas.agents.messages.Message;

// TODO: Auto-generated Javadoc
/**
 * The abstract class of all agents in charge of the generation of the output from Context Agent.
 * For the sake of simplicity, it's not scheduled as agent like other of the system.
 */
public abstract class LocalModelAgent extends SystemAgent implements Serializable, Cloneable{

	Context context;
	
	/**
	 * Instantiates a new local model agent.
	 *
	 * @param world the world
	 */
	public LocalModelAgent(World world, Context associatedContext) {
		super(world);
		context = associatedContext;
	}

	/* (non-Javadoc)
	 * @see agents.SystemAgent#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		return null;
	}

	/* (non-Javadoc)
	 * @see agents.Agent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {		
	}
	
	/**
	 * Gets the proposition.
	 *
	 * @param context the context
	 * @return the proposition
	 */
	public abstract double getProposition(Context context);
	public abstract double getProposition(Experiment experiment);
	
	public abstract double getProposition(Context context, ContextOverlap contextOverlap);
	
	public abstract double getProposition(ArrayList<Experiment> experimentsList, Experiment experiment);
	
	/**
	 * Version of getProposition for 2D display.
	 *
	 * @param context the context
	 * @param p1 the p 1
	 * @param p2 the p 2
	 * @param v1 the v 1
	 * @param v2 the v 2
	 * @return the proposition
	 */
	public abstract double getProposition(Context context, Percept p1, Percept p2, double v1, double v2);

	/**
	 * Gets the formula.
	 *
	 * @param context the context
	 * @return the formula
	 */
	public abstract String getFormula(Context context);
	public abstract String getCoefsFormula();

	/**
	 * Update model.
	 *
	 * @param context the context
	 */
	public abstract void updateModel(Context context);
	
	public abstract void updateModelWithExperiments(ArrayList<Experiment> experimentsList);
	public abstract void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight, int numberOfPointsForRegression);
	public abstract void updateModel(Experiment newExperiment, double weight, int numberOfPointsForRegression);
	public abstract String coefsToString();
	public abstract double distance(Experiment experiment);
	public abstract ArrayList<Experiment> getFirstExperiments();
	
	public abstract double[] getCoef();
	
	/* (non-Javadoc)
	 * @see agents.Agent#die()
	 */
	public void die() {
		super.die();
		world.kill(this);
	}
	
	public LocalModelAgent clone() throws CloneNotSupportedException{
		return (LocalModelAgent)super.clone();
	}

}
