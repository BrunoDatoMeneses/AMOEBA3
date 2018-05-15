package MAS.init;

import java.util.HashMap;
import java.util.concurrent.FutureTask;

// TODO: Auto-generated Javadoc
/**
 * The Class ManagerOutput.
 */
public class ManagerOutput {
	
	/** The actions. */
	private HashMap<String, Double> actions;
	
	/** The type. */
	private String type;
	
	/** The future task. */
	private FutureTask<Double> futureTask = null;
	
	/**
	 * Instantiates a new manager output.
	 *
	 * @param actions the actions
	 * @param type the type
	 */
	public ManagerOutput(HashMap<String, Double> actions, String type) {
		this.actions = actions;
		this.type = type;
	}
	
	/**
	 * Sets the actions.
	 *
	 * @param actions the actions
	 */
	public void setActions(HashMap<String, Double> actions) {
		this.actions = actions;
	}
	
	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Gets the actions.
	 *
	 * @return the actions
	 */
	public HashMap<String, Double> getActions() {
		return actions;
	}
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the future task.
	 *
	 * @param futureTask the new future task
	 */
	public void setFutureTask(FutureTask<Double> futureTask) {
		this.futureTask = futureTask;
	}
	
	/**
	 * Gets the future task.
	 *
	 * @return the future task
	 */
	public FutureTask<Double> getFutureTask() {
		return futureTask;
	}
}
