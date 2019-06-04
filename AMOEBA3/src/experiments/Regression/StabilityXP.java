package experiments.Regression;

import java.util.ArrayList;
import java.util.Arrays;

import experiments.FILE;

public class StabilityXP {

	public static final int dimension = 30	;
	public static final int regressionPoints = 50;
	public static final int cycles = 100000	;
	public static final double spaceSize = 50.0	;
	public static final double noise = 1.0	;
	public static final double coefsMarges = 255	;
	
	static double[] initCoefs = new double[dimension+1];
	static double[] errors = new double[dimension+1];
	
	public static void main(String[] args) {
		
		
		String fileName = fileName(new ArrayList<String>(Arrays.asList(
				"03012019","RegressionStability",
				"Dim",""+dimension,
				"RegressionPoints",""+regressionPoints,
				"Cyles",""+cycles,
				"SpaceSize",""+spaceSize,
				"Noise",""+noise,
				"CoefsMarges",""+coefsMarges
				)));
		
		FILE file = new FILE("Regression",fileName);
		
		for(int i = 0; i<=dimension; i++) {
			
			initCoefs[i] = (int) ((Math.random() * 2 * coefsMarges) - coefsMarges);
			if(initCoefs[i]==0) initCoefs[i]=1;
		}
		
		
		
		RegressionManager regressionManager = new RegressionManager(dimension, spaceSize, initCoefs);
		
		display(initCoefs);
		
		for(int i=0;i<cycles;i++) {
			
			regressionManager.updateModelWithArtificialPoints(regressionPoints, noise);
			
			updateErrors(regressionManager.getCoefs());
			
			if(i%100000==0) System.out.println(i);
			
			writeMessage(file,errors,i);
			
		}
		
		display(regressionManager.getCoefs());
		display(errors);
		System.out.println(errorsSum());
		System.out.println(((float)(errorsMean()*100)) + " %");
		
		file.close();
		
	}
	
	
	public static void writeMessage(FILE file, double[] values, int cycle) {
		
		file.initManualMessage();
		
		for(int i = 0; i< values.length;i++) {
			file.addManualMessage(""+values[i]);
			
		}
		
		file.addManualMessage(""+errorsSum());
		
		file.addManualMessage(""+errorsMean());
		file.addManualMessage(""+cycle);
		
		file.sendManualMessage();
		
	}
	
	public static void display(double[] coefs) {
		
		for(int i = 0; i<coefs.length;i++) {
			System.out.print(coefs[i] + "\t");
		}
		System.out.println("");
	}
	
	public static void updateErrors(double[] newCoefs) {

		
		for(int i = 0 ; i< newCoefs.length;i++) {
			errors[i] = Math.abs(newCoefs[i]-initCoefs[i])/Math.abs(initCoefs[i]);
		}
		
	}
	
	public static double errorsSum() {
		double sum = 0.0;
		for(int i=0;i<errors.length;i++) {
			sum+=errors[i];
		}
		return sum;
	}
	
	public static double errorsMean() {
		double mean = 0.0;
		for(int i=0;i<errors.length;i++) {
			mean+=errors[i];
		}
		return mean/errors.length;
	}
	
	public static String fileName(ArrayList<String> infos) {
		String fileName = "";
		
		for(String info : infos) {
			fileName += info + "_";
		}
		
		return fileName;
	}

}
