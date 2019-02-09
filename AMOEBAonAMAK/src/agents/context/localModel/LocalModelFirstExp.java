package agents.context.localModel;

import agents.context.Context;
import agents.percept.Percept;
import kernel.AMOEBA;

// TODO: Auto-generated Javadoc
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
	public LocalModelFirstExp() {
		
	}

	@Override
	public double getProposition(AMOEBA amoeba,Context context) {
		return context.getExperiments().get(0).getProposition();
	}

	@Override
	public double getProposition(AMOEBA amoeba,Context context, Percept p1, Percept p2,
			double v1, double v2) {
		return 0;
	}

	@Override
	public String getFormula(AMOEBA amoeba,Context context) {
		return  context.getExperiments().get(0).getProposition() +"";
	}
	
	public String getCoefsFormula() {
		return "";
	}

	@Override
	public void updateModel(Context context) {
		// TODO Auto-generated method stub
	}


	
	public double[] getCoef() {
		return new double[1];
	}

}
