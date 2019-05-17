package experiments.Regression;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import mas.kernel.StudiedSystem;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextManager.
 */
public class F_XY_Manager implements StudiedSystem, Serializable{

	/** The x. */
	double x = 0;
	
	/** The y. */
	double y = 0;
	
	/** The result. */
	double result = 0;
	
	/** The first step. */
	boolean firstStep = true;
	
	double spaceSize;
	
	/** The world. */
	Random generator;
	
	
	public F_XY_Manager(double size) {
		this.spaceSize= size;
	}
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public void playOneStep(double action) {
		HashMap<String, Double> out = new HashMap<String, Double>();
			
		if (generator == null)	generator = new Random(29);
			
		
		x = (generator.nextDouble() - 0.5) * spaceSize * 4;
		y = (generator.nextDouble()- 0.5) * spaceSize * 4;
	}
	
	public void playOneStepConstrained(double[] constrains) {
				
		x = constrains[0] + (Math.random()*(constrains[1] - constrains[0]));
		y = constrains[0] + (Math.random()*(constrains[1] - constrains[0]));
	}

	
	public double model(double x, double y) {
		
		/* Disc */
		//return (y*y + x*x < spaceSize*spaceSize ) ? 2*x + y : 5*x - 8*y;
		
		/* Square */
		//return (y > -spaceSize && y < spaceSize && x < spaceSize && x > -spaceSize) ? 2*x + y : 5*x - 8*y ;
		return 5*x - 8*y ;
		
		/* Triangle */
		//return (y > x) ? 2*x + y : 5*x - 8*y;
		
		/* Split */
		//return ( x <= 0 ) ? 2*x + y : 5*x - 8*y;
		
	}
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */
	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model(x, y);
		
		out.put("px",x);
		out.put("py",y);
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOriginOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model(0, 0);
		
		out.put("px",x);
		out.put("py",y);
		out.put("oracle",result);
		return out;
	}
	
	
	
	
	public HashMap<String, Double> getOutputRequest(HashMap<String, Double> values) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		double xValue = values.get("px");
		double yValue = values.get("py");
		
		result = model(xValue, yValue);
		
		out.put("px",xValue);
		out.put("py",yValue);
		out.put("oracle",result);
		return out;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#switchControlMode()
	 */
	@Override
	public void switchControlMode() {
		
	}
	

	public double getSpaceSize() {
		return spaceSize;
	}



}