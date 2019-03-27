package kernel;

import java.util.HashMap;

/**
 * Any system studied by an instance of AMOEBA must implement this interface.
 * 
 */
public interface StudiedSystem {

	/**
	 * When the scheduler of AMOEBA has run one cycle, playOneStep is called to
	 * allow the studied system to perform it's own cycle.
	 */
	public void playOneStep();

	/**
	 * Gets the output.
	 * 
	 * @return the output
	 */
	public HashMap<String, Double> getOutput();

}
