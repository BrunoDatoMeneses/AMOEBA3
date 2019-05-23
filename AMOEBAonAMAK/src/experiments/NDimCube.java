package experiments;

import java.util.HashMap;
import java.util.Random;

import fr.irit.smac.amak.tools.Log;
import kernel.StudiedSystem;

/**
 * System for a N dimension cube.
 */
public class NDimCube implements StudiedSystem{


	double[] x ;
	double[] modelCoefs1;
	double[] modelCoefs2;
	
	double result = 0;
	boolean firstStep = true;
	double spaceSize;
	Random generator;
	int dim ;
	
	
	public NDimCube(double size, int dimension) {
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
		
		String print = "COEFS : ";
		for(int i = 0; i<dimension; i++) {
			print += modelCoefs1[i] + " " + modelCoefs2[i];
			if(i+1 < dimension)
				print += ", ";
		}
		Log.inform("NDimCube", print);
	}
	
	@Override
	public void playOneStep() {
		for(int i = 0; i<dim; i++) {
			x[i] = (generator.nextDouble() - 0.5) * spaceSize * 4;
		}
	}
	
	@Override
	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();
		
		for(int i = 0; i<dim; i++) {

			if(x[0] > 0) {
				result = getModelResult(1);
			}
			else {
				result = getModelResult(2);
			}
			
			out.put("px" + (i + 1),x[i]);
			
		}

		out.put("oracle",result);
		return out;
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

	@Override
	public double requestOracle(HashMap<String, Double> request) {
		return 0;
	}
}