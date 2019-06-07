package mas.agents.localModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import mas.kernel.World;
import mas.Pair;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.Experiment;
import mas.agents.messages.Message;

// TODO: Auto-generated Javadoc
/**
 * The Class LocalModelMillerRegression.
 */
public class LocalModelMillerRegression extends LocalModelAgent implements Serializable{
	
	/** The n parameters. */
	private int nParameters;
	
	/** The regression. */
	transient private Regression regression;

	
	/** The coef. */
	private double[] coefs;
	
	private ArrayList<Experiment> firstExperiments;

	/**
	 * Instantiates a new local model miller regression.
	 *
	 * @param world the world
	 */
	public LocalModelMillerRegression(World world, Context associatedContext) {
		super(world, associatedContext);
		ArrayList<Percept> var = world.getAllPercept();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		firstExperiments = new ArrayList<Experiment>();
	}
	
	public LocalModelMillerRegression(World world, Context associatedContext, double[] coefsCopy, ArrayList<Experiment> fstExperiments) {
		super(world, associatedContext);
		ArrayList<Percept> var = world.getAllPercept();
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
	public void setCoef(double[] coef) {
		this.coefs = coef.clone();
	}
	
	/**
	 * Gets the coef.
	 *
	 * @return the coef
	 */
	public double[] getCoef() {
		return coefs;
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getTargets()
	 */
	@Override
	public ArrayList<? extends Agent> getTargets() {
		return null;
	}


	@Override
	public void computeAMessage(Message m) {
	}


	public double getProposition(Context context) {
			
		ArrayList<Percept> percepts = world.getAllPercept();
		

			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			result += coefs[i] * percepts.get(i-1).getValue();

		}
	
		return result;
	}
	
	
	
	public double getProposition(Context context, double[] situation) {
		
		ArrayList<Percept> percepts = world.getAllPercept();
		
			
		double result = coefs[0];

		if (coefs[0] == Double.NaN) System.exit(0);
		
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			result += coefs[i] * situation[i-1];

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
				
				if (Double.isNaN(coefs[i])) coefs[i] = 0;
				result += coefs[i] * experiment.getValuesAsArray()[i-1];

			}
		
			return result;
		}
			
		
	}
	
	public double getProposition(Experiment experiment) {
		
		if (coefs[0] == Double.NaN) System.exit(0);
		
		double result = coefs[0];
		for (int i = 1 ; i < coefs.length ; i++) {
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			result += coefs[i] * experiment.getValuesAsArray()[i-1];

		}
	
		return result;
			
	}

	

	public double getProposition(Context context, Percept p1, Percept p2, double v1, double v2) {
		

		
		ArrayList<Percept> var = world.getAllPercept();
		

		
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
			
			ArrayList<Percept> var = world.getAllPercept();
			
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
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
			
			result += "\t" + coefs[i];
			
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
		

		
		coefs = regression.regress().getParameterEstimates();
		
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
		

		
		coefs = regression.regress().getParameterEstimates();
		
		
	}
	
	
	public void updateModel(Experiment newExperiment, double weight, int numberOfPointsForRegression) {
		
		world.trace(new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "OLD MODEL :", coefsToString()))); 
		
		if(firstExperiments.size()< (nParameters + 2)) {
			firstExperiments.add(newExperiment); 
			updateModel();
			
		}else {
			updateModelWithExperimentAndWeight(newExperiment, weight, numberOfPointsForRegression);
		}
		
		
		world.trace(new ArrayList<String>(Arrays.asList(context.getName(),"NEW POINT REGRESSION", "FIRST POINTS :", ""+firstExperiments.size(), "MODEL :", coefsToString()))); 
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
		

		coefs = regression.regress().getParameterEstimates();
		
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
		

		
		coefs = regression.regress().getParameterEstimates();
		
		world.regressionPoints = numberOfXPPoints + numberOfArtificialPoints;
		
		
	}
	
	private Pair<double[][], double[]> getEquallyDistributedArtificialExperiments(int amount){
		
		

		
		int[] nbPointsByPercept = new int[world.getScheduler().getPercepts().size()];
		
		ArrayList<Double> pointsByPercept[] = new ArrayList[world.getScheduler().getPercepts().size()];
		
		int totalNumberOfPoints = 1;
		
		for(int i = 0 ; i < world.getScheduler().getPercepts().size() ; i++) {
			
			double startRange = this.context.getRanges().get(world.getScheduler().getPercepts().get(i)).getStart();
			double endRange = this.context.getRanges().get(world.getScheduler().getPercepts().get(i)).getEnd();			
			
			nbPointsByPercept[i] = (int)(amount*this.context.rangeLengthRatio(world.getScheduler().getPercepts().get(i)));
			pointsByPercept[i] = new ArrayList<Double>();

			totalNumberOfPoints *= nbPointsByPercept[i];
			
			
			for(int j=0;j<=nbPointsByPercept[i]-1;j++) {
				pointsByPercept[i].add(startRange + ((endRange-startRange)/(nbPointsByPercept[i]-1))*j );
			}
			
		}
		
		int[] perceptIndices = new int[world.getScheduler().getPercepts().size()];
		for(int i = 0 ; i < world.getScheduler().getPercepts().size() ; i++) {
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
				
				double startRange = this.context.getRanges().get(world.getAllPercept().get(j)).getStart();
				double endRange = this.context.getRanges().get(world.getAllPercept().get(j)).getEnd();
				
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
			
			if (Double.isNaN(coefs[i])) coefs[i] = 0;
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

}
