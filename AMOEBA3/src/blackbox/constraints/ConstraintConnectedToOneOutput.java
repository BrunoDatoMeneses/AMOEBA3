package blackbox.constraints;

import java.io.Serializable;
import java.util.ArrayList;

import agents.Agent;
import blackbox.BBFunction;
import blackbox.BlackBox;
import blackbox.Input;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstraintConnectedToOneOutput.
 */
public class ConstraintConnectedToOneOutput extends Constraint implements Serializable{

	/** The BB function. */
	BBFunction BBFunction;
	
	/**
	 * Instantiates a new constraint connected to one output.
	 *
	 * @param BBFunction the BB function
	 */
	public ConstraintConnectedToOneOutput(BBFunction BBFunction) {
		criticity = 2.0;
		this.BBFunction = BBFunction;
	}
	
	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#checkConstraint()
	 */
	@Override
	public boolean checkConstraint() {
		ArrayList<BBFunction> listedAgents = new ArrayList<BBFunction>();

		
		return recCheckConstraint(BBFunction,listedAgents);
	}
	
	/**
	 * Rec check constraint.
	 *
	 * @param func the func
	 * @param listedBBFunction the listed BB function
	 * @return true, if successful
	 */
	private boolean recCheckConstraint(BBFunction func, ArrayList<BBFunction> listedBBFunction) {
		Agent a = BBFunction.getAgentA();
		Agent b = BBFunction.getAgentA();
		listedBBFunction.add(func);
		
		//TODO : ugly
		if (a instanceof Input || b instanceof Input) {
			return true;
		}
		if (!listedBBFunction.contains(a) && !listedBBFunction.contains(b)) {
			return recCheckConstraint((BBFunction) a,listedBBFunction) || recCheckConstraint((BBFunction) b,listedBBFunction);
		}
		if (!listedBBFunction.contains(a) && listedBBFunction.contains(b)) {
			return recCheckConstraint((BBFunction) a,listedBBFunction);
		}
		if (listedBBFunction.contains(a) && !listedBBFunction.contains(b)) {
			return recCheckConstraint((BBFunction) b,listedBBFunction);
		}
		if (listedBBFunction.contains(a) && listedBBFunction.contains(b)) {
			return false;
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#solveConstraint(blackbox.BlackBox)
	 */
	@Override
	public boolean solveConstraint(BlackBox bb) {
		System.out.println("solve constraint input");
		
		return true;
	}

}
