package experiments.Regression;

import java.util.ArrayList;

import mas.Pair;
import mas.agents.context.Context;
import mas.agents.context.Experiment;
import mas.agents.localModel.Regression;
import mas.agents.percept.Percept;

public class RegressionManager {
	
	
	/** The n parameters. */
	private int dimension;
	
	/** The regression. */
	transient private Regression regression;

	
	/** The coef. */
	private double[] coefs;
	
	private double spaceSize;
	
	public RegressionManager(int dim, double spcsize) {
		this.dimension = dim;
		this.spaceSize = spcsize;
		regression = new Regression(dimension,true);
	}
	
	public RegressionManager(int dim, double spcsize, double[] coeficients) {
		this.dimension = dim;
		this.spaceSize = spcsize;
		regression = new Regression(dimension,true);
		coefs = coeficients;
	}
	
	
	public void setCoefs(double[] newCoefs) {
		coefs = newCoefs;
	}
	
	public double[] getCoefs() {
		return coefs;
	}
	
	
	
	public void updateModelWithArtificialPoints(int numberOfPoints, double noise) {
		
		regression = new Regression(dimension,true);
		
		Pair<double[][], double[]> artificialSituations = getRandomlyDistributedArtificialExperiments(numberOfPoints, noise);
		
			
		for (int i =0;i<numberOfPoints;i++) {
			
			regression.addObservation(artificialSituations.getA()[i], artificialSituations.getB()[i]);
			
		}
			
			
		int i = 0;
		while (regression.getN() < dimension + 2) { //TODO : to improve
			
			regression.addObservation(artificialSituations.getA()[i%numberOfPoints], artificialSituations.getB()[i%numberOfPoints]);
			i++;
			
			System.out.println("ADING Observations " + regression.getN());
			
		}
		

		
		coefs = regression.regress().getParameterEstimates();
		

		

		
	}
	
	
	private Pair<double[][], double[]> getRandomlyDistributedArtificialExperiments(int amount, double noise){
		
		double[][] artificalExperiments = new double[amount][dimension];
		double[] artificalResults = new double[amount];
		

		
		for (int i = 0; i < amount;i ++) {
			
			for(int j = 0;j<dimension;j++) {
				
				double startRange = - spaceSize;
				double endRange = spaceSize;
				artificalExperiments[i][j] = startRange + (Math.random()*(endRange - startRange));
			}
			artificalResults[i] = this.getProposition(artificalExperiments[i], noise);
			
		}
		
		return new Pair<double[][], double[]>(artificalExperiments, artificalResults);
	}
	
	public double getProposition(double[] situation) {
		
		
			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			result += coefs[i] * situation[i-1];

		}
	
		return result;
	}
	
	public double getProposition(double[] situation, double noise) {
		
		
		
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			result += coefs[i] * situation[i-1];

		}
	
		return result - noise + (Math.random()*2*noise);
	}
	
}
