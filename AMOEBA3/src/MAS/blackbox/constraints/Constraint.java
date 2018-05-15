package MAS.blackbox.constraints;

import java.io.Serializable;

import MAS.blackbox.BlackBox;

// TODO: Auto-generated Javadoc
/**
 * The Class Constraint.
 */
public abstract class Constraint implements Serializable {

	/** The criticity. */
	protected double criticity;

	/**
	 * Gets the criticity.
	 *
	 * @return the criticity
	 */
	public double getCriticity() {
		return criticity;
	}

	/**
	 * Sets the criticity.
	 *
	 * @param criticity the new criticity
	 */
	public void setCriticity(double criticity) {
		this.criticity = criticity;
	}
	
	/**
	 * Check constraint.
	 *
	 * @return true, if successful
	 */
	public abstract boolean checkConstraint();
	
	/**
	 * Solve constraint.
	 *
	 * @param bb the bb
	 * @return true, if successful
	 */
	public abstract boolean solveConstraint(BlackBox bb);
	
}
