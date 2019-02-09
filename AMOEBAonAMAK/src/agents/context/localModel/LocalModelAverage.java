package agents.context.localModel;

import agents.context.Context;
import agents.context.Experiment;
import agents.percept.Percept;
import kernel.AMOEBA;

/**
 * A simple local model which computes the average of all Context Agents experiments.
 *
 */
public class LocalModelAverage extends LocalModel {

	public double getProposition(AMOEBA amoeba,Context context) {
		double average = 0.0;
		for (Experiment exp : context.getExperiments()) {
			average += exp.getProposition();
		}
		return average / context.getExperiments().size();
	}


	public double getProposition(AMOEBA amoeba,Context context, Percept p1, Percept p2,
			double v1, double v2) {
		return 0;
	}

	public String getFormula(AMOEBA amoeba,Context context) {
		return  getProposition(amoeba,context) +"";
	}
	
	public String getCoefsFormula() {
		return  "";
	}

	@Override
	public void updateModel(Context context) {
		// TODO Auto-generated method stub
	}

	public double[] getCoef() {
		return new double[1];
	}

}
