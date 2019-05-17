package mas.agents.localModel;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.AbstractPair;
import mas.agents.Agent;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.ContextOverlap;
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
	}
	
	public LocalModelMillerRegression(World world, Context associatedContext, double[] coefsCopy) {
		super(world, associatedContext);
		ArrayList<Percept> var = world.getAllPercept();
		this.nParameters = var.size();
		regression = new Regression(nParameters,true);
		coefs = coefsCopy;
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

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public void computeAMessage(Message m) {
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context)
	 */
	public double getProposition(Context context) {
			
		ArrayList<Percept> percepts = world.getAllPercept();
		
//		if (context.getExperiments().size() == 1) {
//			return context.getExperiments().get(0).getOracleProposition();
//		}
			
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
		
//		if (context.getExperiments().size() == 1) {
//			return context.getExperiments().get(0).getOracleProposition();
//		}
			
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

	
	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context, agents.Percept, agents.Percept, double, double)
	 */
	public double getProposition(Context context, Percept p1, Percept p2, double v1, double v2) {
		
	//	//System.out.println("get proposition");
	//	LinkedHashMap<Percept,Double> values = new LinkedHashMap<Percept,Double>();
		
		ArrayList<Percept> var = world.getAllPercept();
		
//		if (context.getExperiments().size() == 1) {
//			return context.getExperiments().get(0).getOracleProposition();
//		}
		
		regression = new Regression(nParameters,true);
		for (Experiment exp : context.getExperiments()) {
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());

			while (regression.getN() < context.getExperiments().get(0).getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getOracleProposition());
		}
		}

		
		double[] coef = regression.regress().getParameterEstimates();
		
		//
	//	for (int i = 0 ; i < coef.length ; i++ ) {
	//		//System.out.print(var.get(i).getName() + " coef : " + coef[i] + "   " );
	//	}
	//	//System.out.println();
		
		double[] tabv = {v1,v2};
		
		double result = coef[0];
	//	//System.out.println("Result 0" + " : " + result);
		if (coef[0] == Double.NaN) System.exit(0);
		for (int i = 1 ; i < coef.length ; i++) {
			if (Double.isNaN(coef[i])) coef[i] = 0;
			result += coef[i]*tabv[i-1];
		}
//		//System.out.println("Result final" + " : " + result);
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
			
			//System.out.println(exp.getValuesAsLinkedHashMap().toString());
			for (int i = 0 ; i < exp.getValuesAsArray().length ; i++ ) {
				//System.out.print(exp.getValuesAsArray()[i] + "   " );
			}
			//System.out.println(exp.getOracleProposition() + "   " );
		}
		
		//System.out.println("Number of experiments : " + context.getExperiments().size());
		
		//System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
		
		while (regression.getN() < context.getExperiments().get(0).getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			regression.addObservation(context.getExperiments().get(0).getValuesAsArray(), context.getExperiments().get(0).getOracleProposition());
		}
		

		
		coefs = regression.regress().getParameterEstimates();
		
	}
	
	@Override
	public void updateModelWithExperiments(ArrayList<Experiment> experimentsList) {
		
		regression = new Regression(nParameters,true);
		//System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
		for (Experiment exp : experimentsList) {
			
			regression.addObservation(exp.getValuesAsArray(), exp.getOracleProposition());
			
			//System.out.println(exp.getValuesAsLinkedHashMap().toString());
			for (int i = 0 ; i < exp.getValuesAsArray().length ; i++ ) {
				//System.out.print(exp.getValuesAsArray()[i] + "   " );
			}
			//System.out.println(exp.getOracleProposition() + "   " );
		}
		
		//System.out.println("Number of experiments : " + experimentsList.size());
		
		
		
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
		
		//System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
		
	}
	
	public void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight) {
		
		regression = new Regression(nParameters,true);
		//System.out.println("***********************************************************************************************************************************************************");
		
		int numberOfPoints = 20;
		
		AbstractPair<double[][], double[]> artificialSituations = getHomogeneouslyDistributedArtificialExperiments((int)(numberOfPoints - (numberOfPoints*weight)));
		
		//System.out.println("ARTIFICIAL ");
		for (int i =0;i<(int)(numberOfPoints - (numberOfPoints*weight));i++) {
			
			regression.addObservation(artificialSituations.getA()[i], artificialSituations.getB()[i]);
			
			
			//System.out.println(artificialSituations.getA()[i].toString());
			for (int j = 0 ; j < nParameters ; j++ ) {
				//System.out.print(artificialSituations.getA()[i][j] + "   " );
			}
			//System.out.println(artificialSituations.getB()[i] + "   " );
		}
		
		//System.out.println("XP");
		for (int i =0;i<(int)(numberOfPoints*weight);i++) {
			
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getOracleProposition());
			
			
			//System.out.println(newExperiment.getValuesAsLinkedHashMap().toString());
			for (int j = 0 ; j < newExperiment.getValuesAsArray().length ; j++ ) {
				//System.out.print(newExperiment.getValuesAsArray()[j] + "   " );
			}
			//System.out.println(newExperiment.getOracleProposition() + "   " );
		}
		
		while (regression.getN() < newExperiment.getValuesAsLinkedHashMap().size() + 2) { //TODO : to improve
			
			regression.addObservation(newExperiment.getValuesAsArray(), newExperiment.getOracleProposition());
			
			System.out.println("ADING Observations " + regression.getN());
			
		}
		

		
		coefs = regression.regress().getParameterEstimates();
		
		//System.out.println("***********************************************************************************************************************************************************");
		
	}
	
	private AbstractPair<double[][], double[]> getHomogeneouslyDistributedArtificialExperiments(int amount){
		
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
		
		return new AbstractPair<double[][], double[]>(artificalExperiments, artificalResults);
	}

	@Override
	public double getProposition(Context context, ContextOverlap contextOverlap) {
		
			ArrayList<Percept> var = world.getAllPercept();
			
//			if (context.getExperiments().size() == 1) {
//				return context.getExperiments().get(0).getOracleProposition();
//			}
			

			//
		/*	for (int i = 0 ; i < coef.length ; i++ ) {
				//System.out.print(coef[i] + "   " );
			}
			//System.out.println();*/
			
			
			double result = coefs[0];
		//	//System.out.println("Result 0" + " : " + result);
			if (coefs[0] == Double.NaN) System.exit(0);
			
			for (int i = 1 ; i < coefs.length ; i++) {
				if (Double.isNaN(coefs[i])) coefs[i] = 0;
				
				result += coefs[i] * contextOverlap.getMiddleValue(var.get(i-1));
				
		//		//System.out.println("Result " + i + " : " + result);
		//		//System.out.print(var.get(i-1).getName() + " coef : " + coef[i] + "   " );
			}
//			//System.out.println("Result final" + " : " + result);


			
			return result;

	}
	
	public String coefsToString() {
		String coefsString = "";
		for(int i=0;i<coefs.length;i++) {
			coefsString += coefs[i] + " ";
		}
		return coefsString;
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

}
