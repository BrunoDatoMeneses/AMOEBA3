package blackbox.constraints;

import java.io.Serializable;
import java.util.ArrayList;

import agents.Agent;
import blackbox.BBFunction;
import blackbox.BlackBox;
import blackbox.Input;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstraintConnectedToOneInput.
 */
public class ConstraintConnectedToOneInput extends Constraint implements Serializable{

	/** The function. */
	BBFunction function;
	
	/**
	 * Instantiates a new constraint connected to one input.
	 *
	 * @param function the function
	 */
	public ConstraintConnectedToOneInput(BBFunction function) {
		criticity = 2.0;
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#checkConstraint()
	 */
	@Override
	public boolean checkConstraint() {
		ArrayList<BBFunction> listedAgents = new ArrayList<BBFunction>();

		
		return recCheckConstraint(function,listedAgents);
	}
	
	/**
	 * Rec check constraint.
	 *
	 * @param func the func
	 * @param listedFunction the listed function
	 * @return true, if successful
	 */
	private boolean recCheckConstraint(BBFunction func, ArrayList<BBFunction> listedFunction) {
		Agent a = function.getAgentA();
		Agent b = function.getAgentA();
		listedFunction.add(func);
		
		//TODO : ugly
		if (a instanceof Input || b instanceof Input) {
			return true;
		}
		if (!listedFunction.contains(a) && !listedFunction.contains(b)) {
			return recCheckConstraint((BBFunction) a,listedFunction) || recCheckConstraint((BBFunction) b,listedFunction);
		}
		if (!listedFunction.contains(a) && listedFunction.contains(b)) {
			return recCheckConstraint((BBFunction) a,listedFunction);
		}
		if (listedFunction.contains(a) && !listedFunction.contains(b)) {
			return recCheckConstraint((BBFunction) b,listedFunction);
		}
		if (listedFunction.contains(a) && listedFunction.contains(b)) {
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
