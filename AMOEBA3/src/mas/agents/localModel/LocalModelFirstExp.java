package mas.agents.localModel;

import java.io.Serializable;
import java.util.ArrayList;

import mas.kernel.World;
import mas.agents.percept.Percept;
import mas.agents.context.Context;
import mas.agents.context.ContextOverlap;
import mas.agents.context.Experiment;

// TODO: Auto-generated Javadoc
/**
 * A simple local model which uses only the value of the first experiment.
 * Useful for output like integer.
 *
 */
public class LocalModelFirstExp extends LocalModelAgent implements Serializable{

	/**
	 * Instantiates a new local model first exp.
	 *
	 * @param world the world
	 */
	public LocalModelFirstExp(World world) {
		super(world);
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context)
	 */
	@Override
	public double getProposition(Context context) {
		return context.getExperiments().get(0).getOracleProposition();
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
		return  context.getExperiments().get(0).getOracleProposition() +"";
	}
	
	public String getCoefsFormula() {
		return "";
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#updateModel(agents.context.Context)
	 */
	@Override
	public void updateModel(Context context) {
		// TODO Auto-generated method stub
	}

	@Override
	public double getProposition(Context context, ContextOverlap contextOverlap) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public double[] getCoef() {
		return new double[1];
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

}
