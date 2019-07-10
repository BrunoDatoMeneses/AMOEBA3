package experiments;

import java.util.HashMap;
import java.util.Random;

import kernel.StudiedSystem;

/**
 * The Class BadContextManager.
 */
public class F_XY_System implements StudiedSystem {

	/** The x. */
	double x = 0;
	/** The y. */
	double y = 0;
	/** The result. */
	double result = 0;
	/** The first step. */
	boolean firstStep = true;
	double spaceSize;
	Random generator;

	public F_XY_System(double size) {
		this.spaceSize = size;
	}

	private double getResult(double x, double y) {
		return (y > -spaceSize && y < spaceSize && x < spaceSize && x > -spaceSize) ? 2 * x + y : 5 * x - 8 * y;
	}
	
	@Override
	public void playOneStep() {
		if (generator == null) {
			generator = new Random(29);
		}

		x = (generator.nextDouble() - 0.5) * spaceSize * 4;
		y = (generator.nextDouble() - 0.5) * spaceSize * 4;
	}

	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = getResult(x, y);

		out.put("px0", x);
		out.put("px1", y);
		out.put("oracle", result);
		return out;
	}

	@Override
	public double requestOracle(HashMap<String, Double> request) {
		return getResult(request.get("px"), request.get("py"));
	}

	@Override
	public HashMap<String, Double> getOutputWithNoise(double noiseRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Double> getOutputWithAmoebaRequest(HashMap<String, Double> amoebaRequest,
			double noiseRange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActiveLearning(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelfRequest(HashMap<String, Double> request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashMap<String, Double> getIntput() {
		// TODO Auto-generated method stub
		return null;
	}
}