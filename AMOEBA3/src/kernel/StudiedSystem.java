package kernel;

import java.util.HashMap;

// TODO: Auto-generated Javadoc
/**
 * Any system studied by an instance of AMOEBA must implement this interface.
 * 
 */
public interface StudiedSystem {

	/**
	 * When the scheduler of AMOEBA has run one cycle, playOneStep is called to allow the studied system to perform it's own cycle.
	 * @param action : output action of AMOEBA.
	 */
	public void playOneStep(double action);
	
	/**
	 * Gets the output.
	 *
	 * @return the output
	 */
	public HashMap<String, Double> getOutput();
	
	/**
	 * Switch control mode.
	 */
	public void switchControlMode();
}
