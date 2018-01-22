package blackbox.constraints;

import java.io.Serializable;
import java.util.ArrayList;

import blackbox.BBFunction;
import blackbox.BlackBox;
import blackbox.BlackBoxAgent;
import blackbox.Output;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstraintFuncOneLinkOut.
 */
public class ConstraintFuncOneLinkOut extends Constraint implements Serializable{

	/** The function. */
	BBFunction function;
	
	/**
	 * Instantiates a new constraint func one link out.
	 *
	 * @param function the function
	 */
	public ConstraintFuncOneLinkOut(BBFunction function) {
		criticity = 4.0;
		this.function = function;
	}
	
	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#checkConstraint()
	 */
	@Override
	public boolean checkConstraint() {
		return (!function.getTargets().isEmpty() && (function.getTargets().size() > 1 || function.getTargets().get(0) != function));
	}

	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#solveConstraint(blackbox.BlackBox)
	 */
	@Override
	public boolean solveConstraint(BlackBox bb) {
		//	System.out.println("int " + blackBox.getbBAofClasses(new Class<?>[] {Function.class}).size() );
		ArrayList<BlackBoxAgent> targetsAgents = bb.getBBAofClasses(new Class<?>[] {Output.class , BBFunction.class});
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof Output) {
				if (((Output) bba).getFunc() == null) {
					((Output) bba).setFunc(function);
					function.getTargets().add(bba);
					return true;
				}
			}
			if (bba instanceof BBFunction) {
				if (((BBFunction) bba).countFreeInputSlot() > 0 && !((BBFunction) bba).ownSpecificInput(function)) {
					((BBFunction) bba).addInputAgent(function);
					function.getTargets().add(bba);
					return true;
				}
			}
		}
		return false;
	}

}
