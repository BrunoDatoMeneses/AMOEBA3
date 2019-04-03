package kernel;

import java.util.HashMap;

import agents.context.localModel.TypeLocalModel;
import agents.head.Head;

/**
 * An AMOEBA is an autonomous multi-agent system, 
 * capable of learning and making prediction 
 *
 */
public interface IAMOEBA {
	/**
	 * Remove ALL agents.
	 */
	public void clearAgents();
	
	/**
	 * Run a learning cycle.
	 * @param perceptionsActionState the output of your studied system.
	 */
	public void learn(HashMap<String, Double> perceptionsActionState);
	
	/**
	 * Run a cycle without learning.
	 * @param perceptionsActionState the output of your studied system.
	 * @return the result estimated by AMOEBA.
	 */
	public double request(HashMap<String, Double> perceptionsActionState);
	
	/**
	 * Set the Head agent.
	 * @param head
	 */
	public void setHead(Head head);
	
	/**
	 * Set the type of local model that will be used by newly created context.
	 */
	public void setLocalModel(TypeLocalModel localModel);
	
	/**
	 * Allow the creation of new context.
	 * @param creationOfNewContext
	 */
	public void setCreationOfNewContext(boolean creationOfNewContext);
	
	/**
	 * If AMOEBA is allowed to create new context.
	 * @return
	 */
	public boolean isCreationOfNewContext();
}
