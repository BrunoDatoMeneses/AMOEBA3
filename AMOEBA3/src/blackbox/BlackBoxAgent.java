package blackbox;

import java.io.Serializable;
import java.util.ArrayList;

import agents.Agent;
import agents.messages.Message;
import blackbox.constraints.Constraint;

// TODO: Auto-generated Javadoc
/**
 * The Class BlackBoxAgent.
 */
public abstract class BlackBoxAgent extends Agent implements Serializable {

	/** The targets. */
	protected ArrayList<Agent> targets = new ArrayList<Agent>();
	
	/** The criticity. */
	/*For generator*/
	protected double criticity;
	
	/** The constraints. */
	protected ArrayList<Constraint> constraints = new ArrayList<Constraint>();

	/* (non-Javadoc)
	 * @see agents.Agent#computeAMessage(agents.messages.Message)
	 */
	@Override
	public abstract void computeAMessage(Message m);

	/**
	 * Gets the targets.
	 *
	 * @return the targets
	 */
	public ArrayList<Agent> getTargets() {
		return targets;
	}

	/**
	 * Sets the targets.
	 *
	 * @param targets the new targets
	 */
	public void setTargets(ArrayList<Agent> targets) {
		this.targets = targets;
	}

	/* (non-Javadoc)
	 * @see agents.Agent#readMessage()
	 */
	public void readMessage() {
		// System.out.println("Play : " + this.getName());
		super.readMessage();
		fastPlay();
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public abstract double getValue();

	/**
	 * Fast play.
	 */
	public abstract void fastPlay();
	
	/**
	 * Compute criticity.
	 */
	public void computeCriticity() {
		criticity = 0;
		for (Constraint c : constraints) {
			if (!c.checkConstraint()) {
				criticity += c.getCriticity();				
			}
		}
	}
	
	/**
	 * Gets the worst constraint.
	 *
	 * @return the worst constraint
	 */
	public Constraint getWorstConstraint() {
		Constraint constraint = null;
		for (Constraint c : constraints) {
			if (!c.checkConstraint() && (constraint == null || constraint.getCriticity() < c.getCriticity())) {
				constraint = c;
			}
		}
		return constraint;
	}

	/**
	 * Gets the criticity.
	 *
	 * @return the criticity
	 */
	public double getCriticity() {
		computeCriticity();
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
	 * Gets the constraints.
	 *
	 * @return the constraints
	 */
	public ArrayList<Constraint> getConstraints() {
		return constraints;
	}

	/**
	 * Sets the constraints.
	 *
	 * @param constraints the new constraints
	 */
	public void setConstraints(ArrayList<Constraint> constraints) {
		this.constraints = constraints;
	}

	/**
	 * Adds the constraint.
	 *
	 * @param c the c
	 */
	public void addConstraint(Constraint c) {
		constraints.add(c);
	}
}
