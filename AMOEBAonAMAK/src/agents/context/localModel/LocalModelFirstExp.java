package agents.context.localModel;

import java.util.ArrayList;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;

/**
 * A simple local model which uses only the value of the first experiment.
 * Useful for output like integer.
 *
 */
public class LocalModelFirstExp extends LocalModel {

	/**
	 * Instantiates a new local model first exp.
	 *
	 * @param world the world
	 */
	public LocalModelFirstExp(Context context) {
		super(context);
	}

	@Override
	public double getProposition(Context context) {
		return context.getExperiments().get(0).getOracleProposition();
	}

	@Override
	public double getProposition(Context context, Percept p1, Percept p2,
			double v1, double v2) {
		return 0;
	}

	@Override
	public String getFormula(Context context) {
		return  context.getExperiments().get(0).getOracleProposition() +"";
	}
	
	public String getCoefsFormula() {
		return "";
	}

	@Override
	public void updateModel(Context context) {
		// TODO Auto-generated method stub
	}


	
	public Double[] getCoef() {
		return new Double[1];
	}

	@Override
	public void updateModelWithExperiments(ArrayList<Experiment> experimentsList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getProposition(ArrayList<Experiment> experimentsList, Experiment experiment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateModelWithExperimentAndWeight(Experiment newExperiment, double weight, int numberOfPointsForRegression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getProposition(Experiment experiment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String coefsToString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double distance(Experiment experiment) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Experiment> getFirstExperiments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateModel(Experiment newExperiment, double weight, int numberOfPointsForRegression) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean finishedFirstExperiments() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeLocalModel getType() {
		return TypeLocalModel.FIRST_EXPERIMENT;
	}

	@Override
	public void setCoef(Double[] coef) {
		// TODO Auto-generated method stub
		
	}
}
