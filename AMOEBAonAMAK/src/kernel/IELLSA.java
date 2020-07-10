package kernel;

import java.util.HashMap;

import agents.context.localModel.TypeLocalModel;
import agents.head.Head;

/**
 * An AMOEBA is an autonomous multi-agent system, 
 * capable of learning and making prediction 
 *
 */
public interface IELLSA {
	/**
	 * Remove ALL agents.
	 */
	public void clearAgents();
	
	/**
	 * Run a learning cycle.
	 * @param perceptionsActionState the output of your studied system.
	 */
	public HashMap<String, Double> learn(HashMap<String, Double> perceptionsActionState);
	
	/**
	 * Run a cycle without learning.
	 * @param perceptionsActionState the output of your studied system.
	 * @return the result estimated by AMOEBA.
	 */
	public double request(HashMap<String, Double> perceptionsActionState);
	
	/**
	 * Try to maximize the oracle. <br/>
	 * The result will be inside a context.<br/>
	 * Inputs are percepts whose value is already known, aka constraints.<br/>
	 * If your request constraint all percepts, return null (you should use {@link ELLSA#request(HashMap)}).
	 * @param known : HasMap of percept name with their value. Can be empty.
	 * @return HashMap of the percepts optimized, with their value. 
	 * If a solution is found, the field "oracle" has its value, else "oracle" is at negative infinity.
	 */
	public HashMap<String, Double> maximize(HashMap<String, Double> known);
	
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
