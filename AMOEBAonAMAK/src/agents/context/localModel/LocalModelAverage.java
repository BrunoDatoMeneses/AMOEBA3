package agents.context.localModel;

import java.util.ArrayList;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;

/**
 * A simple local model which computes the average of all Context Agents
 * experiments.
 *
 */
public class LocalModelAverage extends LocalModel {


	public LocalModelAverage(Context associatedContext) {
		super(associatedContext);
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context)
	 */
	@Override
	public double getProposition(Context context) {
		double average = 0.0;
		for (Experiment exp : context.getExperiments()) {
			average += exp.getOracleProposition();
		}
		return average / context.getExperiments().size();
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context, agents.Percept, agents.Percept, double, double)
	 */
	@Override
	public double getProposition(Context context, Percept p1, Percept p2,
			double v1, double v2) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getFormula(agents.context.Context)
	 */
	@Override
	public String getFormula(Context context) {
		return  getProposition(context) +"";
	}
	
	public String getCoefsFormula() {
		return  "";
	}

	@Override
	public void updateModel(Context context) {
		// TODO Auto-generated method stub
	}


	@Override
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
		return TypeLocalModel.AVERAGE;
	}

	@Override
	public void setCoef(Double[] coef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getMaxProposition(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinProposition(Context context) {
		// TODO Auto-generated method stub
		return 0;
	}
}
