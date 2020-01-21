package kernel;

import java.io.Serializable;
import java.util.HashMap;

import agents.percept.Percept;

/**
 * Any system studied by an instance of AMOEBA must implement this interface.
 * 
 */
public interface StudiedSystem extends Serializable{

	/**
	 * Tell the StudiedSystem to advance its simulation of one step
	 */
	public HashMap<String, Double> playOneStep();

	/**
	 * Gets the output for the current step.
	 * 
	 * @return an {@link HashMap} containing a value for each {@link Percept} of an {@link AMOEBA} and a value for the oracle.
	 */
	public HashMap<String, Double> getOutput();
	public HashMap<String, Double> getIntput();
	
	public HashMap<String, Double> getOutputWithNoise(double noiseRange);
	
	public HashMap<String, Double> getOutputWithAmoebaRequest(HashMap<String, Double> amoebaRequest,  double noiseRange);
	
	/**
	 * Ask the studied system to provide an oracle for a specific input.
	 * 
	 * @param request the input we wish to get an oracle.
	 * @return the oracle value.
	 */
	public double requestOracle(HashMap<String, Double> request);
	
	public void setActiveLearning(boolean value);
	public void setSelfLearning(boolean value);
	
	public void setSelfRequest(HashMap<Percept, Double> request);


	public HashMap<String, Double> playOneStepWithControlModel();
	
	public void setControlModels(HashMap<String, AMOEBA> controlModels);
	
	public void setControl(boolean value);

}
