package experiments.nDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import mas.kernel.StudiedSystem;

// TODO: Auto-generated Javadoc
/**
 * The Class BadContextManager.
 */
public class NDimManager implements StudiedSystem, Serializable{


	double[] x ;
	double[] modelCoefs1;
	double[] modelCoefs2;
	
	double result = 0;
	boolean firstStep = true;
	double spaceSize;
	Random generator;
	int dim ;
	
	
	public NDimManager(double size, int dimension) {
		

		
		generator = new Random(29);
		this.spaceSize= size;
		this.dim = dimension;
		
		x = new double[dim];
		modelCoefs1 = new double[dim];
		modelCoefs2 = new double[dim];
		
		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			
			modelCoefs1[i] = (int) (Math.random() * 255);
			modelCoefs2[i] = (int) (Math.random() * 255);
		}
		
		System.out.println("COEFS");
		for(int i = 0; i<dimension; i++) {
			System.out.print(modelCoefs1[i] + " ");
			System.out.println(modelCoefs2[i]);
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public void playOneStep(double action) {

			
		for(int i = 0; i<dim; i++) {
			x[i] = (generator.nextDouble() - 0.5) * spaceSize * 4;
		}

	}
	
	

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */
	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		
//		boolean xPositionTest = true;
//		for(int i = 0; i<1; i++) {
//			xPositionTest = xPositionTest && (x[i] > 0  );
//		}
		
		for(int i = 0; i<dim; i++) {

			result = getModelResult(1);
			
//			if(x[0] > 0) {
//				result = getModelResult(1);
//			}
//			else {
//				result = getModelResult(2);
//			}
			
			out.put("px" + (i + 1),x[i]);
			
		}

		out.put("oracle",result);
		return out;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#switchControlMode()
	 */
	@Override
	public void switchControlMode() {
		
	}
	
	private double getModelResult(int num) {
		double modelresult = 0.0;
		
		if(num == 1) {
			for(int i=0; i<modelCoefs1.length;i++) {
				modelresult += x[i]*modelCoefs1[i];
			}
		}
		else if(num == 2) {
			for(int i=0; i<modelCoefs2.length;i++) {
				modelresult += x[i]*modelCoefs2[i];
			}
		}
				
		
		return modelresult;
	}
	

	



}