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
 * A simple local model which computes the average of all Context Agents experiments.
 *
 */
public class LocalModelAverage extends LocalModelAgent implements Serializable{

	/**
	 * Instantiates a new local model average.
	 *
	 * @param world the world
	 */
	public LocalModelAverage(World world, Context associatedContext) {
		super(world, associatedContext);
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

}
