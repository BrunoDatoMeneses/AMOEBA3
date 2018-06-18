package mas.agents.localModel;

import java.io.Serializable;

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
	public LocalModelAverage(World world) {
		super(world);
	}

	/* (non-Javadoc)
	 * @see agents.localModel.LocalModelAgent#getProposition(agents.context.Context)
	 */
	@Override
	public double getProposition(Context context) {
		double average = 0.0;
		for (Experiment exp : context.getExperiments()) {
			average += exp.getProposition();
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

}
