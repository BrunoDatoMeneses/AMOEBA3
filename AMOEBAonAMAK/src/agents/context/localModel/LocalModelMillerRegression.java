package agents.context.localModel;

import java.util.ArrayList;
import java.util.Arrays;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import utils.Pair;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalModelMillerRegression.
 */
public class LocalModelMillerRegression extends LocalModel{
	
	/** The n parameters. */
	private int nParameters;
	
	/** The regression. */
	transient private Regression regression;

	
	/** The coef. */
	private Double[] coefs;
	
	private ArrayList<Experiment> firstExperiments;

	/**
	 * Instantiates a new local model miller regression.
	 *
	 * @param world the world
	 */
	public LocalModelMillerRegression(Context associatedContext) {
		super(associatedContext);
		ArrayList<Percept> var = associatedContext.getAmas().getPercepts();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		firstExperiments = new ArrayList<Experiment>();
	}
	
	public LocalModelMillerRegression(Context associatedContext, Double[] coefsCopy, ArrayList<Experiment> fstExperiments) {
		super(associatedContext);
		ArrayList<Percept> var = associatedContext.getAmas().getPercepts();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		coefs = coefsCopy;
		firstExperiments = new ArrayList<Experiment>(fstExperiments);
	}
	
	/**
	 * Sets the coef.
	 *
	 * @param coef the new coef
	 */
	@Override
	public void setCoef(Double[] coef) {
		this.coefs = coef.clone();
	}
	
	/**
	 * Gets the coef.
	 *
	 * @return the coef
	 */
	@Override
	public Double[] getCoef() {
		return coefs;
	}

	public double getProposition(Context context) {
			
		ArrayList<Percept> percepts = context.getAmas().getPercepts();
		

			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * percepts.get(i-1).getValue();

		}
	
