package mas.blackbox.constraints;

import java.io.Serializable;
import java.util.ArrayList;

import mas.blackbox.BBFunction;
import mas.blackbox.BlackBox;
import mas.blackbox.BlackBoxAgent;
import mas.blackbox.Input;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstraintFuncTwoLinkIn.
 */
public class ConstraintFuncTwoLinkIn extends Constraint implements Serializable{

	/** The function. */
	BBFunction function;
	
	/**
	 * Instantiates a new constraint func two link in.
	 *
	 * @param function the function
	 */
	public ConstraintFuncTwoLinkIn(BBFunction function) {
		criticity = 5.0;
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#checkConstraint()
	 */
	@Override
	public boolean checkConstraint() {
		return function.getAgentA() != null && function.getAgentB() != null;
	}

	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#solveConstraint(blackbox.BlackBox)
	 */
	@Override
	public boolean solveConstraint(BlackBox bb) {
		//	System.out.println("int " + blackBox.getbBAofClasses(new Class<?>[] {Function.class}).size() );
		ArrayList<BlackBoxAgent> targetsAgents = bb.getBBAofClasses(new Class<?>[] {Input.class});
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof Input) {
				if (bba.getTargets().isEmpty()) {
					bba.getTargets().add(function);
					function.addInputAgent(bba);
					return true;
				}
			}
		}
		targetsAgents = bb.getBBAofClasses(new Class<?>[] {BBFunction.class});
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof BBFunction) {
				if (function.getTargets().isEmpty()) {
						bba.getTargets().add(function);
						function.addInputAgent(bba);
						return true;
				}
			}
		}
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof BBFunction) {
				if ((!function.isLoopingItself() || bba != function) && !function.ownSpecificInput(bba)) {
						bba.getTargets().add(function);
						function.addInputAgent(bba);
						return true;
				}
			}
		}
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof Input) {
					bba.getTargets().add(function);
					function.addInputAgent(bba);
					return true;
			}
		}
		return false;
	}

}
