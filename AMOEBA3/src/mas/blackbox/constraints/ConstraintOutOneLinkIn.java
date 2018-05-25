package mas.blackbox.constraints;

import java.io.Serializable;
import java.util.ArrayList;

import mas.blackbox.BBFunction;
import mas.blackbox.BlackBox;
import mas.blackbox.BlackBoxAgent;
import mas.blackbox.Output;

// TODO: Auto-generated Javadoc
/**
 * The Class ConstraintOutOneLinkIn.
 */
public class ConstraintOutOneLinkIn extends Constraint implements Serializable{

	/** The output. */
	Output output;
	
	/**
	 * Instantiates a new constraint out one link in.
	 *
	 * @param output the output
	 */
	public ConstraintOutOneLinkIn(Output output) {
		criticity = 1.0;
		this.output = output;
	}
	
	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#checkConstraint()
	 */
	@Override
	public boolean checkConstraint() {
		return output.getFunc() != null;
	}

	/* (non-Javadoc)
	 * @see blackbox.constraints.Constraint#solveConstraint(blackbox.BlackBox)
	 */
	@Override
	public boolean solveConstraint(BlackBox bb) {
		//	System.out.println("int " + blackBox.getbBAofClasses(new Class<?>[] {Function.class}).size() );
		ArrayList<BlackBoxAgent> targetsAgents = bb.getBBAofClasses(new Class<?>[] {BBFunction.class});
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof BBFunction) {
				if (bba.getTargets().isEmpty()) {
					bba.getTargets().add(output);
					output.setFunc((BBFunction) bba);;
					return true;
				}
			}
		}
		for (BlackBoxAgent bba : targetsAgents) {
			if (bba instanceof BBFunction) {
				if (!bba.getTargets().stream().anyMatch(a -> a instanceof Output)) {
					System.out.println("output connect");
					bba.getTargets().add(output);
					output.setFunc((BBFunction) bba);;
					return true;
				}
			}
		}
		return false;
	}

}