		return result;
	}
	
	
	
	public double getProposition(Context context, double[] situation) {
		
		ArrayList<Percept> percepts = context.getAmas().getPercepts();
		
			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * situation[i-1];

		}
	
		return result;
	}
	
	public double getMaxProposition(Context context) {
		
		ArrayList<Percept> percepts = context.getAmas().getPercepts();			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			if(coefs[i]>0) {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getEnd();
			}
			else {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getStart();
			}
		}
	
		return result;
	}
	
	public double getMinProposition(Context context) {
		
		ArrayList<Percept> percepts = context.getAmas().getPercepts();			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			if(coefs[i]<0) {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getEnd();
			}
			else {
				result += coefs[i] * context.getRanges().get(percepts.get(i-1)).getStart();
			}
		}
	
		return result;
	}
	
	public double getProposition(ArrayList<Experiment> experimentsList, Experiment experiment) {
		

		
		if (experimentsList.size() == 1) {
			return experimentsList.get(0).getOracleProposition();
		}
		else {
			double result = coefs[0];

			if (coefs[0] == Double.NaN) System.exit(0);
			
			for (int i = 1 ; i < coefs.length ; i++) {
				
				if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
				result += coefs[i] * experiment.getValuesAsArray()[i-1];

			}
		
			return result;
		}
			
		
	}
	
	public double getProposition(Experiment experiment) {
		
		if (coefs[0] == Double.NaN) System.exit(0);
		
		double result = coefs[0];
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			result += coefs[i] * experiment.getValuesAsArray()[i-1];

		}
	
		return result;
			
	}

	

	public double getProposition(Context context, Percept p1, Percept p2, double v1, double v2) {
		regression = new Regression(nParameters,true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());

			while (regression.getN() < context.getExperiments().get(0).getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getOracleProposition());
		}
		}

		
		double[] coef = regression.regress().getParameterEstimates();
		
		
		double[] tabv = {v1,v2};
		
		double result = coef[0];

		if (coef[0] == Double.NaN) System.exit(0);
		for (int i = 1 ; i < coef.length ; i++) {
			if (Double.isNaN(coef[i])) coef[i] = 0;
			result += coef[i]*tabv[i-1];
		}

		return result;

	

	}
	
	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getFormula(agents.context.Context)
	 */
	public String getFormula(Context context) {
		String s = "";
		if (context.getExperiments().size() == 1) {
			return ""+context.getExperiments().get(0).getOracleProposition();
		}
		else {
			if (regression == null) updateModel(context);
			double[] coef = regression.regress().getParameterEstimates();
			
			ArrayList<Percept> var = context.getAmas().getPercepts();
			
			if (coef[0] == Double.NaN) System.exit(0);
			for (int i = 1 ; i < coef.length ; i++) {
				if (Double.isNaN(coef[i])) coef[i] = 0;
				s += coef[i] + "*" + var.get(i-1).getName();
				
				if (i < coef.length - 1) s += " + ";
			}
			
			s += "\n with " ;
			
			for (int i = 1 ; i < coef.length ; i++) {
				if (Double.isNaN(coef[i])) coef[i] = 0;
				s += var.get(i-1).getName() + " = " + var.get(i-1).getValue();
				s += ", ";
			}
			
			s += "\n with " ;
			s += context.getExperiments().size() + " experimentations";
			
			s += "\n with " ;
			s += getProposition(context) + " as result";
			
			return s;
		}

	}
	
	public String getCoefsFormula() {
		
				
		
		String result = "" +coefs[0];
	//	//System.out.println("Result 0" + " : " + result);
		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			
			result += "\t" + coefs[i] + " (" + context.getAmas().getPercepts().get(i-1) +")";
			
		}
		
		return result;

}
	



	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#updateModel(agents.context.Context)
	 */
	@Override
	public void updateModel(Context context) {
		
		regression = new Regression(nParameters,true);
		//System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());
			
		
		}
		

		
		while (regression.getN() < context.getExperiments().get(0).getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getOracleProposition());
		}
		
		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
		}
		
	}
	
	@Override
	public void updateModelWithExperiments(ArrayList<Experiment> experimentsList) {
		
		regression = new Regression(nParameters,true);
	
		for (Experiment exp : experimentsList) {
			
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());
			
	
		}
		
			
		
		while (regression.getN() < experimentsList.get(0).getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			
			regression.addObservation(experimentsList.get(0).getValuesAsArray(), experimentsList.get(0).getOracleProposition());
			
			System.out.println("Observations " + regression.getN());
			
			System.out.println(experimentsList.get(0).getValuesAsLinkedHashMap().toString());
			for (int i = 0 ; i < experimentsList.get(0).getValuesAsArray().length ; i++ ) {
				System.out.print(experimentsList.get(0).getValuesAsArray()[i] + "   " );
			}
			System.out.println(experimentsList.get(0).getOracleProposition() + "   " );
		}
		

		
		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
		}
		
		
	}
	
	
	public void updateModel(Experiment newExperiment, double weight, int numberOfPointsForRegression) {
		context.getAmas().getEnvironment().trace(new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "OLD MODEL :", coefsToString()))); 
		
		if(firstExperiments.size()< (nParameters + 2)) {
			firstExperiments.add(newExperiment); 
			updateModel();
			
		}else {
			updateModelWithExperimentAndWeight(newExperiment, weight, numberOfPointsForRegression);
		}
		
		
		context.getAmas().getEnvironment().trace(new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "MODEL :", coefsToString()))); 
	}
	
	public void updateModel() {
		
		
		regression = new Regression(nParameters,true);
		
		for (Experiment exp : firstExperiments) {
			
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());
			
		}
		
		int i = 0;
		while (regression.getN() < nParameters + 2) { 
			
			regression.addObservation(firstExperiments.get(i%firstExperiments.size()).getValuesAsArray(), firstExperiments.get(i%firstExperiments.size()).getOracleProposition());
			i++;
		}
		

		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int j = 0; j < coef.length; j++) {
			coefs[j] = coef[j];
		}
		
	}
	
	
	public void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight, int numberOfPoints) {
		
		regression = new Regression(nParameters,true);

		
		int numberOfPointsForRegression = numberOfPoints;
		if(numberOfPointsForRegression < (nParameters+2)) {
			numberOfPointsForRegression += numberOfPointsForRegression*((int)((nParameters+2)/numberOfPointsForRegression));
		}

		Pair<double[][], double[]> artificialSituations = getRandomlyDistributedArtificialExperiments((int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight)));
		//Pair<double[][], double[]> artificialSituations = getEquallyDistributedArtificialExperiments((int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight)));
		

		int numberOfArtificialPoints = artificialSituations.getB().length;
		for (int i =0;i<numberOfArtificialPoints;i++) {
			
			regression.addObservation(artificialSituations.getA()[i], artificialSituations.getB()[i]);	
		}
		

		int numberOfXPPoints;
		if(numberOfArtificialPoints != (int)(numberOfPointsForRegression - (numberOfPointsForRegression*weight))) {
			numberOfXPPoints = (int)(weight*numberOfArtificialPoints/(1-weight));
		}
		else {
			numberOfXPPoints = (int)(numberOfPointsForRegression*weight);
		}

		for (int i =0;i<numberOfXPPoints;i++) {
			
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getOracleProposition());
			
			
	
		}
		
		while (regression.getN() < newExperiment.getValuesAsLinkedHashMap().size() + 2) { 
			
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getOracleProposition());
			
			System.out.println("ADING Observations " + regression.getN());
			
		}
		

		
		double[] coef = regression.regress().getParameterEstimates();
		coefs = new Double[coef.length];
		for(int i = 0; i < coef.length; i++) {
			coefs[i] = coef[i];
		}
		
		context.getAmas().getEnvironment().regressionPoints = numberOfXPPoints + numberOfArtificialPoints;
		
		
	}
	
	private Pair<double[][], double[]> getEquallyDistributedArtificialExperiments(int amount){
		
		
		int nbPercept = context.getAmas().getPercepts().size();
		
		int[] nbPointsByPercept = new int[nbPercept];
		
		ArrayList<Double> pointsByPercept[] = new ArrayList[nbPercept];
		
		int totalNumberOfPoints = 1;
		
		for(int i = 0 ; i < nbPercept ; i++) {
			
			double startRange = this.context.getRanges().get(context.getAmas().getPercepts().get(i)).getStart();
			double endRange = this.context.getRanges().get(context.getAmas().getPercepts().get(i)).getEnd();			
			
			nbPointsByPercept[i] = (int)(amount*this.context.rangeLengthRatio(context.getAmas().getPercepts().get(i)));
			pointsByPercept[i] = new ArrayList<Double>();

			totalNumberOfPoints *= nbPointsByPercept[i];
			
			
			for(int j=0;j<=nbPointsByPercept[i]-1;j++) {
				pointsByPercept[i].add(startRange + ((endRange-startRange)/(nbPointsByPercept[i]-1))*j );
			}
			
		}
		
		nbPercept = context.getAmas().getPercepts().size();
		int[] perceptIndices = new int[nbPercept];
		for(int i = 0 ; i < nbPercept ; i++) {
			perceptIndices[i] = 0;
		}

		
		int i = 0;
		boolean test = true;
		double[][] artificalExperiments = new double[totalNumberOfPoints][nParameters];
		double[] artificalResults = new double[totalNumberOfPoints];

		while(test) {
			
			for(int j = 0;j<nParameters;j++) {
				
				artificalExperiments[i][j] = pointsByPercept[j].get(perceptIndices[j]);
				
			}
			artificalResults[i] = this.getProposition(context, artificalExperiments[i]);
			
			test = nextMultiDimCounter(perceptIndices, nbPointsByPercept);
			
			i++;
			
		}

		
		
		
		return new Pair<double[][], double[]>(artificalExperiments, artificalResults);
	}
	
	private boolean nextMultiDimCounter(int[] indices, int[] bounds){
		
		
		
		for(int i = 0; i<indices.length;i++) {
			
			if(indices[i]==bounds[i]-1) {
				if(i==indices.length-1) {
					indices[i]=0;
					return false;
				}
				else {
					indices[i]=0;
				}				
			}
			else {
				indices[i] += 1;
				return true;
			}
			
		}
		
		return false;

		
	}
	
	private Pair<double[][], double[]> getRandomlyDistributedArtificialExperiments(int amount){
		
		double[][] artificalExperiments = new double[amount][nParameters];
		double[] artificalResults = new double[amount];
		

		
		for (int i = 0; i < amount;i ++) {
			
			for(int j = 0;j<nParameters;j++) {
				
				double startRange = this.context.getRanges().get(context.getAmas().getPercepts().get(j)).getStart();
				double endRange = this.context.getRanges().get(context.getAmas().getPercepts().get(j)).getEnd();
				
				artificalExperiments[i][j] = startRange + (Math.random()*(endRange - startRange));
			}
			artificalResults[i] = this.getProposition(context, artificalExperiments[i]);
			
		}
		
		return new Pair<double[][], double[]>(artificalExperiments, artificalResults);
	}

	
	

	
	
	public double distance(Experiment experiment) {
		
		if (coefs[0] == Double.NaN) System.exit(0);
		double distanceToTheModel = -coefs[0];
		double normOfOthogonalVectorToThePlane = 0.0;
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0.0;
			distanceToTheModel += -coefs[i] * experiment.getValuesAsArray()[i-1];
			normOfOthogonalVectorToThePlane += coefs[i]*coefs[i];

		}
		distanceToTheModel += experiment.getOracleProposition();
		normOfOthogonalVectorToThePlane += 1;
		
		distanceToTheModel = Math.abs(distanceToTheModel);
		normOfOthogonalVectorToThePlane = Math.sqrt(normOfOthogonalVectorToThePlane);
	
		distanceToTheModel /= normOfOthogonalVectorToThePlane;
		
		return distanceToTheModel;
			
	}
	
	public ArrayList<Experiment> getFirstExperiments() {
		return firstExperiments;
	}
	
	public void setFirstExperiments( ArrayList<Experiment> frstExp) {
		firstExperiments = frstExp;
	}
	
	public String coefsToString() {
		String coefsString = "";
		if(coefs != null) {
			for(int i=0; i<coefs.length; i ++) {
				coefsString += coefs[i]  + "\t";
			}
		}
		return coefsString;
	}
	
	public boolean finishedFirstExperiments() {
		return firstExperiments.size()>= (nParameters + 2);
	}

	@Override
	public TypeLocalModel getType() {
		return TypeLocalModel.MILLER_REGRESSION;
	}

}
