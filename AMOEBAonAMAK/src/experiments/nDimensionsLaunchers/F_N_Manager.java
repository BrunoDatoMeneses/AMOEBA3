package experiments.nDimensionsLaunchers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

import kernel.StudiedSystem;


// TODO: Auto-generated Javadoc
/**
 * The Class BadContextManager.
 */
public class F_N_Manager implements StudiedSystem{

	/** The x. */
	double[] x ;
	
	
	/** The result. */
	double result = 0;
	
	int[] modelCoefs1;
	int[] modelCoefs2;
	
	int[][] modelCoefs;
	double[][] modelCenterZones;
	
	/** The first step. */
	boolean firstStep = true;
	
	double spaceSize;
	
	int dimension;
	int numberOfModels;
	
	HashMap<String,Double> selfRequest;
	boolean activeLearning = false;
	
	/** The world. */
	Random generator;
	
	
	public F_N_Manager(double size, int dim, int nbOfModels) {
		this.spaceSize= size;
		dimension = dim;
		numberOfModels = nbOfModels;
		x = new double[dimension];
		
		modelCoefs = new int[nbOfModels][dim+1];
		modelCenterZones = new double[nbOfModels][dim];
		
		
		for(int nb = 0; nb<nbOfModels; nb++) {
			for(int i = 0; i<dimension; i++) {
				x[i] = 0.0;
				
				modelCoefs[nb][i] = (int) (Math.random() * 500 - 255);
				modelCenterZones[nb][i] = (Math.random() - 0.5) * spaceSize * 4;
			}
			modelCoefs[nb][dimension] = (int) (Math.random() * 500 - 255);


		}
		
		modelCoefs1 = new int[dim+1];
		modelCoefs2 = new int[dim+1];
		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			
			modelCoefs1[i] = (int) (Math.random() * 500 - 255);
			modelCoefs2[i] = (int) (Math.random() * 500 - 255);
		}
		modelCoefs1[dimension] = (int) (Math.random() * 500 - 255);
		modelCoefs2[dimension] = (int) (Math.random() * 500 - 255);
		
		
		for(int nb = 0; nb<nbOfModels; nb++) {
			System.out.print(modelCoefs[nb][dimension] + "\t");
			for(int i =0;i<dimension;i++) {
				System.out.print(modelCoefs[nb][i] + "\t");
			}
			System.out.println("");
		}
		
//		System.out.print(modelCoefs1[modelCoefs1.length-1] + "\t");
//		for(int i =0;i<modelCoefs1.length-1;i++) {
//			System.out.print(modelCoefs1[i] + "\t");
//		}
//		System.out.println("");
//		System.out.print(modelCoefs2[modelCoefs2.length-1] + "\t");
//		for(int i =0;i<modelCoefs2.length-1;i++) {
//			System.out.print(modelCoefs2[i] + "\t");
//		}
//		System.out.println("");
	}
	
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#playOneStep(double)
	 */
	@Override
	public void playOneStep() {
		

		
		if(activeLearning) {
			activeLearning = false;
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = selfRequest.get("px" + i);
			}
		}
		else {
			if (generator == null)	generator = new Random(29);
			
			for(int i = 0 ; i < dimension ; i++) {
				x[i] = (generator.nextDouble() - 0.5) * spaceSize * 4;
			}
		}
		
		
	}
	
	public void playOneStepConstrained(double[][] constrains) {		
		
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = constrains[i][0] + (Math.random()*(constrains[i][1] - constrains[i][0]));
		}
	}
	
	public void playOneStepConstrainedWithNoise(double[][] constrains, double noiseRange) {
		
		
		for(int i = 0 ; i < dimension ; i++) {
			x[i] = constrains[i][0] + (Math.random()*(constrains[i][1] - constrains[i][0])) - noiseRange/2 + Math.random()*noiseRange;
		}

	}

	
	public double model() {
		
		/* Disc */
		//return (x[1]*x[1] + x[0]*x[0] < spaceSize*spaceSize ) ? model1() :  model2();
		
		/* Square */
		//return (x[1] > -spaceSize && x[1] < spaceSize && x[0] < spaceSize && x[0] > -spaceSize) ? model1() : model2() ;
		//return model1();
		
		/* Triangle */
		//return (x[1] > x[0]) ? model1() : model2();
		
		/* Split */
		//return ( x <= 0 ) ? 2*x + y : 5*x - 8*y;
		
		/* Exp */
		//return (x[1] > 100*Math.exp(-(Math.pow(x[0]/25, 2))/2) -50) ? model1() : model2();
		
		
		/* Cercle */
//		double rho = Math.sqrt(x[1]*x[1] + x[0]*x[0]);
//		double start = 50.0;
//		double width = 25.0;
//		return ( (start  < rho) && (rho < start + width)) ? model1() : model2();
		
		
		/* Disques */
		return modelN();
		
		
		
	}
	

	
	public double model(double[] xRequest) {
		
		/* Disc */
		//return (y*y + x*x < spaceSize*spaceSize ) ? 2*x + y : 5*x - 8*y;
		
		/* Square */
		//return (x1 > -spaceSize && x1 < spaceSize && x0 < spaceSize && x0 > -spaceSize) ? model1(x0,x1) : model2(x0,x1) ;
		//return model1();
		
		/* Triangle */
		//return (y > x) ? 2*x + y : 5*x - 8*y;
		
		/* Split */
		//return ( x <= 0 ) ? 2*x + y : 5*x - 8*y;
		
		/* Exp */
		//return (xRequest[1] > 100*Math.exp(-(Math.pow(xRequest[0]/25, 2))/2) -50) ? model1() : model2();
		
		/* Cercle */
//		double rho = Math.sqrt(x1*x1 + x0*x0);
//		double start = 50.0;
//		double width = 25.0;
//		return ( (start  < rho) && (rho < start + width)) ? model1() : model2();
		
		/* Disques */
		return modelN(xRequest);
		
	}
	
	
	
	private double modelN() {
		
		for(int nb = 0; nb<numberOfModels-1; nb++) {
			
			if(distance(x,modelCenterZones[nb]) < spaceSize) {
				return modeli(nb);
			}
			
		}
		return modeli(numberOfModels-1);
		
	}
	
	private double modelN(double[] xRequest) {
		
		for(int nb = 0; nb<numberOfModels-1; nb++) {
			
			if(distance(xRequest,modelCenterZones[nb]) < spaceSize) {
				return modeli(nb, xRequest);
			}
			
		}
		return modeli(numberOfModels-1, xRequest);
		
	}
	
	private double distance(double[] x1, double[] x2) {
		double distance = 0;
		for(int i = 0; i < x1.length; i ++) {
			distance += Math.pow(x2[i] - x1[i], 2) ;
		}
		return Math.sqrt(distance);
	}
	
	private double modeli(int modelNb) {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs[modelNb][i];
		}
		result += modelCoefs[modelNb][dimension];
		return result;		
	}
	
	private double modeli(int modelNb, double[] xRequest) {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += xRequest[i]*modelCoefs[modelNb][i];
		}
		result += modelCoefs[modelNb][dimension];
		return result;		
	}
	
	public double model1() {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs1[i];
		}
		result += modelCoefs1[dimension];
		return result;		
	}
	
	public double model2() {
		double result = 0.0;
		for(int i = 0; i<dimension;i++) {
			result += x[i]*modelCoefs2[i];
		}
		result += modelCoefs2[dimension];
		return result;		
	}
	
	public double model1(double x0, double x1) {
		double result = 0.0;
		result += x0*modelCoefs1[0];
		result += x1*modelCoefs1[1];
		result += modelCoefs1[dimension];
		return result;		
	}
	
	public double model2(double x0, double x1) {
		double result = 0.0;
		result += x0*modelCoefs2[0];
		result += x1*modelCoefs2[1];
		result += modelCoefs2[dimension];
		return result;		
	}
	
	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#getOutput()
	 */

	public HashMap<String, Double> getOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model();
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOutputWithNoise(double noiseRange) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		result = model() - noiseRange/2 + Math.random()*noiseRange ;
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOutputWithAmoebaRequest(HashMap<String, Double> amoebaRequest,  double noiseRange) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		for(int i = 0; i<dimension; i++) {
			
			x[i] = amoebaRequest.get("px" + i);
			
		}
		
		result = model() - noiseRange/2 + Math.random()*noiseRange ;
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	public HashMap<String, Double> getOriginOutput() {
		HashMap<String, Double> out = new HashMap<String, Double>();

		for(int i = 0; i<dimension; i++) {
			x[i] = 0.0;
			
		}
		
		result = model();
		
		for(int i = 0; i<dimension; i++) {
			
			out.put("px" + i,x[i]);
			
		}
		out.put("oracle",result);
		return out;
	}
	
	
	
	
	public HashMap<String, Double> getOutputRequest2D(HashMap<String, Double> values) {
		HashMap<String, Double> out = new HashMap<String, Double>();

		x[0] = values.get("px0");
		x[1] = values.get("px1");
		
		
		result =  model();
		
		out.put("px0",x[0]);
		out.put("px1",x[1]);
		out.put("oracle",result);
		return out;
	}

	/* (non-Javadoc)
	 * @see kernel.StudiedSystem#switchControlMode()
	 */

	public void switchControlMode() {
		
	}
	

	public double getSpaceSize() {
		return spaceSize;
	}


	
	


	@Override
	public double requestOracle(HashMap<String, Double> request) {
		
		double[] xRequest = new double[request.size()];
		
		for(int i = 0; i<dimension; i++) {
			
			xRequest[i] = request.get("px" + i);
			
		}
		
		return model(xRequest);
	}
	
	@Override
	public void setActiveLearning(boolean value) {
		activeLearning = value;
	}
	
	@Override
	public void setSelfRequest(HashMap<String, Double> request){
		selfRequest = request;
	}



}